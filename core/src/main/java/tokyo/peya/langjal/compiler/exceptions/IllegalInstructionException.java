package tokyo.peya.langjal.compiler.exceptions;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;

public class IllegalInstructionException extends IllegalValueException
{

    public IllegalInstructionException(@NotNull String detailedMessage,
                                       @NotNull TerminalNode node)
    {
        super(detailedMessage, node);
    }

    public IllegalInstructionException(@NotNull String detailedMessage,
                                       @NotNull ParserRuleContext node)
    {
        super(detailedMessage, node);
    }
}
