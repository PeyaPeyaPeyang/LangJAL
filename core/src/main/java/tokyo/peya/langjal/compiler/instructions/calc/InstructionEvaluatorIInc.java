package tokyo.peya.langjal.compiler.instructions.calc;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.IincInsnNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.compiler.exceptions.IllegalInstructionException;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.JALMethodCompiler;
import tokyo.peya.langjal.compiler.member.LocalVariableInfo;
import tokyo.peya.langjal.compiler.utils.EvaluatorCommons;

public class InstructionEvaluatorIInc extends AbstractInstructionEvaluator<JALParser.JvmInsIincContext>
{
    public InstructionEvaluatorIInc()
    {
        super(EOpcodes.IINC);
    }

    @Override
    protected @NotNull EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler,
                                                     JALParser.@NotNull JvmInsIincContext ctxt)
    {
        JALParser.JvmInsArgLocalRefContext ref = ctxt.jvmInsArgLocalRef();
        LocalVariableInfo local = compiler.getLocals().resolve(ref, "iinc");

        int idx = local.index();
        boolean isWide = ctxt.INSN_WIDE() != null;
        int increment = EvaluatorCommons.asInteger(ctxt.NUMBER());

        if (!isWide)
        {
            if (idx >= 0xFF)
                throw new IllegalInstructionException(
                        String.format(
                        "Local variable index %d is too large for iinc instruction. Use wide variant with.",
                        idx
                        ), ref
                );
            else if (increment < Byte.MIN_VALUE || increment > Byte.MAX_VALUE)
                throw new IllegalInstructionException(
                        String.format(
                        "Increment value %d is out of range for iinc instruction. Use wide variant with.",
                        increment
                        ), ref
                );
        }

        int size = isWide ? 6: 3;
        IincInsnNode insn = new IincInsnNode(idx, increment);
        return EvaluatedInstruction.of(this, insn, size);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.same();

    }

    @Override
    protected JALParser.JvmInsIincContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsIinc();
    }
}
