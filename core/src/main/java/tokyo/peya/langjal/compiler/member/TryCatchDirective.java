package tokyo.peya.langjal.compiler.member;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;

public record TryCatchDirective(
        @NotNull
        LabelInfo tryBlockStartLabel,
        @NotNull
        LabelInfo tryBlockEndLabel,
        @Nullable
        LabelInfo catchBlockLabel,
        @Nullable
        TypeDescriptor exceptionType,
        @Nullable
        LabelInfo finallyBlockLabel
)
{
}
