package tokyo.peya.langjal.compiler.jvm;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccessAttribute
{
    STATIC("static", EOpcodes.ACC_STATIC),
    FINAL("final", EOpcodes.ACC_FINAL),
    SUPER("super", EOpcodes.ACC_SUPER),
    SYNCHRONIZED("synchronized", EOpcodes.ACC_SYNCHRONIZED),
    VOLATILE("volatile", EOpcodes.ACC_VOLATILE),
    BRIDGE("bridge", EOpcodes.ACC_BRIDGE),
    VARARGS("varargs", EOpcodes.ACC_VARARGS),
    TRANSIENT("transient", EOpcodes.ACC_TRANSIENT),
    NATIVE("native", EOpcodes.ACC_NATIVE),
    INTERFACE("interface", EOpcodes.ACC_INTERFACE),
    ABSTRACT("abstract", EOpcodes.ACC_ABSTRACT),
    STRICTFP("strictfp", EOpcodes.ACC_STRICT),
    SYNTHETIC("synthetic", EOpcodes.ACC_SYNTHETIC),
    ANNOTATION("annotation", EOpcodes.ACC_ANNOTATION),
    ENUM("enum", EOpcodes.ACC_ENUM),
    MANDATED("mandated", EOpcodes.ACC_MANDATED),
    ;

    private final String name;
    private final int asmFlag;

    public static AccessAttribute fromString(String name)
    {
        return switch (name.trim().toLowerCase())
        {
            case "static" -> STATIC;
            case "final" -> FINAL;
            case "super" -> SUPER;
            case "synchronized" -> SYNCHRONIZED;
            case "volatile" -> VOLATILE;
            case "bridge" -> BRIDGE;
            case "varargs" -> VARARGS;
            case "transient" -> TRANSIENT;
            case "native" -> NATIVE;
            case "interface" -> INTERFACE;
            case "abstract" -> ABSTRACT;
            case "strictfp" -> STRICTFP;
            case "synthetic" -> SYNTHETIC;
            case "annotation" -> ANNOTATION;
            case "enum" -> ENUM;
            case "mandated" -> MANDATED;
            default -> throw new IllegalArgumentException("Unknown access attribute: " + name);
        };
    }
}
