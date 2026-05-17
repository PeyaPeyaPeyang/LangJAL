package tokyo.peya.langjal.jalp;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OutputFormatter {
    public static final int INDENT_SIZE = 2;

    @Nullable
    private final OutputFormatter parent;
    private final int indentSize;

    public OutputFormatter() {
        this.parent = null;
        this.indentSize = 0;
    }

    public OutputFormatter(@NotNull OutputFormatter base) {
        this.parent = base;
        this.indentSize = base.indentSize + INDENT_SIZE;
    }

    public @NotNull OutputChain chained() {
        return new OutputChain(this);
    }

    public @NotNull String output(@NotNull String value) {
        return " ".repeat(this.indentSize) + value;
    }

    public @NotNull String noIndentOutput(@NotNull String value) {
        if (this.parent == null) {
            return value;
        }

        return this.parent.output(value);
    }

    public @NotNull String righten(int width, @NotNull String value) {
        int padding = Math.max(0, width - value.length());
        return output(" ".repeat(padding) + value);
    }

    public @NotNull OutputFormatter print(@NotNull String value) {
        System.out.print(output(value));
        return this;
    }

    public @NotNull OutputFormatter noIndentPrint(@NotNull String value) {
        System.out.print(noIndentOutput(value));
        return this;
    }

    public @NotNull OutputFormatter println(@NotNull String value) {
        System.out.println(output(value));
        return this;
    }

    public @NotNull OutputFormatter noIndentPrintln(@NotNull String value) {
        System.out.println(noIndentOutput(value));
        return this;
    }

    @Nullable
    public OutputFormatter parent() {
        return this.parent;
    }

}
