package tokyo.peya.langjal.analyser;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.analyser.stack.LocalStackElement;
import tokyo.peya.langjal.analyser.stack.StackElement;
import tokyo.peya.langjal.compiler.member.LabelInfo;

import java.util.Arrays;
import java.util.Objects;

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
