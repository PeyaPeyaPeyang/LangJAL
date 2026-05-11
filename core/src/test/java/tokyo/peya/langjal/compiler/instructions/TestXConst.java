package tokyo.peya.langjal.compiler.instructions;

import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.jupiter.api.Nested;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.utils.AbstractInstructionTestCase;
import tokyo.peya.langjal.compiler.instructions.utils.StackMachine;
import tokyo.peya.langjal.compiler.instructions.xconst.InstructionEvaluatorAConstNull;
import tokyo.peya.langjal.compiler.instructions.xconst.InstructionEvaluatorDConstN;
import tokyo.peya.langjal.compiler.instructions.xconst.InstructionEvaluatorFConstN;
import tokyo.peya.langjal.compiler.instructions.xconst.InstructionEvaluatorIConstN;
import tokyo.peya.langjal.compiler.instructions.xconst.InstructionEvaluatorLConstN;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;

import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.doubleValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.floatValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.integerValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.longValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.nullValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.create;

public class TestXConst
{
    private abstract static class XConstTestCase<T extends ParserRuleContext, E extends AbstractInstructionEvaluator<T>>
            extends AbstractInstructionTestCase<T, E>
    {
        private final StackMachine.StackValue outputValue;
        private final String syntax;
        private final int opcode;

        protected XConstTestCase(E evaluator, int... expectedOpCodes)
        {
            super(evaluator, expectedOpCodes);
            this.outputValue = null;
            this.syntax = null;
            this.opcode = -1;
        }

        protected XConstTestCase(E evaluator, StackMachine.StackValue outputValue, String syntax, int opcode)
        {
            super(evaluator, opcode);
            this.outputValue = outputValue;
            this.syntax = syntax;
            this.opcode = opcode;
        }

        protected AbstractInstructionTestCase.InstructionCase[] single(StackMachine.StackValue outputValue, String syntax, int opcode)
        {
            return set(
                    of(
                            create().expected(create(outputValue)),
                            syntax,
                            opcode
                    ),
                    of(
                            create(integerValue()).expected(create(integerValue(), outputValue)),
                            syntax,
                            opcode
                    )
            );
        }

        @Override
        public AbstractInstructionTestCase.InstructionCase[] getValidInstructionSyntaxes()
        {
            if (!(this.outputValue == null || this.syntax == null || this.opcode == -1))
            {
                return single(this.outputValue, this.syntax, this.opcode);
            }

            return set();
        }
    }

    @Nested
    class TestAConstNull extends XConstTestCase<JALParser.JvmInsAconstNullContext, InstructionEvaluatorAConstNull>
    {
        TestAConstNull()
        {
            super(new InstructionEvaluatorAConstNull(), nullValue(), "aconst_null", EOpcodes.ACONST_NULL);
        }
    }

    @Nested
    class TestDConstN extends XConstTestCase<JALParser.JvmInsDconstNContext, InstructionEvaluatorDConstN>
    {
        TestDConstN()
        {
            super(new InstructionEvaluatorDConstN(), EOpcodes.DCONST_0, EOpcodes.DCONST_1);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    of(create().expected(create(doubleValue())), "dconst_0", EOpcodes.DCONST_0),
                    of(create().expected(create(doubleValue())), "dconst_1", EOpcodes.DCONST_1)
            );
        }
    }

    @Nested
    class TestFConstN extends XConstTestCase<JALParser.JvmInsFconstNContext, InstructionEvaluatorFConstN>
    {
        TestFConstN()
        {
            super(new InstructionEvaluatorFConstN(), EOpcodes.FCONST_0, EOpcodes.FCONST_1, EOpcodes.FCONST_2);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    of(create().expected(create(floatValue())), "fconst_0", EOpcodes.FCONST_0),
                    of(create().expected(create(floatValue())), "fconst_1", EOpcodes.FCONST_1),
                    of(create().expected(create(floatValue())), "fconst_2", EOpcodes.FCONST_2)
            );
        }
    }

    @Nested
    class TestIConstN extends XConstTestCase<JALParser.JvmInsIconstNContext, InstructionEvaluatorIConstN>
    {
        TestIConstN()
        {
            super(new InstructionEvaluatorIConstN(), EOpcodes.ICONST_0, EOpcodes.ICONST_1, EOpcodes.ICONST_2,
                    EOpcodes.ICONST_3, EOpcodes.ICONST_4, EOpcodes.ICONST_5);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    of(create().expected(create(integerValue())), "iconst_m1", EOpcodes.ICONST_M1),
                    of(create().expected(create(integerValue())), "iconst_0", EOpcodes.ICONST_0),
                    of(create().expected(create(integerValue())), "iconst_1", EOpcodes.ICONST_1),
                    of(create().expected(create(integerValue())), "iconst_2", EOpcodes.ICONST_2),
                    of(create().expected(create(integerValue())), "iconst_3", EOpcodes.ICONST_3),
                    of(create().expected(create(integerValue())), "iconst_4", EOpcodes.ICONST_4),
                    of(create().expected(create(integerValue())), "iconst_5", EOpcodes.ICONST_5)
            );
        }
    }

    @Nested
    class TestLConstN extends XConstTestCase<JALParser.JvmInsLconstNContext, InstructionEvaluatorLConstN>
    {
        TestLConstN()
        {
            super(new InstructionEvaluatorLConstN(), EOpcodes.LCONST_0, EOpcodes.LCONST_1);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    of(create().expected(create(longValue())), "lconst_0", EOpcodes.LCONST_0),
                    of(create().expected(create(longValue())), "lconst_1", EOpcodes.LCONST_1)
            );
        }
    }
}
