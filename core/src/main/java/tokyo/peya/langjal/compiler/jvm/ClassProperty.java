package tokyo.peya.langjal.compiler.jvm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
@AllArgsConstructor
public enum ClassProperty
{
    MAJOR_VERSION("major_version"),
    MINOR_VERSION("minor_version"),
    SUPER_CLASS("super_class"),
    INTERFACES("interfaces"),

    UNKNOWN("unknown");

    private final String name;

    public static @NotNull ClassProperty fromString(@NotNull String name)
    {
        for (ClassProperty property : ClassProperty.values())
            if (property.getName().equalsIgnoreCase(name))
                return property;
        return UNKNOWN;
    }
}
