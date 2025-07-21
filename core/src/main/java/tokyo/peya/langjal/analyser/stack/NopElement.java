package tokyo.peya.langjal.analyser.stack;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public record NopElement(
        @NotNull InstructionInfo producer
) implements StackElement
{

    @Override
    public @NotNull StackElementType type()
    {
        return StackElementType.NOP;
    }

    @Override
    public Object toASMStackElement()
    {
        return null;
    }
}
