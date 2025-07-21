package tokyo.peya.langjal.compiler.instructions.xstore;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.VarInsnNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.JALMethodCompiler;

public class InstructionEvaluatorDStore extends AbstractInstructionEvaluator<JALParser.JvmInsDstoreContext>
{
    @Override
    protected @NotNull EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler,
                                                     JALParser.@NotNull JvmInsDstoreContext ctxt)
    {
        return InstructionEvaluateHelperXStore.evaluate(
                this,
                EOpcodes.DSTORE,
                compiler,
                ctxt.jvmInsArgLocalRef(),
                ctxt.localInstigation(),
                "D",
                "dstore",
                ctxt.INSN_WIDE()
        );
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        VarInsnNode varInsnNode = (VarInsnNode) instruction.insn();
        return FrameDifferenceInfo.builder(instruction)
                                  .popPrimitive(StackElementType.DOUBLE)
                                  .addLocalPrimitive(varInsnNode.var, StackElementType.DOUBLE)
                                  .build();
    }

    @Override
    protected JALParser.JvmInsDstoreContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsDstore();
    }
}
