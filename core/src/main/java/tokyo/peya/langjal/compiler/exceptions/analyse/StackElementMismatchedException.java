package tokyo.peya.langjal.compiler.exceptions.analyse;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.analyser.stack.StackElement;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

@Getter
public class StackElementMismatchedException extends ClassAnalyseException
{
    @NotNull
    private final InstructionInfo actualElementProducer;
    @NotNull
    private final StackElement expected;
    @NotNull
    private final StackElement actual;

    public StackElementMismatchedException(@NotNull InstructionInfo actualElementProducer,
                                           @NotNull StackElement expected, @NotNull StackElement actual)
    {
        super("Stack element mismatch: " + actualElementProducer + " expected " + expected + ", but got " + actual);
        this.actualElementProducer = actualElementProducer;
        this.expected = expected;
        this.actual = actual;
    }

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
