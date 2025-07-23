package tokyo.peya.langjal.compiler.jvm;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents the type of method invocation in JVM bytecode.
 */
@Getter
@AllArgsConstructor
public enum InvocationType
{
    /**
     * Standard virtual method invocation.
     */
    INVOKE_VIRTUAL("invokevirtual", EOpcodes.INVOKEVIRTUAL),
    /**
     * Special method invocation (constructors, private methods, etc.).
     */
    INVOKE_SPECIAL("invokespecial", EOpcodes.INVOKESPECIAL),
    /**
     * Static method invocation.
     */
    INVOKE_STATIC("invokestatic", EOpcodes.INVOKESTATIC),
    /**
     * Interface method invocation.
     */
    INVOKE_INTERFACE("invokeinterface", EOpcodes.INVOKEINTERFACE),
    /**
     * Dynamic method invocation.
     */
    INVOKE_DYNAMIC("invokedynamic", EOpcodes.INVOKEDYNAMIC),
    ;

    /**
     * The name of the invocation type.
     */
    private final String name;
    /**
     * The opcode for this invocation type.
     */
    private final int opcode;

    /**
     * Gets the InvocationType from the opcode.
     * @param opcode The opcode value.
     * @return The corresponding InvocationType.
     * @throws IllegalArgumentException if the opcode is unknown.
     */
    public static InvocationType fromOpcode(int opcode)
    {
        for (InvocationType type : values())
            if (type.getOpcode() == opcode)
                return type;

        throw new IllegalArgumentException("Unknown opcode: " + opcode);
    }

    /**
     * Gets the InvocationType from its name.
     * @param name The name of the invocation type.
     * @return The corresponding InvocationType.
     * @throws IllegalArgumentException if the name is unknown.
     */
    public static InvocationType fromName(String name)
    {
        for (InvocationType type : values())
            if (type.getName().equals(name))
                return type;

        throw new IllegalArgumentException("Unknown invocation type name: " + name);
    }
}
