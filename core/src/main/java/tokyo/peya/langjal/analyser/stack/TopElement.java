package tokyo.peya.langjal.analyser.stack;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

/**
 * Represents a TOP value in the JVM stack frame (used for category 2 types).
 *
 * @param producer The instruction that produced this element.
 */
public record TopElement(
        InstructionInfo producer
) implements StackElement
{
    /**
     * Gets the type of this stack element.
     * @return The stack element type.
     */
    @Override
    public @NotNull StackElementType type()
    {
        return StackElementType.TOP;
    }

    /**
     * Converts this element to an ASM stack element.
     * @return The ASM representation for TOP.
     */
    @Override
    public Object toASMStackElement()
    {
        return EOpcodes.TOP;
    }

    /**
     * Returns a string representation of this top element.
     * @return String representation.
     */
    @Override
    public @NotNull String toString()
    {
        return "Top type (by " + this.producer + ")";
    }
}
