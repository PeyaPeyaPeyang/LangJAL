package tokyo.peya.langjal.analyser;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.analyser.stack.LocalStackElement;
import tokyo.peya.langjal.analyser.stack.StackElement;

public record InstructionSetAnalysisResult(
        @NotNull
        AnalysedInstruction[] analyzedInstructions,
        @NotNull
        FramePropagation[] framePropagations,
        @NotNull
        StackElement[] stack,
        @NotNull
        LocalStackElement[] locals,

        int maxStackSize,
        int maxLocalSize
)
{
}
