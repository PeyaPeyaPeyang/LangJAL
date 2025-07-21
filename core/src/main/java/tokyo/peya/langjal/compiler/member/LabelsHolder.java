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

public class LabelsHolder
{
    private final JALMethodCompiler methodEvaluator;
    private final List<LabelInfo> labels;

    @Getter
    private LabelInfo globalStart;
    @Getter
    private final LabelInfo globalEnd;

    @Getter
    @Setter
    private LabelInfo currentLabel; // 現在解析中の最後のラベル

    public LabelsHolder(@NotNull JALMethodCompiler methodEvaluator)
    {
        this.methodEvaluator = methodEvaluator;

        this.labels = new ArrayList<>();

        this.globalStart = this.currentLabel = new LabelInfo("MBEGIN", new Label(), 0);
        this.globalEnd = new LabelInfo("MEND", new Label(), 50);
    }

    public void setGlobalStart(@NotNull LabelInfo globalStart)
    {
        if (!this.globalStart.name().equals("MBEGIN"))
            throw new IllegalStateException("Global start label cannot be set after it has been initialized.");

        this.globalStart = globalStart;
        this.currentLabel = globalStart;  // グローバル開始ラベルを現在のラベルに設定
        // グローバル開始ラベルをメソッドに登録
    }

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

    private int currentInstructionIndex()
    {
        return this.methodEvaluator.getInstructions().getSize();
    }

    public boolean isInScope(@NotNull LabelInfo scopeStart, @NotNull LabelInfo scopeEnd)
    {
        return isInScope(scopeStart, scopeEnd, this.currentLabel);
    }

    public LabelInfo getNextBlock(@NotNull LabelInfo label)
    {
        int currentIndex = label.instructionIndex();
        for (LabelInfo nextLabel : this.labels)
            if (nextLabel.instructionIndex() > currentIndex)
                return nextLabel;  // 次のラベルを返す
        return null;  // 次のラベルが見つからない場合は null を返す
    }

    public void finalise(@NotNull MethodNode method)
    {
        LabelNode globalEndNode = this.globalEnd.node();
        method.instructions.add(globalEndNode);
        this.labels.add(this.globalEnd);  // グローバル終了ラベルも登録
        // ↑ END なので，いっちゃんさいご
    }

    public void registerGlobalStart(@NotNull MethodNode method)
    {
        LabelNode globalStartNode = this.globalStart.node();
        method.instructions.add(globalStartNode);
        this.labels.add(this.globalStart);  // グローバル開始ラベルも登録
    }

    @NotNull
    public List<LabelInfo> getLabels()
    {
        return Collections.unmodifiableList(this.labels);
    }

    @Nullable
    public LabelInfo getLabelByNode(@NotNull LabelNode targetNode)
    {
        for (LabelInfo label : this.labels)
            if (label.node().equals(targetNode))
                return label;  // ラベルが見つかったら返す

        return null;  // 見つからなかった場合は null を返す
    }

    public static boolean isInScope(@NotNull LabelInfo scopeStart, @NotNull LabelInfo scopeEnd,
                                    @NotNull LabelInfo currentLabel)
    {
        int startIndex = scopeStart.instructionIndex();
        int endIndex = scopeEnd.instructionIndex();
        int currentIndex = currentLabel.instructionIndex();

        return currentIndex >= startIndex && currentIndex <= endIndex;
    }
}
