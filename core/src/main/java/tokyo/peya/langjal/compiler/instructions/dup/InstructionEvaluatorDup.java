package tokyo.peya.langjal.compiler.instructions.dup;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementCapsule;
import tokyo.peya.langjal.compiler.instructions.AbstractSingleInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public class InstructionEvaluatorDup extends AbstractSingleInstructionEvaluator<JALParser.JvmInsDupContext>
{
    public InstructionEvaluatorDup()
    {
        super(EOpcodes.DUP);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        StackElementCapsule topElement = new StackElementCapsule(instruction);

        return FrameDifferenceInfo.builder(instruction)
                                  .popToCapsule(topElement)
                                  .pushFromCapsule(topElement)
                                  .pushFromCapsule(topElement)
                                  .build();
    }

    @Override
    protected JALParser.JvmInsDupContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsDup();
    }
}
