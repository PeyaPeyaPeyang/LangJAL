package tokyo.peya.langjal.compiler.instructions.calc.xmul;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.instructions.AbstractSingleInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public class InstructionEvaluatorFMul extends AbstractSingleInstructionEvaluator<JALParser.JvmInsFmulContext>
{
    public InstructionEvaluatorFMul()
    {
        super(EOpcodes.FMUL);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.builder(instruction)
                                  .popPrimitive(StackElementType.FLOAT)
                                  .popPrimitive(StackElementType.FLOAT)
                                  .pushPrimitive(StackElementType.FLOAT)
                                  .build();
    }

    @Override
    protected JALParser.JvmInsFmulContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsFmul();
    }
}
