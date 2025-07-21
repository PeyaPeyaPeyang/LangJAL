package tokyo.peya.langjal.compiler.member;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;

import java.util.ArrayList;
import java.util.List;

public class TryCatchDirectivesHolder
{
    private final FileEvaluatingReporter context;
    private final LabelsHolder labels;
    private final List<TryCatchDirective> tryCatchDirectives;

    public TryCatchDirectivesHolder(@NotNull FileEvaluatingReporter ctxt, @NotNull LabelsHolder labels)
    {
        this.context = ctxt;
        this.labels = labels;

        this.tryCatchDirectives = new ArrayList<>();
    }

    public TryCatchDirective addTryCatchDirective(
            @NotNull LabelInfo tryBlockStartLabel,
            @NotNull LabelInfo tryBlockEndLabel,
            @Nullable LabelInfo catchBlockLabel,
            @Nullable TypeDescriptor exceptionType,
            @Nullable LabelInfo finallyBlockLabel
    )
    {
        TryCatchDirective directive = new TryCatchDirective(
                tryBlockStartLabel,
                tryBlockEndLabel,
                catchBlockLabel,
                exceptionType,
                finallyBlockLabel
        );
        this.tryCatchDirectives.add(directive);
        return directive;
    }

    public void finaliseTryCatchDirectives(@NotNull MethodNode method)
    {
        if (this.tryCatchDirectives.isEmpty())
            return;  // トライキャッチディレクティブがない場合は何もしない

        this.context.postInfo("Finalising try-catch directives for method " + method.name + method.desc);
        for (TryCatchDirective directive : this.tryCatchDirectives)
        {
            LabelNode tryBlock = directive.tryBlockStartLabel().node();
            LabelNode tryEndBlock = directive.tryBlockEndLabel().node();
            TypeDescriptor exceptionType = directive.exceptionType();
            LabelNode catchBlock = directive.catchBlockLabel() == null ? null: directive.catchBlockLabel().node();
            LabelNode finallyBlock = directive.finallyBlockLabel() == null ? null: directive.finallyBlockLabel().node();

            // トライキャッチブロックをメソッドに追加
            method.tryCatchBlocks.add(new TryCatchBlockNode(
                    tryBlock,
                    tryEndBlock,
                    catchBlock,
                    exceptionType == null ? null: exceptionType.toString()
            ));
            // finally ブロックがある場合は、トライキャッチブロックに追加
            if (finallyBlock != null)
            {
                // finally ブロックは try-catch ブロックの後に追加される
                method.tryCatchBlocks.add(new TryCatchBlockNode(
                        tryBlock,
                        tryEndBlock,
                        finallyBlock,
                        null  // finally ブロックは例外型を持たない
                ));
            }
        }
    }
}
