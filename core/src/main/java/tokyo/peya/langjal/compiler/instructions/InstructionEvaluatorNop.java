package tokyo.peya.langjal.compiler.instructions;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public class InstructionEvaluatorNop extends AbstractSingleInstructionEvaluator<JALParser.JvmInsNopContext>
{
    public InstructionEvaluatorNop()
    {
        super(EOpcodes.NOP);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.same();
    }

    @Override
    protected JALParser.JvmInsNopContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsNop();
    }
}
