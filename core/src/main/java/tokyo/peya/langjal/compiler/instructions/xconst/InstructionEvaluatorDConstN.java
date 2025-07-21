package tokyo.peya.langjal.compiler.instructions.xconst;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.exceptions.IllegalInstructionException;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.JALMethodCompiler;

public class InstructionEvaluatorDConstN extends AbstractInstructionEvaluator<JALParser.JvmInsDconstNContext>
{
    @Override
    protected @NotNull EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler,
                                                     JALParser.@NotNull JvmInsDconstNContext ctxt)
    {
        if (has(ctxt.INSN_DCONST_0()))
            return this.visitSingle(ctxt, EOpcodes.DCONST_0);
        if (has(ctxt.INSN_DCONST_1()))
            return this.visitSingle(ctxt, EOpcodes.DCONST_0);

        throw new IllegalInstructionException("Unknown instruction: " + ctxt.getText(), ctxt);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.builder(instruction)
                                  .pushPrimitive(StackElementType.DOUBLE)
                                  .build();
    }

    @Override
    protected JALParser.JvmInsDconstNContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsDconstN();
    }
}
