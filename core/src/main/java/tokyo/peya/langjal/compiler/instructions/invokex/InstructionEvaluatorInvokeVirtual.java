package tokyo.peya.langjal.compiler.instructions.invokex;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.JALMethodCompiler;

public class InstructionEvaluatorInvokeVirtual
        extends AbstractInstructionEvaluator<JALParser.JvmInsInvokevirtualContext>
{
    public InstructionEvaluatorInvokeVirtual()
    {
        super(EOpcodes.INVOKEVIRTUAL);
    }

    @Override
    protected @NotNull EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler,
                                                     JALParser.@NotNull JvmInsInvokevirtualContext ctxt)
    {
        return InstructionEvaluateHelperInvocation.evaluate(
                this,
                compiler.getClazz(),
                ctxt.jvmInsArgMethodRef(),
                EOpcodes.INVOKEVIRTUAL
        );
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return InstructionEvaluateHelperInvocation.getFrameNormalDifferenceInfo(instruction);
    }

    @Override
    protected JALParser.JvmInsInvokevirtualContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsInvokevirtual();
    }
}
