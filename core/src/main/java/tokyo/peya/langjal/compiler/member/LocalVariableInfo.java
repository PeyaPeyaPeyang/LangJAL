package tokyo.peya.langjal.compiler.member;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;

/**
 * Represents a local variable in a method, including its name, type, scope labels, index, and parameter status.
 *
 * @param name         The name of the local variable.
 * @param type         The type descriptor of the variable.
 * @param start        The label where the variable's scope starts.
 * @param end          The label where the variable's scope ends.
 * @param index        The index of the variable in the local variable table.
 * @param isParameter  True if this variable is a method parameter, false otherwise.
 */
public record LocalVariableInfo(
        @NotNull String name,
        @NotNull TypeDescriptor type,
        @NotNull LabelInfo start,
        @NotNull LabelInfo end,
        int index,
        boolean isParameter
)
{
    /**
     * Constructs a LocalVariableInfo for a non-parameter variable.
     *
     * @param name   The variable name.
     * @param type   The type descriptor.
     * @param start  The start label.
     * @param end    The end label.
     * @param index  The variable index.
     */
    public LocalVariableInfo(@NotNull String name, @NotNull TypeDescriptor type,
                             @NotNull LabelInfo start, @NotNull LabelInfo end, int index)
    {
        this(name, type, start, end, index, false);
    }
}
