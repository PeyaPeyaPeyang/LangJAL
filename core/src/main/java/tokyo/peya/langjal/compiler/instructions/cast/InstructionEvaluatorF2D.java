package tokyo.peya.langjal.compiler.instructions.cast;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.instructions.AbstractSingleInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public class InstructionEvaluatorF2D extends AbstractSingleInstructionEvaluator<JALParser.JvmInsF2DContext>
{
    public InstructionEvaluatorF2D()
    {
        super(EOpcodes.F2D);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.builder(instruction)
                                  .popPrimitive(StackElementType.FLOAT)
                                  .pushPrimitive(StackElementType.DOUBLE)
                                  .build();
    }

    @Override
    protected JALParser.JvmInsF2DContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsF2D();
    }
}
