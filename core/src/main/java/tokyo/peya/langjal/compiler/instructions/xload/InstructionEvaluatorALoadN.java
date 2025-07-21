package tokyo.peya.langjal.compiler.instructions.xload;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.VarInsnNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementCapsule;
import tokyo.peya.langjal.compiler.exceptions.IllegalInstructionException;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.JALMethodCompiler;

public class InstructionEvaluatorALoadN extends AbstractInstructionEvaluator<JALParser.JvmInsAloadNContext>
{
    @Override
    protected @NotNull EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler,
                                                     JALParser.@NotNull JvmInsAloadNContext ctxt)
    {
        if (has(ctxt.INSN_ALOAD_0()))
            return InstructionEvaluateHelperXLoad.evaluateN(this, ctxt, compiler, EOpcodes.ALOAD, 0);
        else if (has(ctxt.INSN_ALOAD_1()))
            return InstructionEvaluateHelperXLoad.evaluateN(this, ctxt, compiler, EOpcodes.ALOAD, 1);
        else if (has(ctxt.INSN_ALOAD_2()))
            return InstructionEvaluateHelperXLoad.evaluateN(this, ctxt, compiler, EOpcodes.ALOAD, 2);
        else if (has(ctxt.INSN_ALOAD_3()))
            return InstructionEvaluateHelperXLoad.evaluateN(this, ctxt, compiler, EOpcodes.ALOAD, 3);

        throw new IllegalInstructionException("Unexpected instruction: " + ctxt.getText(), ctxt);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        VarInsnNode insn = (VarInsnNode) instruction.insn();
        StackElementCapsule capsule = new StackElementCapsule(instruction);
        return FrameDifferenceInfo.builder(instruction)
                                  .consumeLocal(insn.var, capsule)
                                  .pushFromCapsule(capsule)
                                  .build();
    }

    @Override
    protected JALParser.JvmInsAloadNContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsAloadN();
    }
}
