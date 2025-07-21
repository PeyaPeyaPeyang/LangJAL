package tokyo.peya.langjal.compiler.instructions.xaload;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.instructions.AbstractSingleInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public class InstructionEvaluatorAALoad extends AbstractSingleInstructionEvaluator<JALParser.JvmInsAaloadContext>
{
    public InstructionEvaluatorAALoad()
    {
        super(EOpcodes.AALOAD);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.builder(instruction)
                                  .popPrimitive(StackElementType.INTEGER)
                                  .popObjectRef(TypeDescriptor.parse("[Ljava/lang/Object;"))
                                  .pushObjectRef()
                                  .build();
    }

    @Override
    protected JALParser.JvmInsAaloadContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsAaload();
    }
}
