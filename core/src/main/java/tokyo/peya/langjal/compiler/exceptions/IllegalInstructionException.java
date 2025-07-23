package tokyo.peya.langjal.compiler.exceptions;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;

/**
 * Exception thrown when an illegal instruction is encountered during compilation.
 */
public class IllegalInstructionException extends IllegalValueException
{
    /**
     * Constructs an IllegalInstructionException with a message and terminal node.
     *
     * @param detailedMessage The error message.
     * @param node            The terminal node where the error occurred.
     */
    public IllegalInstructionException(@NotNull String detailedMessage,
                                       @NotNull TerminalNode node)
    {
        super(detailedMessage, node);
    }

    /**
     * Constructs an IllegalInstructionException with a message and parser rule context.
     *
     * @param detailedMessage The error message.
     * @param node            The parser rule context where the error occurred.
     */
    public IllegalInstructionException(@NotNull String detailedMessage,
                                       @NotNull ParserRuleContext node)
    {
        super(detailedMessage, node);
    }
}
