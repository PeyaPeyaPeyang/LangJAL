package tokyo.peya.langjal.compiler.exceptions;

import lombok.Getter;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;

/**
 * Exception thrown when a compile error occurs.
 * Stores detailed message and location information for error reporting.
 */
@Getter
public class CompileErrorException extends RuntimeException
{
    /**
     * Detailed error message.
     */
    private final String detailedMessage;
    /**
     * Line number where the error occurred.
     */
    private final long line;
    /**
     * Column number where the error occurred.
     */
    private final long column;
    /**
     * Length of the error region.
     */
    private final long length;

    /**
     * Constructs a CompileErrorException with message and location info.
     *
     * @param detailedMessage Detailed error message.
     * @param line            Line number.
     * @param column          Column number.
     * @param length          Length of the error region.
     */
    public CompileErrorException(@NotNull String detailedMessage,
                                 long line, long column, long length)
    {
        super(detailedMessage);
        this.detailedMessage = detailedMessage;
        this.line = line;
        this.column = column;
        this.length = length;
    }

    /**
     * Constructs a CompileErrorException from a parser rule context.
     *
     * @param detailedMessage Detailed error message.
     * @param node            Parser rule context.
     */
    public CompileErrorException(@NotNull String detailedMessage, @NotNull ParserRuleContext node)
    {
        this(
                detailedMessage,
                node.getStart().getLine(),
                node.getStart().getCharPositionInLine(),
                node.getStop().getStopIndex() - node.getStart().getStartIndex() + 1
        );
    }

    /**
     * Constructs a CompileErrorException from a terminal node.
     *
     * @param detailedMessage Detailed error message.
     * @param node            Terminal node.
     */
    public CompileErrorException(@NotNull String detailedMessage, @NotNull TerminalNode node)
    {
        this(
                detailedMessage,
                node.getSymbol().getLine(),
                node.getSymbol().getCharPositionInLine(),
                node.getSymbol().getStopIndex() - node.getSymbol().getStartIndex() + 1
        );
    }

    /**
     * Constructs a CompileErrorException with a cause and location info.
     *
     * @param message         Error message.
     * @param cause           Cause of the error.
     * @param detailedMessage Detailed error message.
     * @param line            Line number.
     * @param column          Column number.
     * @param length          Length of the error region.
     */
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

    /**
     * Constructs a CompileErrorException with a cause and no location info.
     *
     * @param message         Error message.
     * @param cause           Cause of the error.
     * @param detailedMessage Detailed error message.
     */
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
