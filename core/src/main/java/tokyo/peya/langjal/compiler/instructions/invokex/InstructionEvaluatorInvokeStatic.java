package tokyo.peya.langjal.compiler.instructions.invokex;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.*;

public class InstructionEvaluatorInvokeStatic extends AbstractInstructionEvaluator<JALParser.JvmInsInvokestaticContext> {
    public InstructionEvaluatorInvokeStatic() {
        super(EOpcodes.INVOKESTATIC);
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsInvokestaticContext instruction) {
        return InstructionEvaluateHelperInvocation.evaluate(
                this,
                clazz,
                instruction.jvmInsArgMethodRef(),
                EOpcodes.INVOKESTATIC
        );
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction) {
        return InstructionEvaluateHelperInvocation.getFrameNormalDifferenceInfo(instruction);
    }

    @Override
    public JALParser.JvmInsInvokestaticContext map(JALParser.@NotNull InstructionContext instruction) {
        return instruction.jvmInsInvokestatic();
    }
}
