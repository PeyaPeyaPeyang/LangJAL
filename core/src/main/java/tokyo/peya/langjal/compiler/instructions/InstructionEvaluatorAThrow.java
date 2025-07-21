package tokyo.peya.langjal.compiler.instructions;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public class InstructionEvaluatorAThrow extends AbstractSingleInstructionEvaluator<JALParser.JvmInsAthrowContext>
{
    public InstructionEvaluatorAThrow()
    {
        super(EOpcodes.ATHROW);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.builder(instruction)
                                  .popObjectRef(TypeDescriptor.parse("Ljava/lang/Throwable;"))
                                  .build();
    }

    @Override
    protected JALParser.JvmInsAthrowContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsAthrow();
    }
}
