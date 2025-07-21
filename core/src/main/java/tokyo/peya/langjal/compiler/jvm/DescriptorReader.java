package tokyo.peya.langjal.compiler.jvm;

import lombok.Getter;

@Getter
public class DescriptorReader
{
    private final String source;
    private int pos;

    private DescriptorReader(String source)
    {
        this.source = source;
    }

    public boolean hasMore()
    {
        return this.pos < this.source.length();
    }

    public char peek()
    {
        return this.source.charAt(this.pos);
    }

    public char read()
    {
        return this.source.charAt(this.pos++);
    }

    public void expect(char expected)
    {
        char c = read();
        if (c != expected)
            throw new IllegalArgumentException("Expected '" + expected + "' but got '" + c + "'");
    }

    public static DescriptorReader fromString(String source)
    {
        return new DescriptorReader(source);
    }
}
