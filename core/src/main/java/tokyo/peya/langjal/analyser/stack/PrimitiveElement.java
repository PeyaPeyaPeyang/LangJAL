package tokyo.peya.langjal.analyser.stack;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public record PrimitiveElement(
        @NotNull
        InstructionInfo producer,
        @NotNull
        StackElementType type
) implements StackElement
{
    public PrimitiveElement
    {
        if (!(type == StackElementType.INTEGER ||
                type == StackElementType.FLOAT ||
                type == StackElementType.LONG ||
                type == StackElementType.DOUBLE))
            throw new IllegalArgumentException("PrimitiveElement must be INTEGER, FLOAT, LONG, or DOUBLE, but was: " + type);
    }

    @Override
    public Object toASMStackElement()
    {
        return this.type.getOpcode();
    }

    public static PrimitiveElement of(
            @NotNull InstructionInfo producer,
            @NotNull StackElementType type
    )
    {
        return new PrimitiveElement(producer, type);
    }

    @Override
    public @NotNull String toString()
    {
        return "Primitive type of " + this.type + " (by " + this.producer + ")";
    }
}
