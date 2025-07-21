package tokyo.peya.langjal.compiler.instructions;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public class InstructionEvaluatorMonitorExit
        extends AbstractSingleInstructionEvaluator<JALParser.JvmInsMonitorexitContext>
{
    public InstructionEvaluatorMonitorExit()
    {
        super(EOpcodes.MONITOREXIT);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.builder(instruction)
                                  .popObjectRef()
                                  .build();
    }

    @Override
    protected JALParser.JvmInsMonitorexitContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsMonitorexit();
    }
}
