package tokyo.peya.langjal.analyser.stack;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public sealed interface StackElement
        permits ObjectElement, PrimitiveElement, UninitializedElement,
        UninitializedThisElement, NullElement, LocalStackElement, StackElementCapsule, TopElement, NopElement
{
    @NotNull
    InstructionInfo producer();  // Push 系命令のときに，だれがこの要素をスタックに積んだのかを示す．

    @NotNull
    // Pop 系命令のときは，この値を無視する
    StackElementType type();

    Object toASMStackElement();
}
