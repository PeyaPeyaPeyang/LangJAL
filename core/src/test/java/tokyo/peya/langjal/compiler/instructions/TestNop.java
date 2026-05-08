package tokyo.peya.langjal.compiler.instructions;

import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.utils.AbstractInstructionTestCase;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;

public class TestNop extends AbstractInstructionTestCase<JALParser.JvmInsNopContext, InstructionEvaluatorNop>
{
    public TestNop()
    {
        super(new InstructionEvaluatorNop(), EOpcodes.NOP);
    }

    @Override
    public String[] getValidInstructions()
    {
        return new String[] {
                "nop",
        };
    }
}
