package tokyo.peya.langjal.analyser;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.analyser.stack.LocalStackElement;
import tokyo.peya.langjal.analyser.stack.StackElement;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

/**
 * Represents an analysed instruction with its frame difference, stack snapshot, and local variable snapshot.
 *
 * @param instruction      The instruction information.
 * @param frameDifference  The difference in the frame caused by this instruction.
 * @param stackSnapshot    The stack state after this instruction.
 * @param localSnapshot    The local variable state after this instruction.
 */
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
