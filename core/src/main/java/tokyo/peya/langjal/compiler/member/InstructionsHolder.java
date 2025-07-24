package tokyo.peya.langjal.compiler.member;

import lombok.Getter;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
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

    @MagicConstant(valuesFromClass = CompileSettings.class)
    private final int compileSettings;

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
                              @NotNull LabelsHolder labels,
                              int compileSettings)
    {
        this.ownerClass = ownerClass;
        this.ownerMethod = ownerMethod;
        this.labels = labels;
        this.compileSettings = compileSettings;

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
        this.instructions.add(instruction);
        this.bytecodeOffset += instruction.instructionSize();
        return instruction;
    }

    /**
     * Finalizes instructions by adding them to the method and handling labels and line numbers.
     */
    public void finaliseInstructions()
    {
        boolean includeLineNumberTable =
                (this.compileSettings & CompileSettings.INCLUDE_LINE_NUMBER_TABLE) != 0;
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
