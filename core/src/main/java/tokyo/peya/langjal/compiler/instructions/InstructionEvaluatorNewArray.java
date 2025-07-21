package tokyo.peya.langjal.compiler.instructions;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.IntInsnNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.exceptions.IllegalInstructionException;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.PrimitiveTypes;
import tokyo.peya.langjal.compiler.jvm.Type;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.JALMethodCompiler;

public class InstructionEvaluatorNewArray extends AbstractInstructionEvaluator<JALParser.JvmInsNewarrayContext>
{
    @Override
    protected @NotNull EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler,
                                                     JALParser.@NotNull JvmInsNewarrayContext ctxt)
    {
        JALParser.TypeDescriptorContext typeDescriptor = ctxt.typeDescriptor();
        TypeDescriptor desc = TypeDescriptor.parse(typeDescriptor.getText());
        Type descType = desc.getBaseType();
        if (!(descType instanceof PrimitiveTypes primitive))
            throw new IllegalInstructionException(
                    "newarray instruction requires a primitive type: " + descType.getDescriptor(),
                    ctxt
            );
        else if (desc.isArray())
            throw new IllegalInstructionException(
                    "newarray instruction cannot create an array of arrays: " + desc,
                    ctxt
            );

        if (primitive == PrimitiveTypes.VOID)
            throw new IllegalInstructionException(
                    "newarray instruction cannot create an array of void type: " + desc,
                    ctxt
            );

        IntInsnNode type = new IntInsnNode(EOpcodes.NEWARRAY, primitive.getAsmType());
        return EvaluatedInstruction.of(this, type);
    }


    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        IntInsnNode insn = (IntInsnNode) instruction.insn();
        TypeDescriptor desc = TypeDescriptor.parse("[" + PrimitiveTypes.fromASMType(insn.operand));
        return FrameDifferenceInfo.builder(instruction)
                                  .popPrimitive(StackElementType.INTEGER) // 配列のサイズを指定する int 型をポップ
                                  .pushObjectRef(desc)
                                  .build();
    }

    @Override
    protected JALParser.JvmInsNewarrayContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsNewarray();
    }
}
