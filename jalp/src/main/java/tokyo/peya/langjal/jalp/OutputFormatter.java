package tokyo.peya.langjal.jalp;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;

public class OutputFormatter {
    public static final int INDENT_SIZE = 2;

    @Nullable
    private final OutputFormatter parent;
    @NotNull
    private final PrintStream out;
    private final int indentSize;

    public OutputFormatter() {
        this(System.out);
    }

    public OutputFormatter(@NotNull PrintStream out) {
        this.parent = null;
        this.out = out;
        this.indentSize = 0;
    }

    public OutputFormatter(@NotNull OutputFormatter base) {
        this.parent = base;
        this.out = base.out;
        this.indentSize = base.indentSize + INDENT_SIZE;
    }

    public @NotNull OutputChain chained() {
        return new OutputChain(this);
    }

    public @NotNull String output(@NotNull String value) {
        return " " .repeat(this.indentSize) + value;
    }

    public @NotNull String noIndentOutput(@NotNull String value) {
        if (this.parent == null) {
            return value;
        }

        return this.parent.output(value);
    }

    public @NotNull String righten(int width, @NotNull String value) {
        int padding = Math.max(0, width - value.length());
        return output(" " .repeat(padding) + value);
    }

    public @NotNull OutputFormatter print(@NotNull String value) {
        out(output(value));
        return this;
    }

    public @NotNull OutputFormatter noIndentPrint(@NotNull String value) {
        out(noIndentOutput(value));
        return this;
    }

    public @NotNull OutputFormatter println(@NotNull String value) {
        outln(output(value));
        return this;
    }

    public @NotNull OutputFormatter noIndentPrintln(@NotNull String value) {
        outln(noIndentOutput(value));
        return this;
    }

    private void out(@NotNull String value) {
        if (this.parent != null) {
            this.parent.out(value);
            return;
        }

        this.out.print(value);
    }

    private void outln(@NotNull String value) {
        if (this.parent != null) {
            this.parent.outln(value);
            return;
        }

        this.out.println(value);
    }

    @Nullable
    public OutputFormatter parent() {
        return this.parent;
    }

}
