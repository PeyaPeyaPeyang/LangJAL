package tokyo.peya.langjal.analyser.stack;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public record TopElement(
        InstructionInfo producer
) implements StackElement
{
    @Override
    public @NotNull StackElementType type()
    {
        return StackElementType.TOP;
    }

    @Override
    public Object toASMStackElement()
    {
        return EOpcodes.TOP;
    }

    @Override
    public @NotNull String toString()
    {
        return "Top type (by " + this.producer + ")";
    }
}
