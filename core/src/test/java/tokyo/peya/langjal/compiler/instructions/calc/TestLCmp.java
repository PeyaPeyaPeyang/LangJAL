package tokyo.peya.langjal.compiler.instructions.calc;

import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.utils.AbstractInstructionTestCase;
import tokyo.peya.langjal.compiler.instructions.utils.StackMachine;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;

import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.longValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.create;

public class TestLCmp extends AbstractInstructionTestCase<JALParser.JvmInsLcmpContext, InstructionEvaluatorLCmp>
{
    public TestLCmp()
    {
        super(new InstructionEvaluatorLCmp(), EOpcodes.LCMP);
    }

    @Override
    public InstructionCase[] getValidInstructionSyntaxes()
    {
        return set(
                of(
                        create(longValue(), longValue())
                                .expected(create(longValue())),
                        "lcmp",
                        null
                )
        );
    }
}
