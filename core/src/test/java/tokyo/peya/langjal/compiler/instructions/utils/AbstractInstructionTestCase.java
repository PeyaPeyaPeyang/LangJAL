package tokyo.peya.langjal.compiler.instructions.utils;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.compiler.JALLexer;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractInstructionTestCase<P extends ParserRuleContext, T extends AbstractInstructionEvaluator<P>>
{
    private static final ClassNode TEST_DUMMY_CLASS;
    private static final MethodNode TEST_DUMMY_METHOD;

    static {
        TEST_DUMMY_CLASS = new ClassNode();
        TEST_DUMMY_CLASS.visit(
                EOpcodes.V1_8,
                EOpcodes.ACC_PUBLIC,
                "TestDummyClass",
                null,
                "java/lang/Object",
                null
        );

        TEST_DUMMY_METHOD = new MethodNode(
                EOpcodes.ACC_PUBLIC,
                "testDummyMethod",
                "()V",
                null,
                null
        );
        TEST_DUMMY_CLASS.methods.add(TEST_DUMMY_METHOD);
    }

    private final int[] expectedOpCodes;
    private final T evaluator;

    public AbstractInstructionTestCase(T evaluator, int... expectedOpCodes)
    {
        this.evaluator = evaluator;
        this.expectedOpCodes = expectedOpCodes;
    }

    @Test
    public void checkEvaluatorAcceptsExpectedOpCodes()
    {
        int[] actualOpCodes = this.evaluator.getEvaluatableOpcodes();
        for (int expectedOpCode : this.expectedOpCodes)
        {
            boolean found = false;
            for (int actualOpCode : actualOpCodes)
            {
                if (actualOpCode == expectedOpCode)
                {
                    found = true;
                    break;
                }
            }
            assertTrue(found, String.format("Expected opcode %d not found in evaluator's accepted opcodes", expectedOpCode));
        }

        Assertions.assertEquals(this.expectedOpCodes.length, actualOpCodes.length, "Evaluator accepts unexpected number of opcodes");
    }

    public abstract String[] getValidInstructionSyntaxes();

    @ParameterizedTest
    @MethodSource("getValidInstructionSyntaxes")
    public void testParseValidInstructions(String instruction)
    {
        try
        {
            parseInstruction(instruction, this.evaluator::map);
        }
        catch (Exception e)
        {
            Assertions.fail(String.format("Valid instruction '%s' threw an exception: %s", instruction, e.getMessage()));
        }
    }

    public abstract StackMachine[] validSituations();

    @ParameterizedTest
    @MethodSource("validSituations")
    public void testFrameDifferenceEvaluations(StackMachine situation)
    {
        if (this instanceof AbstractInstructionTestCase.Same<P,T>) {
            return;  // Same タイプのテストケースは，フレームの操作が無いため。
        }

        InstructionInfo info = new InstructionInfo(
                this.evaluator,
                TEST_DUMMY_CLASS,
                TEST_DUMMY_METHOD,
                this.expectedOpCodes[0],
                0,
                null,
                1,
                0
        );

        FrameDifferenceInfo difference = this.evaluator.getFrameDifferenceInfo(info);
        situation.emulate(difference);
    }

    protected static <T extends ParserRuleContext> T parseInstruction(String instruction, Function<? super JALParser.InstructionContext, T> mapper)
    {
        return parseSource(instruction, p -> mapper.apply(p.instruction()));
    }

    protected static <T extends ParserRuleContext> T parseSource(String source, Function<? super JALParser, T> mapper)
    {
        JALLexer lexer = new JALLexer(CharStreams.fromString(source));
        TokenStream tokens = new CommonTokenStream(lexer);
        JALParser parser = new JALParser(tokens);
        return mapper.apply(parser);
    }


    public abstract static class Same<P1 extends ParserRuleContext, T1 extends AbstractInstructionEvaluator<P1>>
            extends AbstractInstructionTestCase<P1, T1>
    {
        public Same(T1 evaluator, int... expectedOpCodes)
        {
            super(evaluator, expectedOpCodes);
        }

        @Override
        public final StackMachine[] validSituations()
        {
            return new StackMachine[0];
        }
    }
}
