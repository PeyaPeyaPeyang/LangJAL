package tokyo.peya.langjal.compiler.instructions.calc.xadd;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.instructions.AbstractSingleInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public class InstructionEvaluatorLAdd extends AbstractSingleInstructionEvaluator<JALParser.JvmInsLaddContext>
{
    public InstructionEvaluatorLAdd()
    {
        super(EOpcodes.LADD);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.builder(instruction)
                                  .popPrimitive(StackElementType.LONG)
                                  .popPrimitive(StackElementType.LONG)
                                  .pushPrimitive(StackElementType.LONG)
                                  .build();
    }
    @Override
    protected JALParser.JvmInsLaddContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsLadd();
    }
}
