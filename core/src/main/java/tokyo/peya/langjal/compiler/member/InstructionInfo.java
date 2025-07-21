package tokyo.peya.langjal.compiler.member;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;

import java.util.Objects;

public record InstructionInfo(
        @NotNull
        AbstractInstructionEvaluator<?> evaluator,
        @NotNull
        ClassNode ownerClass,
        @NotNull
        MethodNode owner,
        @NotNull
        AbstractInsnNode insn,
        int bytecodeOffset,
        @Nullable
        LabelInfo assignedLabel,
        int instructionSize,
        int sourceLine
)
{
    public InstructionInfo(@NotNull AbstractInstructionEvaluator<?> evaluator,
                           @NotNull ClassNode ownerClass,
                           @NotNull MethodNode owner,
                           int insn,
                           int bytecodeOffset,
                           @Nullable LabelInfo assignedLabel,
                           int instructionSize,
                           int sourceLine)
    {
        this(
                evaluator,
                ownerClass,
                owner,
                new InsnNode(insn),
                bytecodeOffset,
                assignedLabel,
                instructionSize,
                sourceLine
        );
    }

    public int opcode()
    {
        return this.insn.getOpcode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof InstructionInfo that))
            return false;

        return Objects.equals(this.evaluator, that.evaluator) &&
                Objects.equals(this.insn, that.insn) &&
                this.bytecodeOffset == that.bytecodeOffset &&
                Objects.equals(this.assignedLabel, that.assignedLabel) &&
                this.instructionSize == that.instructionSize;
    }

    @Override
    public @NotNull String toString()
    {
        return EOpcodes.getName(this.opcode()) +
                " at " + this.bytecodeOffset +
                (this.assignedLabel != null ? " with label " + this.assignedLabel: "");
    }
}
