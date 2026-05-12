package tokyo.peya.langjal.compiler.instructions.ldc;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.*;

public class InstructionEvaluatorLDC extends AbstractInstructionEvaluator<JALParser.JvmInsLdcContext> {
    public InstructionEvaluatorLDC() {
        super(EOpcodes.LDC);
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsLdcContext instruction) {
        return InstructionEvaluationHelperLDC.evaluate(
                this, instruction.jvmInsArgScalarType(), InstructionEvaluationHelperLDC.LDC
        );
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction) {
        return InstructionEvaluationHelperLDC.getFrameDifferenceInfo(instruction);
    }

    @Override
    public JALParser.JvmInsLdcContext map(JALParser.@NotNull InstructionContext instruction) {
        return instruction.jvmInsLdc();
    }
}
