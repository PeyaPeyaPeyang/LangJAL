package tokyo.peya.langjal.compiler.instructions.cast;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.instructions.AbstractSingleInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public class InstructionEvaluatorI2B extends AbstractSingleInstructionEvaluator<JALParser.JvmInsI2BContext>
{
    public InstructionEvaluatorI2B()
    {
        super(EOpcodes.I2B);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.builder(instruction)
                                  .popPrimitive(StackElementType.INTEGER)
                                  .pushPrimitive(StackElementType.INTEGER)  // 型変換後も整数型なので、StackElementType.INTEGERを使用
                                  .build();
    }

    @Override
    protected JALParser.JvmInsI2BContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsI2B();
    }
}
