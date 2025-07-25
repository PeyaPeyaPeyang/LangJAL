package tokyo.peya.langjal.compiler.instructions;

import org.antlr.v4.runtime.ParserRuleContext;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.InsnNode;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.JALMethodCompiler;

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
     * @param compiler The method compiler.
     * @param ctxt     The parser rule context.
     * @return The evaluated instruction.
     */
    @Override
    protected @NotNull EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler, @NotNull T ctxt)
    {
        return this.visitSingle(ctxt, this.opcode);
    }
}
