package tokyo.peya.langjal.compiler.member;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;

public record LocalVariableInfo(
        @NotNull
        String name,
        @NotNull
        TypeDescriptor type,
        @NotNull
        LabelInfo start,
        @NotNull
        LabelInfo end,
        int index,
        boolean isParameter
)
{
    public LocalVariableInfo(@NotNull String name, @NotNull TypeDescriptor type,
                             @NotNull LabelInfo start, @NotNull LabelInfo end, int index)
    {
        this(name, type, start, end, index, false);
    }
}
