package tokyo.peya.langjal.analyser.stack;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public record UninitializedElement(
        @NotNull
        InstructionInfo instruction // new をしている命令
) implements StackElement
{
    public UninitializedElement
    {
        if (instruction.opcode() != EOpcodes.NEW)
            throw new IllegalArgumentException("UninitializedElement must be created with a NEW instruction, but was: " + instruction.opcode());
    }

    @Override
    public @NotNull InstructionInfo producer()
    {
        return this.instruction;
    }

    @Override
    public @NotNull StackElementType type()
    {
        return StackElementType.UNINITIALIZED;
    }

    @Override
    public Object toASMStackElement()
    {
        // TODO: ラベルがいるらしい。
        return this.instruction.toString();
    }

    @Override
    public @NotNull String toString()
    {
        return "Uninitialized type (by " + this.instruction + ")";
    }
}
