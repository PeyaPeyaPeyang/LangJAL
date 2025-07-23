package tokyo.peya.langjal.analyser.stack;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

/**
 * Represents an object reference on the JVM stack, including its type.
 *
 * @param producer The instruction that produced this element.
 * @param content  The type descriptor of the object.
 */
public record ObjectElement(
        @NotNull
        InstructionInfo producer,
        @NotNull
        TypeDescriptor content
) implements StackElement
{
    /**
     * Validates that the content is not a primitive type.
     * @param producer The instruction that produced this element.
     * @param content The type descriptor.
     */
    public ObjectElement
    {
        if (!content.isArray() && content.getBaseType().isPrimitive())
            throw new IllegalArgumentException(
                    "ObjectElement content must not be a primitive type: " + content
            );
    }

    /**
     * Constructs an ObjectElement with java/lang/Object as the type.
     * @param producer The instruction that produced this element.
     */
    public ObjectElement(@NotNull InstructionInfo producer)
    {
        this(producer, TypeDescriptor.className("java/lang/Object"));
    }

    /**
     * Gets the type of this stack element.
     * @return The stack element type.
     */
    @Override
    public @NotNull StackElementType type()
    {
        return StackElementType.OBJECT;
    }

    /**
     * Converts this element to an ASM stack element.
     * @return The ASM representation.
     */
    @Override
    public Object toASMStackElement()
    {
        return this.content.toString();
    }

    /**
     * Returns a string representation of this object element.
     * @return String representation.
     */
    @Override
    public @NotNull String toString()
    {
        return "Object type of " + this.content + " (by " + this.producer + ")";
    }
}
