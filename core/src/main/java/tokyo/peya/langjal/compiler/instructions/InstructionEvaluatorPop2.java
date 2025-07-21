package tokyo.peya.langjal.compiler.instructions;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementCapsule;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public class InstructionEvaluatorPop2 extends AbstractSingleInstructionEvaluator<JALParser.JvmInsPop2Context>
{
    public InstructionEvaluatorPop2()
    {
        super(EOpcodes.POP2);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.builder(instruction)
                                  .popToCapsule(new StackElementCapsule(instruction))  // 捨てる
                                  .popToCapsule(new StackElementCapsule(instruction))  // 捨てる
                                  .build();
    }

    @Override
    protected JALParser.JvmInsPop2Context map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsPop2();
    }
}
