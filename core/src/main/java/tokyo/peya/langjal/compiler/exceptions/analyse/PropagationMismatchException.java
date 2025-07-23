package tokyo.peya.langjal.compiler.exceptions.analyse;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.analyser.FramePropagation;
import tokyo.peya.langjal.compiler.member.LabelInfo;

/**
 * Exception thrown when a frame propagation does not match the expected receiver during bytecode analysis.
 * <p>
 * This typically indicates a control flow or verification error in the JVM stack frame propagation logic.
 * </p>
 */
@Getter
public class PropagationMismatchException extends ClassAnalyseException
{
    /**
     * The frame propagation that caused the mismatch.
     */
    @NotNull
    private final FramePropagation propagation;

    /**
     * The label that was expected to receive the propagation.
     */
    @NotNull
    private final LabelInfo receiver;

    /**
     * Constructs a new PropagationMismatchException with the given propagation and receiver.
     *
     * @param propagation The frame propagation that caused the mismatch.
     * @param receiver    The label that was expected to receive the propagation.
     */
    public PropagationMismatchException(@NotNull FramePropagation propagation, @NotNull LabelInfo receiver)
    {
        super("CANNOT ANALYSE FRAME PROPAGATION: " + propagation + " -> " + receiver);
        this.propagation = propagation;
        this.receiver = receiver;
    }

    /**
     * Constructs a new PropagationMismatchException with a custom message.
     *
     * @param propagation The frame propagation that caused the mismatch.
     * @param receiver    The label that was expected to receive the propagation.
     * @param message     The detail message.
     */
    public PropagationMismatchException(@NotNull FramePropagation propagation, @NotNull LabelInfo receiver,
                                        @NotNull String message)
    {
        super(message);
        this.propagation = propagation;
        this.receiver = receiver;
    }
}
