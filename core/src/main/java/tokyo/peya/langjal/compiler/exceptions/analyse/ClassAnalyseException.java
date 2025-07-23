package tokyo.peya.langjal.compiler.exceptions.analyse;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.exceptions.CompileErrorException;

/**
 * Exception thrown when an error occurs during class analysis.
 */
public class ClassAnalyseException extends CompileErrorException
{
    /**
     * Constructs a new ClassAnalyseException with the specified message.
     *
     * @param message The detail message for the exception.
     */
    public ClassAnalyseException(@NotNull String message)
    {
        super(message, 0, 0, 0);
    }
}
