package tokyo.peya.langjal.compiler.instructions.cast;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.instructions.AbstractSingleInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public class InstructionEvaluatorD2F extends AbstractSingleInstructionEvaluator<JALParser.JvmInsD2FContext>
{
    public InstructionEvaluatorD2F()
    {
        super(EOpcodes.D2F);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.builder(instruction)
                                  .popPrimitive(StackElementType.DOUBLE)
                                  .pushPrimitive(StackElementType.FLOAT)
                                  .build();
    }

    @Override
    protected JALParser.JvmInsD2FContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsD2F();
    }
}
