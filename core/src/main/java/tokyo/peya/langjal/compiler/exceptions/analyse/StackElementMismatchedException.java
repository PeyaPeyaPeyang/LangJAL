package tokyo.peya.langjal.compiler.exceptions.analyse;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.analyser.stack.StackElement;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

/**
 * Exception thrown when the actual stack element does not match the expected stack element during analysis.
 * <p>
 * This is typically used for type or value mismatches in the operand stack.
 */
@Getter
public class StackElementMismatchedException extends ClassAnalyseException
{
    /**
     * The instruction that produced the actual stack element.
     */
    @NotNull
    private final InstructionInfo actualElementProducer;

    /**
     * The expected stack element.
     */
    @NotNull
    private final StackElement expected;

    /**
     * The actual stack element found.
     */
    @NotNull
    private final StackElement actual;

    /**
     * Constructs a new StackElementMismatchedException with the given details.
     *
     * @param actualElementProducer The instruction that produced the actual stack element.
     * @param expected              The expected stack element.
     * @param actual                The actual stack element found.
     */
    public StackElementMismatchedException(@NotNull InstructionInfo actualElementProducer,
                                           @NotNull StackElement expected, @NotNull StackElement actual)
    {
        super("Stack element mismatch: " + actualElementProducer + " expected " + expected + ", but got " + actual);
        this.actualElementProducer = actualElementProducer;
        this.expected = expected;
        this.actual = actual;
    }

    /**
     * Constructs a new StackElementMismatchedException with a custom message.
     *
     * @param actualElementProducer The instruction that produced the actual stack element.
     * @param expected              The expected stack element.
     * @param actual                The actual stack element found.
     * @param message               The detail message.
     */
    public StackElementMismatchedException(@NotNull InstructionInfo actualElementProducer,
                                           @NotNull StackElement expected, @NotNull StackElement actual,
                                           @NotNull String message)
    {
        super(message);
        this.actualElementProducer = actualElementProducer;
        this.expected = expected;
        this.actual = actual;
    }
}
