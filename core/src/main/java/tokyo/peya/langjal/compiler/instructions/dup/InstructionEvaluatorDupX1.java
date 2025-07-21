package tokyo.peya.langjal.compiler.instructions.dup;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementCapsule;
import tokyo.peya.langjal.compiler.instructions.AbstractSingleInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public class InstructionEvaluatorDupX1 extends AbstractSingleInstructionEvaluator<JALParser.JvmInsDupX1Context>
{
    public InstructionEvaluatorDupX1()
    {
        super(EOpcodes.DUP_X1);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        StackElementCapsule topElement1 = new StackElementCapsule(instruction);
        StackElementCapsule topElement2 = new StackElementCapsule(instruction);
        return FrameDifferenceInfo.builder(instruction)  // ..., topElement2, topElement1
                                  .popToCapsule(topElement1)  // ..., topElement2
                                  .popToCapsule(topElement2)  // ...
                                  .pushFromCapsule(topElement1)  // ..., topElement1
                                  .pushFromCapsule(topElement2)  // ..., topElement1, topElement2
                                  .pushFromCapsule(topElement1)  // ..., topElement1, topElement2, topElement1
                                  .build();
    }

    @Override
    protected JALParser.JvmInsDupX1Context map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsDupX1();
    }
}
