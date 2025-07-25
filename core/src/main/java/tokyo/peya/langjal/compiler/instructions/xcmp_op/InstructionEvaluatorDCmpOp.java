package tokyo.peya.langjal.compiler.instructions.xcmp_op;

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

public class InstructionEvaluatorDCmpOp extends AbstractInstructionEvaluator<JALParser.JvmInsDcmpOPContext>
{
    public InstructionEvaluatorDCmpOp()
    {
        super(EOpcodes.DCMPG, EOpcodes.DCMPL);
    }

    @Override
    protected @NotNull EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler,
                                                     JALParser.@NotNull JvmInsDcmpOPContext ctxt)
    {
        if (has(ctxt.INSN_DCMPG()))
            return this.visitSingle(ctxt, EOpcodes.DCMPG);
        else if (has(ctxt.INSN_DCMPL()))
            return this.visitSingle(ctxt, EOpcodes.DCMPL);

        throw new IllegalInstructionException("Invalid instruction: " + ctxt.getText(), ctxt);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.builder(instruction)
                                  .popPrimitive(StackElementType.DOUBLE)
                                  .popPrimitive(StackElementType.DOUBLE)
                                  .pushPrimitive(StackElementType.INTEGER)
                                  .build();
    }

    @Override
    protected JALParser.JvmInsDcmpOPContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsDcmpOP();
    }
}
