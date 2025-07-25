package tokyo.peya.langjal.compiler.instructions.ifx;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.JumpInsnNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.compiler.exceptions.IllegalInstructionException;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.JALMethodCompiler;
import tokyo.peya.langjal.compiler.member.LabelInfo;

public class InstructionEvaluatorIfACmpOP extends AbstractInstructionEvaluator<JALParser.JvmInsIfAcmpOPContext>
{
    public InstructionEvaluatorIfACmpOP()
    {
        super(EOpcodes.IF_ACMPEQ, EOpcodes.IF_ACMPNE);
    }

    @Override
    protected @NotNull EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler,
                                                     JALParser.@NotNull JvmInsIfAcmpOPContext ctxt)
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
                                  .popObjectRef()
                                  .popObjectRef()
                                  .build();
    }

    @Override
    protected JALParser.JvmInsIfAcmpOPContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsIfAcmpOP();
    }

    private static int getOpcode(JALParser.JvmInsIfAcmpOPContext ctxt)
    {
        if (ctxt.INSN_IF_ACMPEQ() != null)
            return EOpcodes.IF_ICMPEQ;
        if (ctxt.INSN_IF_ACMPNE() != null)
            return EOpcodes.IF_ICMPNE;

        throw new IllegalInstructionException("Unknown IF_ICMP opcode", ctxt);
    }
}
