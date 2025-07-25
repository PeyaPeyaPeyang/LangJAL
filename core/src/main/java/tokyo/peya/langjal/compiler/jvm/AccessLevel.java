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

    /**
     * Returns the AccessLevel corresponding to the given JVM access flags.
     * @param access The JVM access flags to check.
     * @return The AccessLevel corresponding to the flags.
     */
    public static AccessLevel fromAccess(int access)
    {
        if ((access & EOpcodes.ACC_PUBLIC) != 0)
            return PUBLIC;
        else if ((access & EOpcodes.ACC_PROTECTED) != 0)
            return PROTECTED;
        else if ((access & EOpcodes.ACC_PRIVATE) != 0)
            return PRIVATE;

        return PACKAGE_PRIVATE;
    }
}
