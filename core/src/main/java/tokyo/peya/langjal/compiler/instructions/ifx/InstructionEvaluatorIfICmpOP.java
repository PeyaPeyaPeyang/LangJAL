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

public class InstructionEvaluatorIfICmpOP extends AbstractInstructionEvaluator<JALParser.JvmInsIfIcmpOPContext>
{
    public InstructionEvaluatorIfICmpOP()
    {
        super(EOpcodes.IF_ICMPEQ, EOpcodes.IF_ICMPNE);
    }

    @Override
    protected @NotNull EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler,
                                                     JALParser.@NotNull JvmInsIfIcmpOPContext ctxt)
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
                                  .popPrimitive(StackElementType.INTEGER)
                                  .build();
    }

    @Override
    protected JALParser.JvmInsIfIcmpOPContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsIfIcmpOP();
    }

    private static int getOpcode(JALParser.JvmInsIfIcmpOPContext ctxt)
    {
        if (ctxt.INSN_IF_ICMPEQ() != null)
            return EOpcodes.IF_ICMPEQ;
        if (ctxt.INSN_IF_ICMPNE() != null)
            return EOpcodes.IF_ICMPNE;
        if (ctxt.INSN_IF_ICMPLT() != null)
            return EOpcodes.IF_ICMPLT;
        if (ctxt.INSN_IF_ICMPGE() != null)
            return EOpcodes.IF_ICMPGE;
        if (ctxt.INSN_IF_ICMPGT() != null)
            return EOpcodes.IF_ICMPGT;
        if (ctxt.INSN_IF_ICMPLE() != null)
            return EOpcodes.IF_ICMPLE;

        throw new IllegalInstructionException("Unknown IF_ICMP opcode", ctxt);
    }
}
