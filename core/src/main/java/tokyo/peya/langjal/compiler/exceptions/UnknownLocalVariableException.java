package tokyo.peya.langjal.compiler.exceptions;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;

@Getter
public class UnknownLocalVariableException extends CompileErrorException
{
    private final String name;

    public UnknownLocalVariableException(@NotNull String message,
                                         @NotNull String name,
                                         @NotNull JALParser.JvmInsArgLocalRefContext ref)
    {
        super(message, ref);
        this.name = name;
    }

    public UnknownLocalVariableException(@NotNull String message,
                                         @NotNull String name)
    {
        super(message, 0, 0, 0);
        this.name = name;
    }
}
