package tokyo.peya.langjal.compiler.instructions.calc;

import org.junit.jupiter.api.Nested;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.calc.xneg.InstructionEvaluatorDNeg;
import tokyo.peya.langjal.compiler.instructions.calc.xneg.InstructionEvaluatorFNeg;
import tokyo.peya.langjal.compiler.instructions.calc.xneg.InstructionEvaluatorINeg;
import tokyo.peya.langjal.compiler.instructions.calc.xneg.InstructionEvaluatorLNeg;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;

import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.*;

public class TestXNeg {
    @Nested
    class TestINeg extends AbstractMathInstructionTestCase<JALParser.JvmInsInegContext, InstructionEvaluatorINeg> {
        TestINeg() {
            super(new InstructionEvaluatorINeg(), integerValue(), "ineg", EOpcodes.INEG);
        }
    }

    @Nested
    class TestFNeg extends AbstractMathInstructionTestCase<JALParser.JvmInsFnegContext, InstructionEvaluatorFNeg> {
        TestFNeg() {
            super(new InstructionEvaluatorFNeg(), floatValue(), "fneg", EOpcodes.FNEG);
        }
    }

    @Nested
    class TestDNeg extends AbstractMathInstructionTestCase<JALParser.JvmInsDnegContext, InstructionEvaluatorDNeg> {
        TestDNeg() {
            super(new InstructionEvaluatorDNeg(), doubleValue(), "dneg", EOpcodes.DNEG);
        }
    }

    @Nested
    class TestLNeg extends AbstractMathInstructionTestCase<JALParser.JvmInsLnegContext, InstructionEvaluatorLNeg> {
        TestLNeg() {
            super(new InstructionEvaluatorLNeg(), longValue(), "lneg", EOpcodes.LNEG);
        }
    }
}


