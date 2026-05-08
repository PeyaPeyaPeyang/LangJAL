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
import tokyo.peya.langjal.compiler.JALLexer;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractInstructionTestCase<P extends ParserRuleContext, T extends AbstractInstructionEvaluator<P>>
{
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

    public abstract String[] getValidInstructions();

    @ParameterizedTest
    @MethodSource("getValidInstructions")
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
}
