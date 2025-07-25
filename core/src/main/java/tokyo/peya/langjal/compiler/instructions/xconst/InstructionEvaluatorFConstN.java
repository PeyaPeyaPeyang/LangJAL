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

public class InstructionEvaluatorFConstN extends AbstractInstructionEvaluator<JALParser.JvmInsFconstNContext>
{
    public InstructionEvaluatorFConstN()
    {
        super(EOpcodes.FCONST_0, EOpcodes.FCONST_1, EOpcodes.FCONST_2);
    }

    @Override
    protected @NotNull EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler,
                                                     JALParser.@NotNull JvmInsFconstNContext ctxt)
    {
        if (has(ctxt.INSN_FCONST_0()))
            return this.visitSingle(ctxt, EOpcodes.FCONST_0);
        else if (has(ctxt.INSN_FCONST_1()))
            return this.visitSingle(ctxt, EOpcodes.FCONST_1);
        else if (has(ctxt.INSN_FCONST_2()))
            return this.visitSingle(ctxt, EOpcodes.FCONST_2);

        throw new IllegalInstructionException("Unexpected instruction: " + ctxt.getText(), ctxt);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.builder(instruction)
                                  .pushPrimitive(StackElementType.FLOAT)
                                  .build();
    }

    @Override
    protected JALParser.JvmInsFconstNContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsFconstN();
    }
}
