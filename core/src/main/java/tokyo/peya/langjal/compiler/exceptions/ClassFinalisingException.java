package tokyo.peya.langjal.compiler.exceptions;

import org.jetbrains.annotations.NotNull;

/**
 * Exception thrown when an error occurs while finalising a class.
 */
public class ClassFinalisingException extends CompileErrorException
{
    /**
     * Constructs a new ClassFinalisingException with the specified cause.
     *
     * @param cause The throwable that caused this exception.
     */
    public ClassFinalisingException(@NotNull Throwable cause)
    {
        super(
                "Exception while finalising class",
                cause,
                "An exception occurred while finalising the class: " + cause.getMessage()
        );
    }
}
