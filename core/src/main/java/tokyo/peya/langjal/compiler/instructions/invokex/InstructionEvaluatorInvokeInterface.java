package tokyo.peya.langjal.compiler.instructions.invokex;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.JALMethodCompiler;

public class InstructionEvaluatorInvokeInterface
        extends AbstractInstructionEvaluator<JALParser.JvmInsInvokeinterfaceContext>
{
    @Override
    protected @NotNull EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler,
                                                     JALParser.@NotNull JvmInsInvokeinterfaceContext ctxt)
    {
        return InstructionEvaluateHelperInvocation.evaluate(
                this,
                compiler.getClazz(),
                ctxt.jvmInsArgMethodRef(),
                EOpcodes.INVOKEINTERFACE
        );
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return InstructionEvaluateHelperInvocation.getFrameNormalDifferenceInfo(instruction);
    }

    @Override
    protected JALParser.JvmInsInvokeinterfaceContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsInvokeinterface();
    }
}
