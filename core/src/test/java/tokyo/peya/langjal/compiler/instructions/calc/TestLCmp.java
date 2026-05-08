package tokyo.peya.langjal.compiler.instructions.calc;

import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.utils.AbstractInstructionTestCase;
import tokyo.peya.langjal.compiler.instructions.utils.StackMachineEmulator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;

import static tokyo.peya.langjal.compiler.instructions.utils.StackMachineEmulator.StackValues.longValue;

public class TestLCmp extends AbstractInstructionTestCase<JALParser.JvmInsLcmpContext, InstructionEvaluatorLCmp>
{
    public TestLCmp()
    {
        super(new InstructionEvaluatorLCmp(), EOpcodes.LCMP);
    }

    @Override
    public String[] getValidInstructionSyntaxes()
    {
        return new String[] {
                "lcmp",
        };
    }

    @Override
    public StackMachineEmulator[] validSituations()
    {
        return new StackMachineEmulator[] {
                StackMachineEmulator.create(longValue(), longValue()),
        };
    }
}
