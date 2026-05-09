package tokyo.peya.langjal.compiler.instructions.calc;

import org.junit.jupiter.api.Nested;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.calc.xsub.InstructionEvaluatorDSub;
import tokyo.peya.langjal.compiler.instructions.calc.xsub.InstructionEvaluatorFSub;
import tokyo.peya.langjal.compiler.instructions.calc.xsub.InstructionEvaluatorISub;
import tokyo.peya.langjal.compiler.instructions.calc.xsub.InstructionEvaluatorLSub;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;

import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.doubleValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.floatValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.integerValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.longValue;

public class TestXSub
{
    @Nested
    class TestISub extends AbstractMathInstructionTestCase<JALParser.JvmInsIsubContext, InstructionEvaluatorISub>
    {
        TestISub()
        {
            super(new InstructionEvaluatorISub(), integerValue(), "isub", EOpcodes.ISUB);
        }
    }

    @Nested
    class TestFSub extends AbstractMathInstructionTestCase<JALParser.JvmInsFsubContext, InstructionEvaluatorFSub>
    {
        TestFSub()
        {
            super(new InstructionEvaluatorFSub(), floatValue(), "fsub", EOpcodes.FSUB);
        }
    }

    @Nested
    class TestDSub extends AbstractMathInstructionTestCase<JALParser.JvmInsDsubContext, InstructionEvaluatorDSub>
    {
        TestDSub()
        {
            super(new InstructionEvaluatorDSub(), doubleValue(), "dsub", EOpcodes.DSUB);
        }
    }

    @Nested
    class TestLSub extends AbstractMathInstructionTestCase<JALParser.JvmInsLsubContext, InstructionEvaluatorLSub>
    {
        TestLSub()
        {
            super(new InstructionEvaluatorLSub(), longValue(), "lsub", EOpcodes.LSUB);
        }
    }
}


