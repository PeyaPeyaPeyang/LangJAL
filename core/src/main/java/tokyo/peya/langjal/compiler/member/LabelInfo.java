package tokyo.peya.langjal.compiler.member;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.LabelNode;

/**
 * Represents a label in JVM bytecode, including its name, ASM label, label node, and instruction index.
 * Used for marking positions in the instruction stream for jumps and exception handling.
 *
 * @param name              The name of the label.
 * @param label             The ASM Label object.
 * @param node              The ASM LabelNode corresponding to this label.
 * @param instructionIndex  The index of the instruction associated with this label.
 */
public record LabelInfo(
        @NotNull String name,
        @NotNull Label label,
        @NotNull LabelNode node,
        int instructionIndex
)
{
    /**
     * Constructs a LabelInfo with a name, label, and instruction index.
     *
     * @param name             The label name.
     * @param label            The ASM label.
     * @param instructionIndex The instruction index.
     */
    public LabelInfo(@NotNull String name, @NotNull Label label, int instructionIndex)
    {
        this(name, label, new LabelNode(label), instructionIndex);
        label.info = this.node;
    }

    /**
     * Returns a string representation of the label, including its name and index.
     *
     * @return String representation.
     */
    @Override
    public @NotNull String toString()
    {
        return this.name + " (idx: " + this.instructionIndex + ")";
    }
}
