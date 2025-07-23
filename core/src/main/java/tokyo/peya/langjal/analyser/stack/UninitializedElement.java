package tokyo.peya.langjal.analyser.stack;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

/**
 * Represents an uninitialized object created by the NEW instruction on the JVM stack.
 *
 * @param instruction The instruction that created the uninitialized object (must be NEW).
 */
public record UninitializedElement(
        @NotNull
        InstructionInfo instruction // new をしている命令
) implements StackElement
{
    /**
     * Validates that the instruction is a NEW opcode.
     * @param instruction The instruction info.
     */
    public UninitializedElement
    {
        if (instruction.opcode() != EOpcodes.NEW)
            throw new IllegalArgumentException("UninitializedElement must be created with a NEW instruction, but was: " + instruction.opcode());
    }

    /**
     * Gets the instruction that produced this uninitialized element.
     * @return The instruction info.
     */
    @Override
    public @NotNull InstructionInfo producer()
    {
        return this.instruction;
    }

    /**
     * Gets the type of this stack element.
     * @return The stack element type.
     */
    @Override
    public @NotNull StackElementType type()
    {
        return StackElementType.UNINITIALIZED;
    }

    /**
     * Converts this element to an ASM stack element.
     * @return The ASM representation.
     */
    @Override
    public Object toASMStackElement()
    {
        // TODO: ラベルがいるらしい。
        return this.instruction.toString();
    }

    /**
     * Returns a string representation of this uninitialized element.
     * @return String representation.
     */
    @Override
    public @NotNull String toString()
    {
        return "Uninitialized type (by " + this.instruction + ")";
    }
}
