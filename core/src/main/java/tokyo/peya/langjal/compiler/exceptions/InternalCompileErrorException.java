package tokyo.peya.langjal.compiler.exceptions;

import org.antlr.v4.runtime.ParserRuleContext;
import org.jetbrains.annotations.NotNull;

/**
 * Exception thrown when an unexpected internal compiler error occurs.
 * <p>
 * This exception is used to wrap unexpected errors that are not caused by user code,
 * but by bugs or unhandled situations in the compiler implementation itself.
 * </p>
 */
public class InternalCompileErrorException extends CompileErrorException
{
    /**
     * Constructs a new InternalCompileErrorException with a message and a cause.
     *
     * @param message The error message describing the internal error.
     * @param cause   The underlying cause of the error.
     */
    public InternalCompileErrorException(@NotNull String message, @NotNull Throwable cause)
    {
        super(message, cause, "An internal compiler error occurred: " + message);
    }

    /**
     * Constructs a new InternalCompileErrorException with a message and a parser rule context.
     *
     * @param message   The error message describing the internal error.
     * @param causeRule The parser rule context where the error occurred.
     */
    public InternalCompileErrorException(@NotNull String message, @NotNull ParserRuleContext causeRule)
    {
        super(message, causeRule);
    }
}
