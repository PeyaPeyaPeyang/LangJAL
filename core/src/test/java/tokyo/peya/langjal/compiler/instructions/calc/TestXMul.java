package tokyo.peya.langjal.compiler.instructions.calc;

import org.junit.jupiter.api.Nested;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.calc.xmul.InstructionEvaluatorDMul;
import tokyo.peya.langjal.compiler.instructions.calc.xmul.InstructionEvaluatorFMul;
import tokyo.peya.langjal.compiler.instructions.calc.xmul.InstructionEvaluatorIMul;
import tokyo.peya.langjal.compiler.instructions.calc.xmul.InstructionEvaluatorLMul;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;

import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.*;

public class TestXMul {
    @Nested
    class TestIMul extends AbstractMathInstructionTestCase<JALParser.JvmInsImulContext, InstructionEvaluatorIMul> {
        TestIMul() {
            super(new InstructionEvaluatorIMul(), integerValue(), "imul", EOpcodes.IMUL);
        }
    }

    @Nested
    class TestFMul extends AbstractMathInstructionTestCase<JALParser.JvmInsFmulContext, InstructionEvaluatorFMul> {
        TestFMul() {
            super(new InstructionEvaluatorFMul(), floatValue(), "fmul", EOpcodes.FMUL);
        }
    }

    @Nested
    class TestDMul extends AbstractMathInstructionTestCase<JALParser.JvmInsDmulContext, InstructionEvaluatorDMul> {
        TestDMul() {
            super(new InstructionEvaluatorDMul(), doubleValue(), "dmul", EOpcodes.DMUL);
        }
    }

    @Nested
    class TestLMul extends AbstractMathInstructionTestCase<JALParser.JvmInsLmulContext, InstructionEvaluatorLMul> {
        TestLMul() {
            super(new InstructionEvaluatorLMul(), longValue(), "lmul", EOpcodes.LMUL);
        }
    }
}

