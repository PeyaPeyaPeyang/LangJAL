package tokyo.peya.langjal.analyser;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.analyser.stack.LocalStackElement;
import tokyo.peya.langjal.analyser.stack.StackElement;
import tokyo.peya.langjal.compiler.member.LabelInfo;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents the propagation of a stack frame between two labels in the control flow.
 * Contains sender and receiver label info, analysed instructions, stack and local variable states,
 * and maximum stack/local sizes.
 *
 * @param sender        The label sending the frame.
 * @param analysed      The analysed instructions in the propagation.
 * @param receiver      The label receiving the frame.
 * @param stack         The stack state at the receiver.
 * @param locals        The local variable state at the receiver.
 * @param maxStackSize  The maximum stack size encountered.
 * @param maxLocalSize  The maximum local variable size encountered.
 */
public record FramePropagation(
        @NotNull
        LabelInfo sender,
        @NotNull
        AnalysedInstruction[] analysed,
        @NotNull
        LabelInfo receiver,
        @NotNull
        StackElement[] stack,
        @NotNull
        LocalStackElement[] locals,
        int maxStackSize,
        int maxLocalSize
)
{
    /**
     * Checks equality with another object, comparing sender, receiver, stack, and locals.
     *
     * @param o The object to compare.
     * @return true if equal, false otherwise.
     */
    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof FramePropagation that))
            return false;

        return Objects.equals(this.sender, that.sender) &&
                Objects.equals(this.receiver, that.receiver) &&
                Arrays.equals(this.stack, that.stack) &&
                Arrays.equals(this.locals, that.locals);
    }

    /**
     * Returns a string representation of the frame propagation, including sender, receiver,
     * stack, locals, and max sizes.
     *
     * @return The string representation.
     */
    @Override
    public @NotNull String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\n-- FramePropagation --\n");
        sb.append("Sender: ").append(this.sender.name()).append("\n");
        sb.append("Receiver: ").append(this.receiver.name()).append("\n");
        sb.append("Stacks: ");
        for (int i = 0; i < this.stack.length; i++)
        {
            StackElement element = this.stack[i];
            sb.append("[").append(i).append("]: ").append(element).append(", \n");
        }
        sb.append("\n");
        sb.append("Locals: ");
        for (LocalStackElement element : this.locals)
            sb.append(element).append(", \n");
        sb.append("\n");
        sb.append("Max stack size: ").append(this.maxStackSize).append("\n");
        sb.append("Max local size: ").append(this.maxLocalSize).append("\n");

        return sb.toString();
    }
}
