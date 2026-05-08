package tokyo.peya.langjal.compiler.instructions;

import tokyo.peya.langjal.compiler.instructions.utils.AbstractInstructionTestCase;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;

public class TestNop extends AbstractInstructionTestCase
{
    public TestNop()
    {
        super(new InstructionEvaluatorNop(), EOpcodes.NOP);
    }
}
