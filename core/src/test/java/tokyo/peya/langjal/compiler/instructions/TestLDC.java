package tokyo.peya.langjal.compiler.instructions;

import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.jupiter.api.Nested;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.ldc.InstructionEvaluatorLDC;
import tokyo.peya.langjal.compiler.instructions.ldc.InstructionEvaluatorLDCW;
import tokyo.peya.langjal.compiler.instructions.ldc.InstructionEvaluatorLDCW2;
import tokyo.peya.langjal.compiler.instructions.utils.AbstractInstructionTestCase;
import tokyo.peya.langjal.compiler.instructions.utils.StackMachine;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.doubleValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.floatValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.integerValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.longValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.object;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.create;

public class TestLDC
{
    private abstract class LDCTestCase<T extends ParserRuleContext, E extends AbstractInstructionEvaluator<T>>
            extends AbstractInstructionTestCase<T, E>
    {
        protected LDCTestCase(E evaluator, int... expectedOpCodes)
        {
            super(evaluator, expectedOpCodes);
        }

        protected InstructionCase constantLoad(Object value, StackMachine.StackValue stackValue, String syntax)
        {
            return of(
                    create().expected(create(stackValue)),
                    syntax,
                    new LdcInsnNode(value)
            );
        }

        protected InstructionCase constantLoadWithBase(Object value, StackMachine.StackValue stackValue, String syntax)
        {
            return of(
                    create(object(TypeDescriptor.className("java/lang/String")))
                            .expected(create(object(TypeDescriptor.className("java/lang/String")), stackValue)),
                    syntax,
                    new LdcInsnNode(value)
            );
        }

        @Override
        protected void assertInstructionEquals(AbstractInsnNode expected, AbstractInsnNode actual)
        {
            super.assertInstructionEquals(expected, actual);

            LdcInsnNode expectedLdc = (LdcInsnNode) expected;
            LdcInsnNode actualLdc = (LdcInsnNode) actual;
            assertEquals(expectedLdc.cst, actualLdc.cst, "ldc constant does not match");
        }
    }

    @Nested
    class TestLdc extends LDCTestCase<JALParser.JvmInsLdcContext, InstructionEvaluatorLDC>
    {
        TestLdc()
        {
            super(new InstructionEvaluatorLDC(), EOpcodes.LDC);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    constantLoad(123, integerValue(), "ldc 123"),
                    constantLoadWithBase(123, integerValue(), "ldc 123"),
                    constantLoad(1.5f, floatValue(), "ldc 1.5f"),
                    constantLoadWithBase(1.5f, floatValue(), "ldc 1.5f"),
                    constantLoad("hello", object(TypeDescriptor.className("java/lang/String")), "ldc \"hello\"")
            );
        }
    }

    @Nested
    class TestLdcW extends LDCTestCase<JALParser.JvmInsLdcWContext, InstructionEvaluatorLDCW>
    {
        TestLdcW()
        {
            super(new InstructionEvaluatorLDCW(), EOpcodes.LDC_W);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    constantLoad(456, integerValue(), "ldc_w 456"),
                    constantLoadWithBase(456, integerValue(), "ldc_w 456"),
                    constantLoad(2.5f, floatValue(), "ldc_w 2.5f")
            );
        }
    }

    @Nested
    class TestLdc2W extends LDCTestCase<JALParser.JvmInsLdc2WContext, InstructionEvaluatorLDCW2>
    {
        TestLdc2W()
        {
            super(new InstructionEvaluatorLDCW2(), EOpcodes.LDC2_W);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    constantLoad(123L, longValue(), "ldc2_w 123L"),
                    constantLoadWithBase(123L, longValue(), "ldc2_w 123L"),
                    constantLoad(3.5d, doubleValue(), "ldc2_w 3.5d")
            );
        }
    }
}
