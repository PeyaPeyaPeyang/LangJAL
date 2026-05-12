package tokyo.peya.langjal.compiler.instructions;

import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.jupiter.api.Nested;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.utils.AbstractInstructionTestCase;
import tokyo.peya.langjal.compiler.instructions.utils.StackMachine;
import tokyo.peya.langjal.compiler.instructions.xaload.*;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;

import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.*;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.create;

public class TestXALoad {
    private abstract class XALoadTestCase<T extends ParserRuleContext, E extends AbstractInstructionEvaluator<T>>
            extends AbstractInstructionTestCase<T, E> {
        protected XALoadTestCase(E evaluator, int... expectedOpcodes) {
            super(evaluator, expectedOpcodes);
        }

        protected InstructionCase load(StackMachine.StackValue arrayType,
                                       StackMachine.StackValue resultType,
                                       String syntax,
                                       int opcode) {
            return of(
                    create(arrayType, integerValue()).expected(create(resultType)),
                    syntax,
                    opcode
            );
        }

        protected InstructionCase loadWithBase(StackMachine.StackValue arrayType,
                                               StackMachine.StackValue resultType,
                                               String syntax,
                                               int opcode) {
            return of(
                    create(object(TypeDescriptor.className("java/lang/String")), arrayType, integerValue())
                            .expected(create(object(TypeDescriptor.className("java/lang/String")), resultType)),
                    syntax,
                    opcode
            );
        }
    }

    @Nested
    class TestAALoadCase extends XALoadTestCase<JALParser.JvmInsAaloadContext, InstructionEvaluatorAALoad> {
        TestAALoadCase() {
            super(new InstructionEvaluatorAALoad(), EOpcodes.AALOAD);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    load(
                            object(TypeDescriptor.parse("[Ljava/lang/String;")),
                            object(TypeDescriptor.className("java/lang/String")),
                            "aaload",
                            EOpcodes.AALOAD
                    ),
                    loadWithBase(
                            object(TypeDescriptor.parse("[Ljava/lang/String;")),
                            object(TypeDescriptor.className("java/lang/String")),
                            "aaload",
                            EOpcodes.AALOAD
                    )
            );
        }
    }

    @Nested
    class TestBALoadCase extends XALoadTestCase<JALParser.JvmInsBaloadContext, InstructionEvaluatorBALoad> {
        TestBALoadCase() {
            super(new InstructionEvaluatorBALoad(), EOpcodes.BALOAD);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    load(object(TypeDescriptor.parse("[B")), integerValue(), "baload", EOpcodes.BALOAD),
                    loadWithBase(object(TypeDescriptor.parse("[B")), integerValue(), "baload", EOpcodes.BALOAD)
            );
        }
    }

    @Nested
    class TestCALoadCase extends XALoadTestCase<JALParser.JvmInsCaloadContext, InstructionEvaluatorCALoad> {
        TestCALoadCase() {
            super(new InstructionEvaluatorCALoad(), EOpcodes.CALOAD);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    load(object(TypeDescriptor.parse("[C")), integerValue(), "caload", EOpcodes.CALOAD),
                    loadWithBase(object(TypeDescriptor.parse("[C")), integerValue(), "caload", EOpcodes.CALOAD)
            );
        }
    }

    @Nested
    class TestDALoadCase extends XALoadTestCase<JALParser.JvmInsDaloadContext, InstructionEvaluatorDALoad> {
        TestDALoadCase() {
            super(new InstructionEvaluatorDALoad(), EOpcodes.DALOAD);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    load(object(TypeDescriptor.parse("[D")), doubleValue(), "daload", EOpcodes.DALOAD),
                    loadWithBase(object(TypeDescriptor.parse("[D")), doubleValue(), "daload", EOpcodes.DALOAD)
            );
        }
    }

    @Nested
    class TestFALoadCase extends XALoadTestCase<JALParser.JvmInsFaloadContext, InstructionEvaluatorFALoad> {
        TestFALoadCase() {
            super(new InstructionEvaluatorFALoad(), EOpcodes.FALOAD);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    load(object(TypeDescriptor.parse("[F")), floatValue(), "faload", EOpcodes.FALOAD),
                    loadWithBase(object(TypeDescriptor.parse("[F")), floatValue(), "faload", EOpcodes.FALOAD)
            );
        }
    }

    @Nested
    class TestIALoadCase extends XALoadTestCase<JALParser.JvmInsIaloadContext, InstructionEvaluatorIALoad> {
        TestIALoadCase() {
            super(new InstructionEvaluatorIALoad(), EOpcodes.IALOAD);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    load(object(TypeDescriptor.parse("[I")), integerValue(), "iaload", EOpcodes.IALOAD),
                    loadWithBase(object(TypeDescriptor.parse("[I")), integerValue(), "iaload", EOpcodes.IALOAD)
            );
        }
    }

    @Nested
    class TestLALoadCase extends XALoadTestCase<JALParser.JvmInsLaloadContext, InstructionEvaluatorLALoad> {
        TestLALoadCase() {
            super(new InstructionEvaluatorLALoad(), EOpcodes.LALOAD);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    load(object(TypeDescriptor.parse("[J")), longValue(), "laload", EOpcodes.LALOAD),
                    loadWithBase(object(TypeDescriptor.parse("[J")), longValue(), "laload", EOpcodes.LALOAD)
            );
        }
    }

    @Nested
    class TestSALoadCase extends XALoadTestCase<JALParser.JvmInsSaloadContext, InstructionEvaluatorSALoad> {
        TestSALoadCase() {
            super(new InstructionEvaluatorSALoad(), EOpcodes.SALOAD);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    load(object(TypeDescriptor.parse("[S")), integerValue(), "saload", EOpcodes.SALOAD),
                    loadWithBase(object(TypeDescriptor.parse("[S")), integerValue(), "saload", EOpcodes.SALOAD)
            );
        }
    }
}
