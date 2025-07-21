package tokyo.peya.langjal.compiler.jvm;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccessLevel
{
    PUBLIC("public"),
    PROTECTED("protected"),
    PRIVATE("private"),
    PACKAGE_PRIVATE("package-private");

    private final String name;

    @Override
    public String toString()
    {
        return this.name;
    }

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
