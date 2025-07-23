package tokyo.peya.langjal.analyser;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.MethodNode;

/**
 * Represents the result of analyzing a JVM method.
 * <p>
 * Contains the method node, all frame propagations (control flow transitions),
 * and the maximum stack and local variable sizes required by the method.
 * <br>
 * This is used for bytecode verification and stack frame generation.
 * <br>
 * Example:
 * <pre>
 * MethodAnalysisResult result = analyser.analyse();
 * System.out.println("Max stack: " + result.maxStack());
 * System.out.println("Max locals: " + result.maxLocals());
 * </pre>
 *
 * @param node       The ASM MethodNode for the method.
 * @param propagations The frame propagations (control flow transitions).
 * @param maxStack   The maximum stack size required.
 * @param maxLocals  The maximum local variable size required.
 */
public record MethodAnalysisResult(
        @NotNull
        MethodNode node,
        @NotNull
        FramePropagation[] propagations,
        int maxStack,
        int maxLocals
)
{
    /**
     * Returns an empty analysis result for a method.
     * @param node The method node.
     * @return An empty MethodAnalysisResult.
     */
    @NotNull
    public static MethodAnalysisResult empty(@NotNull MethodNode node)
    {
        return new MethodAnalysisResult(node, new FramePropagation[0], 0, 0);
    }
}
