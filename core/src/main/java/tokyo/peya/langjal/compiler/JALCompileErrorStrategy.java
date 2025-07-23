package tokyo.peya.langjal.compiler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.Nullable;
import tokyo.peya.langjal.compiler.exceptions.SyntaxErrorException;

import java.nio.file.Path;

/**
 * Custom error strategy for JAL compilation.
 * <p>
 * Integrates with the reporter to provide detailed syntax error messages and tracks error state.
 */
@Getter
@RequiredArgsConstructor
        /* non-public */ class JALCompileErrorStrategy extends DefaultErrorStrategy
{
    /**
     * Reporter for error messages during parsing.
     */
    private final CompileReporter reporter;
    /**
     * Path to the source file being parsed, or null.
     */
    @Nullable
    private final Path sourcePath;

    /**
     * Indicates whether an error has occurred during parsing.
     */
    private boolean error;

    /**
     * Resets the error state for a new parse operation.
     *
     * @param recognizer The parser instance.
     */
    @Override
    public void reset(Parser recognizer)
    {
        super.reset(recognizer);
        this.error = false;
    }

    /**
     * Handles inline recovery from a parsing error.
     *
     * @param recognizer The parser instance.
     * @return The recovered token.
     * @throws RecognitionException If recovery fails.
     */
    @Override
    public Token recoverInline(Parser recognizer) throws RecognitionException
    {
        return super.recoverInline(recognizer);
    }

    /**
     * Performs recovery from a recognition exception.
     *
     * @param recognizer The parser instance.
     * @param e          The recognition exception.
     * @throws RecognitionException If recovery fails.
     */
    @Override
    public void recover(Parser recognizer, RecognitionException e) throws RecognitionException
    {
        super.recover(recognizer, e);
    }

    /**
     * Synchronizes the parser state after an error.
     *
     * @param recognizer The parser instance.
     * @throws RecognitionException If synchronization fails.
     */
    @Override
    public void sync(Parser recognizer) throws RecognitionException
    {
        super.sync(recognizer);
    }

    /**
     * Checks if the parser is currently in error recovery mode.
     *
     * @param recognizer The parser instance.
     * @return True if in error recovery mode, false otherwise.
     */
    @Override
    public boolean inErrorRecoveryMode(Parser recognizer)
    {
        return super.inErrorRecoveryMode(recognizer);
    }

    /**
     * Reports a successful match to the error strategy.
     *
     * @param recognizer The parser instance.
     */
    @Override
    public void reportMatch(Parser recognizer)
    {
        super.reportMatch(recognizer);
    }

    /**
     * Reports a parsing error to the reporter and updates error state.
     *
     * @param recognizer The parser instance.
     * @param e          The recognition exception.
     */
    @Override
    public void reportError(Parser recognizer, RecognitionException e)
    {
        Token offendingToken = e.getOffendingToken();
        if (offendingToken == null)
        {
            this.reporter.postError(
                    "Unexpected error in source " + this.sourcePath + ": " + e.getMessage(),
                    this.sourcePath
            );
            this.error = true;
            return;
        }

        long line = offendingToken.getLine();
        long column = offendingToken.getCharPositionInLine();
        long length = offendingToken.getText().length();

        SyntaxErrorException syntaxErrorException = new SyntaxErrorException(
                e,
                this.sourcePath,
                line,
                column,
                length
        );
        this.reporter.postError(
                syntaxErrorException.getDetailedMessage(),
                syntaxErrorException,
                this.sourcePath
        );


        this.error = true;

        super.reportError(recognizer, e);
    }
}
