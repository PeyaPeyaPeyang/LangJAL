package tokyo.peya.langjal.compiler;

import lombok.AllArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.exceptions.CompileErrorException;

import java.nio.file.Path;

@AllArgsConstructor
public class FileEvaluatingReporter
{
    private final CompileReporter delegate;
    private final Path sourcePath;

    public void postWarning(@NotNull String message)
    {
        this.delegate.postWarning(message, this.sourcePath);
    }

    public void postInfo(@NotNull String message)
    {
        this.delegate.postInfo(message, this.sourcePath);
    }

    public void postError(@NotNull String message)
    {
        this.delegate.postError(message, this.sourcePath);
    }

    public void postError(@NotNull CompileErrorException cause)
    {
        this.delegate.postError(cause.getMessage(), cause, this.sourcePath);
    }

    public void postError(@NotNull String message, @NotNull CompileErrorException cause)
    {
        this.delegate.postError(message, cause, this.sourcePath);
    }

    public void postWarning(@NotNull String message, long line, long column, long length)
    {
        this.delegate.postWarning(message, this.sourcePath, line, column, length);
    }

    public void postWarning(@NotNull String message, @NotNull ParserRuleContext ctxt)
    {
        this.delegate.postWarning(message, this.sourcePath, ctxt);
    }
}
