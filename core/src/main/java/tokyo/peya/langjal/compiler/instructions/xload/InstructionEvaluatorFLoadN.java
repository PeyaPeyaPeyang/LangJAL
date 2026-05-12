package tokyo.peya.langjal.compiler.instructions.xload;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.exceptions.IllegalInstructionException;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.*;

public class InstructionEvaluatorFLoadN extends AbstractInstructionEvaluator<JALParser.JvmInsFloadNContext> {
    public InstructionEvaluatorFLoadN() {
        super(EOpcodes.FLOAD_0, EOpcodes.FLOAD_1, EOpcodes.FLOAD_2, EOpcodes.FLOAD_3);
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsFloadNContext instruction) {
        if (has(instruction.INSN_FLOAD_0()))
            return InstructionEvaluateHelperXLoad.evaluateN(this, instruction, locals, EOpcodes.FLOAD, 0);
        else if (has(instruction.INSN_FLOAD_1()))
            return InstructionEvaluateHelperXLoad.evaluateN(this, instruction, locals, EOpcodes.FLOAD, 1);
        else if (has(instruction.INSN_FLOAD_2()))
            return InstructionEvaluateHelperXLoad.evaluateN(this, instruction, locals, EOpcodes.FLOAD, 2);
        else if (has(instruction.INSN_FLOAD_3()))
            return InstructionEvaluateHelperXLoad.evaluateN(this, instruction, locals, EOpcodes.FLOAD, 3);

        throw new IllegalInstructionException("Unexpected instruction: " + instruction.getText(), instruction);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction) {
        return FrameDifferenceInfo.builder(instruction)
                .pushPrimitive(StackElementType.FLOAT)
                .build();
    }

    @Override
    public JALParser.JvmInsFloadNContext map(JALParser.@NotNull InstructionContext instruction) {
        return instruction.jvmInsFloadN();
    }
}
