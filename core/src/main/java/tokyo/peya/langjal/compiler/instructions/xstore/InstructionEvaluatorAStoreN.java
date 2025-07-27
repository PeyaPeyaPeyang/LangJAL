package tokyo.peya.langjal.compiler.instructions.xstore;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.VarInsnNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementCapsule;
import tokyo.peya.langjal.compiler.exceptions.IllegalInstructionException;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.ClassReferenceType;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.JALMethodCompiler;

public class InstructionEvaluatorAStoreN extends AbstractInstructionEvaluator<JALParser.JvmInsAstoreNContext>
{
    public InstructionEvaluatorAStoreN()
    {
        super(EOpcodes.ASTORE_0, EOpcodes.ASTORE_1, EOpcodes.ASTORE_2, EOpcodes.ASTORE_3);
    }

    @Override
    protected @NotNull EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler,
                                                     JALParser.@NotNull JvmInsAstoreNContext ctxt)
    {
        String type = ClassReferenceType.OBJECT.toString();
        JALParser.LocalInstigationContext ins = ctxt.localInstigation();
        if (has(ctxt.INSN_ASTORE_0()))
            return InstructionEvaluateHelperXStore.evaluateN(this, EOpcodes.ASTORE, 0, compiler, type, ins);
        else if (has(ctxt.INSN_ASTORE_1()))
            return InstructionEvaluateHelperXStore.evaluateN(this, EOpcodes.ASTORE, 1, compiler, type, ins);
        else if (has(ctxt.INSN_ASTORE_2()))
            return InstructionEvaluateHelperXStore.evaluateN(this, EOpcodes.ASTORE, 2, compiler, type, ins);
        else if (has(ctxt.INSN_ASTORE_3()))
            return InstructionEvaluateHelperXStore.evaluateN(this, EOpcodes.ASTORE, 3, compiler, type, ins);

        throw new IllegalInstructionException("Unexpected instruction: " + ctxt.getText(), ctxt);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        VarInsnNode varInsnNode = (VarInsnNode) instruction.insn();

        StackElementCapsule elementCapsule = new StackElementCapsule(instruction);
        return FrameDifferenceInfo.builder(instruction)
                                  .popToCapsule(elementCapsule)
                                  .addLocalFromCapsule(varInsnNode.var, elementCapsule)
                                  .build();
    }

    @Override
    protected JALParser.JvmInsAstoreNContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsAstoreN();
    }
}
