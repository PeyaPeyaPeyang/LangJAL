package tokyo.peya.langjal.compiler.instructions;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.exceptions.IllegalInstructionException;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.*;
import tokyo.peya.langjal.compiler.utils.EvaluatorCommons;

public class InstructionEvaluatorBiPush extends AbstractInstructionEvaluator<JALParser.JvmInsBipushContext> {
    public InstructionEvaluatorBiPush() {
        super(EOpcodes.BIPUSH);
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsBipushContext instruction) {
        int value = EvaluatorCommons.asInteger(instruction.NUMBER());
        if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE)
            throw new IllegalInstructionException(
                    "Value out of range for bipush: " + value + ", expected " + Byte.MIN_VALUE + " ~ " + Byte.MAX_VALUE,
                    instruction.NUMBER()
            );
        return EvaluatedInstruction.of(this, new IntInsnNode(EOpcodes.BIPUSH, value));
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction) {
        return FrameDifferenceInfo.builder(instruction)
                .pushPrimitive(StackElementType.INTEGER)
                .build();
    }

    @Override
    public JALParser.JvmInsBipushContext map(JALParser.@NotNull InstructionContext instruction) {
        return instruction.jvmInsBipush();
    }
}
