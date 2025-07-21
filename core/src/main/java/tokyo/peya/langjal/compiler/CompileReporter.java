package tokyo.peya.langjal.compiler;

import org.antlr.v4.runtime.ParserRuleContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tokyo.peya.langjal.compiler.exceptions.CompileErrorException;

import java.nio.file.Path;

public interface CompileReporter
{
    void postWarning(@NotNull String message, @Nullable Path sourcePath);

    void postInfo(@NotNull String message, @Nullable Path sourcePath);

    void postError(@NotNull String message, @Nullable Path sourcePath);

    void postError(@NotNull String message, @NotNull CompileErrorException cause, @Nullable Path sourcePath);

    void postWarning(@NotNull String message, @Nullable Path sourcePath, long line, long column, long length);

    void postWarning(@NotNull String message, @NotNull Path sourcePath, @NotNull ParserRuleContext ctxt);
}
