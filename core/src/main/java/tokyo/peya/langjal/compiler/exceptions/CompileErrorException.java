package tokyo.peya.langjal.compiler.exceptions;

import lombok.Getter;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;

@Getter
public class CompileErrorException extends RuntimeException
{
    private final String detailedMessage;
    private final long line;
    private final long column;
    private final long length;

    public CompileErrorException(@NotNull String detailedMessage,
                                 long line, long column, long length)
    {
        super(detailedMessage);
        this.detailedMessage = detailedMessage;
        this.line = line;
        this.column = column;
        this.length = length;
    }

    public CompileErrorException(@NotNull String detailedMessage, @NotNull ParserRuleContext node)
    {
        this(
                detailedMessage,
                node.getStart().getLine(),
                node.getStart().getCharPositionInLine(),
                node.getStop().getStopIndex() - node.getStart().getStartIndex() + 1
        );
    }

    public CompileErrorException(@NotNull String detailedMessage, @NotNull TerminalNode node)
    {
        this(
                detailedMessage,
                node.getSymbol().getLine(),
                node.getSymbol().getCharPositionInLine(),
                node.getSymbol().getStopIndex() - node.getSymbol().getStartIndex() + 1
        );
    }

    public CompileErrorException(@NotNull String message, @NotNull Throwable cause,
                                 @NotNull String detailedMessage, long line,
                                 long column, long length)
    {
        super(message, cause);
        this.detailedMessage = detailedMessage;
        this.line = line;
        this.column = column;
        this.length = length;
    }

    public CompileErrorException(@NotNull String message, @NotNull Throwable cause,
                                 @NotNull String detailedMessage)
    {
        super(message, cause);
        this.detailedMessage = detailedMessage;
        this.line = 0;
        this.column = 0;
        this.length = 0;
    }
}
