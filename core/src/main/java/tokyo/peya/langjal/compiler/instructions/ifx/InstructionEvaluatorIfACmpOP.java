package tokyo.peya.langjal.compiler.instructions.ifx;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.exceptions.IllegalInstructionException;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.*;

public class InstructionEvaluatorIfACmpOP extends AbstractInstructionEvaluator<JALParser.JvmInsIfAcmpOPContext> {
    public InstructionEvaluatorIfACmpOP() {
        super(EOpcodes.IF_ACMPEQ, EOpcodes.IF_ACMPNE);
    }

    private static int getOpcode(JALParser.JvmInsIfAcmpOPContext ctxt) {
        if (ctxt.INSN_IF_ACMPEQ() != null)
            return EOpcodes.IF_ACMPEQ;
        if (ctxt.INSN_IF_ACMPNE() != null)
            return EOpcodes.IF_ACMPNE;

        throw new IllegalInstructionException("Unknown IF_ICMP opcode", ctxt);
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsIfAcmpOPContext instruction) {
        int opcode = getOpcode(instruction);
        JALParser.LabelNameContext labelNameContext = instruction.labelName();
        LabelInfo label = labels.resolve(labelNameContext);

        JumpInsnNode insn = new JumpInsnNode(opcode, label.node());
        return EvaluatedInstruction.of(this, insn);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction) {
        return FrameDifferenceInfo.builder(instruction)
                .popObjectRef()
                .popObjectRef()
                .build();
    }

    @Override
    public JALParser.JvmInsIfAcmpOPContext map(JALParser.@NotNull InstructionContext instruction) {
        return instruction.jvmInsIfAcmpOP();
    }
}
