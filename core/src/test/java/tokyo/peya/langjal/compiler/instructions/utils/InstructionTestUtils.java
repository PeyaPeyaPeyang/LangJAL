package tokyo.peya.langjal.compiler.instructions.utils;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import tokyo.peya.langjal.compiler.JALLexer;
import tokyo.peya.langjal.compiler.JALParser;

import java.util.function.Function;

public class InstructionTestUtils
{
    public static <T extends ParserRuleContext> T parseInstruction(String instruction, Function<? super JALParser.InstructionContext, T> mapper)
    {
        return parseSource(instruction, p -> mapper.apply(p.instruction()));
    }

    public static <T extends ParserRuleContext> T parseSource(String source, Function<? super JALParser, T> mapper)
    {
        JALLexer lexer = new JALLexer(CharStreams.fromString(source));
        TokenStream tokens = new CommonTokenStream(lexer);
        JALParser parser = new JALParser(tokens);
        return mapper.apply(parser);
    }
}
