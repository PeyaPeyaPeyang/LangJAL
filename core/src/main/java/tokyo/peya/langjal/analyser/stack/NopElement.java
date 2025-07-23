package tokyo.peya.langjal.analyser.stack;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

/**
 * Represents a no-operation (NOP) element on the JVM stack.
 *
 * @param producer The instruction that produced this element.
 */
public record NopElement(
        @NotNull InstructionInfo producer
) implements StackElement
{

    /**
     * Gets the type of this stack element.
     * @return The stack element type.
     */
    @Override
    public @NotNull StackElementType type()
    {
        return StackElementType.NOP;
    }

    /**
     * Converts this element to an ASM stack element.
     * @return Always null for NOP.
     */
    @Override
    public Object toASMStackElement()
    {
        return null;
    }
}
