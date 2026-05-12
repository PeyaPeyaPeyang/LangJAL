package tokyo.peya.langjal.compiler.instructions.calc;

import org.junit.jupiter.api.Nested;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.calc.xxor.InstructionEvaluatorIXOr;
import tokyo.peya.langjal.compiler.instructions.calc.xxor.InstructionEvaluatorLXOr;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;

import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.integerValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.longValue;

public class TestXXOr {
    @Nested
    class TestIXOr extends AbstractMathInstructionTestCase<JALParser.JvmInsIxorContext, InstructionEvaluatorIXOr> {
        TestIXOr() {
            super(new InstructionEvaluatorIXOr(), integerValue(), "ixor", EOpcodes.IXOR);
        }
    }

    @Nested
    class TestLXOr extends AbstractMathInstructionTestCase<JALParser.JvmInsLxorContext, InstructionEvaluatorLXOr> {
        TestLXOr() {
            super(new InstructionEvaluatorLXOr(), longValue(), "lxor", EOpcodes.LXOR);
        }
    }
}


