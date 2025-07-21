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

public class InstructionEvaluatorFStoreN extends AbstractInstructionEvaluator<JALParser.JvmInsFstoreNContext>
{
    @Override
    protected @NotNull EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler,
                                                     JALParser.@NotNull JvmInsFstoreNContext ctxt)
    {
        JALParser.LocalInstigationContext ins = ctxt.localInstigation();
        if (has(ctxt.INSN_FSTORE_0()))
            return InstructionEvaluateHelperXStore.evaluateN(this, EOpcodes.FSTORE, 0, compiler, "F", ins);
        else if (has(ctxt.INSN_FSTORE_1()))
            return InstructionEvaluateHelperXStore.evaluateN(this, EOpcodes.FSTORE, 1, compiler, "F", ins);
        else if (has(ctxt.INSN_FSTORE_2()))
            return InstructionEvaluateHelperXStore.evaluateN(this, EOpcodes.FSTORE, 2, compiler, "F", ins);
        else if (has(ctxt.INSN_FSTORE_3()))
            return InstructionEvaluateHelperXStore.evaluateN(this, EOpcodes.FSTORE, 3, compiler, "F", ins);

        throw new IllegalInstructionException("Unexpected instruction: " + ctxt.getText(), ctxt);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        VarInsnNode varInsnNode = (VarInsnNode) instruction.insn();
        return FrameDifferenceInfo.builder(instruction)
                                  .popPrimitive(StackElementType.FLOAT)
                                  .addLocalPrimitive(varInsnNode.var, StackElementType.FLOAT)
                                  .build();
    }

    @Override
    protected JALParser.JvmInsFstoreNContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsFstoreN();
    }
}
