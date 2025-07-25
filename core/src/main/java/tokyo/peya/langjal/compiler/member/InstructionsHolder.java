package tokyo.peya.langjal.compiler.member;

import lombok.Getter;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import tokyo.peya.langjal.compiler.CompileSettings;
import tokyo.peya.langjal.compiler.instructions.InstructionEvaluatorReturn;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds and manages a list of {@link InstructionInfo} objects for a method,
 * tracking bytecode offsets and providing methods to add, finalize, and query instructions.
 */
public class InstructionsHolder
{
    /**
     * The class that owns these instructions.
     */
    private final ClassNode ownerClass;
    /**
     * The method that owns these instructions.
     */
    private final MethodNode ownerMethod;
    /**
     * Holder for labels associated with instructions.
     */
    private final LabelsHolder labels;
    /**
     * List of instructions in this holder.
     */
    private final List<InstructionInfo> instructions;


    /**
     * Current bytecode offset for the next instruction.
     */
    @Getter
    private int bytecodeOffset;

    /**
     * Constructs an InstructionsHolder for the given class, method, and labels.
     *
     * @param ownerClass  The owning class.
     * @param ownerMethod The owning method.
     * @param labels      The labels holder.
     */
    public InstructionsHolder(@NotNull ClassNode ownerClass,
                              @NotNull MethodNode ownerMethod,
                              @NotNull LabelsHolder labels)
    {
        this.ownerClass = ownerClass;
        this.ownerMethod = ownerMethod;
        this.labels = labels;

        this.instructions = new ArrayList<>();
        this.bytecodeOffset = 0;
    }

    /**
     * Returns the number of instructions held.
     *
     * @return The instruction count.
     */
    public int getSize()
    {
        return this.instructions.size();
    }

    /**
     * Adds a RETURN instruction to the holder.
     *
     * @return The added InstructionInfo.
     */
    public InstructionInfo addReturn()
    {
        int returnOpcode = EOpcodes.RETURN;
        InstructionInfo instruction = new InstructionInfo(
                new InstructionEvaluatorReturn(),
                this.ownerClass,
                this.ownerMethod,
                returnOpcode,
                this.bytecodeOffset,
                this.labels.getCurrentLabel(),
                EOpcodes.getOpcodeSize(returnOpcode),
                -1
        );
        this.instructions.add(instruction);
        this.bytecodeOffset += instruction.instructionSize();
        return instruction;
    }

    /**
     * Adds a generic instruction to the holder.
     *
     * @param evaluatedInstruction The evaluated instruction.
     * @param labelAssignation     The label to assign, if any.
     * @param sourceLine           The source line number.
     * @return The added InstructionInfo.
     */
    public InstructionInfo addInstruction(@NotNull EvaluatedInstruction evaluatedInstruction,
                                          @Nullable LabelInfo labelAssignation,
                                          int sourceLine)
    {
        InstructionInfo instruction = new InstructionInfo(
                this.bytecodeOffset,
                evaluatedInstruction.insn(),
                this.ownerClass,
                this.ownerMethod,
                evaluatedInstruction.evaluator(),
                labelAssignation,
                evaluatedInstruction.getInstructionSize(),
                sourceLine
        );

        this.addInstruction(instruction);
        return instruction;
    }

    public InstructionInfo importInstruction(@NotNull AbstractInsnNode instruction,
                                             @Nullable LabelInfo labelAssignation,
                                             int sourceLine)
    {
        int opcode = instruction.getOpcode();
        if (opcode == -1)  // 無効な命令コードの場合
            throw new IllegalArgumentException("Invalid opcode for instruction: " + instruction);

        InstructionInfo instructionInfo = new InstructionInfo(
                this.bytecodeOffset,
                instruction,
                this.ownerClass,
                this.ownerMethod,
                JALInstructionEvaluator.getEvaluatorByOpcode(opcode),
                labelAssignation,
                calcInstructionSize(instruction),
                sourceLine
        );

        this.addInstruction(instructionInfo);
        return instructionInfo;
    }

    private int calcInstructionSize(@NotNull AbstractInsnNode instruction)
    {
        int opcode = instruction.getOpcode();
        if (opcode == -1)  // 無効な命令コードの場合
            throw new IllegalArgumentException("Invalid opcode for instruction: " + instruction);

        // tableswitch/lookupswitch/wide は可変だが，その他は固定なのでらくらく。
        return switch (opcode)
        {
            case EOpcodes.TABLESWITCH -> {
                TableSwitchInsnNode tableSwitch = (TableSwitchInsnNode) instruction;
                int padding = this.calcSwitchPadding(); // パディングを計算
                int cases = tableSwitch.labels.size();
                yield 1  // opcode
                        + 4 // default label
                        + 4 // low
                        + 4 // high
                        + (cases * 4) // 各caseのラベル
                        + padding; // パディング
            }
            case EOpcodes.LOOKUPSWITCH -> {
                LookupSwitchInsnNode lookupSwitch = (LookupSwitchInsnNode) instruction;
                int padding = this.calcSwitchPadding(); // パディングを計算
                int cases = lookupSwitch.keys.size();
                yield 1  // opcode
                        + 4 // default label
                        + (cases * 8) // 各caseのキーとラベル
                        + padding; // パディング
            }
            // WIDE 命令は ASM が卸してくれないので，手動
            case EOpcodes.ILOAD, EOpcodes.FLOAD, EOpcodes.ALOAD, EOpcodes.LLOAD, EOpcodes.DLOAD,
                 EOpcodes.ISTORE, EOpcodes.FSTORE, EOpcodes.ASTORE, EOpcodes.LSTORE, EOpcodes.DSTORE,
                 EOpcodes.RET -> {
                VarInsnNode varInsn = (VarInsnNode) instruction;
                int idx = varInsn.var;

                yield (idx < 0xFF) ? 2 : 4; // idx が 0xFF(255) 未満なら 2 バイト，それ以上なら 4 バイト
            }
            case EOpcodes.IINC -> {
                IincInsnNode iincInsn = (IincInsnNode) instruction;
                int idx = iincInsn.var;
                int increment = iincInsn.incr;
                // idx が 0xFF(255) 未満なら 3 バイト，それ以上なら 6 バイト
                yield (idx <= 0xFF && increment >= Byte.MIN_VALUE && increment <= Byte.MAX_VALUE) ? 3 : 6;
            }
            default -> EOpcodes.getOpcodeSize(opcode);
        };
    }

    private int calcSwitchPadding()
    {
        // パディングは 4 バイト境界に合わせる
        return (4 - (this.bytecodeOffset + 1) % 4) % 4;
    }

    private void addInstruction(@NotNull InstructionInfo instruction)
    {
        this.instructions.add(instruction);
        this.bytecodeOffset += instruction.instructionSize();
    }

    /**
     * Finalizes instructions by adding them to the method and handling labels and line numbers.
     */
    public void finaliseInstructions(@MagicConstant(flagsFromClass = CompileSettings.class) int compileSettings)
    {
        boolean includeLineNumberTable =
                (compileSettings & CompileSettings.INCLUDE_LINE_NUMBER_TABLE) != 0;
        for (InstructionInfo instruction : this.instructions)
        {
            if (instruction.assignedLabel() != null)  // 命令にラベルが割り当てられている場合
                this.ownerMethod.instructions.add(instruction.assignedLabel().node());

            // 行番号を付加する。それにはラベルが必要なので，もし命令にラベルが貼っ付いていたら再利用する。
            if (includeLineNumberTable)
                // 行番号を付加する。
                this.addLineNumberOnCurrentInstruction(instruction);

            this.ownerMethod.instructions.add(instruction.insn());
        }
    }

    private void addLineNumberOnCurrentInstruction(@NotNull InstructionInfo instruction)
    {
        int lineNumber = instruction.sourceLine();
        if (lineNumber >= 0)
        {
            Label label;
            if (instruction.assignedLabel() == null)
            {
                label = new Label();
                this.ownerMethod.visitLabel(label);
            }
            else
                label = instruction.assignedLabel().node().getLabel();
            this.ownerMethod.visitLineNumber(lineNumber, label);
        }
    }

    /**
     * Checks if there are no instructions.
     *
     * @return True if empty, false otherwise.
     */
    public boolean isEmpty()
    {
        return this.instructions.isEmpty();
    }

    /**
     * Gets the instruction at the specified index, or null if out of bounds.
     *
     * @param index The instruction index.
     * @return The InstructionInfo or null.
     */
    @Nullable
    public InstructionInfo getInstruction(int index)
    {
        if (index < 0 || index >= this.instructions.size())
            return null; // インデックスが範囲外の場合はnullを返す
        return this.instructions.get(index);
    }

    /**
     * Gets the last instruction in the holder.
     *
     * @return The last InstructionInfo.
     * @throws IllegalStateException If there are no instructions.
     */
    @NotNull
    public InstructionInfo getLastInstruction()
    {
        if (this.instructions.isEmpty())
            throw new IllegalStateException("No instructions available");
        return this.instructions.get(this.instructions.size() - 1);
    }

    /**
     * Returns an unmodifiable list of instructions starting from the given label.
     *
     * @param instructionSet The label marking the start of the instruction set.
     * @return List of InstructionInfo objects.
     * @throws IndexOutOfBoundsException If the start index is invalid.
     */
    public List<InstructionInfo> getInstructions(LabelInfo instructionSet)
    {
        int startIndex = instructionSet.instructionIndex();
        if (startIndex < 0 || startIndex >= this.instructions.size())
            throw new IndexOutOfBoundsException("Start index is out of bounds: " + startIndex);

        List<InstructionInfo> instructionSetList = new ArrayList<>();
        for (int i = startIndex; i < this.instructions.size(); i++)
        {
            InstructionInfo instruction = this.instructions.get(i);
            if (!(instruction.assignedLabel() == null || instruction.assignedLabel().equals(instructionSet)))
                break; // 次のラベルが割り当てられた命令に到達したら終了
            instructionSetList.add(instruction);
        }

        return Collections.unmodifiableList(instructionSetList);
    }
}
