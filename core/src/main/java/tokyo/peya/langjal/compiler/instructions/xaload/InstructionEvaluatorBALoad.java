package tokyo.peya.langjal.compiler.instructions.xaload;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.instructions.AbstractSingleInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public class InstructionEvaluatorBALoad extends AbstractSingleInstructionEvaluator<JALParser.JvmInsBaloadContext>
{
    public InstructionEvaluatorBALoad()
    {
        super(EOpcodes.BALOAD);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.builder(instruction)
                                  .popPrimitive(StackElementType.INTEGER)
                                  .popObjectRef(TypeDescriptor.parse("[B"))
                                  .pushPrimitive(StackElementType.INTEGER)  // Byte は int として扱われる
                                  .build();
    }

    @Override
    protected JALParser.JvmInsBaloadContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsBaload();
    }
}
