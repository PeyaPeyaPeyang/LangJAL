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

public class InstructionEvaluatorPutStatic extends AbstractInstructionEvaluator<JALParser.JvmInsPutstaticContext> {
    public InstructionEvaluatorPutStatic() {
        super(EOpcodes.PUTSTATIC);
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsPutstaticContext instruction) {
        return InstructionEvaluateHelperField.evaluate(this, instruction.jvmInsArgFieldRef(), EOpcodes.PUTSTATIC);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction) {
        FieldInsnNode fieldInsnNode = (FieldInsnNode) instruction.insn();
        return FrameDifferenceInfo.builder(instruction)
                .popUnknownType(TypeDescriptor.parse(fieldInsnNode.desc))
                .build();
    }

    @Override
    public JALParser.JvmInsPutstaticContext map(JALParser.@NotNull InstructionContext instruction) {
        return instruction.jvmInsPutstatic();
    }
}
