package tokyo.peya.langjal.compiler.instructions.calc;

import org.junit.jupiter.api.Nested;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.calc.xrem.InstructionEvaluatorDRem;
import tokyo.peya.langjal.compiler.instructions.calc.xrem.InstructionEvaluatorFRem;
import tokyo.peya.langjal.compiler.instructions.calc.xrem.InstructionEvaluatorIRem;
import tokyo.peya.langjal.compiler.instructions.calc.xrem.InstructionEvaluatorLRem;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;

import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.doubleValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.floatValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.integerValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.longValue;

public class TestXRem
{
    @Nested
    class TestIRem extends AbstractMathInstructionTestCase<JALParser.JvmInsIremContext, InstructionEvaluatorIRem>
    {
        TestIRem()
        {
            super(new InstructionEvaluatorIRem(), integerValue(), "irem", EOpcodes.IREM);
        }
    }

    @Nested
    class TestFRem extends AbstractMathInstructionTestCase<JALParser.JvmInsFremContext, InstructionEvaluatorFRem>
    {
        TestFRem()
        {
            super(new InstructionEvaluatorFRem(), floatValue(), "frem", EOpcodes.FREM);
        }
    }

    @Nested
    class TestDRem extends AbstractMathInstructionTestCase<JALParser.JvmInsDremContext, InstructionEvaluatorDRem>
    {
        TestDRem()
        {
            super(new InstructionEvaluatorDRem(), doubleValue(), "drem", EOpcodes.DREM);
        }
    }

    @Nested
    class TestLRem extends AbstractMathInstructionTestCase<JALParser.JvmInsLremContext, InstructionEvaluatorLRem>
    {
        TestLRem()
        {
            super(new InstructionEvaluatorLRem(), longValue(), "lrem", EOpcodes.LREM);
        }
    }
}


