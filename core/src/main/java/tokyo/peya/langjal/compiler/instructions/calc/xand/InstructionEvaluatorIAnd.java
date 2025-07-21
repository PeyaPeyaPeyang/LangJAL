package tokyo.peya.langjal.compiler.instructions.calc.xand;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.instructions.AbstractSingleInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public class InstructionEvaluatorIAnd extends AbstractSingleInstructionEvaluator<JALParser.JvmInsIandContext>
{
    public InstructionEvaluatorIAnd()
    {
        super(EOpcodes.IAND);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.builder(instruction)
                                  .popPrimitive(StackElementType.INTEGER)
                                  .popPrimitive(StackElementType.INTEGER)
                                  .pushPrimitive(StackElementType.INTEGER)
                                  .build();
    }

    @Override
    protected JALParser.JvmInsIandContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsIand();
    }
}
