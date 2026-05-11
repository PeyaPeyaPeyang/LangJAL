package tokyo.peya.langjal.compiler.instructions.calc;

import org.antlr.v4.runtime.ParserRuleContext;
import tokyo.peya.langjal.compiler.instructions.AbstractSingleInstructionEvaluator;
import tokyo.peya.langjal.compiler.instructions.utils.AbstractInstructionTestCase;
import tokyo.peya.langjal.compiler.instructions.utils.StackMachine;

import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.integerValue;

/**
 * Helper base class for simple binary numeric instruction tests where two values
 * of the same stack type produce one value of the same type (e.g. iadd, dmul).
 */
public abstract class AbstractMathInstructionTestCase<P extends ParserRuleContext, T extends AbstractSingleInstructionEvaluator<P>>
        extends AbstractInstructionTestCase<P, T>
{
    private final StackMachine.StackValue value;
    private final String syntax;
    private final int opcode;

    protected AbstractMathInstructionTestCase(T evaluator, int... expectedOpCodes)
    {
        super(evaluator, expectedOpCodes);
        this.value = null;
        this.syntax = null;
        this.opcode = -1;
    }

    /**
     * Convenience constructor for the common case where a single test (two same-type -> one same-type)
     * is desired and the concrete test class doesn't need to override getValidInstructionSyntaxes().
     */
    protected AbstractMathInstructionTestCase(T evaluator, StackMachine.StackValue value, String syntax, int opcode)
    {
        super(evaluator, opcode);
        this.value = value;
        this.syntax = syntax;
        this.opcode = opcode;
    }
    /**
     * Create a single test case: two same-type stack values -> one same-type result.
     *
     * @param value  representative stack value (use StackMachine.StackValues.* helpers)
     * @param syntax instruction syntax string (e.g. "iadd")
     * @param opcode expected opcode constant (EOpcodes.*)
     * @return array containing one InstructionCase
     */
    protected InstructionCase[] single(StackMachine.StackValue value, String syntax, int opcode)
    {
        return set(
                of(
                        StackMachine.create(value, value).expected(StackMachine.create(value)),
                        syntax,
                        opcode
                ),
                of(
                        StackMachine.create(integerValue(), value, value).expected(StackMachine.create(integerValue(), value)),
                        syntax,
                        opcode
                )
        );
    }

    @Override
    public InstructionCase[] getValidInstructionSyntaxes()
    {
        if (!(this.value == null || this.syntax == null || this.opcode == -1))
        {
            return single(this.value, this.syntax, this.opcode);
        }

        // Fallback for subclasses that still override getValidInstructionSyntaxes
        return set();
    }
}
