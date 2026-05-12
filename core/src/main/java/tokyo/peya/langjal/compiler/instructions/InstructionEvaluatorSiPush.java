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

public class InstructionEvaluatorSiPush extends AbstractInstructionEvaluator<JALParser.JvmInsSipushContext> {
    public InstructionEvaluatorSiPush() {
        super(EOpcodes.SIPUSH);
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsSipushContext instruction) {
        int value = EvaluatorCommons.asInteger(instruction.NUMBER());
        if (value < Short.MIN_VALUE || value > Short.MAX_VALUE)
            throw new IllegalInstructionException(
                    String.format(
                            "Value out of range for sipush: %d, expected %d ~ %d",
                            value, Short.MIN_VALUE, Short.MAX_VALUE
                    ), instruction.NUMBER()
            );


        IntInsnNode insn = new IntInsnNode(EOpcodes.SIPUSH, value);
        return EvaluatedInstruction.of(this, insn);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction) {
        return FrameDifferenceInfo.builder(instruction)
                .pushPrimitive(StackElementType.INTEGER) // sipush の結果は int 型
                .build();
    }

    @Override
    public JALParser.JvmInsSipushContext map(JALParser.@NotNull InstructionContext instruction) {
        return instruction.jvmInsSipush();
    }
}
