package tokyo.peya.langjal.compiler.instructions.xload;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.exceptions.IllegalInstructionException;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.*;

public class InstructionEvaluatorILoadN extends AbstractInstructionEvaluator<JALParser.JvmInsIloadNContext> {
    public InstructionEvaluatorILoadN() {
        super(EOpcodes.ILOAD_0, EOpcodes.ILOAD_1, EOpcodes.ILOAD_2, EOpcodes.ILOAD_3);
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsIloadNContext instruction) {
        if (has(instruction.INSN_ILOAD_0()))
            return InstructionEvaluateHelperXLoad.evaluateN(this, instruction, locals, EOpcodes.ILOAD, 0);
        else if (has(instruction.INSN_ILOAD_1()))
            return InstructionEvaluateHelperXLoad.evaluateN(this, instruction, locals, EOpcodes.ILOAD, 1);
        else if (has(instruction.INSN_ILOAD_2()))
            return InstructionEvaluateHelperXLoad.evaluateN(this, instruction, locals, EOpcodes.ILOAD, 2);
        else if (has(instruction.INSN_ILOAD_3()))
            return InstructionEvaluateHelperXLoad.evaluateN(this, instruction, locals, EOpcodes.ILOAD, 3);

        throw new IllegalInstructionException("Unexpected instruction: " + instruction.getText(), instruction);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction) {
        VarInsnNode insn = (VarInsnNode) instruction.insn();
        return FrameDifferenceInfo.builder(instruction)
                .consumeLocalPrimitive(insn.var, StackElementType.INTEGER)
                .pushPrimitive(StackElementType.INTEGER)
                .build();
    }

    @Override
    public JALParser.JvmInsIloadNContext map(JALParser.@NotNull InstructionContext instruction) {
        return instruction.jvmInsIloadN();
    }
}
