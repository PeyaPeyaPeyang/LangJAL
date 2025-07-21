package tokyo.peya.langjal.compiler.instructions;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public class InstructionEvaluatorMonitorEnter
        extends AbstractSingleInstructionEvaluator<JALParser.JvmInsMonitorenterContext>
{
    public InstructionEvaluatorMonitorEnter()
    {
        super(EOpcodes.MONITORENTER);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.builder(instruction)
                                  .popObjectRef()
                                  .build();
    }

    @Override
    protected JALParser.JvmInsMonitorenterContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsMonitorenter();
    }
}
