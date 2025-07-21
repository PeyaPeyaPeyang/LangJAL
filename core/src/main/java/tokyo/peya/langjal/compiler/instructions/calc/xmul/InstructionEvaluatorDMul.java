package tokyo.peya.langjal.compiler.instructions.calc.xmul;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.instructions.AbstractSingleInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public class InstructionEvaluatorDMul extends AbstractSingleInstructionEvaluator<JALParser.JvmInsDmulContext>
{
    public InstructionEvaluatorDMul()
    {
        super(EOpcodes.DMUL);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.builder(instruction)
                                  .popPrimitive(StackElementType.DOUBLE)
                                  .popPrimitive(StackElementType.DOUBLE)
                                  .pushPrimitive(StackElementType.DOUBLE)
                                  .build();
    }

    @Override
    protected JALParser.JvmInsDmulContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsDmul();
    }
}
