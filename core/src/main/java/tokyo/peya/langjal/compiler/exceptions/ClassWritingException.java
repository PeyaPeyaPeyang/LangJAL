package tokyo.peya.langjal.compiler.exceptions;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ClassWritingException extends CompileErrorException
{
    public ClassWritingException(@NotNull IOException ioe)
    {
        super(
                "Exception while writing class file",
                ioe,
                "An exception occurred while writing the class file to disk: " + ioe.getMessage()
        );
    }

    @Override
    public synchronized IOException getCause()
    {
        return (IOException) super.getCause();
    }
}
