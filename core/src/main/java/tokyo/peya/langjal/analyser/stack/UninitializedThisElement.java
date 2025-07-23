package tokyo.peya.langjal.analyser.stack;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

/**
 * Represents the uninitialized 'this' reference before superclass constructor call in JVM stack analysis.
 *
 * @param producer The instruction that produced this element (always NOP).
 */
public record UninitializedThisElement(
        @NotNull InstructionInfo producer // かならず NOP
) implements StackElement
{
    /**
     * Gets the type of this stack element.
     * @return The stack element type.
     */
    @Override
    public @NotNull StackElementType type()
    {
        return StackElementType.UNINITIALIZED_THIS;
    }

    /**
     * Converts this element to an ASM stack element.
     * @return The ASM representation for uninitialized this.
     */
    @Override
    public Object toASMStackElement()
    {
        return EOpcodes.UNINITIALIZED_THIS;
    }

    /**
     * Returns a string representation of this uninitialized this element.
     * @return String representation.
     */
    @Override
    public @NotNull String toString()
    {
        return "Uninitialized this";
    }
}
