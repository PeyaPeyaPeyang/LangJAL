package tokyo.peya.langjal.compiler.exceptions;

import lombok.Getter;
import org.antlr.v4.runtime.RecognitionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

/**
 * Exception thrown when a syntax error is encountered during parsing.
 * <p>
 * This exception provides information about the location and source of the syntax error.
 * </p>
 */
@Getter
public class SyntaxErrorException extends CompileErrorException
{
    /**
     * The path to the source file where the syntax error occurred, or null if unknown.
     */
    @Nullable
    private final Path sourcePath;

    /**
     * Constructs a new SyntaxErrorException with the given cause and error location.
     *
     * @param cause  The underlying recognition exception.
     * @param line   The line number where the error occurred.
     * @param column The column number where the error occurred.
     * @param length The length of the erroneous token or region.
     */
    public SyntaxErrorException(@NotNull RecognitionException cause, long line,
                                long column, long length)
    {
        super(
                "Syntax error",
                cause,
                "Critical syntax error at line " + line + ", column " + column,
                line, column, length
        );

        this.sourcePath = null;
    }

    /**
     * Constructs a new SyntaxErrorException with the given cause, source path, and error location.
     *
     * @param cause      The underlying recognition exception.
     * @param sourcePath The path to the source file where the error occurred.
     * @param line       The line number where the error occurred.
     * @param column     The column number where the error occurred.
     * @param length     The length of the erroneous token or region.
     */
    public SyntaxErrorException(@NotNull RecognitionException cause,
                                @Nullable Path sourcePath,
                                long line,
                                long column, long length)
    {
        super(
                "Syntax error",
                cause,
                "Critical syntax error at line " + line + ", column " + column,
                line, column, length
        );

        this.sourcePath = sourcePath;
    }
}
