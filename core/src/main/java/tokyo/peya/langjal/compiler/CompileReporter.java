package tokyo.peya.langjal.compiler;

import org.antlr.v4.runtime.ParserRuleContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tokyo.peya.langjal.compiler.exceptions.CompileErrorException;

import java.nio.file.Path;

/**
 * Interface for reporting compile-time messages such as warnings, info, and errors.
 * Implementations can handle reporting to logs, consoles, or other outputs.
 */
public interface CompileReporter
{
    /**
     * Posts a warning message.
     *
     * @param message    The warning message.
     * @param sourcePath The source file path, or null if not applicable.
     */
    void postWarning(@NotNull String message, @Nullable Path sourcePath);

    /**
     * Posts an informational message.
     *
     * @param message    The info message.
     * @param sourcePath The source file path, or null if not applicable.
     */
    void postInfo(@NotNull String message, @Nullable Path sourcePath);

    /**
     * Posts an error message.
     *
     * @param message    The error message.
     * @param sourcePath The source file path, or null if not applicable.
     */
    void postError(@NotNull String message, @Nullable Path sourcePath);

    /**
     * Posts an error message with a cause.
     *
     * @param message    The error message.
     * @param cause      The exception that caused the error.
     * @param sourcePath The source file path, or null if not applicable.
     */
    void postError(@NotNull String message, @NotNull CompileErrorException cause, @Nullable Path sourcePath);

    /**
     * Posts a warning message with location information.
     *
     * @param message    The warning message.
     * @param sourcePath The source file path, or null if not applicable.
     * @param line       The line number where the warning occurred.
     * @param column     The column number where the warning occurred.
     * @param length     The length of the affected region.
     */
    void postWarning(@NotNull String message, @Nullable Path sourcePath, long line, long column, long length);

    /**
     * Posts a warning message with parser context.
     *
     * @param message    The warning message.
     * @param sourcePath The source file path.
     * @param ctxt       The parser rule context for the warning.
     */
    void postWarning(@NotNull String message, @NotNull Path sourcePath, @NotNull ParserRuleContext ctxt);
}
