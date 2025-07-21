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

public class InstructionEvaluatorLLoadN extends AbstractInstructionEvaluator<JALParser.JvmInsLloadNContext>
{
    @Override
    protected @NotNull EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler,
                                                     JALParser.@NotNull JvmInsLloadNContext ctxt)
    {
        if (has(ctxt.INSN_LLOAD_0()))
            return InstructionEvaluateHelperXLoad.evaluateN(this, ctxt, compiler, EOpcodes.LLOAD, 0);
        else if (has(ctxt.INSN_LLOAD_1()))
            return InstructionEvaluateHelperXLoad.evaluateN(this, ctxt, compiler, EOpcodes.LLOAD, 1);
        else if (has(ctxt.INSN_LLOAD_2()))
            return InstructionEvaluateHelperXLoad.evaluateN(this, ctxt, compiler, EOpcodes.LLOAD, 2);
        else if (has(ctxt.INSN_LLOAD_3()))
            return InstructionEvaluateHelperXLoad.evaluateN(this, ctxt, compiler, EOpcodes.LLOAD, 3);

        throw new IllegalInstructionException("Unexpected instruction: " + ctxt.getText(), ctxt);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.builder(instruction)
                                  .pushPrimitive(StackElementType.LONG)
                                  .build();
    }

    @Override
    protected JALParser.JvmInsLloadNContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsLloadN();
    }
}
