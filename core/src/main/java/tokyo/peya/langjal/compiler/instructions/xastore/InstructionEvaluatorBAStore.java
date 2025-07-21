package tokyo.peya.langjal.compiler.instructions.xastore;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.instructions.AbstractSingleInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public class InstructionEvaluatorBAStore extends AbstractSingleInstructionEvaluator<JALParser.JvmInsBastoreContext>
{
    public InstructionEvaluatorBAStore()
    {
        super(EOpcodes.BASTORE);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.builder(instruction)
                                  .popPrimitive(StackElementType.INTEGER)  // Byte は 整数として扱われる
                                  .popPrimitive(StackElementType.INTEGER)
                                  .popObjectRef(TypeDescriptor.parse("[B"))
                                  .build();
    }

    @Override
    protected JALParser.JvmInsBastoreContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsBastore();
    }
}
