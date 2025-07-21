package tokyo.peya.langjal.analyser.stack;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

import java.util.function.Function;

// DUP や SWAP などのために，スタック要素を一旦退避する特別な参照.
@Getter
@Setter
public final class StackElementCapsule implements StackElement
{
    private final InstructionInfo instruction;
    @Nullable
    private final Function<? super StackElement, ? extends StackElement> producerFunction;

    private StackElement element;

    public StackElementCapsule(@NotNull InstructionInfo instruction)
    {
        this.instruction = instruction;
        this.producerFunction = null;
    }

    public StackElementCapsule(@NotNull InstructionInfo instruction,
                               @NotNull Function<? super StackElement, ? extends StackElement> producerFunction)
    {
        this.instruction = instruction;
        this.producerFunction = producerFunction;
    }

    public @NotNull StackElement getElement()
    {
        return this.element;
    }

    public void setElement(@NotNull StackElement element)
    {
        if (this.producerFunction != null)
            this.element = this.producerFunction.apply(element);
        else
            this.element = element;
    }

    @Override
    public @NotNull InstructionInfo producer()
    {
        return this.instruction;
    }

    @Override
    public @NotNull StackElementType type()
    {
        if (this.producerFunction != null)
            return this.producerFunction.apply(this.element).type();
        return this.element.type();
    }

    @Override
    public Object toASMStackElement()
    {
        return null;
    }
}
