package tokyo.peya.langjal.compiler.member;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;

/**
 * Represents a try-catch-finally directive in a method, including start/end labels,
 * catch block label, exception type, and finally block label.
 *
 * @param tryBlockStartLabel   The label where the try block starts.
 * @param tryBlockEndLabel     The label where the try block ends.
 * @param catchBlockLabel      The label where the catch block starts, or null if not present.
 * @param exceptionType        The type of exception to catch, or null for a finally block.
 * @param finallyBlockLabel    The label where the finally block starts, or null if not present.
 */
public record TryCatchDirective(
        @NotNull
        LabelInfo tryBlockStartLabel,
        @NotNull
        LabelInfo tryBlockEndLabel,
        @Nullable
        LabelInfo catchBlockLabel,
        @Nullable
        TypeDescriptor exceptionType,
        @Nullable
        LabelInfo finallyBlockLabel
)
{
}
