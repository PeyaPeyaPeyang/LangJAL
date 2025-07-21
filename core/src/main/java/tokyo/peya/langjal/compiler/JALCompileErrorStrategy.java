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

@Getter
@RequiredArgsConstructor
        /* non-public */ class JALCompileErrorStrategy extends DefaultErrorStrategy
{
    private final CompileReporter reporter;
    @Nullable
    private final Path sourcePath;

    private boolean error;

    @Override
    public void reset(Parser recognizer)
    {
        super.reset(recognizer);
        this.error = false;
    }

    @Override
    public Token recoverInline(Parser recognizer) throws RecognitionException
    {
        return super.recoverInline(recognizer);
    }

    @Override
    public void recover(Parser recognizer, RecognitionException e) throws RecognitionException
    {
        super.recover(recognizer, e);
    }

    @Override
    public void sync(Parser recognizer) throws RecognitionException
    {
        super.sync(recognizer);
    }

    @Override
    public boolean inErrorRecoveryMode(Parser recognizer)
    {
        return super.inErrorRecoveryMode(recognizer);
    }

    @Override
    public void reportMatch(Parser recognizer)
    {
        super.reportMatch(recognizer);
    }

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
