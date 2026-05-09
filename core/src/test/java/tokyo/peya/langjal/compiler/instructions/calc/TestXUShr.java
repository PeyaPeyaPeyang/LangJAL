package tokyo.peya.langjal.compiler.instructions.calc;

import org.junit.jupiter.api.Nested;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.calc.xushr.InstructionEvaluatorIUShr;
import tokyo.peya.langjal.compiler.instructions.calc.xushr.InstructionEvaluatorLUShr;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;

import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.integerValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.longValue;

public class TestXUShr
{
    @Nested
    class TestIUShr extends AbstractMathInstructionTestCase<JALParser.JvmInsIushrContext, InstructionEvaluatorIUShr>
    {
        TestIUShr()
        {
            super(new InstructionEvaluatorIUShr(), integerValue(), "iushr", EOpcodes.IUSHR);
        }
    }

    @Nested
    class TestLUShr extends AbstractMathInstructionTestCase<JALParser.JvmInsLushrContext, InstructionEvaluatorLUShr>
    {
        TestLUShr()
        {
            super(new InstructionEvaluatorLUShr(), longValue(), "lushr", EOpcodes.LUSHR);
        }
    }
}


