package tokyo.peya.langjal.analyser;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.analyser.stack.LocalStackElement;
import tokyo.peya.langjal.analyser.stack.StackElement;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public record AnalysedInstruction(
        @NotNull
        InstructionInfo instruction,
        @NotNull
        FrameDifferenceInfo frameDifference,
        @NotNull
        StackElement[] stackSnapshot,
        @NotNull
        LocalStackElement[] localSnapshot
)
{
}
