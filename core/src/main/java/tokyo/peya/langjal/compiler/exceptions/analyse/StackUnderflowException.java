package tokyo.peya.langjal.compiler.exceptions.analyse;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.analyser.stack.StackElement;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

/**
 * Exception thrown when a stack underflow occurs during bytecode analysis.
 * <p>
 * Indicates that an instruction tried to pop more elements than available on the operand stack.
 */
public class StackUnderflowException extends ClassAnalyseException
{
    /**
     * The instruction at which the stack underflow occurred.
     */
    private final InstructionInfo instruction;

    /**
     * The stack element that was expected to be present.
     */
    private final StackElement expectedElement;

    /**
     * Constructs a new StackUnderflowException with the given instruction and expected element.
     *
     * @param instruction     The instruction at which the stack underflow occurred.
     * @param expectedElement The stack element that was expected to be present.
     */
    public StackUnderflowException(@NotNull InstructionInfo instruction, StackElement expectedElement)
    {
        super("Stack underflow at instruction: " + instruction +
                      " expected at least one element like " + expectedElement
        );
        this.instruction = instruction;
        this.expectedElement = expectedElement;
    }
}
