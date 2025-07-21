package tokyo.peya.langjal.compiler.jvm;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InvocationType
{
    INVOKE_VIRTUAL("invokevirtual", EOpcodes.INVOKEVIRTUAL),
    INVOKE_SPECIAL("invokespecial", EOpcodes.INVOKESPECIAL),
    INVOKE_STATIC("invokestatic", EOpcodes.INVOKESTATIC),
    INVOKE_INTERFACE("invokeinterface", EOpcodes.INVOKEINTERFACE),
    INVOKE_DYNAMIC("invokedynamic", EOpcodes.INVOKEDYNAMIC),
    ;

    private final String name;
    private final int opcode;

    public static InvocationType fromOpcode(int opcode)
    {
        for (InvocationType type : values())
            if (type.getOpcode() == opcode)
                return type;

        throw new IllegalArgumentException("Unknown opcode: " + opcode);
    }

    public static InvocationType fromName(String name)
    {
        for (InvocationType type : values())
            if (type.getName().equals(name))
                return type;

        throw new IllegalArgumentException("Unknown invocation type name: " + name);
    }
}
