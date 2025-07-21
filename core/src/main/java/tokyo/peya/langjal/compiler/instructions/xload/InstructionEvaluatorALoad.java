package tokyo.peya.langjal.compiler.instructions.xload;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.VarInsnNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementCapsule;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.JALMethodCompiler;

public class InstructionEvaluatorALoad extends AbstractInstructionEvaluator<JALParser.JvmInsAloadContext>
{
    @Override
    protected @NotNull EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler,
                                                     JALParser.@NotNull JvmInsAloadContext ctxt)
    {
        return InstructionEvaluateHelperXLoad.evaluate(
                this,
                compiler,
                ctxt.jvmInsArgLocalRef(),
                Opcodes.ALOAD,
                "aload",
                ctxt.INSN_WIDE()
        );
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
    protected JALParser.JvmInsAloadContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsAload();
    }
}
