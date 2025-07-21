package tokyo.peya.langjal.compiler.instructions.xconst;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.compiler.instructions.AbstractSingleInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public class InstructionEvaluatorAConstNull
        extends AbstractSingleInstructionEvaluator<JALParser.JvmInsAconstNullContext>
{
    public InstructionEvaluatorAConstNull()
    {
        super(EOpcodes.ACONST_NULL);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.builder(instruction)
                                  .pushNull()
                                  .build();
    }

    @Override
    protected JALParser.JvmInsAconstNullContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsAconstNull();
    }
}
