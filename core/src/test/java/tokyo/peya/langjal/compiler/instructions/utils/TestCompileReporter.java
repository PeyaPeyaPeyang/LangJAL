package tokyo.peya.langjal.compiler.instructions.utils;

import org.antlr.v4.runtime.ParserRuleContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tokyo.peya.langjal.compiler.CompileReporter;
import tokyo.peya.langjal.compiler.exceptions.CompileErrorException;

import java.nio.file.Path;

public class TestCompileReporter implements CompileReporter {
    @Override
    public void postWarning(@NotNull String message, @Nullable Path sourcePath) {
        System.out.println("WARNING: " + message + (sourcePath != null ? " at " + sourcePath : ""));
    }

    @Override
    public void postInfo(@NotNull String message, @Nullable Path sourcePath) {
        System.out.println("INFO: " + message + (sourcePath != null ? " at " + sourcePath : ""));
    }

    @Override
    public void postError(@NotNull String message, @Nullable Path sourcePath) {
        System.err.println("ERROR: " + message + (sourcePath != null ? " at " + sourcePath : ""));
    }

    @Override
    public void postError(@NotNull String message, @NotNull CompileErrorException cause, @Nullable Path sourcePath) {
        System.err.println("ERROR: " + message + (sourcePath != null ? " at " + sourcePath : ""));
        cause.printStackTrace(System.err);
    }

    @Override
    public void postWarning(@NotNull String message, @Nullable Path sourcePath, long line, long column, long length) {
        System.out.println("WARNING: " + message + (sourcePath != null ? " at " + sourcePath : "") +
                " (line " + line + ", column " + column + ", length " + length + ")");
    }

    @Override
    public void postWarning(@NotNull String message, @NotNull Path sourcePath, @NotNull ParserRuleContext ctxt) {
        System.out.println("WARNING: " + message + " at " + sourcePath +
                " (line " + ctxt.getStart().getLine() + ", column " + ctxt.getStart()
                .getCharPositionInLine() + ")");
    }
}
