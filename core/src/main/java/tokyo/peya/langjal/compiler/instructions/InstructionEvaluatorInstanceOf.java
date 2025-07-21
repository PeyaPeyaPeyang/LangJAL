package tokyo.peya.langjal.compiler.instructions;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.TypeInsnNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.JALMethodCompiler;
import tokyo.peya.langjal.compiler.utils.EvaluatorCommons;

public class InstructionEvaluatorInstanceOf extends AbstractInstructionEvaluator<JALParser.JvmInsInstanceofContext>
{
    @Override
    protected @NotNull EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler,
                                                     JALParser.@NotNull JvmInsInstanceofContext ctxt)
    {
        JALParser.TypeDescriptorContext typeDescriptor = ctxt.typeDescriptor();
        // Ljava/lang/String; -> java.lang.String に変換
        String typeName = EvaluatorCommons.unwrapClassTypeDescriptor(typeDescriptor);

        TypeInsnNode type = new TypeInsnNode(EOpcodes.INSTANCEOF, typeName);
        return EvaluatedInstruction.of(this, type);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.builder(instruction)
                                  .popObjectRef()  // なんでも
                                  .pushPrimitive(StackElementType.INTEGER) // instanceof の結果は int 型
                                  .build();
    }

    @Override
    protected JALParser.JvmInsInstanceofContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsInstanceof();
    }
}
