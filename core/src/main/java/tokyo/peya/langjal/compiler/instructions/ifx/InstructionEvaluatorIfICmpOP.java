package tokyo.peya.langjal.compiler.instructions.ifx;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.exceptions.IllegalInstructionException;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.*;

public class InstructionEvaluatorIfICmpOP extends AbstractInstructionEvaluator<JALParser.JvmInsIfIcmpOPContext> {
    public InstructionEvaluatorIfICmpOP() {
        super(
                EOpcodes.IF_ICMPEQ,
                EOpcodes.IF_ICMPNE,
                EOpcodes.IF_ICMPLT,
                EOpcodes.IF_ICMPGE,
                EOpcodes.IF_ICMPGT,
                EOpcodes.IF_ICMPLE
        );
    }

    private static int getOpcode(JALParser.JvmInsIfIcmpOPContext ctxt) {
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

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsIfIcmpOPContext instruction) {
        int opcode = getOpcode(instruction);
        JALParser.LabelNameContext labelNameContext = instruction.labelName();
        LabelInfo label = labels.resolve(labelNameContext);

        JumpInsnNode insn = new JumpInsnNode(opcode, label.node());
        return EvaluatedInstruction.of(this, insn);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction) {
        return FrameDifferenceInfo.builder(instruction)
                .popPrimitive(StackElementType.INTEGER)
                .popPrimitive(StackElementType.INTEGER)
                .build();
    }

    @Override
    public JALParser.JvmInsIfIcmpOPContext map(JALParser.@NotNull InstructionContext instruction) {
        return instruction.jvmInsIfIcmpOP();
    }
}
