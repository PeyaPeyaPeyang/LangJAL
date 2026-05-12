package tokyo.peya.langjal.compiler.instructions.calc;

import org.junit.jupiter.api.Nested;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.calc.xshl.InstructionEvaluatorIShl;
import tokyo.peya.langjal.compiler.instructions.calc.xshl.InstructionEvaluatorLShl;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;

import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.integerValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.longValue;

public class TestXShl {
    @Nested
    class TestIShl extends AbstractMathInstructionTestCase<JALParser.JvmInsIshlContext, InstructionEvaluatorIShl> {
        TestIShl() {
            super(new InstructionEvaluatorIShl(), integerValue(), "ishl", EOpcodes.ISHL);
        }
    }

    @Nested
    class TestLShl extends AbstractMathInstructionTestCase<JALParser.JvmInsLshlContext, InstructionEvaluatorLShl> {
        TestLShl() {
            super(new InstructionEvaluatorLShl(), longValue(), "lshl", EOpcodes.LSHL);
        }
    }
}


