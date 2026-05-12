package tokyo.peya.langjal.compiler.instructions;

import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.jupiter.api.Nested;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.cast.*;
import tokyo.peya.langjal.compiler.instructions.utils.AbstractInstructionTestCase;
import tokyo.peya.langjal.compiler.instructions.utils.StackMachine;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;

import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.*;

public class TestX2X {
    private abstract static class X2XTestCase<T extends ParserRuleContext, E extends AbstractSingleInstructionEvaluator<T>>
            extends AbstractInstructionTestCase<T, E> {
        private final StackMachine.StackValue inputValue;
        private final StackMachine.StackValue outputValue;
        private final String syntax;
        private final int opcode;

        protected X2XTestCase(E evaluator, int... expectedOpCodes) {
            super(evaluator, expectedOpCodes);
            this.inputValue = null;
            this.outputValue = null;
            this.syntax = null;
            this.opcode = -1;
        }

        protected X2XTestCase(E evaluator, StackMachine.StackValue inputValue, StackMachine.StackValue outputValue,
                              String syntax, int opcode) {
            super(evaluator, opcode);
            this.inputValue = inputValue;
            this.outputValue = outputValue;
            this.syntax = syntax;
            this.opcode = opcode;
        }

        protected AbstractInstructionTestCase.InstructionCase[] single(StackMachine.StackValue inputValue,
                                                                       StackMachine.StackValue outputValue,
                                                                       String syntax, int opcode) {
            return set(
                    of(
                            StackMachine.create(inputValue).expected(StackMachine.create(outputValue)),
                            syntax,
                            opcode
                    ),
                    of(
                            StackMachine.create(integerValue(), inputValue)
                                    .expected(StackMachine.create(integerValue(), outputValue)),
                            syntax,
                            opcode
                    )
            );
        }

        @Override
        public AbstractInstructionTestCase.InstructionCase[] getValidInstructionSyntaxes() {
            if (!(this.inputValue == null || this.outputValue == null || this.syntax == null || this.opcode == -1)) {
                return single(this.inputValue, this.outputValue, this.syntax, this.opcode);
            }

            return set();
        }
    }

    @Nested
    class TestD2F extends X2XTestCase<JALParser.JvmInsD2FContext, InstructionEvaluatorD2F> {
        TestD2F() {
            super(new InstructionEvaluatorD2F(), doubleValue(), floatValue(), "d2f", EOpcodes.D2F);
        }
    }

    @Nested
    class TestD2I extends X2XTestCase<JALParser.JvmInsD2IContext, InstructionEvaluatorD2I> {
        TestD2I() {
            super(new InstructionEvaluatorD2I(), doubleValue(), integerValue(), "d2i", EOpcodes.D2I);
        }
    }

    @Nested
    class TestD2L extends X2XTestCase<JALParser.JvmInsD2LContext, InstructionEvaluatorD2L> {
        TestD2L() {
            super(new InstructionEvaluatorD2L(), doubleValue(), longValue(), "d2l", EOpcodes.D2L);
        }
    }

    @Nested
    class TestF2D extends X2XTestCase<JALParser.JvmInsF2DContext, InstructionEvaluatorF2D> {
        TestF2D() {
            super(new InstructionEvaluatorF2D(), floatValue(), doubleValue(), "f2d", EOpcodes.F2D);
        }
    }

    @Nested
    class TestF2I extends X2XTestCase<JALParser.JvmInsF2IContext, InstructionEvaluatorF2I> {
        TestF2I() {
            super(new InstructionEvaluatorF2I(), floatValue(), integerValue(), "f2i", EOpcodes.F2I);
        }
    }

    @Nested
    class TestF2L extends X2XTestCase<JALParser.JvmInsF2LContext, InstructionEvaluatorF2L> {
        TestF2L() {
            super(new InstructionEvaluatorF2L(), floatValue(), longValue(), "f2l", EOpcodes.F2L);
        }
    }

    @Nested
    class TestI2B extends X2XTestCase<JALParser.JvmInsI2BContext, InstructionEvaluatorI2B> {
        TestI2B() {
            super(new InstructionEvaluatorI2B(), integerValue(), integerValue(), "i2b", EOpcodes.I2B);
        }
    }

    @Nested
    class TestI2C extends X2XTestCase<JALParser.JvmInsI2CContext, InstructionEvaluatorI2C> {
        TestI2C() {
            super(new InstructionEvaluatorI2C(), integerValue(), integerValue(), "i2c", EOpcodes.I2C);
        }
    }

    @Nested
    class TestI2D extends X2XTestCase<JALParser.JvmInsI2DContext, InstructionEvaluatorI2D> {
        TestI2D() {
            super(new InstructionEvaluatorI2D(), integerValue(), doubleValue(), "i2d", EOpcodes.I2D);
        }
    }

    @Nested
    class TestI2F extends X2XTestCase<JALParser.JvmInsI2FContext, InstructionEvaluatorI2F> {
        TestI2F() {
            super(new InstructionEvaluatorI2F(), integerValue(), floatValue(), "i2f", EOpcodes.I2F);
        }
    }

    @Nested
    class TestI2L extends X2XTestCase<JALParser.JvmInsI2LContext, InstructionEvaluatorI2L> {
        TestI2L() {
            super(new InstructionEvaluatorI2L(), integerValue(), longValue(), "i2l", EOpcodes.I2L);
        }
    }

    @Nested
    class TestI2S extends X2XTestCase<JALParser.JvmInsI2SContext, InstructionEvaluatorI2S> {
        TestI2S() {
            super(new InstructionEvaluatorI2S(), integerValue(), integerValue(), "i2s", EOpcodes.I2S);
        }
    }

    @Nested
    class TestL2D extends X2XTestCase<JALParser.JvmInsL2DContext, InstructionEvaluatorL2D> {
        TestL2D() {
            super(new InstructionEvaluatorL2D(), longValue(), doubleValue(), "l2d", EOpcodes.L2D);
        }
    }

    @Nested
    class TestL2F extends X2XTestCase<JALParser.JvmInsL2FContext, InstructionEvaluatorL2F> {
        TestL2F() {
            super(new InstructionEvaluatorL2F(), longValue(), floatValue(), "l2f", EOpcodes.L2F);
        }
    }

    @Nested
    class TestL2I extends X2XTestCase<JALParser.JvmInsL2IContext, InstructionEvaluatorL2I> {
        TestL2I() {
            super(new InstructionEvaluatorL2I(), longValue(), integerValue(), "l2i", EOpcodes.L2I);
        }
    }
}

