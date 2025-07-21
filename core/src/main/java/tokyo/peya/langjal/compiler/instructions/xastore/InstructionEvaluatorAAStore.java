package tokyo.peya.langjal.compiler.instructions.xastore;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.instructions.AbstractSingleInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public class InstructionEvaluatorAAStore extends AbstractSingleInstructionEvaluator<JALParser.JvmInsAastoreContext>
{
    public InstructionEvaluatorAAStore()
    {
        super(EOpcodes.AASTORE);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.builder(instruction)
                                  .popObjectRef(TypeDescriptor.parse("[java/lang/Object;"))
                                  .popPrimitive(StackElementType.INTEGER)
                                  .popObjectRef(TypeDescriptor.parse("Ljava/lang/Object;"))
                                  .build();
    }

    @Override
    protected JALParser.JvmInsAastoreContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsAastore();
    }
}
