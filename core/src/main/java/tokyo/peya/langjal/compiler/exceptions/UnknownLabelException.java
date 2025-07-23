package tokyo.peya.langjal.compiler.exceptions;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;

/**
 * Exception thrown when a referenced label is not found in the current context.
 * <p>
 * This exception is typically thrown during semantic analysis when a jump or branch
 * references a label that does not exist.
 * </p>
 */
@Getter
public class UnknownLabelException extends CompileErrorException
{
    /**
     * The name of the label that could not be resolved.
     */
    private final String name;

    /**
     * Constructs a new UnknownLabelException with the given message, label name, and reference context.
     *
     * @param message The error message.
     * @param name    The name of the unknown label.
     * @param ref     The parser context referencing the label.
     */
    public UnknownLabelException(@NotNull String message,
                                 @NotNull String name,
                                 @NotNull JALParser.LabelNameContext ref)
    {
        super(message, ref);
        this.name = name;
    }
}
