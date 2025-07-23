package tokyo.peya.langjal.analyser.stack;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

/**
 * Represents an element on the JVM operand stack or in a local variable slot.
 */
public sealed interface StackElement
        permits ObjectElement, PrimitiveElement, UninitializedElement,
        UninitializedThisElement, NullElement, LocalStackElement, StackElementCapsule, TopElement, NopElement
{
    /**
     * Returns the instruction that produced this stack element.
     * @return The producer instruction.
     */
    @NotNull
    InstructionInfo producer();  // Push 系命令のときに，だれがこの要素をスタックに積んだのかを示す．

    /**
     * Gets the type of this stack element.
     * @return The stack element type.
     */
    @NotNull
    // Pop 系命令のときは，この値を無視する
    StackElementType type();

    /**
     * Converts this element to an ASM stack element.
     * @return The ASM representation.
     */
    Object toASMStackElement();
}
