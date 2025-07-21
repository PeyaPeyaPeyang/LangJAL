package tokyo.peya.langjal.compiler.instructions.dup;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementCapsule;
import tokyo.peya.langjal.compiler.instructions.AbstractSingleInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public class InstructionEvaluatorDup2X1 extends AbstractSingleInstructionEvaluator<JALParser.JvmInsDup2X1Context>
{
    public InstructionEvaluatorDup2X1()
    {
        super(EOpcodes.DUP2_X1);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        StackElementCapsule topElement1 = new StackElementCapsule(instruction);
        StackElementCapsule topElement2 = new StackElementCapsule(instruction);
        StackElementCapsule topElement3 = new StackElementCapsule(instruction);
        return FrameDifferenceInfo.builder(instruction)  // ..., topElement3, topElement2, topElement1
                                  .popToCapsule(topElement1)  // ..., topElement3, topElement2
                                  .popToCapsule(topElement2)  // ..., topElement3
                                  .popToCapsule(topElement3)  // ...
                                  .pushFromCapsule(topElement2)  // ..., topElement2
                                  .pushFromCapsule(topElement1)  // ..., topElement2, topElement1
                                  .pushFromCapsule(topElement3)  // ..., topElement2, topElement1, topElement3
                                  .pushFromCapsule(topElement2)  // ..., topElement2, topElement1, topElement3, topElement2
                                  .pushFromCapsule(topElement1)  // ..., topElement2, topElement1, topElement3, topElement2, topElement1
                                  .build();
    }

    @Override
    protected JALParser.JvmInsDup2X1Context map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsDup2X1();
    }
}
