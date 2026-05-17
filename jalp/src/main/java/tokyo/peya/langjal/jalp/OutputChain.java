package tokyo.peya.langjal.jalp;

import org.jetbrains.annotations.NotNull;

public class OutputChain {
    private final OutputFormatter formatter;
    private final StringBuilder buffer;

    public OutputChain(@NotNull OutputFormatter formatter) {
        this.formatter = formatter;
        this.buffer = new StringBuilder();
    }

    public @NotNull OutputChain output(@NotNull String value) {
        this.buffer.append(value);
        return this;
    }

    public @NotNull OutputChain righten(int width, @NotNull String value) {
        int padding = Math.max(0, width - value.length());
        this.buffer.repeat(" ", padding).append(value);
        return this;
    }

    public @NotNull OutputFormatter print() {
        this.formatter.print(this.buffer.toString());
        return this.formatter;
    }

    public @NotNull OutputFormatter println() {
        this.formatter.println(this.buffer.toString());
        return this.formatter;
    }
}
