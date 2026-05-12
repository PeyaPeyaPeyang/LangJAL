package tokyo.peya.langjal.compiler.instructions;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.member.*;

public class InstructionEvaluatorNew extends AbstractInstructionEvaluator<JALParser.JvmInsNewContext> {
    public InstructionEvaluatorNew() {
        super(EOpcodes.NEW);
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsNewContext instruction) {
        JALParser.FullQualifiedClassNameContext typeDescriptor = instruction.fullQualifiedClassName();
        TypeInsnNode type = new TypeInsnNode(EOpcodes.NEW, typeDescriptor.getText());
        return EvaluatedInstruction.of(this, type);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction) {
        TypeInsnNode insn = (TypeInsnNode) instruction.insn();
        return FrameDifferenceInfo.builder(instruction)
                .pushObjectRef(TypeDescriptor.className(insn.desc))
                .build();
    }

    @Override
    public JALParser.JvmInsNewContext map(JALParser.@NotNull InstructionContext instruction) {
        return instruction.jvmInsNew();
    }
}
