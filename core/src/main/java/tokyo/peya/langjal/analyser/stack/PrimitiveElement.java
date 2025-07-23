package tokyo.peya.langjal.analyser.stack;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

/**
 * Represents a primitive value (int, float, long, double) on the JVM stack.
 *
 * @param producer The instruction that produced this element.
 * @param type     The stack element type.
 */
public record PrimitiveElement(
        @NotNull
        InstructionInfo producer,
        @NotNull
        StackElementType type
) implements StackElement
{
    /**
     * Validates that the type is a primitive type.
     * @param producer The instruction that produced this element.
     * @param type The stack element type.
     */
    public PrimitiveElement
    {
        if (!(type == StackElementType.INTEGER ||
                type == StackElementType.FLOAT ||
                type == StackElementType.LONG ||
                type == StackElementType.DOUBLE))
            throw new IllegalArgumentException("PrimitiveElement must be INTEGER, FLOAT, LONG, or DOUBLE, but was: " + type);
    }

    /**
     * Converts this element to an ASM stack element.
     * @return The ASM opcode for this primitive type.
     */
    @Override
    public Object toASMStackElement()
    {
        return this.type.getOpcode();
    }

    /**
     * Creates a new PrimitiveElement for the given producer and type.
     * @param producer The instruction that produced this element.
     * @param type The stack element type.
     * @return A new PrimitiveElement.
     */
    public static PrimitiveElement of(
            @NotNull InstructionInfo producer,
            @NotNull StackElementType type
    )
    {
        return new PrimitiveElement(producer, type);
    }

    /**
     * Returns a string representation of this primitive element.
     * @return String representation.
     */
    @Override
    public @NotNull String toString()
    {
        return "Primitive type of " + this.type + " (by " + this.producer + ")";
    }
}
