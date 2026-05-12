package tokyo.peya.langjal.compiler.instructions.field;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.member.*;

public class InstructionEvaluatorGetField extends AbstractInstructionEvaluator<JALParser.JvmInsGetfieldContext> {
    public InstructionEvaluatorGetField() {
        super(EOpcodes.GETFIELD);
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsGetfieldContext instruction) {
        return InstructionEvaluateHelperField.evaluate(this, instruction.jvmInsArgFieldRef(), EOpcodes.GETFIELD);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction) {
        FieldInsnNode fieldInsnNode = (FieldInsnNode) instruction.insn();
        return FrameDifferenceInfo.builder(instruction)
                .popObjectRef(TypeDescriptor.className(fieldInsnNode.owner))
                .pushUnknownType(TypeDescriptor.parse(fieldInsnNode.desc))
                .build();
    }

    @Override
    public JALParser.JvmInsGetfieldContext map(JALParser.@NotNull InstructionContext instruction) {
        return instruction.jvmInsGetfield();
    }
}
