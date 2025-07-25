package tokyo.peya.langjal.compiler.instructions.ifx;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.JumpInsnNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.exceptions.IllegalInstructionException;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.JALMethodCompiler;
import tokyo.peya.langjal.compiler.member.LabelInfo;

public class InstructionEvaluatorIfOP extends AbstractInstructionEvaluator<JALParser.JvmInsIfOPContext>
{
    public InstructionEvaluatorIfOP()
    {
        super(EOpcodes.IFEQ, EOpcodes.IFNE, EOpcodes.IFLT, EOpcodes.IFGE, EOpcodes.IFGT, EOpcodes.IFLE);
    }

    @Override
    protected @NotNull EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler,
                                                     JALParser.@NotNull JvmInsIfOPContext ctxt)
    {
        int opcode = getOpcode(ctxt);
        JALParser.LabelNameContext labelNameContext = ctxt.labelName();
        LabelInfo label = compiler.getLabels().resolve(labelNameContext);

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
    protected JALParser.JvmInsIfOPContext map(JALParser.@NotNull InstructionContext instruction)
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
