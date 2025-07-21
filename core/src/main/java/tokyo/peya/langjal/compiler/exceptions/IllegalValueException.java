package tokyo.peya.langjal.compiler.exceptions;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;

public class IllegalValueException extends CompileErrorException
{
    public IllegalValueException(@NotNull String detailedMessage, @NotNull TerminalNode node)
    {
        super(detailedMessage, node);
    }

    public IllegalValueException(@NotNull String detailedMessage, @NotNull ParserRuleContext node)
    {
        super(
                detailedMessage,
                node.getStart().getLine(),
                node.getStart().getCharPositionInLine(),
                node.getStop().getStopIndex() - node.getStart().getStartIndex() + 1
        );
    }
}
