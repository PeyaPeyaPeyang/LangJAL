package tokyo.peya.langjal.compiler.member;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.AbstractInsnNode;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;

/**
 * Represents an evaluated instruction, including its evaluator, instruction node, and custom size.
 *
 * @param evaluator   The instruction evaluator.
 * @param insn        The ASM instruction node.
 * @param customSize  The custom size for variable-sized instructions (e.g., wide, lookupswitch).
 */
public record EvaluatedInstruction(
        @NotNull
        AbstractInstructionEvaluator<?> evaluator,
        @NotNull
        AbstractInsnNode insn,
        int customSize // wide, lookupswitch, tableswitch, etc.
)
{
    /**
     * Constructs an EvaluatedInstruction with default size.
     *
     * @param evaluator The instruction evaluator.
     * @param insn      The ASM instruction node.
     */
    private EvaluatedInstruction(@NotNull AbstractInstructionEvaluator<?> evaluator, @NotNull AbstractInsnNode insn)
    {
        this(evaluator, insn, 0);
    }

    /**
     * Returns the size of the instruction.
     * If a custom size is specified, returns it; otherwise, determines size from opcode.
     *
     * @return The instruction size.
     */
    public int getInstructionSize()
    {
        if (this.customSize > 0)
            return this.customSize;  // Returns explicitly specified size
        else
            return EOpcodes.getOpcodeSize(this.insn.getOpcode());
    }

    /**
     * Creates an EvaluatedInstruction with default size.
     *
     * @param evaluator The instruction evaluator.
     * @param insn      The ASM instruction node.
     * @return The EvaluatedInstruction instance.
     */
    public static EvaluatedInstruction of(@NotNull AbstractInstructionEvaluator<?> evaluator,
                                          @NotNull AbstractInsnNode insn)
    {
        return new EvaluatedInstruction(evaluator, insn);
    }

    /**
     * Creates an EvaluatedInstruction with a specified size.
     *
     * @param evaluator The instruction evaluator.
     * @param insn      The ASM instruction node.
     * @param size      The custom size for the instruction.
     * @return The EvaluatedInstruction instance.
     * @throws IllegalArgumentException if a custom size is required but not provided.
     */
    public static EvaluatedInstruction of(@NotNull AbstractInstructionEvaluator<?> evaluator,
                                          @NotNull AbstractInsnNode insn, int size)
    {
        checkSizeProvided(insn.getOpcode(), size);
        return new EvaluatedInstruction(evaluator, insn, size);
    }

    /**
     * Checks if a custom size is required for the given opcode.
     *
     * @param opcode The opcode to check.
     * @param size   The provided size.
     * @throws IllegalArgumentException if a custom size is required but not provided.
     */
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
