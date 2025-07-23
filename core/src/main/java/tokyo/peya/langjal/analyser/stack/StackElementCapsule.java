package tokyo.peya.langjal.analyser.stack;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

import java.util.function.Function;

/**
 * A special reference for temporarily storing stack elements, used for DUP, SWAP, etc.
 * Wraps a stack element and optionally applies a transformation function.
 */
@Getter
@Setter
public final class StackElementCapsule implements StackElement
{
    private final InstructionInfo instruction;
    @Nullable
    private final Function<? super StackElement, ? extends StackElement> producerFunction;

    private StackElement element;

    /**
     * Constructs a capsule with the given instruction.
     * @param instruction The instruction that produced this capsule.
     */
    public StackElementCapsule(@NotNull InstructionInfo instruction)
    {
        this.instruction = instruction;
        this.producerFunction = null;
    }

    /**
     * Constructs a capsule with an instruction and a transformation function.
     * @param instruction The instruction that produced this capsule.
     * @param producerFunction The function to transform the element.
     */
    public StackElementCapsule(@NotNull InstructionInfo instruction,
                               @NotNull Function<? super StackElement, ? extends StackElement> producerFunction)
    {
        this.instruction = instruction;
        this.producerFunction = producerFunction;
    }

    /**
     * Gets the encapsulated stack element.
     * @return The stack element.
     */
    public @NotNull StackElement getElement()
    {
        return this.element;
    }

    /**
     * Sets the encapsulated stack element, applying the producer function if present.
     * @param element The stack element to set.
     */
    public void setElement(@NotNull StackElement element)
    {
        if (this.producerFunction != null)
            this.element = this.producerFunction.apply(element);
        else
            this.element = element;
    }

    /**
     * Returns the instruction that produced this capsule.
     * @return The instruction info.
     */
    @Override
    public @NotNull InstructionInfo producer()
    {
        return this.instruction;
    }

    /**
     * Gets the type of the encapsulated stack element.
     * @return The stack element type.
     */
    @Override
    public @NotNull StackElementType type()
    {
        if (this.producerFunction != null)
            return this.producerFunction.apply(this.element).type();
        return this.element.type();
    }

    /**
     * Converts this capsule to an ASM stack element (always null).
     * @return Always null.
     */
    @Override
    public Object toASMStackElement()
    {
        return null;
    }
}
