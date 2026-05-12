package tokyo.peya.langjal.compiler.instructions;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.exceptions.IllegalInstructionException;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.member.*;
import tokyo.peya.langjal.compiler.utils.EvaluatorCommons;

public class InstructionEvaluatorANewArray extends AbstractInstructionEvaluator<JALParser.JvmInsAnewArrayContext> {
    public InstructionEvaluatorANewArray() {
        super(EOpcodes.ANEWARRAY);
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsAnewArrayContext instruction) {
        JALParser.TypeDescriptorContext typeDescriptor = instruction.typeDescriptor();
        if (!typeDescriptor.getText().startsWith("L"))
            throw new IllegalInstructionException(
                    "Reference type expected for anewarray, but got " + typeDescriptor.getText(),
                    typeDescriptor
            );

        // Ljava/lang/String; -> java.lang.String に変換
        String typeName = EvaluatorCommons.unwrapClassTypeDescriptor(typeDescriptor);

        TypeInsnNode type = new TypeInsnNode(EOpcodes.ANEWARRAY, typeName);
        return EvaluatedInstruction.of(this, type);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction) {
        TypeInsnNode type = (TypeInsnNode) instruction.insn();
        return FrameDifferenceInfo.builder(instruction)
                .popPrimitive(StackElementType.INTEGER)
                .pushObjectRef(TypeDescriptor.className(type.desc))
                .build();
    }

    @Override
    public JALParser.JvmInsAnewArrayContext map(JALParser.@NotNull InstructionContext instruction) {
        return instruction.jvmInsAnewArray();
    }
}
