package tokyo.peya.langjal.analyser;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.analyser.stack.LocalStackElement;
import tokyo.peya.langjal.analyser.stack.StackElement;

/**
 * Represents the result of analyzing a set of JVM instructions.
 * <p>
 * This record contains the analyzed instructions, frame propagations (jump/branch information),
 * the final stack and local variable states, and the maximum stack and local variable sizes encountered.
 * <br>
 * It is used to describe the state transitions and resource requirements for a block of JVM bytecode.
 * <br>
 * Example usage:
 * <pre>
 * InstructionSetAnalysisResult result = analyser.analyse(propagation);
 * System.out.println("Max stack size: " + result.maxStackSize());
 * System.out.println("Max local size: " + result.maxLocalSize());
 * </pre>
 *
 * @param analyzedInstructions The analyzed instructions.
 * @param framePropagations   The frame propagations (jump/branch information).
 * @param stack               The final stack state.
 * @param locals              The final local variable state.
 * @param maxStackSize        The maximum stack size encountered.
 * @param maxLocalSize        The maximum local variable size encountered.
 */
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
