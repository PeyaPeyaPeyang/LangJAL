package tokyo.peya.langjal.compiler.member;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.LabelNode;

public record LabelInfo(
        @NotNull String name,
        @NotNull Label label,
        @NotNull LabelNode node,
        int instructionIndex
)
{
    public LabelInfo(@NotNull String name, @NotNull Label label, int instructionIndex)
    {
        this(name, label, new LabelNode(label), instructionIndex);
        label.info = this.node;
    }

    @Override
    public @NotNull String toString()
    {
        return this.name + " (idx: " + this.instructionIndex + ")";
    }
}
