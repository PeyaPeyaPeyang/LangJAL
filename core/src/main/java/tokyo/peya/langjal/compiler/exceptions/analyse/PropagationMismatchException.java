package tokyo.peya.langjal.compiler.exceptions.analyse;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.analyser.FramePropagation;
import tokyo.peya.langjal.compiler.member.LabelInfo;

@Getter
public class PropagationMismatchException extends ClassAnalyseException
{
    @NotNull
    private final FramePropagation propagation;
    @NotNull
    private final LabelInfo receiver;

    public PropagationMismatchException(@NotNull FramePropagation propagation, @NotNull LabelInfo receiver)
    {
        super("CANNOT ANALYSE FRAME PROPAGATION: " + propagation + " -> " + receiver);
        this.propagation = propagation;
        this.receiver = receiver;
    }

    public PropagationMismatchException(@NotNull FramePropagation propagation, @NotNull LabelInfo receiver,
                                        @NotNull String message)
    {
        super(message);
        this.propagation = propagation;
        this.receiver = receiver;
    }
}
