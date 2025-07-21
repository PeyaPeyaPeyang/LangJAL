package tokyo.peya.langjal.compiler.exceptions.analyse;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.analyser.stack.StackElement;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public class StackUnderflowException extends ClassAnalyseException
{
    private final InstructionInfo instruction;
    private final StackElement expectedElement;

    public StackUnderflowException(@NotNull InstructionInfo instruction, StackElement expectedElement)
    {
        super("Stack underflow at instruction: " + instruction +
                      " expected at least one element like " + expectedElement
        );
        this.instruction = instruction;
        this.expectedElement = expectedElement;
    }
}
