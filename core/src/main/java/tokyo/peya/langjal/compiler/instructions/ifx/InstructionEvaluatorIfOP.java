package tokyo.peya.langjal.compiler.instructions.ifx;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.exceptions.IllegalInstructionException;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.InstructionsHolder;
import tokyo.peya.langjal.compiler.member.LabelInfo;
import tokyo.peya.langjal.compiler.member.LabelsHolder;
import tokyo.peya.langjal.compiler.member.LocalVariablesHolder;

public class InstructionEvaluatorIfOP extends AbstractInstructionEvaluator<JALParser.JvmInsIfOPContext>
{
    public InstructionEvaluatorIfOP()
    {
        super(EOpcodes.IFEQ, EOpcodes.IFNE, EOpcodes.IFLT, EOpcodes.IFGE, EOpcodes.IFGT, EOpcodes.IFLE);
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsIfOPContext instruction)
    {
        int opcode = getOpcode(instruction);
        JALParser.LabelNameContext labelNameContext = instruction.labelName();
        LabelInfo label = labels.resolve(labelNameContext);

        JumpInsnNode insn = new JumpInsnNode(opcode, label.node());
        return EvaluatedInstruction.of(this, insn);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.builder(instruction)
                                  .popPrimitive(StackElementType.INTEGER)
                                  .build();
    }

    @Override
    public JALParser.JvmInsIfOPContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsIfOP();
    }

    private static int getOpcode(JALParser.JvmInsIfOPContext ctxt)
    {
        if (ctxt.INSN_IFEQ() != null)
            return EOpcodes.IFEQ;
        if (ctxt.INSN_IFNE() != null)
            return EOpcodes.IFNE;
        if (ctxt.INSN_IFLT() != null)
            return EOpcodes.IFLT;
        if (ctxt.INSN_IFGE() != null)
            return EOpcodes.IFGE;
        if (ctxt.INSN_IFGT() != null)
            return EOpcodes.IFGT;
        if (ctxt.INSN_IFLE() != null)
            return EOpcodes.IFLE;

        throw new IllegalInstructionException("Unknown IF opcode", ctxt);
    }
}
