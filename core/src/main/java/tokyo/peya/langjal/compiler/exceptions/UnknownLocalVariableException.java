package tokyo.peya.langjal.compiler.exceptions;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;

/**
 * Exception thrown when a referenced local variable is not found in the current context.
 * <p>
 * This exception is typically thrown during semantic analysis when a variable reference
 * does not correspond to any declared local variable.
 * </p>
 */
@Getter
public class UnknownLocalVariableException extends CompileErrorException
{
    /**
     * The name of the local variable that could not be resolved.
     */
    private final String name;

    /**
     * Constructs a new UnknownLocalVariableException with the given message, variable name, and reference context.
     *
     * @param message The error message.
     * @param name    The name of the unknown local variable.
     * @param ref     The parser context referencing the variable.
     */
    public UnknownLocalVariableException(@NotNull String message,
                                         @NotNull String name,
                                         @NotNull JALParser.JvmInsArgLocalRefContext ref)
    {
        super(message, ref);
        this.name = name;
    }

    /**
     * Constructs a new UnknownLocalVariableException with the given message and variable name.
     * The error location is unspecified.
     *
     * @param message The error message.
     * @param name    The name of the unknown local variable.
     */
    public UnknownLocalVariableException(@NotNull String message,
                                         @NotNull String name)
    {
        super(message, 0, 0, 0);
        this.name = name;
    }
}
