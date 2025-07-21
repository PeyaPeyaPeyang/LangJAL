package tokyo.peya.langjal.analyser;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.analyser.stack.LocalStackElement;
import tokyo.peya.langjal.analyser.stack.StackElement;
import tokyo.peya.langjal.compiler.member.LabelInfo;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StackFrameMapCreator
{
    private final FileEvaluatingReporter context;
    private final MethodNode method;

    private final Map<LabelInfo, InstructionSetFrame> frames;

    public StackFrameMapCreator(@NotNull FileEvaluatingReporter context, @NotNull MethodNode method)
    {
        this.context = context;
        this.method = method;

        this.frames = new HashMap<>();
    }

    public void updateFrames(@NotNull FramePropagation[] propagations)
    {
        this.context.postInfo("Updating stack frames for method: " + this.method.name);
        for (FramePropagation propagation : propagations)
            this.updateFrame(propagation);
        this.context.postInfo("Finished updating stack frames for method: " + this.method.name);
    }

    public void updateFrame(@NotNull FramePropagation propagation)
    {
        LabelInfo label = propagation.receiver();  // StacKFrameMap はジャンプ先に貼っつけるため。

        InstructionSetFrame newFrame = new InstructionSetFrame(
                label,
                propagation.stack(),
                propagation.locals()
        );

        // 同じものがなかったら何もせずに，マップに入れる。
        if (!this.frames.containsKey(label))
        {
            this.printFrame(newFrame);
            this.frames.put(label, newFrame);
            return;
        }

        this.context.postInfo("Merging frame at " + label.name() + " with existing frame.");

        // 既に同じものがあったら，スタックとローカル変数をマージする。
        InstructionSetFrame existingFrame = this.frames.get(label);
        InstructionSetFrame mergedFrame = mergeFrames(existingFrame, newFrame);

        this.context.postInfo("Merged frame at " + label.name() + ":");
        this.printFrame(mergedFrame);

        this.frames.put(label, mergedFrame);
    }

    public StackFrameMapEntry[] createStackFrameMap()
    {
        this.context.postInfo("Creating stack frame map for method: " + this.method.name);
        // フレームをラベルのインデックス順にする
        List<InstructionSetFrame> frames =
                this.frames.values().stream()
                           .sorted(Comparator.comparingInt(frame -> frame.label().instructionIndex()))
                           .toList();
        this.printFrames(frames);
        if (frames.size() < 2)
        {
            this.context.postInfo("No need to compute frames, StackFrameMap will be empty.");
            return new StackFrameMapEntry[0];
        }

        StackFrameMapEntry[] stackFrameMap = new StackFrameMapEntry[frames.size() - 1];
        // 各フレームの次のフレームを計算する
        for (int i = 0; i < frames.size() - 1; i++)
        {
            InstructionSetFrame previous = frames.get(i);
            InstructionSetFrame next = frames.get(i + 1);
            StackFrameMapEntry nextFrame = computeNextFrame(previous, next);
            stackFrameMap[i] = nextFrame;
        }

        this.context.postInfo("Stack frame map created with " + stackFrameMap.length + " entries.");
        return stackFrameMap;
    }

    private StackFrameMapEntry computeNextFrame(@NotNull InstructionSetFrame previous,
                                                @NotNull InstructionSetFrame next)
    {
        StackElement[] previousStack = previous.stack();
        StackElement[] nextStack = next.stack();

        LocalStackElement[] previousLocals = previous.locals();
        LocalStackElement[] nextLocals = next.locals();

        // スタックとローカル変数が同じならば、同じエントリを返す。
        boolean isStackSame = isSameStack(previousStack, nextStack);
        boolean isLocalsSame = isSameStack(previousLocals, nextLocals);
        if (isStackSame && isLocalsSame)
            return StackFrameMapEntry.same(previous, next);

        if (isLocalsSame)  // スタックのみ変わった場合
        {
            // same_locals_1_stack_item かどうか？（スタックに１つのアイテムのみが存在するか）
            if (nextStack.length == 1)
            {
                StackElement stackItem = nextStack[0];
                return StackFrameMapEntry.sameLocals1StackItem(previous, next, stackItem);
            }
        }

        if (nextStack.length != 0)
        {
            // スタックが空ではない＆何らかの変更がある -> FULL フレーム
            return StackFrameMapEntry.full(previous, next, nextStack, nextLocals);
        }

        // スタックが空の場合は, ローカル変数について CHOP/APPEND が使える
        int chopCount = previousLocals.length - nextLocals.length;  // 末尾に TOP がないことは確認済み
        if (chopCount > 0)
        {
            // CHOP フレーム
            if (chopCount <= 3)  // 最大で 3 個まで CHOP できる
            {
                LocalStackElement[] choppedLocals = new LocalStackElement[chopCount];
                System.arraycopy(
                        previousLocals,
                        previousLocals.length - chopCount,
                        choppedLocals,
                        0,
                        chopCount
                );
                return StackFrameMapEntry.chop(previous, next, choppedLocals);
            }
        }
        else if (chopCount < 0)  // APPEND フレーム
        {
            int appendCount = -chopCount;  // 負の値を正に変換
            if (appendCount <= 3)  // 最大で 3 個まで APPEND できる
            {
                LocalStackElement[] appendedLocals = new LocalStackElement[appendCount];
                System.arraycopy(
                        nextLocals,
                        nextLocals.length - appendCount,
                        appendedLocals,
                        0,
                        appendCount
                );
                return StackFrameMapEntry.append(previous, next, appendedLocals);
            }
        }

        // どうしようもない場合は FULL フレーム
        return StackFrameMapEntry.full(previous, next, nextStack, nextLocals);
    }

    private void printFrames(List<InstructionSetFrame> frames)
    {
        this.context.postInfo("----- Stack Frames of " + this.method.name + " -----");
        if (this.frames.isEmpty())
        {
            this.context.postInfo("No stack frames found.");
            return;
        }
        for (InstructionSetFrame frame : frames)
            this.printFrame(frame);
    }

    private void printFrame(@NotNull InstructionSetFrame frame)
    {
        this.context.postInfo(
                "--- Frame at " + this.method.name + ":" + frame.label().name() + " ---" +
                        "Stack: " + StackElementUtils.stackToString(frame.stack())
                        + ", Locals: " + StackElementUtils.stackToString(frame.locals())
        );
    }

    private static boolean isSameStack(@NotNull StackElement[] stack1, @NotNull StackElement[] stack2, int max)
    {
        // 先頭 max 要素まで比較。比較対象が足りなければ false。
        if (stack1.length < max || stack2.length < max)
            return false;

        for (int i = 0; i < max; i++)
            if (!stack1[i].equals(stack2[i]))
                return false;

        return true;
    }

    private static boolean isSameStack(@NotNull StackElement[] stack1, @NotNull StackElement[] stack2)
    {
        if (stack1.length != stack2.length)
            return false;
        return isSameStack(stack1, stack2, stack1.length);
    }

    private static InstructionSetFrame mergeFrames(@NotNull InstructionSetFrame frame1,
                                                   @NotNull InstructionSetFrame frame2)
    {
        StackElement[] mergedStack = StackElementUtils.mergeStack(
                frame1.label(),
                frame1.stack(),
                frame2.stack()
        );
        LocalStackElement[] mergedLocals = StackElementUtils.mergeLocals(frame1.locals(), frame2.locals());
        StackElementUtils.cleanUpLocals(mergedLocals);

        return new InstructionSetFrame(frame1.label(), mergedStack, mergedLocals);
    }
}
