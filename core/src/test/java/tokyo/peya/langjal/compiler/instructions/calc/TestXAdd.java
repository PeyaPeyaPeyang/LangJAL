package tokyo.peya.langjal.compiler.instructions.calc;

import org.junit.jupiter.api.Nested;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.calc.xadd.InstructionEvaluatorDAdd;
import tokyo.peya.langjal.compiler.instructions.calc.xadd.InstructionEvaluatorFAdd;
import tokyo.peya.langjal.compiler.instructions.calc.xadd.InstructionEvaluatorIAdd;
import tokyo.peya.langjal.compiler.instructions.calc.xadd.InstructionEvaluatorLAdd;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;

import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.*;

public class TestXAdd {
    @Nested
    class TestIAdd extends AbstractMathInstructionTestCase<JALParser.JvmInsIaddContext, InstructionEvaluatorIAdd> {
        TestIAdd() {
            super(new InstructionEvaluatorIAdd(), integerValue(), "iadd", EOpcodes.IADD);
        }
    }

    @Nested
    class TestFAdd extends AbstractMathInstructionTestCase<JALParser.JvmInsFaddContext, InstructionEvaluatorFAdd> {
        TestFAdd() {
            super(new InstructionEvaluatorFAdd(), floatValue(), "fadd", EOpcodes.FADD);
        }
    }

    @Nested
    class TestDAdd extends AbstractMathInstructionTestCase<JALParser.JvmInsDaddContext, InstructionEvaluatorDAdd> {
        TestDAdd() {
            super(new InstructionEvaluatorDAdd(), doubleValue(), "dadd", EOpcodes.DADD);
        }
    }

    @Nested
    class TestLAdd extends AbstractMathInstructionTestCase<JALParser.JvmInsLaddContext, InstructionEvaluatorLAdd> {
        TestLAdd() {
            super(new InstructionEvaluatorLAdd(), longValue(), "ladd", EOpcodes.LADD);
        }
    }
}

