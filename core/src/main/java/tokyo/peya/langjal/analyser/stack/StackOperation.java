package tokyo.peya.langjal.analyser.stack;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a stack operation (push or pop) for JVM stack analysis.
 *
 * @param type    The type of stack operation (PUSH or POP).
 * @param element The stack element involved in the operation.
 */
public record StackOperation(
        @NotNull
        StackOperationType type, // 操作の種類
        @NotNull
        StackElement element
)
{
    /**
     * Creates a push stack operation.
     * @param element The stack element to push.
     * @return The push operation.
     */
    public static StackOperation push(@NotNull StackElement element)
    {
        return new StackOperation(StackOperationType.PUSH, element);
    }

    /**
     * Creates a pop stack operation.
     * @param element The stack element to pop.
     * @return The pop operation.
     */
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
