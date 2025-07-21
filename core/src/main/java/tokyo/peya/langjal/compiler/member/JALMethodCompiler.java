package tokyo.peya.langjal.compiler.member;

import lombok.Getter;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.compiler.CompileSettings;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.MethodAnalyser;
import tokyo.peya.langjal.analyser.MethodAnalysisResult;
import tokyo.peya.langjal.analyser.StackFrameMapCreator;
import tokyo.peya.langjal.analyser.StackFrameMapEntry;
import tokyo.peya.langjal.compiler.exceptions.IllegalValueException;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.MethodDescriptor;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.utils.EvaluatorCommons;

@Getter
public class JALMethodCompiler
{
    private final FileEvaluatingReporter context;
    private final ClassNode clazz;
    private final int compileFlags;

    private final MethodNode method;

    private final InstructionsHolder instructions;
    private final LabelsHolder labels;
    private final LocalVariablesHolder locals;
    private final TryCatchDirectivesHolder tryCatchDirectives;

    public JALMethodCompiler(@NotNull FileEvaluatingReporter reporter, @NotNull ClassNode cn,
                             @MagicConstant(valuesFromClass = CompileSettings.class) int compileFlags)
    {
        this.context = reporter;
        this.clazz = cn;
        this.compileFlags = compileFlags;
        this.method = new MethodNode();

        this.labels = new LabelsHolder(this);
        this.instructions = new InstructionsHolder(cn, this.method, this.labels);
        this.locals = new LocalVariablesHolder(this.context, this.labels);
        this.tryCatchDirectives = new TryCatchDirectivesHolder(this.context, this.labels);
    }

    public void evaluateMethod(@NotNull JALParser.MethodDefinitionContext method)
    {
        this.clazz.methods.add(this.method);

        this.evaluateMethodMetadata(method);
        this.evaluateMethodParameters(method);
        this.evaluateMethodBody(method.methodBody());
        if ((this.compileFlags & CompileSettings.COMPUTE_STACK_FRAME_MAP) != 0)
            this.addStackMapTable();
    }

    public MethodAnalysisResult analyseMethod()
    {
        MethodAnalyser analyser = new MethodAnalyser(
                this.context,
                this.clazz,
                this.method,
                this.instructions,
                this.labels,
                this.locals
        );
        return analyser.analyse();
    }

    private void addStackMapTable()
    {
        // 各命令セットを解析して，スタックフレームを作成する。
        MethodAnalysisResult analysisResult = this.analyseMethod();

        StackFrameMapCreator mapCreator = new StackFrameMapCreator(
                this.context,
                this.method
        );
        mapCreator.updateFrames(analysisResult.propagations());
        StackFrameMapEntry[] mapEntries = mapCreator.createStackFrameMap();
        // スタックマップテーブルを追加
        this.method.visitMaxs(
                analysisResult.maxStack(),
                analysisResult.maxLocals()
        );

        for (StackFrameMapEntry entry : mapEntries)
        {
            LabelInfo atLabel = entry.label();
            FrameNode frameNode = entry.toASMFrameNode();
            InstructionInfo instruction = this.instructions.getInstruction(atLabel.instructionIndex());
            if (instruction == null)
            {
                this.context.postError("No instruction found for label: " + atLabel.name());
                continue;
            }

            AbstractInsnNode node = instruction.insn();
            this.method.instructions.insertBefore(node, frameNode);
        }
    }

    private void evaluateMethodParameters(@NotNull JALParser.MethodDefinitionContext method)
    {
        JALParser.MethodDescriptorContext desc = method.methodDescriptor();
        MethodDescriptor descriptor = MethodDescriptor.parse(desc.getText());
        TypeDescriptor[] parameters = descriptor.getParameterTypes();
        int accessor = asAccess(method.accModMethod());

        int currentIndex = 0;
        boolean isInstanceMethod = (accessor & EOpcodes.ACC_STATIC) == 0;
        if (isInstanceMethod)
        {
            // インスタンスメソッドの場合は，this パラメータを追加
            String thisParamName = "this";
            TypeDescriptor thisParamType = TypeDescriptor.className(this.clazz.name);
            // パラメータをローカル変数として登録
            this.locals.registerParameter(thisParamName, thisParamType, currentIndex++);
        }

        for (int i = 0; i < parameters.length; i++)
        {
            TypeDescriptor paramType = parameters[i];
            String paramName = String.format("arg%05d", i);
            // パラメータをローカル変数として登録
            if (paramType.getBaseType().getCategory() == 2)
            {
                this.locals.registerParameter(paramName, paramType, currentIndex++);
                currentIndex++; // カテゴリ２は ２スロット使うため，インデックスを進める
            }
            else
                this.locals.registerParameter(paramName, paramType, currentIndex++);
        }
    }


    private void finaliseMethod()
    {
        this.instructions.finaliseInstructions();
        this.tryCatchDirectives.finaliseTryCatchDirectives(this.method);
        this.labels.finalise(this.method);
        this.locals.evaluateLocals(this.method);
    }


    private void evaluateMethodMetadata(@NotNull JALParser.MethodDefinitionContext method)
    {
        String desc = method.methodDescriptor().getText();
        String name = method.methodName().getText();
        int access = asAccess(method.accModMethod());

        this.method.name = name;
        this.method.desc = desc;
        this.method.access = access;
    }

    private void evaluateLabels(@NotNull JALParser.MethodBodyContext body)
    {
        boolean globalStartUpdated = false;
        int instructionCount = 0;
        for (JALParser.InstructionSetContext bodyItem : body.instructionSet())
        {
            if (bodyItem.label() != null)
            {
                // ラベルを登録
                LabelInfo label = this.labels.register(bodyItem.label().labelName(), instructionCount);
                // グローバルスタートになり得る場合は，入れ替える。
                if (instructionCount == 0 && !globalStartUpdated)
                {
                    this.labels.setGlobalStart(label);
                    globalStartUpdated = true;
                }
            }

            instructionCount += bodyItem.instruction().size();
        }

        this.labels.registerGlobalStart(this.method);
    }

    private void evaluateMethodBody(@NotNull JALParser.MethodBodyContext body)
    {
        this.context.postInfo("Evaluating method body for " + this.method.name + this.method.desc);

        this.method.visitCode();
        this.evaluateLabels(body);
        this.evaluateTryCatchDirectives(body);
        this.evaluateInstructions(body);
        this.finaliseMethod();
        this.method.visitEnd();
    }

    public void evaluateTryCatchDirectives(JALParser.MethodBodyContext body)
    {
        for (JALParser.InstructionSetContext bodyItem : body.instructionSet())
        {
            if (bodyItem.tryCatchDirective() == null)
                continue;  // トライキャッチディレクティブがない場合はスキップ

            JALParser.LabelNameContext endLabel = bodyItem.tryCatchDirective().labelName();
            if (endLabel == null)
                throw new IllegalArgumentException("Try-catch directive must have an end label.");

            LabelInfo tryStartLabel = this.labels.resolve(bodyItem.label().labelName());
            LabelInfo tryEndLabel = this.labels.resolve(endLabel);

            JALParser.TryCatchDirectiveContext directiveContext = bodyItem.tryCatchDirective();
            for (JALParser.TryCatchDirectiveEntryContext entry : directiveContext.tryCatchDirectiveEntry())
                this.evaluateTryCatchDirective(
                        tryStartLabel,
                        tryEndLabel,
                        entry
                );
        }
    }

    private void evaluateTryCatchDirective(@NotNull LabelInfo tryBlockStartLabel,
                                           @NotNull LabelInfo tryBlockEndLabel,
                                           @NotNull JALParser.TryCatchDirectiveEntryContext entry)
    {
        JALParser.CatchDirectiveContext catchDirective = entry.catchDirective();
        JALParser.FinallyDirectiveContext finallyDirective = entry.finallyDirective();

        if (catchDirective == null && finallyDirective == null)
            throw new IllegalValueException(
                    "Try-catch directive must have at least one catch or finally block.",
                    entry
            );
        // finally は, catcHDirective 内に指定される場合がある
        if (finallyDirective == null)
            finallyDirective = catchDirective.finallyDirective();

        TypeDescriptor exceptionType = null;
        if (catchDirective != null)
        {
            TerminalNode exceptionTypeNode = catchDirective.FULL_QUALIFIED_CLASS_NAME();
            if (exceptionTypeNode == null)
                throw new IllegalValueException("Catch directive must have an exception type.", entry);
            exceptionType = TypeDescriptor.parse(exceptionTypeNode.getText());
        }

        // 各ラベルを解決
        JALParser.LabelNameContext catchLabel = catchDirective == null ? null: catchDirective.labelName();
        JALParser.LabelNameContext finallyLabel = finallyDirective == null ? null: finallyDirective.labelName();
        LabelInfo catchBlockLabel = null;
        LabelInfo finallyBlockLabel = null;
        if (catchLabel != null)
            catchBlockLabel = this.labels.resolve(catchLabel);
        if (finallyLabel != null)
            finallyBlockLabel = this.labels.resolve(finallyLabel);

        // トライキャッチディレクティブを登録
        this.tryCatchDirectives.addTryCatchDirective(
                tryBlockStartLabel,
                tryBlockEndLabel,
                catchBlockLabel,
                exceptionType,
                finallyBlockLabel
        );
    }

    private void evaluateInstructions(@NotNull JALParser.MethodBodyContext body)
    {
        // 各命令を順に評価していく
        // 命令に割り当てるラベル。１命令のみが割り当てられる。
        LabelInfo labelAssignation = this.labels.getGlobalStart();
        for (JALParser.InstructionSetContext bodyItem : body.instructionSet())
        {
            if (bodyItem.label() != null)
                this.labels.setCurrentLabel(
                        labelAssignation = this.labels.resolve(bodyItem.label().labelName())
                );

            for (JALParser.InstructionContext instruction : bodyItem.instruction())
            {
                // 命令を評価して，必要に応じてラベルを設定
                EvaluatedInstruction evaluated = JALInstructionEvaluator.evaluateInstruction(
                        this,
                        instruction
                );
                if (evaluated == null)
                    continue;

                this.instructions.addInstruction(evaluated, labelAssignation, instruction.start.getLine());
                labelAssignation = null;  // 次の命令セットのためにラベルをクリア
            }
        }

        if (this.instructions.isEmpty() || shouldAppendReturnOnLast(this.instructions.getLastInstruction()))
        {
            // 最後にRETURNがない場合は、デフォルトでRETURNを追加
            this.instructions.addReturn();
        }
    }

    private static boolean shouldAppendReturnOnLast(InstructionInfo instruction)
    {
        return switch (instruction.opcode())
        {
            case EOpcodes.IRETURN, EOpcodes.LRETURN, EOpcodes.FRETURN,
                 EOpcodes.DRETURN, EOpcodes.ARETURN, EOpcodes.RETURN,
                 EOpcodes.ATHROW, EOpcodes.GOTO -> false; // これらの命令はRETURNを追加しない
            default -> true; // 他の命令が最後の場合はRETURNを追加する
        };
    }

    private static int asAccess(JALParser.AccModMethodContext methodNode)
    {
        int accessor = EvaluatorCommons.asAccessLevel(methodNode.accessLevel());
        for (JALParser.AccAttrMethodContext ctxt : methodNode.accAttrMethod())
        {
            if (ctxt.KWD_ACC_ATTR_STATIC() != null)
                accessor |= EOpcodes.ACC_STATIC;
            else if (ctxt.KWD_ACC_ATTR_FINAL() != null)
                accessor |= EOpcodes.ACC_FINAL;
            else if (ctxt.KWD_ACC_ATTR_SYNCHRONIZED() != null)
                accessor |= EOpcodes.ACC_SYNCHRONIZED;
            else if (ctxt.KWD_ACC_ATTR_BRIDGE() != null)
                accessor |= EOpcodes.ACC_BRIDGE;
            else if (ctxt.KWD_ACC_ATTR_VARARGS() != null)
                accessor |= EOpcodes.ACC_VARARGS;
            else if (ctxt.KWD_ACC_ATTR_NATIVE() != null)
                accessor |= EOpcodes.ACC_NATIVE;
            else if (ctxt.KWD_ACC_ATTR_ABSTRACT() != null)
                accessor |= EOpcodes.ACC_ABSTRACT;
            else if (ctxt.KWD_ACC_ATTR_STRICTFP() != null)
                accessor |= EOpcodes.ACC_STRICT;
            else if (ctxt.KWD_ACC_ATTR_SYNTHETIC() != null)
                accessor |= EOpcodes.ACC_SYNTHETIC;
        }

        return accessor;
    }
}
