package tokyo.peya.langjal.compiler.jvm;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum representing access levels for classes, methods, and fields in the JVM.
 */
@Getter
@AllArgsConstructor
public enum AccessLevel
{
    /**
     * Public access level.
     */
    PUBLIC("public"),
    /**
     * Protected access level.
     */
    PROTECTED("protected"),
    /**
     * Private access level.
     */
    PRIVATE("private"),
    /**
     * Package-private access level.
     */
    PACKAGE_PRIVATE("package-private");

    /**
     * The name of the access level.
     */
    private final String name;

    /**
     * Returns the name of the access level.
     *
     * @return The access level name.
     */
    @Override
    public String toString()
    {
        return this.name;
    }

    /**
     * Returns the AccessLevel corresponding to the given string name.
     *
     * @param name The access level name to look up.
     * @return The matching AccessLevel.
     * @throws IllegalArgumentException if the name does not match any access level.
     */
    public static AccessLevel fromString(String name)
    {
        return switch (name.trim().toLowerCase())
        {
            case "public" -> PUBLIC;
            case "protected" -> PROTECTED;
            case "private" -> PRIVATE;
            case "package-private", "package", "" -> PACKAGE_PRIVATE;
            default -> throw new IllegalArgumentException("Unknown access level: " + name);
        };
    }
}
