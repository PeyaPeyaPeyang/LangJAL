package tokyo.peya.langjal.compiler.exceptions;

import org.jetbrains.annotations.NotNull;

public class ClassFinalisingException extends CompileErrorException
{
    public ClassFinalisingException(@NotNull Throwable cause)
    {
        super(
                "Exception while finalising class",
                cause,
                "An exception occurred while finalising the class: " + cause.getMessage()
        );
    }
}
