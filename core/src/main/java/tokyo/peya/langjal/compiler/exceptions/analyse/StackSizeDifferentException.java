package tokyo.peya.langjal.compiler.exceptions.analyse;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.analyser.stack.StackElement;
import tokyo.peya.langjal.compiler.member.LabelInfo;

/**
 * Exception thrown when the actual stack size differs from the expected stack size at a specific label.
 * <p>
 * Used for stack consistency verification during control flow analysis.
 */
@Getter
public class StackSizeDifferentException extends ClassAnalyseException
{
    /**
     * The label at which the stack size mismatch occurred.
     */
    @NotNull
    private final LabelInfo atLabel;

    /**
     * The expected stack elements.
     */
    @NotNull
    private final StackElement[] expected;

    /**
     * The actual stack elements found.
     */
    @NotNull
    private final StackElement[] actual;

    /**
     * Constructs a new StackSizeDifferentException with the given details.
     *
     * @param message  The detail message.
     * @param atLabel  The label at which the stack size mismatch occurred.
     * @param expected The expected stack elements.
     * @param actual   The actual stack elements found.
     */
    public StackSizeDifferentException(@NotNull String message,
                                       @NotNull LabelInfo atLabel,
                                       @NotNull StackElement[] expected,
                                       @NotNull StackElement[] actual)
    {
        super(message);
        this.atLabel = atLabel;
        this.expected = expected;
        this.actual = actual;
    }
}
