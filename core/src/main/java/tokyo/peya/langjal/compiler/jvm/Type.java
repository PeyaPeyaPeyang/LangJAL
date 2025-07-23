package tokyo.peya.langjal.compiler.jvm;

import tokyo.peya.langjal.analyser.stack.StackElementType;

/**
 * Represents a type in the compiler's JVM type system.
 * Implementations define specific type behaviors and properties.
 */
public interface Type
{
    /**
     * Returns whether this type is a primitive type.
     *
     * @return true if primitive, false otherwise.
     */
    boolean isPrimitive();

    /**
     * Returns the JVM descriptor string for this type.
     *
     * @return the type descriptor.
     */
    String getDescriptor();

    /**
     * Returns the category of this type (used for stack operations).
     *
     * @return the category value.
     */
    int getCategory();

    /**
     * Returns the stack element type for this type.
     *
     * @return the stack element type.
     */
    StackElementType getStackElementType();
}
