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

/**
 * Holds and manages try-catch(-finally) directives for a method during compilation.
 * <p>
 * This class collects {@link TryCatchDirective} instances and finalizes them into ASM's
 * {@link TryCatchBlockNode} objects for a given {@link MethodNode}.
 * It supports both catch and finally blocks, and ensures correct mapping of labels and exception types.
 */
public class TryCatchDirectivesHolder
{
    /**
     * The compilation context for reporting and diagnostics.
     */
    private final FileEvaluatingReporter context;

    /**
     * The holder for label information used in try-catch directives.
     */
    private final LabelsHolder labels;

    /**
     * The list of try-catch(-finally) directives collected for the method.
     */
    private final List<TryCatchDirective> tryCatchDirectives;

    /**
     * Constructs a new TryCatchDirectivesHolder.
     *
     * @param ctxt   The compilation context for reporting.
     * @param labels The label holder for resolving label references.
     */
    public TryCatchDirectivesHolder(@NotNull FileEvaluatingReporter ctxt, @NotNull LabelsHolder labels)
    {
        this.context = ctxt;
        this.labels = labels;

        this.tryCatchDirectives = new ArrayList<>();
    }

    /**
     * Adds a new try-catch(-finally) directive to the holder.
     *
     * @param tryBlockStartLabel  The label where the try block starts.
     * @param tryBlockEndLabel    The label where the try block ends.
     * @param catchBlockLabel     The label where the catch block starts, or null if not present.
     * @param exceptionType       The type of exception to catch, or null for a finally block.
     * @param finallyBlockLabel   The label where the finally block starts, or null if not present.
     * @return The created {@link TryCatchDirective} instance.
     */
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

    /**
     * Finalizes all collected try-catch(-finally) directives and adds them to the given method.
     * <p>
     * This method converts each {@link TryCatchDirective} into one or more {@link TryCatchBlockNode}
     * objects and appends them to the {@code tryCatchBlocks} list of the provided {@link MethodNode}.
     * If a finally block is present, it is added as a separate try-catch block with a null exception type.
     *
     * @param method The ASM method node to which try-catch blocks will be added.
     */
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
