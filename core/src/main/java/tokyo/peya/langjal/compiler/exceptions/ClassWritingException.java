package tokyo.peya.langjal.compiler.exceptions;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Exception thrown when an error occurs while writing a class file to disk.
 */
public class ClassWritingException extends CompileErrorException
{
    /**
     * Constructs a new ClassWritingException with the specified IOException as the cause.
     *
     * @param ioe The IOException that occurred during class file writing.
     */
    public ClassWritingException(@NotNull IOException ioe)
    {
        super(
                "Exception while writing class file",
                ioe,
                "An exception occurred while writing the class file to disk: " + ioe.getMessage()
        );
    }

    /**
     * Returns the cause of this exception as an IOException.
     *
     * @return The IOException that caused this exception.
     */
    @Override
    public synchronized IOException getCause()
    {
        return (IOException) super.getCause();
    }
}
