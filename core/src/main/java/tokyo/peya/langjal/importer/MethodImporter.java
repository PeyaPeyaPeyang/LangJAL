package tokyo.peya.langjal.importer;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.jvm.AccessAttributeSet;
import tokyo.peya.langjal.compiler.jvm.AccessLevel;
import tokyo.peya.langjal.compiler.jvm.MethodDescriptor;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.member.InstructionsHolder;
import tokyo.peya.langjal.compiler.member.LabelInfo;
import tokyo.peya.langjal.compiler.member.LabelsHolder;
import tokyo.peya.langjal.compiler.member.LocalVariablesHolder;
import tokyo.peya.langjal.compiler.member.TryCatchDirective;
import tokyo.peya.langjal.compiler.member.TryCatchDirectivesHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Handles the import and conversion of ASM {@link MethodNode} representations
 * into internal method structures, including instructions, labels, local variables,
 * and try-catch directives. This class is responsible for detailed extraction of
 * method bodies and metadata, reporting progress and errors via a {@link FileEvaluatingReporter}.
 */
public class MethodImporter
{
    private final ClassNode ownerClass;
    private final FileEvaluatingReporter reporter;

    /**
     * Constructs a new {@code MethodImporter} for the specified owner class and reporter.
     *
     * @param ownerClass The ASM {@link ClassNode} representing the class that owns the method.
     * @param reporter   The reporter used to post informational and error messages during import.
     */
    public MethodImporter(@NotNull ClassNode ownerClass, @NotNull FileEvaluatingReporter reporter)
    {
        this.ownerClass = ownerClass;
        this.reporter = reporter;
    }

    /**
     * Imports the specified ASM {@link MethodNode}, extracting its metadata,
     * instructions, labels, local variables, and try-catch directives.
     *
     * @param asmMethod The ASM {@link MethodNode} representing the method to import.
     * @return The result of the method import, encapsulated in a {@link MethodImportResult}.
     */
    @NotNull
    public MethodImportResult importMethod(@NotNull MethodNode asmMethod)
    {
        this.reporter.postInfo("Importing method: " + asmMethod.name + asmMethod.desc);

        String name = asmMethod.name;
        MethodDescriptor descriptor = MethodDescriptor.parse(asmMethod.desc);
        AccessLevel access = AccessLevel.fromAccess(asmMethod.access);
        AccessAttributeSet accessAttributes = AccessAttributeSet.fromAccess(asmMethod.access);

        LabelsHolder labels = new LabelsHolder();
        InstructionsHolder instructions = new InstructionsHolder(this.ownerClass, asmMethod, labels);
        LocalVariablesHolder locals = new LocalVariablesHolder(this.reporter, labels);
        TryCatchDirectivesHolder tryCatchDirectives = new TryCatchDirectivesHolder(this.reporter, labels);

        this.importMethodBody(asmMethod, labels, locals, instructions, tryCatchDirectives);

        return new MethodImportResult(
                access,
                accessAttributes,
                name,
                descriptor,
                labels,
                locals,
                tryCatchDirectives,
                instructions,
                this.ownerClass,
                asmMethod
        );
    }

    private void importMethodBody(@NotNull MethodNode asmMethod,
                                  @NotNull LabelsHolder labels,
                                  @NotNull LocalVariablesHolder locals,
                                  @NotNull InstructionsHolder instructions,
                                    @NotNull TryCatchDirectivesHolder tryCatchDirectives)
    {
        this.reporter.postInfo("Importing method body for: " + asmMethod.name + asmMethod.desc);


        // 命令列のインポート
        InsnList methodInstructions = asmMethod.instructions;
        if (methodInstructions == null || methodInstructions.size() == 0)
        {
            this.reporter.postInfo("Method body is empty for: " + asmMethod.name + asmMethod.desc);
            return;  // メソッド本体が空の場合は何もしない
        }

        importLabels(methodInstructions, labels);
        importLocals(asmMethod, locals);
        importInstructions(methodInstructions, labels, instructions);
        this.importTryCatchDirectives(asmMethod, labels, tryCatchDirectives);

        this.reporter.postInfo("Finished importing method body for: " + asmMethod.name + asmMethod.desc);
    }

    private static void importLocals(@NotNull MethodNode asmMethod,
                                    @NotNull LocalVariablesHolder locals)
    {
        if (asmMethod.localVariables == null || asmMethod.localVariables.isEmpty())
            return;

        for (LocalVariableNode local : asmMethod.localVariables)
            locals.importLocalVariable(local);
    }

    private static void importLabels(@NotNull InsnList methodInstructions,
                                    @NotNull LabelsHolder labels)
    {
        ListIterator<AbstractInsnNode> iterator = methodInstructions.iterator();
        int index = 0;
        while (iterator.hasNext())
        {
            AbstractInsnNode insn = iterator.next();
            int type = insn.getType();
            if (type == AbstractInsnNode.FRAME || type == AbstractInsnNode.LINE)
                continue;  // デバッグ情報は無視（index に影響しないようにする）
            else if (type == AbstractInsnNode.LABEL)
            {
                LabelNode labelNode = (LabelNode) insn;
                LabelInfo imported = labels.importASMLabel(labelNode, index);
                if (index == 0)
                    labels.setCurrentLabel(imported);    // 最初のラベルはメソッドの開始ラベルとして設定
            }

            assert insn.getOpcode() != -1 : "Invalid instruction at index " + index + ": " + insn;
            index++;
        }
    }

    private static void importInstructions(@NotNull InsnList methodInstructions,
                                    @NotNull LabelsHolder labels,
                                    @NotNull InstructionsHolder instructions)
    {
        ListIterator<AbstractInsnNode> iterator = methodInstructions.iterator();

        List<LineNumberNode> lineNumbers = new ArrayList<>();
        LabelInfo assignLabel = null;
        while (iterator.hasNext())
        {
            AbstractInsnNode insn = iterator.next();

            int type = insn.getType();
            if (type == AbstractInsnNode.FRAME)
                continue;
            else if (type == AbstractInsnNode.LINE)
            {
                LineNumberNode lineNumberNode = (LineNumberNode) insn;
                lineNumbers.add(lineNumberNode);
                continue;
            }
            else if (type == AbstractInsnNode.LABEL)
            {
                // ラベルはあらかじめ登録されている。
                LabelNode labelNode = (LabelNode) insn;
                labels.setCurrentLabel(assignLabel = labels.getLabelByNode(labelNode));
                continue;
            }

            assert insn.getOpcode() != -1 : "Invalid instruction: " + insn;

            // 行番号を格納したものから取得する。
            int sourceLine = -1;  // デフォルトは-1（行番号なし）
            if (!(assignLabel == null || lineNumbers.isEmpty()))
            {
                // 行番号はラベルと紐づいているので，現在の命令にラベルが有るならば，その行番号を取得する。
                LineNumberNode lineNumberNode = lineNumbers.stream()
                                                           .filter(ln -> ln.start == insn)
                                                           .findFirst()
                                                           .orElse(null);
                if (lineNumberNode != null)
                    sourceLine = lineNumberNode.line;
            }

            instructions.importInstruction(
                    insn,
                    assignLabel,
                    sourceLine
            );

            assignLabel = null;  // ラベルは次の命令に影響しないのでリセット
        }
    }


    private void importTryCatchDirectives(@NotNull MethodNode asmMethod,
                                                @NotNull LabelsHolder labels,
                                                @NotNull TryCatchDirectivesHolder tryCatchDirectives)
    {
        if (asmMethod.tryCatchBlocks == null || asmMethod.tryCatchBlocks.isEmpty())
            return;  // 例外処理がない場合は何もしない

        List<TryCatchDirective> tryCatchBlocks = new ArrayList<>();
        List<TryCatchBlockNode> finallyBlocks = new ArrayList<>();
        for (TryCatchBlockNode tryCatch : asmMethod.tryCatchBlocks)
        {
            TypeDescriptor exceptionType;
            if (tryCatch.type == null)
            {
                finallyBlocks.add(tryCatch);
                continue;  // finally ブロックは特別扱い
            }
            else
                exceptionType = TypeDescriptor.parse(tryCatch.type);


            LabelInfo startLabel = labels.getLabelByNode(tryCatch.start);
            LabelInfo endLabel = labels.getLabelByNode(tryCatch.end);
            LabelInfo handlerLabel = labels.getLabelByNode(tryCatch.handler);
            if (startLabel == null || endLabel == null) // handler が無くても OK
            {
                this.reporter.postError("Invalid try-catch block in method: " + asmMethod.name + asmMethod.desc);
                continue;
            }


            TryCatchDirective directive = new TryCatchDirective(
                    startLabel,
                    endLabel,
                    handlerLabel,
                    exceptionType,
                    null
            );

            tryCatchBlocks.add(directive);
        }

        // finally ブロックを try-catch ディレクティブに変換
        for (TryCatchBlockNode finallyBlock : finallyBlocks)
        {
            LabelInfo startLabel = labels.getLabelByNode(finallyBlock.start);
            LabelInfo endLabel = labels.getLabelByNode(finallyBlock.end);
            LabelInfo finallyLabel = labels.getLabelByNode(finallyBlock.handler);

            if (startLabel == null || endLabel == null || finallyLabel == null)
            {
                this.reporter.postError("Invalid finally block in method: " + asmMethod.name + asmMethod.desc);
                continue;
            }

            // finally を付加するブロックを検索
            TryCatchDirective base = tryCatchBlocks.stream()
                    .filter(d -> d.tryBlockStartLabel().equals(startLabel) && d.tryBlockEndLabel().equals(endLabel))
                    .findFirst()
                    .orElse(null);
            tryCatchBlocks.remove(base);

            LabelInfo catchBlockLabel = null; // もとの try-catch ブロックの catch ラベルを引き継ぐ
            TypeDescriptor exceptionType = null; // もとの try-catch ブロックの例外型を引き継ぐ
            if (base != null)
            {
                catchBlockLabel = base.catchBlockLabel();
                exceptionType = base.exceptionType();
            }

            // 新しいディレクティブを追加
            TryCatchDirective directive = new TryCatchDirective(
                    startLabel,
                    endLabel,
                    catchBlockLabel,
                    exceptionType,
                    finallyLabel
            );

            tryCatchBlocks.add(directive);
        }

        // 収集したディレクティブを TryCatchDirectivesHolder に追加
        for (TryCatchDirective directive : tryCatchBlocks)
        {
            tryCatchDirectives.addTryCatchDirective(
                    directive.tryBlockStartLabel(),
                    directive.tryBlockEndLabel(),
                    directive.catchBlockLabel(),
                    directive.exceptionType(),
                    directive.finallyBlockLabel()
            );
        }
    }
}
