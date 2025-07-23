package tokyo.peya.langjal.compiler;

import lombok.AllArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.exceptions.CompileErrorException;

import java.nio.file.Path;

/**
 * Reporter that delegates compile-time messages to another reporter,
 * associating them with a specific source file path.
 */
@AllArgsConstructor
public class FileEvaluatingReporter
{
    /**
     * The underlying reporter to delegate to.
     */
    private final CompileReporter delegate;
    /**
     * The source file path associated with this reporter.
     */
    private final Path sourcePath;

    /**
     * Posts a warning message for the source file.
     *
     * @param message The warning message.
     */
    public void postWarning(@NotNull String message)
    {
        this.delegate.postWarning(message, this.sourcePath);
    }

    /**
     * Posts an informational message for the source file.
     *
     * @param message The info message.
     */
    public void postInfo(@NotNull String message)
    {
        this.delegate.postInfo(message, this.sourcePath);
    }

    /**
     * Posts an error message for the source file.
     *
     * @param message The error message.
     */
    public void postError(@NotNull String message)
    {
        this.delegate.postError(message, this.sourcePath);
    }

    /**
     * Posts an error message with a cause for the source file.
     *
     * @param cause The compile error exception.
     */
    public void postError(@NotNull CompileErrorException cause)
    {
        this.delegate.postError(cause.getMessage(), cause, this.sourcePath);
    }

    /**
     * Posts an error message with a custom message and cause for the source file.
     *
     * @param message The error message.
     * @param cause   The compile error exception.
     */
    public void postError(@NotNull String message, @NotNull CompileErrorException cause)
    {
        this.delegate.postError(message, cause, this.sourcePath);
    }

    /**
     * Posts a warning message with location information for the source file.
     *
     * @param message The warning message.
     * @param line    The line number.
     * @param column  The column number.
     * @param length  The length of the affected region.
     */
    public void postWarning(@NotNull String message, long line, long column, long length)
    {
        this.delegate.postWarning(message, this.sourcePath, line, column, length);
    }

    /**
     * Posts a warning message with parser context for the source file.
     *
     * @param message The warning message.
     * @param ctxt    The parser rule context.
     */
    public void postWarning(@NotNull String message, @NotNull ParserRuleContext ctxt)
    {
        this.delegate.postWarning(message, this.sourcePath, ctxt);
    }
}
