package tokyo.peya.langjal.compiler.preprocessor;

import org.jetbrains.annotations.NotNull;

import java.util.List;

record Macro(List<String> parameters, @NotNull String replacement) {
    boolean isFunctionLike() {
        return this.parameters != null;
    }
}
