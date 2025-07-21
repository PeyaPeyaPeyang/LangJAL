package tokyo.peya.langjal.compiler.instructions;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementCapsule;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public class InstructionEvaluatorPop extends AbstractSingleInstructionEvaluator<JALParser.JvmInsPopContext>
{
    public InstructionEvaluatorPop()
    {
        super(EOpcodes.POP);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.builder(instruction)
                                  .popToCapsule(new StackElementCapsule(instruction))  // 捨てる
                                  .build();
    }

    @Override
    protected JALParser.JvmInsPopContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsPop();
    }
}
