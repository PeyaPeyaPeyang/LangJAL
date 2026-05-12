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
import tokyo.peya.langjal.compiler.jvm.PrimitiveTypes;
import tokyo.peya.langjal.compiler.jvm.Type;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.member.*;

public class InstructionEvaluatorNewArray extends AbstractInstructionEvaluator<JALParser.JvmInsNewarrayContext> {
    public InstructionEvaluatorNewArray() {
        super(EOpcodes.NEWARRAY);
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsNewarrayContext instruction) {
        JALParser.TypeDescriptorContext typeDescriptor = instruction.typeDescriptor();
        TypeDescriptor desc = TypeDescriptor.parse(typeDescriptor.getText());
        Type descType = desc.getBaseType();
        if (!(descType instanceof PrimitiveTypes primitive))
            throw new IllegalInstructionException(
                    "newarray instruction requires a primitive type: " + descType.getDescriptor(),
                    instruction
            );
        else if (desc.isArray())
            throw new IllegalInstructionException(
                    "newarray instruction cannot create an array of arrays: " + desc,
                    instruction
            );

        if (primitive == PrimitiveTypes.VOID)
            throw new IllegalInstructionException(
                    "newarray instruction cannot create an array of void type: " + desc,
                    instruction
            );

        IntInsnNode type = new IntInsnNode(EOpcodes.NEWARRAY, primitive.getAsmType());
        return EvaluatedInstruction.of(this, type);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction) {
        IntInsnNode insn = (IntInsnNode) instruction.insn();
        TypeDescriptor desc = TypeDescriptor.parse("[" + PrimitiveTypes.fromASMType(insn.operand));
        return FrameDifferenceInfo.builder(instruction)
                .popPrimitive(StackElementType.INTEGER) // 配列のサイズを指定する int 型をポップ
                .pushObjectRef(desc)
                .build();
    }

    @Override
    public JALParser.JvmInsNewarrayContext map(JALParser.@NotNull InstructionContext instruction) {
        return instruction.jvmInsNewarray();
    }
}
