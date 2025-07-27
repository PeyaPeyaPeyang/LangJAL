package tokyo.peya.langjal.compiler.jvm;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * An utility class for reading and parsing JVM descriptors.
 * This class provides methods to read characters, check for more input,
 * and expect specific characters in the descriptor string.
 *
 * <p>Descriptors are used in the JVM to describe types, method signatures,
 * and field signatures. This reader helps in parsing such descriptors
 * from a string representation.</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * DescriptorReader reader = DescriptorReader.fromString("Ljava/lang/String;");
 * while (reader.hasMore()) {
 *     char c = reader.read();
 *     // Process character...
 * }
 * }</pre>
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se24/html/jvms-4.html#jvms-4.3">JVM Descriptors</a>
 */
@Getter
public class DescriptorReader
{
    private final String source;
    private int pos;

    private DescriptorReader(@NotNull String source)
    {
        this.source = source;
    }

    /**
     *
     * Checks if there are more characters to read in the descriptor.
     * This method returns true if the current position is less than the length
     * of the source string, indicating that there are more characters available.
     *
     * <p>Usage example:</p>
     * <pre>{@code
     * DescriptorReader reader = DescriptorReader.fromString("Ljava/lang/String;");
     * if (reader.hasMore()) {
     *     char nextChar = reader.peek();
     *     // Process nextChar...
     * }
     * }</pre>
     *  @return Whether there are more characters to read in the descriptor.
     */
    public boolean hasMore()
    {
        return this.pos < this.source.length();
    }

    /**
     * Returns the next character in the descriptor without advancing the position.
     * This method allows you to look at the next character without consuming it,
     * which is useful for checking what comes next in the descriptor.
     *
     * <p>Usage example:</p>
     * <pre>{@code
     * DescriptorReader reader = DescriptorReader.fromString("Ljava/lang/String;");
     * if (reader.hasMore()) {
     *     char nextChar = reader.peek();
     *     // Decide whether to read it or not...
     * }
     * }</pre>
     *
     * @return The next character in the descriptor.
     */
    public char peek()
    {
        return this.source.charAt(this.pos);
    }

    /**
     * Reads the next character in the descriptor and advances the position.
     * This method consumes the next character from the descriptor string,
     * moving the position forward by one.
     *
     * <p>Usage example:</p>
     * <pre>{@code
     * DescriptorReader reader = DescriptorReader.fromString("Ljava/lang/String;");
     * while (reader.hasMore()) {
     *     char c = reader.read();
     *     // Process character c...
     * }
     * }</pre>
     * @return The next character in the descriptor.
     */
    public char read()
    {
        return this.source.charAt(this.pos++);
    }

    /**
     * Reads the next character and checks if it matches the expected character.
     * If the read character does not match the expected one, throws an IllegalArgumentException.
     *
     * <p>Usage example:</p>
     * <pre>{@code
     * reader.expect(';'); // Throws if next character is not ';'
     * }</pre>
     *
     * @param expected The character that is expected at the current position.
     * @throws IllegalArgumentException if the next character does not match the expected character.
     */
    public void expect(char expected)
    {
        char c = read();
        if (c != expected)
            throw new IllegalArgumentException("Expected '" + expected + "' but got '" + c + "'");
    }

    /**
     * Creates a new DescriptorReader from the given descriptor string.
     *
     * <p>Usage example:</p>
     * <pre>{@code
     * DescriptorReader reader = DescriptorReader.fromString("Ljava/lang/String;");
     * }</pre>
     *
     * @param source The descriptor string to read.
     * @return A new DescriptorReader instance for the given string.
     */
    public static DescriptorReader fromString(@NotNull String source)
    {
        return new DescriptorReader(source.trim());
    }
}
