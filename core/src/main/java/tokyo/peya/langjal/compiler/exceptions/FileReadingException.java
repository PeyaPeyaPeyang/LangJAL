package tokyo.peya.langjal.compiler.exceptions;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public class FileReadingException extends CompileErrorException
{
    @NotNull
    private final Path file;

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
