package tokyo.peya.langjal.analyser.stack;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

/**
 * Represents a null reference on the JVM stack.
 *
 * @param producer The instruction that produced this element.
 */
public record NullElement(
        @NotNull
        InstructionInfo producer // この要素を生成した命令
) implements StackElement
{
    /**
     * Gets the type of this stack element.
     * @return The stack element type.
     */
    @Override
    public @NotNull StackElementType type()
    {
        return StackElementType.NULL;
    }

    /**
     * Converts this element to an ASM stack element.
     * @return The ASM representation for null.
     */
    @Override
    public Object toASMStackElement()
    {
        return EOpcodes.NULL;
    }

    /**
     * Creates a new NullElement for the given producer.
     * @param producer The instruction that produced this element.
     * @return A new NullElement.
     */
    @NotNull
    public static NullElement of(@NotNull InstructionInfo producer)
    {
        return new NullElement(producer);
    }

    /**
     * Returns a string representation of this null element.
     * @return String representation.
     */
    @Override
    public @NotNull String toString()
    {
        return "Null type (by " + this.producer + ")";
    }
}
