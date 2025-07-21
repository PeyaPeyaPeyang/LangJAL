package tokyo.peya.langjal.compiler.instructions.xreturn;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.instructions.AbstractSingleInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.JALMethodCompiler;

public class InstructionEvaluatorLReturn extends AbstractSingleInstructionEvaluator<JALParser.JvmInsLreturnContext>
{
    public InstructionEvaluatorLReturn()
    {
        super(EOpcodes.LRETURN);
    }

    @Override
    protected @NotNull EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler,
                                                     JALParser.@NotNull JvmInsLreturnContext ctxt)
    {
        InstructionEvaluateHelperXReturn.checkReturnType(compiler.getMethod(), ctxt, TypeDescriptor.LONG);
        return super.evaluate(compiler, ctxt);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.builder(instruction)
                                  .popPrimitive(StackElementType.LONG)
                                  .build();
    }

    @Override
    protected JALParser.JvmInsLreturnContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsLreturn();
    }
}
