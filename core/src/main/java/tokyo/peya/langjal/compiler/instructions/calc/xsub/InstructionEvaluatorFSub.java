package tokyo.peya.langjal.compiler.instructions.calc.xsub;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.instructions.AbstractSingleInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public class InstructionEvaluatorFSub extends AbstractSingleInstructionEvaluator<JALParser.JvmInsFsubContext>
{
    public InstructionEvaluatorFSub()
    {
        super(EOpcodes.FSUB);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.builder(instruction)
                                  .popPrimitive(StackElementType.FLOAT)
                                  .popPrimitive(StackElementType.FLOAT)
                                  .pushPrimitive(StackElementType.FLOAT)
                                  .build();
    }

    @Override
    protected JALParser.JvmInsFsubContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsFsub();
    }
}
