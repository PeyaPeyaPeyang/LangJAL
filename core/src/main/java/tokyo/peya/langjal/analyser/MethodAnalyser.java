package tokyo.peya.langjal.analyser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.*;
import tokyo.peya.langjal.analyser.stack.*;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.instructions.InstructionEvaluatorNop;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.member.*;

import java.util.*;

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
public class MethodAnalyser {
    private final FileEvaluatingReporter context;
    private final MethodNode method;
    private final InstructionsHolder instructions;
    private final LabelsHolder labels;
    private final LocalVariablesHolder locals;
    private final InstructionInfo nop;

    private final List<InstructionSetAnalyser> analysers;
    private final List<FramePropagation> pendingPropagations;
    private final Map<FramePropagation, InstructionSetAnalysisResult> confirmedAnalysisResults;
    private final Map<LabelInfo, BitSet> liveLocalsAtEntry;

    private int maxStackSize;
    private int maxLocalSize;

    public MethodAnalyser(@NotNull FileEvaluatingReporter context,
                          @NotNull ClassNode ownerClazz,
                          @NotNull MethodNode method,
                          @NotNull InstructionsHolder instructions,
                          @NotNull LabelsHolder labels,
                          @NotNull LocalVariablesHolder locals) {
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
        this.confirmedAnalysisResults = new HashMap<>();
        this.liveLocalsAtEntry = new HashMap<>();
    }

    @Nullable
    private static InstructionSetAnalyser createAnalyser(
            @NotNull FileEvaluatingReporter context,
            @NotNull LabelsHolder labels,
            @NotNull LabelInfo label,
            @NotNull List<InstructionInfo> instructions,
            @NotNull Map<LabelInfo, BitSet> liveLocalsAtEntry) {
        return instructions.isEmpty()
                ? null
                : new InstructionSetAnalyser(context, labels, label, instructions, liveLocalsAtEntry);
    }

    /**
     * Analyses the method and returns the analysis result, including frame propagations and max sizes.
     *
     * @return The method analysis result.
     */
    public MethodAnalysisResult analyse() {
        this.context.postInfo("Analysing method: " + this.method.name + " in class: " + this.method.desc);

        // Make analyse() re-entrant: it can be called multiple times on the same instance.
        this.analysers.clear();
        this.pendingPropagations.clear();
        this.confirmedAnalysisResults.clear();
        this.maxStackSize = 0;
        this.maxLocalSize = 0;

        this.createAnalysers();
        if (this.analysers.isEmpty()) {
            this.context.postInfo("There are no instruction sets to analyse in method: " + this.method.name);
            return MethodAnalysisResult.empty(this.method);  // インストラクションセットがない場合は空の結果を返す
        }
        this.printAnalyseTargets();

        // 最初に渡すグローバル開始ラベルの伝播を作成
        FramePropagation firstPropagation = this.createFirstPropagation(this.labels.getGlobalStart());
        // 初期パラメータのローカル変数のスロット数を最大ローカルサイズとして設定
        this.maxStackSize = Math.max(this.maxStackSize, firstPropagation.maxStackSize());
        this.maxLocalSize = Math.max(this.maxLocalSize, firstPropagation.maxLocalSize());
        this.pendingPropagations.add(firstPropagation);
        this.pendingPropagations.addAll(this.createExceptionHandlerPropagations(firstPropagation));

        // 各インストラクション・セットのスタックとローカル変数の動きを解析
        this.analyseLoop();
        this.maxLocalSize = Math.max(this.maxLocalSize, this.locals.getMaxLocalSize());

        // 分析が完了したら，結果を返答
        return new MethodAnalysisResult(
                this.method,
                this.confirmedAnalysisResults.keySet().toArray(new FramePropagation[0]),
                this.confirmedAnalysisResults.values().toArray(new InstructionSetAnalysisResult[0]),
                this.maxStackSize,
                this.maxLocalSize
        );
    }

    private void analyseLoop() {
        long startTime = System.currentTimeMillis();
        long iterationCount = 0;
        while (!this.pendingPropagations.isEmpty()) {
            iterationCount++;
            if (iterationCount % 5 == 0) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                this.context.postInfo("Processing propagation: " + iterationCount +
                        ", Pending: " + this.pendingPropagations.size() +
                        ", Elapsed: " + elapsedTime + "ms");
            }

            FramePropagation propagation = this.pendingPropagations.removeFirst();
            LabelInfo receiver = propagation.receiver();

            if (receiver == this.labels.getGlobalEnd()) {
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

    private void analysePropagation(@NotNull FramePropagation propagation) {
        LabelInfo sender = propagation.sender();
        LabelInfo receiver = propagation.receiver();
        this.context.postInfo("Analysing propagation for jump " + sender.name() + " -> " + receiver.name() +
                ", Stack size: " + propagation.stack().length +
                ", Local size: " + propagation.locals().length);

        for (InstructionSetAnalyser analyser : this.analysers) {
            if (!analyser.getLabel().equals(receiver))  // 該当するインストラクション・セットを探す
                continue;

            InstructionSetAnalysisResult analysisResult = analyser.analyse(propagation);
            this.confirmedAnalysisResults.put(propagation, analysisResult);  // 分析結果を確定
            this.updateMaxes(analysisResult);
            for (FramePropagation nextPropagation : analysisResult.framePropagations()) {
                if (this.checkConfirmedPropagation(nextPropagation)) {
                    this.context.postInfo("New propagation found: " + nextPropagation);
                    this.pendingPropagations.add(nextPropagation);  // 新しい伝播を追加
                }
            }
            break;
        }
    }

    private boolean checkConfirmedPropagation(@NotNull FramePropagation propagation) {
        Iterator<FramePropagation> iterator = this.confirmedAnalysisResults.keySet().iterator();
        while (iterator.hasNext()) {
            FramePropagation confirmed = iterator.next();
            if (confirmed.sender().equals(propagation.sender()) &&
                    confirmed.receiver().equals(propagation.receiver())) {
                // 既に同じ送信元と受信先の伝播が存在する場合、スタックとローカル変数を比較
                if (Arrays.equals(confirmed.stack(), propagation.stack()) &&
                        Arrays.equals(confirmed.locals(), propagation.locals()))
                    return false;  // 同じスタックとローカル変数の組み合わせが既に存在する
                else {
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

    private void printAnalyseTargets() {
        this.context.postInfo("Analysing the following instruction sets in method: " + this.method.name);
        for (InstructionSetAnalyser analyser : this.analysers)
            this.context.postInfo(" - Name: " + analyser.getLabel().name() +
                    ", Instructions: " + analyser.getInstructions().size());
    }

    private void updateMaxes(@NotNull InstructionSetAnalysisResult analysisResult) {
        this.maxStackSize = Math.max(this.maxStackSize, analysisResult.maxStackSize());
        this.maxLocalSize = Math.max(this.maxLocalSize, analysisResult.maxLocalSize());
    }

    private FramePropagation createFirstPropagation(@NotNull LabelInfo receiver) {
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

    private @NotNull List<FramePropagation> createExceptionHandlerPropagations(
            @NotNull FramePropagation firstPropagation) {
        if (this.method.tryCatchBlocks == null || this.method.tryCatchBlocks.isEmpty())
            return List.of();

        List<FramePropagation> propagations = new ArrayList<>();
        for (TryCatchBlockNode tryCatchBlock : this.method.tryCatchBlocks) {
            LabelInfo handler = this.labels.getLabelByNode(tryCatchBlock.handler);
            if (handler == null)
                continue;

            String exceptionTypeClassName = tryCatchBlock.type == null ? "java/lang/Throwable" : tryCatchBlock.type;
            TypeDescriptor exceptionType = TypeDescriptor.className(exceptionTypeClassName);
            StackElement[] stack = { exceptionType.toStackElement(this.nop) };
            LocalStackElement[] locals = StackElementUtils.filterDeadLocals(
                    firstPropagation.locals(),
                    this.liveLocalsAtEntry.get(handler)
            );
            propagations.add(new FramePropagation(
                    this.labels.getGlobalStart(),
                    new AnalysedInstruction[0],
                    handler,
                    stack,
                    locals,
                    stack.length,
                    locals.length
            ));
        }

        return propagations;
    }

    private LocalStackElement[] createLocalStack(@NotNull LocalVariableInfo[] locals) {
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
        for (int i = 0; i < slotSize; i++) {
            if (pendingLocals.isEmpty() || pendingLocals.getFirst().index() > i) {
                localStack[i] = new LocalStackElement(this.nop, i, top);
                continue;
            }

            LocalVariableInfo local = pendingLocals.removeFirst();
            TypeDescriptor type = local.type();

            if (isInitialiseMethod && local.index() == 0) {
                // 初期化メソッドのthis参照はUninitializedThisElementで埋める
                localStack[i] = new LocalStackElement(this.nop, 0, new UninitializedThisElement(this.nop));
                continue;
            }

            StackElement elem = type.toStackElement(this.nop);
            localStack[i] = new LocalStackElement(this.nop, local.index(), elem, local.isParameter());

            // 2スロット型の場合は次のスロットをTopElementで埋める
            if (type.getBaseType().getCategory() == 2) {
                i++;
                if (i < slotSize)
                    localStack[i] = new LocalStackElement(this.nop, i, top);
            }
        }

        return localStack;
    }

    private void createAnalysers() {
        for (LabelInfo label : this.labels.getLabels())  // アナライザは，１インストラクションセット（ラベル区切り）で管理
        {
            if (label == this.labels.getGlobalEnd())
                continue;  // これはグローバル終了ラベルなのでスキップ
            List<InstructionInfo> instructions = this.instructions.getInstructions(label);

            InstructionSetAnalyser analyser = createAnalyser(
                    this.context,
                    this.labels,
                    label,
                    instructions,
                    this.liveLocalsAtEntry
            );
            if (analyser == null) {
                this.context.postInfo(String.format(
                        "No instructions found for label: %s, creating empty analyser.",
                        label.name()
                ));
                continue;
            }

            this.analysers.add(analyser);
        }

        this.computeLiveLocalsAtEntry();
    }

    private void computeLiveLocalsAtEntry() {
        this.liveLocalsAtEntry.clear();
        for (InstructionSetAnalyser analyser : this.analysers)
            this.liveLocalsAtEntry.put(analyser.getLabel(), new BitSet());

        boolean updated;
        do {
            updated = false;
            for (int i = this.analysers.size() - 1; i >= 0; i--) {
                InstructionSetAnalyser analyser = this.analysers.get(i);
                BitSet nextLive = this.computeLiveLocalsAtEntry(analyser);
                BitSet previousLive = this.liveLocalsAtEntry.get(analyser.getLabel());
                if (!nextLive.equals(previousLive)) {
                    this.liveLocalsAtEntry.put(analyser.getLabel(), nextLive);
                    updated = true;
                }
            }
        }
        while (updated);
    }

    private @NotNull BitSet computeLiveLocalsAtEntry(@NotNull InstructionSetAnalyser analyser) {
        BitSet liveLocals = this.computeLiveLocalsAtExit(analyser.getLabel());
        List<InstructionInfo> instructions = analyser.getInstructions();
        for (int i = instructions.size() - 1; i >= 0; i--)
            this.applyInstructionLiveness(instructions.get(i), liveLocals);

        return liveLocals;
    }

    private @NotNull BitSet computeLiveLocalsAtExit(@NotNull LabelInfo label) {
        BitSet liveLocals = new BitSet();
        for (LabelInfo successor : this.getSuccessors(label)) {
            BitSet successorLive = this.liveLocalsAtEntry.get(successor);
            if (successorLive != null)
                liveLocals.or(successorLive);
        }
        return liveLocals;
    }

    private @NotNull List<LabelInfo> getSuccessors(@NotNull LabelInfo label) {
        List<LabelInfo> successors = new ArrayList<>();
        List<InstructionInfo> blockInstructions = this.instructions.getInstructions(label);
            if (blockInstructions.isEmpty())
                return successors;

        InstructionInfo lastInstruction = blockInstructions.getLast();
        if (lastInstruction == null)
            return successors;

        switch (lastInstruction.insn()) {
            case JumpInsnNode jumpNode -> {
                LabelInfo jumpTarget = this.labels.getLabelByNode(jumpNode.label);
                if (jumpTarget != null)
                    successors.add(jumpTarget);

                int opcode = lastInstruction.opcode();
                if (opcode != EOpcodes.GOTO && opcode != EOpcodes.GOTO_W) {
                    LabelInfo nextBlock = this.labels.getNextBlock(label);
                    if (nextBlock != null && !successors.contains(nextBlock))
                        successors.add(nextBlock);
                }
                return successors;
            }
            case TableSwitchInsnNode tableSwitchNode -> {
                this.addSwitchSuccessors(successors, tableSwitchNode.labels);
                LabelInfo defaultLabel = this.labels.getLabelByNode(tableSwitchNode.dflt);
                if (defaultLabel != null && !successors.contains(defaultLabel))
                    successors.add(defaultLabel);
                return successors;
            }
            case LookupSwitchInsnNode lookupSwitchNode -> {
                this.addSwitchSuccessors(successors, lookupSwitchNode.labels);
                LabelInfo defaultLabel = this.labels.getLabelByNode(lookupSwitchNode.dflt);
                if (defaultLabel != null && !successors.contains(defaultLabel))
                    successors.add(defaultLabel);
                return successors;
            }
            default -> {
            }
        }

        int opcode = lastInstruction.opcode();
        if (isReturnOrThrow(opcode))
            return successors;

        LabelInfo nextBlock = this.labels.getNextBlock(label);
        if (nextBlock != null)
            successors.add(nextBlock);

        return successors;
    }

    private static boolean isReturnOrThrow(int opcode) {
        return opcode == EOpcodes.RETURN
                || opcode == EOpcodes.ARETURN
                || opcode == EOpcodes.IRETURN
                || opcode == EOpcodes.FRETURN
                || opcode == EOpcodes.LRETURN
                || opcode == EOpcodes.DRETURN
                || opcode == EOpcodes.ATHROW;
    }

    private void addSwitchSuccessors(@NotNull List<? super LabelInfo> successors, @NotNull List<? extends LabelNode> labels) {
        for (LabelNode labelNode : labels) {
            LabelInfo label = this.labels.getLabelByNode(labelNode);
            if (label != null && !successors.contains(label))
                successors.add(label);
        }
    }

    private void applyInstructionLiveness(@NotNull InstructionInfo instruction, @NotNull BitSet liveLocals) {
        StackOperation[] operations = instruction.producer().getFrameDifferenceInfo(instruction).getStackOperations();
        for (int i = operations.length - 1; i >= 0; i--) {
            StackOperation operation = operations[i];
            if (!(operation.element() instanceof LocalStackElement localElement))
                continue;

            int slotSize = this.getLocalSlotSize(instruction, localElement);
            if (operation.type() == StackOperation.StackOperationType.PUSH) {
                liveLocals.clear(localElement.index(), localElement.index() + slotSize);
            } else {
                liveLocals.set(localElement.index(), localElement.index() + slotSize);
            }
        }
    }

    private int getLocalSlotSize(@NotNull InstructionInfo instruction, @NotNull LocalStackElement localElement) {
        StackElement element = localElement.stackElement();
        if (!(element instanceof tokyo.peya.langjal.analyser.stack.StackElementCapsule)) {
            StackElementType type = localElement.type();
            return type == StackElementType.LONG || type == StackElementType.DOUBLE ? 2 : 1;
        }

        return switch (instruction.opcode()) {
            case EOpcodes.LLOAD, EOpcodes.LLOAD_0, EOpcodes.LLOAD_1, EOpcodes.LLOAD_2, EOpcodes.LLOAD_3,
                 EOpcodes.LSTORE, EOpcodes.LSTORE_0, EOpcodes.LSTORE_1, EOpcodes.LSTORE_2, EOpcodes.LSTORE_3,
                 EOpcodes.DLOAD, EOpcodes.DLOAD_0, EOpcodes.DLOAD_1, EOpcodes.DLOAD_2, EOpcodes.DLOAD_3,
                 EOpcodes.DSTORE, EOpcodes.DSTORE_0, EOpcodes.DSTORE_1, EOpcodes.DSTORE_2, EOpcodes.DSTORE_3 -> 2;
            default -> 1;
        };
    }
}
