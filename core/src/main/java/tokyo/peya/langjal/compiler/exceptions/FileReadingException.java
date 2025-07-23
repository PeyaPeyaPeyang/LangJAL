package tokyo.peya.langjal.compiler.exceptions;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Exception thrown when an error occurs while reading a file.
 */
public class FileReadingException extends CompileErrorException
{
    /**
     * The file that caused the exception.
     */
    @NotNull
    private final Path file;

    /**
     * Constructs a new FileReadingException with the specified cause and file.
     *
     * @param cause The IOException that occurred.
     * @param file  The file that caused the exception.
     */
    public FileReadingException(@NotNull IOException cause, @NotNull Path file)
    {
        super(
                "Exception while reading file",
                cause,
                "An exception occurred while reading the file: " + file
        );
        this.file = file;
    }
}
