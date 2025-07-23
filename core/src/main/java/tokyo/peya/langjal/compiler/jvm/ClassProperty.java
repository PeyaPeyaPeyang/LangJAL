package tokyo.peya.langjal.compiler.jvm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Enum representing properties of a class in the JVM.
 */
@Getter
@AllArgsConstructor
public enum ClassProperty
{
    /**
     * Major version of the class file.
     */
    MAJOR_VERSION("major_version"),
    /**
     * Minor version of the class file.
     */
    MINOR_VERSION("minor_version"),
    /**
     * Super class of the class.
     */
    SUPER_CLASS("super_class"),
    /**
     * Interfaces implemented by the class.
     */
    INTERFACES("interfaces"),
    /**
     * Unknown property.
     */
    UNKNOWN("unknown");

    /**
     * The name of the property.
     */
    private final String name;

    /**
     * Returns the ClassProperty corresponding to the given string name.
     *
     * @param name The property name to look up.
     * @return The matching ClassProperty, or UNKNOWN if not found.
     */
    public static @NotNull ClassProperty fromString(@NotNull String name)
    {
        for (ClassProperty property : ClassProperty.values())
            if (property.getName().equalsIgnoreCase(name))
                return property;
        return UNKNOWN;
    }
}
