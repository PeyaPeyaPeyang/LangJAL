package tokyo.peya.langjal.compiler.instructions.calc.xxor;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.instructions.AbstractSingleInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public class InstructionEvaluatorLXOr extends AbstractSingleInstructionEvaluator<JALParser.JvmInsLxorContext>
{
    public InstructionEvaluatorLXOr()
    {
        super(EOpcodes.LXOR);
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
    protected JALParser.JvmInsLxorContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsLxor();
    }
}
