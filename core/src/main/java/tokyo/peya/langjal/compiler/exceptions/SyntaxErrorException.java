package tokyo.peya.langjal.compiler.exceptions;

import lombok.Getter;
import org.antlr.v4.runtime.RecognitionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

@Getter
public class SyntaxErrorException extends CompileErrorException
{
    @Nullable
    private final Path sourcePath;

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
