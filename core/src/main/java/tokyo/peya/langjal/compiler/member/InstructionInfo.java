package tokyo.peya.langjal.compiler.member;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;

import java.util.Objects;

/**
 * Represents a single JVM instruction within a method, including its evaluator, owner context,
 * bytecode offset, assigned label, instruction size, and source line information.
 * Used for tracking and manipulating instructions during compilation.
 *
 * @param bytecodeOffset  The offset of this instruction in the bytecode.
 * @param insn            The ASM instruction node.
 * @param ownerClass      The ASM ClassNode representing the owning class.
 * @param owner           The ASM MethodNode representing the owning method.
 * @param producer        The instruction evaluator responsible for this instruction.
 * @param assignedLabel   The label assigned to this instruction, or null if none.
 * @param instructionSize The size of this instruction in bytes.
 * @param sourceLine      The source line number corresponding to this instruction.
 */
public record InstructionInfo(
        int bytecodeOffset,
        @NotNull AbstractInsnNode insn,
        @NotNull ClassNode ownerClass,
        @NotNull MethodNode owner,
        @NotNull AbstractInstructionEvaluator<?> producer,
        @Nullable LabelInfo assignedLabel,
        int instructionSize,
        int sourceLine
)
{
    /**
     * Constructs an InstructionInfo from an opcode integer.
     *
     * @param evaluator      The instruction evaluator.
     * @param ownerClass     The owning class.
     * @param owner          The owning method.
     * @param insn           The opcode integer.
     * @param bytecodeOffset The bytecode offset.
     * @param assignedLabel  The assigned label, if any.
     * @param instructionSize The instruction size in bytes.
     * @param sourceLine     The source line number.
     */
    public InstructionInfo(@NotNull AbstractInstructionEvaluator<?> evaluator,
                           @NotNull ClassNode ownerClass,
                           @NotNull MethodNode owner,
                           int insn,
                           int bytecodeOffset,
                           @Nullable LabelInfo assignedLabel,
                           int instructionSize,
                           int sourceLine)
    {
        this(
                bytecodeOffset, new InsnNode(insn), ownerClass, owner, evaluator,
                assignedLabel,
                instructionSize,
                sourceLine
        );
    }

    /**
     * Returns the opcode of this instruction.
     *
     * @return The opcode integer.
     */
    public int opcode()
    {
        return this.insn.getOpcode();
    }

    /**
     * Checks equality based on evaluator, instruction node, bytecode offset, label, and size.
     *
     * @param obj The object to compare.
     * @return True if equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof InstructionInfo that))
            return false;

        return Objects.equals(this.producer, that.producer) &&
                Objects.equals(this.insn, that.insn) &&
                this.bytecodeOffset == that.bytecodeOffset &&
                Objects.equals(this.assignedLabel, that.assignedLabel) &&
                this.instructionSize == that.instructionSize;
    }

    /**
     * Returns a string representation of this instruction, including opcode name, offset, and label.
     *
     * @return String representation.
     */
    @Override
    public @NotNull String toString()
    {
        return EOpcodes.getName(this.opcode()) +
                " at " + this.bytecodeOffset +
                (this.assignedLabel != null ? " with label " + this.assignedLabel: "");
    }
}
