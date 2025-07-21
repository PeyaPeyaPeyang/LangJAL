package tokyo.peya.langjal.analyser;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.MethodNode;

public record MethodAnalysisResult(
        @NotNull
        MethodNode node,
        @NotNull
        FramePropagation[] propagations,
        int maxStack,
        int maxLocals
)
{
    @NotNull
    public static MethodAnalysisResult empty(@NotNull MethodNode node)
    {
        return new MethodAnalysisResult(node, new FramePropagation[0], 0, 0);
    }
}
