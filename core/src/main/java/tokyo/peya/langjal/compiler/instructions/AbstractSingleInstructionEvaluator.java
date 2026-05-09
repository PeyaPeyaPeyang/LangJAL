package tokyo.peya.langjal.compiler.instructions;

import org.antlr.v4.runtime.ParserRuleContext;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.InstructionsHolder;
import tokyo.peya.langjal.compiler.member.LabelsHolder;
import tokyo.peya.langjal.compiler.member.LocalVariablesHolder;

/**
 * Abstract evaluator for single instructions with a fixed opcode.
 *
 * @param <T> The type of parser rule context handled by this evaluator.
 */
public abstract class AbstractSingleInstructionEvaluator<T extends ParserRuleContext>
        extends AbstractInstructionEvaluator<T>
{
    private final int opcode;

    /**
     * Constructs an evaluator for a single instruction with the specified opcode.
     *
     * @param opcode The opcode for the instruction.
     */
    public AbstractSingleInstructionEvaluator(int opcode)
    {
        super(opcode);
        this.opcode = opcode;
    }

    /**
     * Evaluates the instruction using the fixed opcode.
     *
     * @param context      The file evaluating reporter for error reporting and context.
     * @param clazz        The class node being compiled.
     * @param method       The method node being compiled.
     * @param instructions The instructions' holder.
     * @param labels       The labels' holder.
     * @param locals       The local variables' holder.
     * @param instruction  The parser rule context.
     * @return The evaluated instruction.
     */
    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals, @NotNull T instruction)
    {
        return this.visitSingle(instruction, this.opcode);
    }
}
