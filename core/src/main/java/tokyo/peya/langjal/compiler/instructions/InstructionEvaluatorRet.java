package tokyo.peya.langjal.compiler.instructions;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.VarInsnNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.compiler.exceptions.IllegalInstructionException;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.JALMethodCompiler;
import tokyo.peya.langjal.compiler.member.LocalVariableInfo;

public class InstructionEvaluatorRet extends AbstractInstructionEvaluator<JALParser.JvmInsRetContext>
{
    public InstructionEvaluatorRet()
    {
        super(EOpcodes.RET);
    }

    @Override
    protected @NotNull EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler,
                                                     JALParser.@NotNull JvmInsRetContext ctxt)
    {
        LocalVariableInfo local = compiler.getLocals().resolve(ctxt.jvmInsArgLocalRef(), "ret");

        int idx = local.index();
        boolean isWide = ctxt.INSN_WIDE() != null;
        if (idx >= 0xFF && !isWide)
            throw new IllegalInstructionException(
                    String.format(
                    "Local variable index %d is too large for ret instruction. Use wide variant with.",
                    idx
                    ), ctxt.jvmInsArgLocalRef()
            );

        VarInsnNode insn = new VarInsnNode(EOpcodes.RET, local.index());

        int size = isWide ? 4: 2;
        return EvaluatedInstruction.of(this, insn, size);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.same();
    }

    @Override
    protected JALParser.JvmInsRetContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsRet();
    }
}
