package tokyo.peya.langjal.compiler.instructions.calc;

import org.junit.jupiter.api.Nested;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.calc.xand.InstructionEvaluatorIAnd;
import tokyo.peya.langjal.compiler.instructions.calc.xand.InstructionEvaluatorLAnd;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;

import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.integerValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.longValue;

public class TestXAnd
{
    @Nested
    class TestIAnd extends AbstractMathInstructionTestCase<JALParser.JvmInsIandContext, InstructionEvaluatorIAnd>
    {
        TestIAnd()
        {
            super(new InstructionEvaluatorIAnd(), integerValue(), "iand", EOpcodes.IAND);
        }
    }

    @Nested
    class TestLAnd extends AbstractMathInstructionTestCase<JALParser.JvmInsLandContext, InstructionEvaluatorLAnd>
    {
        TestLAnd()
        {
            super(new InstructionEvaluatorLAnd(), longValue(), "land", EOpcodes.LAND);
        }
    }
}

