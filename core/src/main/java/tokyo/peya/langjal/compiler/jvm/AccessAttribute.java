package tokyo.peya.langjal.compiler.jvm;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum representing access attributes for classes, methods, and fields in the JVM.
 * Each attribute has a name and an ASM flag value.
 */
@Getter
@AllArgsConstructor
public enum AccessAttribute
{
    /**
     * Indicates the static modifier.
     */
    STATIC("static", EOpcodes.ACC_STATIC),
    /**
     * Indicates the final modifier.
     */
    FINAL("final", EOpcodes.ACC_FINAL),
    /**
     * Indicates the super modifier.
     */
    SUPER("super", EOpcodes.ACC_SUPER),
    /**
     * Indicates the synchronized modifier.
     */
    SYNCHRONIZED("synchronized", EOpcodes.ACC_SYNCHRONIZED),
    /**
     * Indicates the volatile modifier.
     */
    VOLATILE("volatile", EOpcodes.ACC_VOLATILE),
    /**
     * Indicates the bridge modifier.
     */
    BRIDGE("bridge", EOpcodes.ACC_BRIDGE),
    /**
     * Indicates the varargs modifier.
     */
    VARARGS("varargs", EOpcodes.ACC_VARARGS),
    /**
     * Indicates the transient modifier.
     */
    TRANSIENT("transient", EOpcodes.ACC_TRANSIENT),
    /**
     * Indicates the native modifier.
     */
    NATIVE("native", EOpcodes.ACC_NATIVE),
    /**
     * Indicates the interface modifier.
     */
    INTERFACE("interface", EOpcodes.ACC_INTERFACE),
    /**
     * Indicates the abstract modifier.
     */
    ABSTRACT("abstract", EOpcodes.ACC_ABSTRACT),
    /**
     * Indicates the strictfp modifier.
     */
    STRICTFP("strictfp", EOpcodes.ACC_STRICT),
    /**
     * Indicates the synthetic modifier.
     */
    SYNTHETIC("synthetic", EOpcodes.ACC_SYNTHETIC),
    /**
     * Indicates the annotation modifier.
     */
    ANNOTATION("annotation", EOpcodes.ACC_ANNOTATION),
    /**
     * Indicates the enum modifier.
     */
    ENUM("enum", EOpcodes.ACC_ENUM),
    /**
     * Indicates the mandated modifier.
     */
    MANDATED("mandated", EOpcodes.ACC_MANDATED),
    ;

    /**
     * The name of the access attribute.
     */
    private final String name;
    /**
     * The ASM flag value for the attribute.
     */
    private final int asmFlag;

    /**
     * Returns the AccessAttribute corresponding to the given string name.
     *
     * @param name The attribute name to look up.
     * @return The matching AccessAttribute.
     * @throws IllegalArgumentException if the name does not match any attribute.
     */
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
