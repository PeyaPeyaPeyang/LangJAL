package tokyo.peya.langjal.compiler.member;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.AbstractInsnNode;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;

public record EvaluatedInstruction(
        @NotNull
        AbstractInstructionEvaluator<?> evaluator,
        @NotNull
        AbstractInsnNode insn,
        int customSize // wide, lookupswitch, tableswitch, など
)
{
    private EvaluatedInstruction(@NotNull AbstractInstructionEvaluator<?> evaluator, @NotNull AbstractInsnNode insn)
    {
        this(evaluator, insn, 0);
    }

    public int getInstructionSize()
    {
        if (this.customSize > 0)
            return this.customSize;  // 明確に指定されたサイズを返す
        else
            return EOpcodes.getOpcodeSize(this.insn.getOpcode());
    }

    public static EvaluatedInstruction of(@NotNull AbstractInstructionEvaluator<?> evaluator,
                                          @NotNull AbstractInsnNode insn)
    {
        return new EvaluatedInstruction(evaluator, insn);
    }

    public static EvaluatedInstruction of(@NotNull AbstractInstructionEvaluator<?> evaluator,
                                          @NotNull AbstractInsnNode insn, int size)
    {
        checkSizeProvided(insn.getOpcode(), size);
        return new EvaluatedInstruction(evaluator, insn, size);
    }

    private static void checkSizeProvided(int opcode, int size)
    {
        if (size > 0)
            return;

        if (opcode == EOpcodes.TABLESWITCH
                || opcode == EOpcodes.LOOKUPSWITCH
                || opcode == EOpcodes.WIDE
                || opcode == EOpcodes.INVOKEDYNAMIC)
            throw new IllegalArgumentException("Instruction with opcode(" + opcode + ") requires a custom size to be specified.");
    }
}
