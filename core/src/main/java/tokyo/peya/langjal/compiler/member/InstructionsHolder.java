package tokyo.peya.langjal.compiler.member;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.compiler.instructions.InstructionEvaluatorReturn;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InstructionsHolder
{
    private final ClassNode ownerClass;
    private final MethodNode ownerMethod;

    private final LabelsHolder labels;
    private final List<InstructionInfo> instructions;

    @Getter
    private int bytecodeOffset;

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

    public int getSize()
    {
        return this.instructions.size();
    }

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

    public InstructionInfo addInstruction(@NotNull EvaluatedInstruction evaluatedInstruction,
                                          @Nullable LabelInfo labelAssignation,
                                          int sourceLine)
    {
        InstructionInfo instruction = new InstructionInfo(
                evaluatedInstruction.evaluator(),
                this.ownerClass,
                this.ownerMethod,
                evaluatedInstruction.insn(),
                this.bytecodeOffset,
                labelAssignation,
                evaluatedInstruction.getInstructionSize(),
                sourceLine
        );
        this.instructions.add(instruction);
        this.bytecodeOffset += instruction.instructionSize();
        return instruction;
    }

    public void finaliseInstructions()
    {
        for (InstructionInfo instruction : this.instructions)
        {
            if (instruction.assignedLabel() != null)  // 命令にラベルが割り当てられている場合
                this.ownerMethod.instructions.add(instruction.assignedLabel().node());

            // 行番号を付加する。それにはラベルが必要なので，もし命令にラベルが貼っ付いていたら再利用する。
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

            this.ownerMethod.instructions.add(instruction.insn());
        }
    }

    public boolean isEmpty()
    {
        return this.instructions.isEmpty();
    }

    @Nullable
    public InstructionInfo getInstruction(int index)
    {
        if (index < 0 || index >= this.instructions.size())
            return null; // インデックスが範囲外の場合はnullを返す
        return this.instructions.get(index);
    }

    @NotNull
    public InstructionInfo getLastInstruction()
    {
        if (this.instructions.isEmpty())
            throw new IllegalStateException("No instructions available");
        return this.instructions.get(this.instructions.size() - 1);
    }

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
