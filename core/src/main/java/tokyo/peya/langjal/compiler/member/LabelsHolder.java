package tokyo.peya.langjal.compiler.member;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.exceptions.UnknownLabelException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Manages labels within a method, including registration, resolution, and scope checks.
 * Provides access to global start/end labels and supports label-based instruction grouping.
 * <p>
 * Methods managed by this holder have a global start label (<code>MBEGIN</code>) and a global end label (<code>MEND</code>).
 * When a method explicitly defines a label as its start, it will be registered as the global start label,
 * replacing the default <code>MBEGIN</code>.
 */
public class LabelsHolder
{
    /**
     * List of all labels registered in this holder.
     */
    private final List<LabelInfo> labels;

    /**
     * The global start label for the method.
     */
    @Getter
    private LabelInfo globalStart;
    /**
     * The global end label for the method.
     */
    @Getter
    private final LabelInfo globalEnd;

    /**
     * The current label being processed.
     */
    @Getter
    @Setter
    private LabelInfo currentLabel; // 現在解析中の最後のラベル

    /**
     * Creates a new LabelsHolder with default global start and end labels.
     */
    public LabelsHolder()
    {
        this.labels = new ArrayList<>();

        this.globalStart = this.currentLabel = new LabelInfo("MBEGIN", new Label(), 0);
        this.globalEnd = new LabelInfo("MEND", new Label(), 50);
    }

    /**
     * Sets the global start label.
     *
     * @param globalStart The label to set as global start.
     * @throws IllegalStateException If already initialized.
     */
    public void setGlobalStart(@NotNull LabelInfo globalStart)
    {
        if (!this.globalStart.name().equals("MBEGIN"))
            throw new IllegalStateException("Global start label cannot be set after it has been initialized.");

        this.globalStart = globalStart;
        this.currentLabel = globalStart;  // グローバル開始ラベルを現在のラベルに設定
        // グローバル開始ラベルをメソッドに登録
    }

    /**
     * Resolves a label by its parser context, throwing if not found.
     *
     * @param labelName The label name context.
     * @return The resolved LabelInfo.
     * @throws UnknownLabelException If not found.
     */
    @NotNull
    public LabelInfo resolve(@NotNull JALParser.LabelNameContext labelName)
    {
        LabelInfo resolvedLabel = this.resolveSafe(labelName.getText());
        if (resolvedLabel == null)
            throw new UnknownLabelException(
                    "Label '" + labelName.getText() + "' is not defined in this method.",
                    labelName.getText(),
                    labelName
            );

        return resolvedLabel;  // すでに登録されているラベルを返す
    }

    /**
     * Resolves a label by its name, returning null if not found.
     *
     * @param labelName The label name.
     * @return The resolved LabelInfo or null.
     */
    @Nullable
    public LabelInfo resolveSafe(@NotNull String labelName)
    {
        for (LabelInfo existingLabel : this.labels)
        {
            if (existingLabel.name().equals(labelName))
                return existingLabel;  // すでに登録されているラベルを返す
        }

        return null;  // ラベルが見つからない場合は null を返す
    }

    /**
     * Registers a new label at the given instruction index.
     *
     * @param labelName        The label name context.
     * @param instructionIndex The instruction index.
     * @return The registered LabelInfo.
     * @throws UnknownLabelException If already defined.
     */
    @NotNull
    public LabelInfo register(@NotNull JALParser.LabelNameContext labelName, int instructionIndex)
    {
        LabelInfo existingLabel = this.resolveSafe(labelName.getText());
        if (existingLabel != null)
            throw new UnknownLabelException(
                    "Label '" + labelName.getText() + "' is already defined in this method.",
                    labelName.getText(),
                    labelName
            );

        // 新しいラベルを登録
        Label newLabel = new Label();
        LabelInfo labelInfo = new LabelInfo(labelName.getText(), newLabel, instructionIndex);
        this.labels.add(labelInfo);
        this.labels.sort(Comparator.comparingInt(LabelInfo::instructionIndex));

        // メソッドへの登録はあと
        return labelInfo;
    }

    /**
     * Imports an ASM label node and registers it in the method.
     * This method is used to convert ASM's LabelNode into a LabelInfo
     * and register it in the LabelsHolder.
     * <p>
     * Each label is associated with a random-generated name like <code>L123456</code> which is identifiable and unique within the method.
     * @param asmLabelNode The ASM label node to import.
     * @param instructionIndex The instruction index where the label is defined.
     * @return The registered LabelInfo.
     */
    public LabelInfo importASMLabel(@NotNull LabelNode asmLabelNode, int instructionIndex)
    {
        // ASMのラベルノードからラベル情報を登録
        LabelInfo existingLabel = this.getLabelByNode(asmLabelNode);
        if (existingLabel != null)
            throw new IllegalStateException("Label '" + asmLabelNode.getLabel() + "' is already defined in the method.");

        // 新しいラベルを登録
        String labelName = asmLabelNode.getLabel().toString();  // ラベル名を取得
        Label newLabel = asmLabelNode.getLabel();
        LabelInfo labelInfo = new LabelInfo(labelName, newLabel, instructionIndex);
        this.labels.add(labelInfo);
        this.labels.sort(Comparator.comparingInt(LabelInfo::instructionIndex));

        return labelInfo;  // 登録したラベル情報を返す
    }

    /**
     * Checks if a label is in scope between two labels.
     *
     * @param scopeStart The start label.
     * @param scopeEnd   The end label.
     * @return True if current label is in scope.
     */
    public boolean isInScope(@NotNull LabelInfo scopeStart, @NotNull LabelInfo scopeEnd)
    {
        return isInScope(scopeStart, scopeEnd, this.currentLabel);
    }

    /**
     * Gets the next label block after the given label.
     *
     * @param label The current label.
     * @return The next LabelInfo or null.
     */
    public LabelInfo getNextBlock(@NotNull LabelInfo label)
    {
        int currentIndex = label.instructionIndex();
        for (LabelInfo nextLabel : this.labels)
            if (nextLabel.instructionIndex() > currentIndex)
                return nextLabel;  // 次のラベルを返す
        return null;  // 次のラベルが見つからない場合は null を返す
    }

    /**
     * Finalizes labels by adding the global end label to the method.
     *
     * @param method The method node.
     */
    public void finalise(@NotNull MethodNode method)
    {
        LabelNode globalEndNode = this.globalEnd.node();
        method.instructions.add(globalEndNode);
        this.labels.add(this.globalEnd);  // グローバル終了ラベルも登録
        // ↑ END なので，いっちゃんさいご
    }

    /**
     * Registers the global start label in the method.
     *
     * @param method The method node.
     */
    public void registerGlobalStart(@NotNull MethodNode method)
    {
        LabelNode globalStartNode = this.globalStart.node();
        method.instructions.add(globalStartNode);
        this.labels.add(this.globalStart);  // グローバル開始ラベルも登録
    }

    /**
     * Returns an unmodifiable list of all labels.
     *
     * @return List of LabelInfo.
     */
    @NotNull
    public List<LabelInfo> getLabels()
    {
        return Collections.unmodifiableList(this.labels);
    }

    /**
     * Gets a label by its ASM node.
     *
     * @param targetNode The label node.
     * @return The LabelInfo or null.
     */
    @Nullable
    public LabelInfo getLabelByNode(@NotNull LabelNode targetNode)
    {
        Label targetLabel = targetNode.getLabel();

        for (LabelInfo label : this.labels)
        {
            LabelNode node = label.node();
            Label asmLabel = label.label();
            if (node.getLabel() == targetLabel
                    || asmLabel == targetLabel
                    || targetLabel.toString().equals(node.getLabel().toString()))
                return label;  // ラベルノードが一致するものを返す
        }

        return null;  // 見つからなかった場合は null を返す
    }

    /**
     * Checks if a label is in scope between two labels.
     *
     * @param scopeStart   The start label.
     * @param scopeEnd     The end label.
     * @param currentLabel The label to check.
     * @return True if in scope.
     */
    public static boolean isInScope(@NotNull LabelInfo scopeStart, @NotNull LabelInfo scopeEnd,
                                    @NotNull LabelInfo currentLabel)
    {
        int startIndex = scopeStart.instructionIndex();
        int endIndex = scopeEnd.instructionIndex();
        int currentIndex = currentLabel.instructionIndex();

        return currentIndex >= startIndex && currentIndex <= endIndex;
    }
}
