package tokyo.peya.langjal.compiler.instructions.xload;

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

public class InstructionEvaluatorFLoadN extends AbstractInstructionEvaluator<JALParser.JvmInsFloadNContext>
{
    @Override
    protected @NotNull EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler,
                                                     JALParser.@NotNull JvmInsFloadNContext ctxt)
    {
        if (has(ctxt.INSN_FLOAD_0()))
            return InstructionEvaluateHelperXLoad.evaluateN(this, ctxt, compiler, EOpcodes.FLOAD, 0);
        else if (has(ctxt.INSN_FLOAD_1()))
            return InstructionEvaluateHelperXLoad.evaluateN(this, ctxt, compiler, EOpcodes.FLOAD, 1);
        else if (has(ctxt.INSN_FLOAD_2()))
            return InstructionEvaluateHelperXLoad.evaluateN(this, ctxt, compiler, EOpcodes.FLOAD, 2);
        else if (has(ctxt.INSN_FLOAD_3()))
            return InstructionEvaluateHelperXLoad.evaluateN(this, ctxt, compiler, EOpcodes.FLOAD, 3);

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
    protected JALParser.JvmInsFloadNContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsFloadN();
    }
}
