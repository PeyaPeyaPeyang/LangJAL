package tokyo.peya.langjal.analyser.stack;

import org.jetbrains.annotations.NotNull;

public record StackOperation(
        @NotNull
        StackOperationType type, // 操作の種類
        @NotNull
        StackElement element
)
{
    public static StackOperation push(@NotNull StackElement element)
    {
        return new StackOperation(StackOperationType.PUSH, element);
    }

    public static StackOperation pop(@NotNull StackElement element)
    {
        return new StackOperation(StackOperationType.POP, element);
    }

    public enum StackOperationType
    {
        PUSH,
        POP
    }
}
