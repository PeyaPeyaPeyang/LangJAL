package tokyo.peya.langjal.compiler.instructions.xastore;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.instructions.AbstractSingleInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public class InstructionEvaluatorFAStore extends AbstractSingleInstructionEvaluator<JALParser.JvmInsFastoreContext>
{
    public InstructionEvaluatorFAStore()
    {
        super(EOpcodes.FASTORE);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.builder(instruction)
                                  .popPrimitive(StackElementType.FLOAT)
                                  .popPrimitive(StackElementType.INTEGER)
                                  .popObjectRef(TypeDescriptor.parse("[F"))
                                  .build();
    }

    @Override
    protected JALParser.JvmInsFastoreContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsFastore();
    }
}
