package tokyo.peya.langjal.compiler.instructions.calc;

import org.junit.jupiter.api.Nested;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.calc.xdiv.InstructionEvaluatorDDiv;
import tokyo.peya.langjal.compiler.instructions.calc.xdiv.InstructionEvaluatorFDiv;
import tokyo.peya.langjal.compiler.instructions.calc.xdiv.InstructionEvaluatorIDiv;
import tokyo.peya.langjal.compiler.instructions.calc.xdiv.InstructionEvaluatorLDiv;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;

import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.*;

public class TestXDiv {
    @Nested
    class TestIDiv extends AbstractMathInstructionTestCase<JALParser.JvmInsIdivContext, InstructionEvaluatorIDiv> {
        TestIDiv() {
            super(new InstructionEvaluatorIDiv(), integerValue(), "idiv", EOpcodes.IDIV);
        }
    }

    @Nested
    class TestFDiv extends AbstractMathInstructionTestCase<JALParser.JvmInsFdivContext, InstructionEvaluatorFDiv> {
        TestFDiv() {
            super(new InstructionEvaluatorFDiv(), floatValue(), "fdiv", EOpcodes.FDIV);
        }
    }

    @Nested
    class TestDDiv extends AbstractMathInstructionTestCase<JALParser.JvmInsDdivContext, InstructionEvaluatorDDiv> {
        TestDDiv() {
            super(new InstructionEvaluatorDDiv(), doubleValue(), "ddiv", EOpcodes.DDIV);
        }
    }

    @Nested
    class TestLDiv extends AbstractMathInstructionTestCase<JALParser.JvmInsLdivContext, InstructionEvaluatorLDiv> {
        TestLDiv() {
            super(new InstructionEvaluatorLDiv(), longValue(), "ldiv", EOpcodes.LDIV);
        }
    }
}

