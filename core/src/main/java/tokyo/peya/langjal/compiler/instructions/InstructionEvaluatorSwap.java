package tokyo.peya.langjal.compiler.instructions;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementCapsule;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public class InstructionEvaluatorSwap extends AbstractSingleInstructionEvaluator<JALParser.JvmInsSwapContext>
{
    public InstructionEvaluatorSwap()
    {
        super(EOpcodes.SWAP);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        StackElementCapsule value1 = new StackElementCapsule(instruction);
        StackElementCapsule value2 = new StackElementCapsule(instruction);
        return FrameDifferenceInfo.builder(instruction)
                                  .pop(value1)
                                  .pop(value2)
                                  .push(value1)
                                  .push(value2)
                                  .build();
    }

    @Override
    protected JALParser.JvmInsSwapContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsSwap();
    }
}
