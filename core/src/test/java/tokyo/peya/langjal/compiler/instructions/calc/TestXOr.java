package tokyo.peya.langjal.compiler.instructions.calc;

import org.junit.jupiter.api.Nested;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.calc.xor.InstructionEvaluatorIOr;
import tokyo.peya.langjal.compiler.instructions.calc.xor.InstructionEvaluatorLOr;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;

import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.integerValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.longValue;

public class TestXOr {
    @Nested
    class TestIOr extends AbstractMathInstructionTestCase<JALParser.JvmInsIorContext, InstructionEvaluatorIOr> {
        TestIOr() {
            super(new InstructionEvaluatorIOr(), integerValue(), "ior", EOpcodes.IOR);
        }
    }

    @Nested
    class TestLOr extends AbstractMathInstructionTestCase<JALParser.JvmInsLorContext, InstructionEvaluatorLOr> {
        TestLOr() {
            super(new InstructionEvaluatorLOr(), longValue(), "lor", EOpcodes.LOR);
        }
    }
}


