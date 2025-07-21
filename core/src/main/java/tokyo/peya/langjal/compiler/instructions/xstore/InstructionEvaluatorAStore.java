package tokyo.peya.langjal.compiler.instructions.xstore;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.VarInsnNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementCapsule;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.JALMethodCompiler;

public class InstructionEvaluatorAStore extends AbstractInstructionEvaluator<JALParser.JvmInsAstoreContext>
{
    @Override
    protected @NotNull EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler,
                                                     JALParser.@NotNull JvmInsAstoreContext ctxt)
    {
        return InstructionEvaluateHelperXStore.evaluate(
                this,
                EOpcodes.ASTORE,
                compiler,
                ctxt.jvmInsArgLocalRef(),
                ctxt.localInstigation(),
                "Ljava/lang/Object;",
                "astore",
                ctxt.INSN_WIDE()
        );
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
    protected JALParser.JvmInsAstoreContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsAstore();
    }
}
