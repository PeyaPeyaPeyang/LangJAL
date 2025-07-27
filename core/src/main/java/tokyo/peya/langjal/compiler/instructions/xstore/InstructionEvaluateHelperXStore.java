package tokyo.peya.langjal.compiler.instructions.xstore;

import lombok.experimental.UtilityClass;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.VarInsnNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.exceptions.IllegalInstructionException;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.ClassReferenceType;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.JALMethodCompiler;
import tokyo.peya.langjal.compiler.member.LabelInfo;
import tokyo.peya.langjal.compiler.member.LocalVariableInfo;

import java.util.Objects;

@UtilityClass
public class InstructionEvaluateHelperXStore
{
    public static @NotNull EvaluatedInstruction evaluate(@NotNull AbstractInstructionEvaluator<?> evaluator,
                                                         int opcode,
                                                         @NotNull JALMethodCompiler compiler,
                                                         @NotNull JALParser.JvmInsArgLocalRefContext localRef,
                                                         @NotNull JALParser.LocalInstigationContext instigation,
                                                         @NotNull String type,
                                                         @NotNull String callerInsn,
                                                         @Nullable TerminalNode wide)
    {
        LocalVariableInfo registeredLocal = compiler.getLocals().resolveSafe(localRef);
        if (registeredLocal == null)
            registeredLocal = registerNewLocal(compiler, localRef, type, instigation);

        int idx = registeredLocal.index();
        boolean isWide = wide != null;
        if (idx >= 0xFF && !isWide)
            throw new IllegalInstructionException(
                    String.format(
                    "Local variable index %d is too large for %s instruction. Use wide variant with.",
                    idx, callerInsn
                    ), localRef
            );

        int size = isWide ? 4: 2;
        VarInsnNode insn = new VarInsnNode(opcode, idx);
        return EvaluatedInstruction.of(evaluator, insn, size);
    }

    public static @NotNull EvaluatedInstruction evaluateN(@NotNull AbstractInstructionEvaluator<?> evaluator,
                                                          int opcode, int idx,
                                                          @NotNull JALMethodCompiler compiler,
                                                          @NotNull String defaultType,
                                                          @Nullable JALParser.LocalInstigationContext instigation)
    {
        LocalVariableInfo registeredLocal = compiler.getLocals().resolveSafe(idx);
        if (registeredLocal == null)
            registeredLocal = registerNewLocal(compiler, idx, defaultType, instigation);

        // 0~3 が確定だから， wide は不要
        VarInsnNode insn = new VarInsnNode(opcode, registeredLocal.index());
        return EvaluatedInstruction.of(evaluator, insn, 1);  // 大体で astore だが，本来は astore_X などカテ１
    }

    private static LocalVariableInfo registerNewLocal(@NotNull JALMethodCompiler compiler,
                                                      int idx,
                                                      @NotNull String defaultType,
                                                      @Nullable JALParser.LocalInstigationContext instigation)
    {
        String localName = pickLocalName(compiler, null, idx, instigation);
        LabelInfo endLabel = resolveEndLabel(compiler, instigation);
        TypeDescriptor localType = getType(defaultType, instigation);
        return compiler.getLocals().register(idx, localType, localName, endLabel);
    }

    private static TypeDescriptor getType(@NotNull String defaultType, @Nullable JALParser.LocalInstigationContext inst)
    {
        if (inst == null)
            return TypeDescriptor.parse(defaultType);

        JALParser.TypeDescriptorContext typeNode = inst.typeDescriptor();
        if (typeNode != null)
        {
            String typeText = typeNode.getText();
            return TypeDescriptor.parse(typeText);
        }

        // インスティゲーションがあっても型指定がない場合はデフォルト型を使用
        return TypeDescriptor.parse(defaultType);
    }

    private static LocalVariableInfo registerNewLocal(@NotNull JALMethodCompiler evaluator,
                                                      @NotNull JALParser.JvmInsArgLocalRefContext localRef,
                                                      @NotNull String defaultType,
                                                      @Nullable JALParser.LocalInstigationContext instigation)
    {
        String localName = pickLocalName(evaluator, localRef, 0, instigation);
        LabelInfo endLabel = resolveEndLabel(evaluator, instigation);
        TypeDescriptor localType = getType(defaultType, instigation);

        return evaluator.getLocals().register(localRef, localType, localName, endLabel);
    }

    private static LabelInfo resolveEndLabel(@NotNull JALMethodCompiler evaluator,
                                             @Nullable JALParser.LocalInstigationContext instigation)
    {
        if (instigation == null)
            return null;

        JALParser.LabelNameContext labelNameContext = instigation.labelName();
        if (labelNameContext == null || labelNameContext.ID() == null)
            return null;

        return evaluator.getLabels().resolve(labelNameContext);
    }

    private static String pickLocalName(
            @NotNull JALMethodCompiler evaluator,
            @Nullable JALParser.JvmInsArgLocalRefContext localRef,
            int idx,
            @Nullable JALParser.LocalInstigationContext instigation
    )
    {
        String instigationName = null;
        if (instigation != null)
        {
            TerminalNode localNameNode = instigation.ID();
            if (localNameNode != null)
                instigationName = localNameNode.getText();
        }

        String preferredName = Objects.requireNonNullElseGet(instigationName, () -> String.format("local_%d", idx));
        if (!(localRef == null || localRef.ID() == null))
        {
            String localName = localRef.ID().getText();
            if (localName.equals(preferredName))
                return localName;
            else
                evaluator.getContext().postWarning(String.format(
                        "Local variable name '%s' does not match the expected name '%s'.",
                        localName, preferredName
                ));
        }

        return preferredName;
    }
}
