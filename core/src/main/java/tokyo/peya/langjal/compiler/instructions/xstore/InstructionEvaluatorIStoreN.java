package tokyo.peya.langjal.compiler.instructions.xstore;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.VarInsnNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.exceptions.IllegalInstructionException;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.JALMethodCompiler;

public class InstructionEvaluatorIStoreN extends AbstractInstructionEvaluator<JALParser.JvmInsIstoreNContext>
{
    @Override
    protected @NotNull EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler,
                                                     JALParser.@NotNull JvmInsIstoreNContext ctxt)
    {
        JALParser.LocalInstigationContext ins = ctxt.localInstigation();
        if (has(ctxt.INSN_ISTORE_0()))
            return InstructionEvaluateHelperXStore.evaluateN(this, EOpcodes.ISTORE, 0, compiler, "I", ins);
        else if (has(ctxt.INSN_ISTORE_1()))
            return InstructionEvaluateHelperXStore.evaluateN(this, EOpcodes.ISTORE, 1, compiler, "I", ins);
        else if (has(ctxt.INSN_ISTORE_2()))
            return InstructionEvaluateHelperXStore.evaluateN(this, EOpcodes.ISTORE, 2, compiler, "I", ins);
        else if (has(ctxt.INSN_ISTORE_3()))
            return InstructionEvaluateHelperXStore.evaluateN(this, EOpcodes.ISTORE, 3, compiler, "I", ins);

        throw new IllegalInstructionException("Unexpected instruction: " + ctxt.getText(), ctxt);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        VarInsnNode varInsnNode = (VarInsnNode) instruction.insn();
        return FrameDifferenceInfo.builder(instruction)
                                  .popPrimitive(StackElementType.INTEGER)
                                  .addLocalPrimitive(varInsnNode.var, StackElementType.INTEGER)
                                  .build();
    }

    @Override
    protected JALParser.JvmInsIstoreNContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsIstoreN();
    }
}
