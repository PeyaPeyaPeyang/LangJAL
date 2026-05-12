package tokyo.peya.langjal.compiler.instructions.calc;

import org.junit.jupiter.api.Nested;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.calc.xshr.InstructionEvaluatorIShr;
import tokyo.peya.langjal.compiler.instructions.calc.xshr.InstructionEvaluatorLShr;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;

import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.integerValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.longValue;

public class TestXShr {
    @Nested
    class TestIShr extends AbstractMathInstructionTestCase<JALParser.JvmInsIshrContext, InstructionEvaluatorIShr> {
        TestIShr() {
            super(new InstructionEvaluatorIShr(), integerValue(), "ishr", EOpcodes.ISHR);
        }
    }

    @Nested
    class TestLShr extends AbstractMathInstructionTestCase<JALParser.JvmInsLshrContext, InstructionEvaluatorLShr> {
        TestLShr() {
            super(new InstructionEvaluatorLShr(), longValue(), "lshr", EOpcodes.LSHR);
        }
    }
}


