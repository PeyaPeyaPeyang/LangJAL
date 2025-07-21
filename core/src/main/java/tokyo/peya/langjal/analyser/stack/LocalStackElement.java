package tokyo.peya.langjal.analyser.stack;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public record LocalStackElement(
        @NotNull
        InstructionInfo producer,
        int index,
        @NotNull
        StackElement stackElement,
        boolean isParameter
) implements StackElement
{
    public LocalStackElement(@NotNull InstructionInfo producer, int index, @NotNull StackElement stackElement)
    {
        this(producer, index, stackElement, false);
    }

    public LocalStackElement
    {
        if (index < 0)
            throw new IllegalArgumentException("Local variable index must be non-negative, but was: " + index);
        else if (index > 65535)
            throw new IllegalArgumentException("Local variable index must be less than or equal to 65535, but was: " + index);
    }

    @Override
    public @NotNull StackElementType type()
    {
        return this.stackElement.type();
    }

    @Override
    public Object toASMStackElement()
    {
        return this.stackElement.toASMStackElement();
    }

    @Override
    public @NotNull String toString()
    {
        return "[" + this.index + "]: " + this.stackElement;
    }
}
