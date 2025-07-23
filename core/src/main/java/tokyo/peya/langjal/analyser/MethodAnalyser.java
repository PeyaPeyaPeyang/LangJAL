package tokyo.peya.langjal.analyser;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.analyser.stack.LocalStackElement;
import tokyo.peya.langjal.analyser.stack.StackElement;
import tokyo.peya.langjal.analyser.stack.TopElement;
import tokyo.peya.langjal.analyser.stack.UninitializedThisElement;
import tokyo.peya.langjal.compiler.instructions.InstructionEvaluatorNop;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.InstructionsHolder;
import tokyo.peya.langjal.compiler.member.LabelInfo;
import tokyo.peya.langjal.compiler.member.LabelsHolder;
import tokyo.peya.langjal.compiler.member.LocalVariableInfo;
import tokyo.peya.langjal.compiler.member.LocalVariablesHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Analyses a JVM method's instructions, stack frames, and control flow for bytecode verification.
 * <p>
 * This class simulates stack and local variable changes, propagates frames,
 * and computes maximum stack and local sizes for the method.
 * <br>
 * <b>Usage Example:</b>
 * <pre>{@code
 * MethodAnalyser analyser = new MethodAnalyser(context, classNode, methodNode, instructions, labels, locals);
 * MethodAnalysisResult result = analyser.analyse();
 * System.out.println("Max stack: " + result.maxStack());
 * }</pre>
 */
public class MethodAnalyser
{
    private final FileEvaluatingReporter context;
    private final MethodNode method;
    private final InstructionsHolder instructions;
    private final LabelsHolder labels;
    private final LocalVariablesHolder locals;
    private final InstructionInfo nop;

    private final List<InstructionSetAnalyser> analysers;
    private final List<FramePropagation> pendingPropagations;
    private final List<FramePropagation> confirmedPropagations;

    private int maxStackSize;
    private int maxLocalSize;

    public MethodAnalyser(@NotNull FileEvaluatingReporter context,
                          @NotNull ClassNode ownerClazz,
                          @NotNull MethodNode method,
                          @NotNull InstructionsHolder instructions,
                          @NotNull LabelsHolder labels,
                          @NotNull LocalVariablesHolder locals)
    {
        this.context = context;
        this.method = method;
        this.instructions = instructions;
        this.labels = labels;
        this.locals = locals;
        this.nop = new InstructionInfo(
                new InstructionEvaluatorNop(),
                ownerClazz,
                method,
                EOpcodes.NOP,
                0,
                null,
                0,
                -1
        );
        this.analysers = new ArrayList<>();
        this.pendingPropagations = new ArrayList<>();
        this.confirmedPropagations = new ArrayList<>();
    }

    /**
     * Analyses the method and returns the analysis result, including frame propagations and max sizes.
     * @return The method analysis result.
     */
    public MethodAnalysisResult analyse()
    {
        this.context.postInfo("Analysing method: " + this.method.name + " in class: " + this.method.desc);
        this.pendingPropagations.clear();
        this.createAnalysers();
        if (this.analysers.isEmpty())
        {
            this.context.postInfo("There are no instruction sets to analyse in method: " + this.method.name);
            return MethodAnalysisResult.empty(this.method);  // インストラクションセットがない場合は空の結果を返す
        }
        this.printAnalyseTargets();

        // 最初に渡すグローバル開始ラベルの伝播を作成
        FramePropagation firstPropagation = this.createFirstPropagation(this.labels.getGlobalStart());
        this.pendingPropagations.add(firstPropagation);

        // 各インストラクション・セットのスタックとローカル変数の動きを解析
        this.analyseLoop();

        // 分析が完了したら，結果を返答
        return new MethodAnalysisResult(
                this.method,
                this.confirmedPropagations.toArray(new FramePropagation[0]),
                this.maxStackSize,
                this.maxLocalSize
        );
    }

    private void analyseLoop()
    {
        long startTime = System.currentTimeMillis();
        long iterationCount = 0;
        while (!this.pendingPropagations.isEmpty())
        {
            iterationCount++;
            if (iterationCount % 5 == 0)
            {
                long elapsedTime = System.currentTimeMillis() - startTime;
                this.context.postInfo("Processing propagation: " + iterationCount +
                                              ", Pending: " + this.pendingPropagations.size() +
                                              ", Elapsed: " + elapsedTime + "ms");
            }

            FramePropagation propagation = this.pendingPropagations.remove(0);
            LabelInfo receiver = propagation.receiver();

            if (receiver == this.labels.getGlobalEnd())
            {
                this.context.postInfo("Reached global end label, stopping analysis for branch: " + propagation.sender()
                                                                                                              .name());
                continue;  // グローバル終了ラベルに到達した場合、分析を停止
            }

            this.analysePropagation(propagation);
        }

        long elapsedTime = System.currentTimeMillis() - startTime;
        this.context.postInfo("Analysis completed for method: " + this.method.name +
                                      ", Total iterations: " + iterationCount +
                                      ", Max stack size: " + this.maxStackSize +
                                      ", Max local size: " + this.maxLocalSize +
                                      ", Elapsed time: " + elapsedTime + "ms");
    }

    private void analysePropagation(@NotNull FramePropagation propagation)
    {
        LabelInfo sender = propagation.sender();
        LabelInfo receiver = propagation.receiver();
        this.context.postInfo("Analysing propagation for jump " + sender.name() + " -> " + receiver.name() +
                                      ", Stack size: " + propagation.stack().length +
                                      ", Local size: " + propagation.locals().length);

        for (InstructionSetAnalyser analyser : this.analysers)
        {
            if (!analyser.getLabel().equals(receiver))  // 該当するインストラクション・セットを探す
                continue;

            InstructionSetAnalysisResult analysisResult = analyser.analyse(propagation);
            this.confirmedPropagations.add(propagation);  // 現在の伝播を処理済み
            this.updateMaxes(analysisResult);
            for (FramePropagation nextPropagation : analysisResult.framePropagations())
            {
                if (this.checkConfirmedPropagation(nextPropagation))
                {
                    this.context.postInfo("New propagation found: " + nextPropagation);
                    this.pendingPropagations.add(nextPropagation);  // 新しい伝播を追加
                }
            }
        }
    }

    private boolean checkConfirmedPropagation(@NotNull FramePropagation propagation)
    {
        Iterator<FramePropagation> iterator = this.confirmedPropagations.iterator();
        while (iterator.hasNext())
        {
            FramePropagation confirmed = iterator.next();
            if (confirmed.sender().equals(propagation.sender()) &&
                    confirmed.receiver().equals(propagation.receiver()))
            {
                // 既に同じ送信元と受信先の伝播が存在する場合、スタックとローカル変数を比較
                if (Arrays.equals(confirmed.stack(), propagation.stack()) &&
                        Arrays.equals(confirmed.locals(), propagation.locals()))
                    return false;  // 同じスタックとローカル変数の組み合わせが既に存在する
                else

                {
                    this.context.postInfo("Found existing propagation with different stack/locals: " + confirmed);
                    // 既存の伝播と異なるスタックやローカル変数がある場合、更新する
                    iterator.remove();  // 古い伝播を削除
                    return true;  // 新しい伝播を追加する必要がある
                }
            }
        }

        // 既存の伝播に同じ送信元と受信先がない場合、新しい伝播として追加
        return true;
    }

    private void printAnalyseTargets()
    {
        this.context.postInfo("Analysing the following instruction sets in method: " + this.method.name);
        for (InstructionSetAnalyser analyser : this.analysers)
            this.context.postInfo(" - Name: " + analyser.getLabel().name() +
                                          ", Instructions: " + analyser.getInstructions().size());
    }

    private void updateMaxes(@NotNull InstructionSetAnalysisResult analysisResult)
    {
        this.maxStackSize = Math.max(this.maxStackSize, analysisResult.maxStackSize());
        this.maxLocalSize = Math.max(this.maxLocalSize, analysisResult.maxLocalSize());
    }

    private FramePropagation createFirstPropagation(@NotNull LabelInfo receiver)
    {
        LabelInfo sender = this.labels.getGlobalStart();
        StackElement[] stack = new StackElement[0];  // グローバル開始時点ではスタックは空

        LocalVariableInfo[] locals = this.locals.getParameters();
        LocalStackElement[] localStack = this.createLocalStack(locals);
        if (localStack.length == 0)
            this.context.postInfo("No local variables found for method: " + this.method.name);

        return new FramePropagation(
                sender,
                new AnalysedInstruction[0],
                receiver,
                stack,
                localStack,
                stack.length,
                localStack.length  // ローカル変数のスロット数を最大ローカルサイズとして設定
        );
    }

    private LocalStackElement[] createLocalStack(@NotNull LocalVariableInfo[] locals)
    {
        // ローカル変数のスロットサイズはカテゴリ（1, 2）で変わる。
        int slotSize = Arrays.stream(locals)
                             .mapToInt(localInfo -> localInfo.index() + localInfo.type().getBaseType().getCategory())
                             .max()
                             .orElse(0);


        // locals を index でソート
        Arrays.sort(locals, Comparator.comparingInt(LocalVariableInfo::index));
        List<LocalVariableInfo> pendingLocals = new ArrayList<>(Arrays.asList(locals));

        boolean isInitialiseMethod = this.method.name.equals("<init>");
        LocalStackElement[] localStack = new LocalStackElement[slotSize];
        TopElement top = new TopElement(this.nop);
        for (int i = 0; i < slotSize; i++)
        {
            if (pendingLocals.isEmpty() || pendingLocals.get(0).index() > i)
            {
                localStack[i] = new LocalStackElement(this.nop, i, top);
                continue;
            }

            LocalVariableInfo local = pendingLocals.remove(0);
            TypeDescriptor type = local.type();

            if (isInitialiseMethod && local.index() == 0)
            {
                // 初期化メソッドのthis参照はUninitializedThisElementで埋める
                localStack[i] = new LocalStackElement(this.nop, 0, new UninitializedThisElement(this.nop));
                continue;
            }

            StackElement elem = type.toStackElement(this.nop);
            localStack[i] = new LocalStackElement(this.nop, local.index(), elem);

            // 2スロット型の場合は次のスロットをTopElementで埋める
            if (type.getBaseType().getCategory() == 2)
            {
                i++;
                if (i < slotSize)
                    localStack[i] = new LocalStackElement(this.nop, i, top);
            }
        }

        return localStack;
    }

    private void createAnalysers()
    {
        for (LabelInfo label : this.labels.getLabels())  // アナライザは，１インストラクションセット（ラベル区切り）で管理
        {
            if (label == this.labels.getGlobalEnd())
                continue;  // これはグローバル終了ラベルなのでスキップ
            List<InstructionInfo> instructions = this.instructions.getInstructions(label);
            if (instructions.isEmpty())
            {
                this.context.postInfo(String.format(
                        "No instructions found for label: %s in method: %s, skipping.",
                        label.name(),
                        this.method.name
                ));
                continue;
            }

            InstructionSetAnalyser analyser = new InstructionSetAnalyser(
                    this.context,
                    this.labels,
                    label,
                    instructions
            );
            this.analysers.add(analyser);
        }
    }
}
