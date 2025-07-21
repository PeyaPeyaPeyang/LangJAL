package tokyo.peya.langjal.compiler.instructions.ldc;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.JALMethodCompiler;

public class InstructionEvaluatorLDC extends AbstractInstructionEvaluator<JALParser.JvmInsLdcContext>
{
    @Override
    protected @NotNull EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler,
                                                     JALParser.@NotNull JvmInsLdcContext ctxt)
    {
        return InstructionEvaluationHelperLDC.evaluate(
                this, ctxt.jvmInsArgScalarType(), InstructionEvaluationHelperLDC.LDC
        );
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return InstructionEvaluationHelperLDC.getFrameDifferenceInfo(instruction);
    }

    @Override
    protected JALParser.JvmInsLdcContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsLdc();
    }
}
