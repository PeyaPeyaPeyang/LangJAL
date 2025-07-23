package tokyo.peya.langjal.analyser.stack;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

/**
 * Represents a local variable slot in the JVM stack frame, including its index and value.
 *
 * @param producer     The instruction that produced this element.
 * @param index        The local variable index.
 * @param stackElement The stack element value.
 * @param isParameter  True if this is a parameter.
 */
public record LocalStackElement(
        @NotNull
        InstructionInfo producer,
        int index,
        @NotNull
        StackElement stackElement,
        boolean isParameter
) implements StackElement
{
    /**
     * Constructs a LocalStackElement that is not a parameter.
     * @param producer The instruction that produced this element.
     * @param index The local variable index.
     * @param stackElement The stack element value.
     */
    public LocalStackElement(@NotNull InstructionInfo producer, int index, @NotNull StackElement stackElement)
    {
        this(producer, index, stackElement, false);
    }

    /**
     * Validates the local variable index.
     * @param producer The instruction that produced this element.
     * @param index The local variable index.
     * @param stackElement The stack element value.
     * @param isParameter True if this is a parameter.
     */
    public LocalStackElement
    {
        if (index < 0)
            throw new IllegalArgumentException("Local variable index must be non-negative, but was: " + index);
        else if (index > 65535)
            throw new IllegalArgumentException("Local variable index must be less than or equal to 65535, but was: " + index);
    }

    /**
     * Gets the type of this stack element.
     * @return The stack element type.
     */
    @Override
    public @NotNull StackElementType type()
    {
        return this.stackElement.type();
    }

    /**
     * Converts this element to an ASM stack element.
     * @return The ASM representation.
     */
    @Override
    public Object toASMStackElement()
    {
        return this.stackElement.toASMStackElement();
    }

    /**
     * Returns a string representation of this local stack element.
     * @return String representation.
     */
    @Override
    public @NotNull String toString()
    {
        return "[" + this.index + "]: " + this.stackElement;
    }
}
