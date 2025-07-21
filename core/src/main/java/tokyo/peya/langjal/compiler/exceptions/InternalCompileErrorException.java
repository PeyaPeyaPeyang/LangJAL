package tokyo.peya.langjal.compiler.exceptions;

import org.antlr.v4.runtime.ParserRuleContext;
import org.jetbrains.annotations.NotNull;

public class InternalCompileErrorException extends CompileErrorException
{
    public InternalCompileErrorException(@NotNull String message, @NotNull Throwable cause)
    {
        super(message, cause, "An internal compiler error occurred: " + message);
    }

    public InternalCompileErrorException(@NotNull String message, @NotNull ParserRuleContext causeRule)
    {
        super(message, causeRule);
    }
}
