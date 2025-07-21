package tokyo.peya.langjal.compiler.exceptions;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;

@Getter
public class UnknownLabelException extends CompileErrorException
{
    private final String name;

    public UnknownLabelException(@NotNull String message,
                                 @NotNull String name,
                                 @NotNull JALParser.LabelNameContext ref)
    {
        super(message, ref);
        this.name = name;
    }
}
