package tokyo.peya.langjal.compiler.preprocessor;

import org.jetbrains.annotations.NotNull;

import java.util.List;

record MacroDefinition(@NotNull List<String> parameters, @NotNull String replacement) {
}
