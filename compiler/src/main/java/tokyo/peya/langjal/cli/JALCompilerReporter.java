package tokyo.peya.langjal.cli;

import lombok.AllArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tokyo.peya.langjal.compiler.CompileReporter;
import tokyo.peya.langjal.compiler.exceptions.CompileErrorException;

import java.nio.file.Files;
import java.nio.file.Path;

@AllArgsConstructor
public class JALCompilerReporter implements CompileReporter
{
    private final boolean verbose;


    @Override
    public void postWarning(@NotNull String message, @Nullable Path sourcePath)
    {
        System.err.println("Warning: " + message + (sourcePath == null ? "" : " in " + sourcePath));
    }

    @Override
    public void postInfo(@NotNull String message, @Nullable Path sourcePath)
    {
        if (this.verbose)
            System.out.println("Info: " + message + (sourcePath == null ? "" : " in " + sourcePath));
    }

    @Override
    public void postError(@NotNull String message, @Nullable Path sourcePath)
    {
        System.err.println("Error: " + message + (sourcePath == null ? "" : " in " + sourcePath));
    }

    @Override
    public void postError(@NotNull String message, @NotNull CompileErrorException cause, @Nullable Path sourcePath)
    {
        System.err.println("Error: " + cause.getDetailedMessage() + (sourcePath == null ? "" : " in " + sourcePath));
        if (cause.getLine() > 0 && cause.getColumn() > 0)
        {
            if (sourcePath == null)
                System.err.println("Error at line " + cause.getLine() + ", column " + cause.getColumn() + ": " + cause.getMessage());
            else
                postAtLine(
                        message,
                        sourcePath,
                        cause.getLine(),
                        cause.getColumn(),
                        cause.getLength()
                );
        }
        else
            System.err.println("Error: " + cause.getMessage());
    }

    private static String getFileContent(@NotNull Path sourcePath)
    {
        try
        {
            return Files.readString(sourcePath);
        }
        catch (Exception e)
        {
            return "File content could not be read: " + e.getMessage();
        }
    }

    @Override
    public void postWarning(@NotNull String message, @Nullable Path sourcePath, long line, long column, long length)
    {
        if (sourcePath == null)
        {
            System.err.println("Warning: " + message + " at line " + line + ", column " + column);
            return;
        }
        postAtLine(message, sourcePath, line, column, length);
    }

    @Override
    public void postWarning(@NotNull String message, @NotNull Path sourcePath, @NotNull ParserRuleContext ctxt)
    {
        long line = ctxt.getStart().getLine();
        long column = ctxt.getStart().getCharPositionInLine() + 1; // ANTLR uses 0-based index, we use 1-based
        long length = ctxt.getStop().getStopIndex() - ctxt.getStart().getStartIndex() + 1;
        postAtLine(message, sourcePath, line, column, length);
    }

    private static void postAtLine(
            @NotNull String message,
            @NotNull Path sourcePath,
            long line,
            long column,
            long length
    )
    {
        String content = getFileContent(sourcePath);
        String[] lines = content.split("\n");
        if (line < 1 || line > lines.length)
        {
            System.err.println("Error: " + message + " at line " + line + ", column " + column + " in " + sourcePath);
            return;
        }
        String lineContent = lines[(int) line - 1];
        System.err.println("Error: " + message + " at line " + line + ", column " + column + " in " + sourcePath);
        System.err.println("Line: " + lineContent);
        if (column > 0 && column <= lineContent.length())
        {
            StringBuilder marker = new StringBuilder();
            for (int i = 1; i < column; i++)
                marker.append("-");
            marker.append("^".repeat(Math.max(0, (int) length)));
            System.err.println(marker);
        }
        else
            System.err.println("Column " + column + " is out of bounds for line " + line);
    }
}
