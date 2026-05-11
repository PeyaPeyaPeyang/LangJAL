package tokyo.peya.langjal.compiler.utils;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import tokyo.peya.langjal.compiler.exceptions.IllegalValueException;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tokyo.peya.langjal.compiler.utils.EvaluatorCommons.asInteger;
import static tokyo.peya.langjal.compiler.utils.EvaluatorCommons.asString;
import static tokyo.peya.langjal.compiler.utils.EvaluatorCommons.getNumberType;
import static tokyo.peya.langjal.compiler.utils.EvaluatorCommons.isNumber;
import static tokyo.peya.langjal.compiler.utils.EvaluatorCommons.toBoolean;
import static tokyo.peya.langjal.compiler.utils.EvaluatorCommons.toNumber;

@DisplayName("EvaluatorCommons")
class EvaluatorCommonsTest
{
    @ParameterizedTest
    @MethodSource("stringTokens")
    void asStringRemovesMatchingQuotesAndUnescapesContent(String tokenText, String expected)
    {
        assertEquals(expected, asString(token(tokenText)));
    }

    @Test
    void asStringReturnsNullForEmptyTokenText()
    {
        assertNull(asString(token("")));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void getNumberTypeReturnsNullForBlankInput(String number)
    {
        assertNull(getNumberType(number));
    }

    @ParameterizedTest
    @CsvSource({
            "123, may-int",
            "12.5, may-double",
            "12.5f, float",
            "12.5D, double",
            "123L, long",
            "0x7f, may-int-hex",
            "-0x7fL, long-hex"
    })
    void getNumberTypeClassifiesNumericSuffixes(String number, String expectedType)
    {
        assertEquals(expectedType, getNumberType(number));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void toNumberReturnsNullForBlankInput(String number)
    {
        assertNull(toNumber(number));
    }

    @ParameterizedTest
    @MethodSource("numbers")
    void toNumberParsesIntegersFloatingPointAndHexValues(String number, Number expected)
    {
        assertEquals(expected, toNumber(number));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "0x"})
    void toNumberRejectsMalformedNumbers(String number)
    {
        assertThrows(IllegalArgumentException.class, () -> toNumber(number));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "abc")
    void isNumberReturnsFalseForInvalidNumbers(String number)
    {
        assertFalse(isNumber(number));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "1.25f", "-0xffL"})
    void isNumberReturnsTrueForValidNumbers(String number)
    {
        assertTrue(isNumber(number));
    }

    @Test
    void terminalNumberHelpersParseAndWrapInvalidValues()
    {
        assertEquals(42, asInteger(token("42")));
        assertEquals(42L, toNumber(token("42L")));
        assertThrows(IllegalValueException.class, () -> asInteger(token("bad")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "TRUE", "1"})
    void toBooleanAcceptsTrueValues(String value)
    {
        assertTrue(toBoolean(token(value)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "FALSE", "0"})
    void toBooleanAcceptsFalseValues(String value)
    {
        assertFalse(toBoolean(token(value)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"yes", "2", ""})
    void toBooleanRejectsInvalidValues(String value)
    {
        assertThrows(IllegalValueException.class, () -> toBoolean(token(value)));
    }

    private static TerminalNode token(String text)
    {
        return new TerminalNodeImpl(new CommonToken(0, text));
    }

    private static Stream<Arguments> stringTokens()
    {
        return Stream.of(
                Arguments.of("plain", "plain"),
                Arguments.of("\"quoted\"", "quoted"),
                Arguments.of("'single'", "single"),
                Arguments.of("\"a\\\"b\\'c\\\\d\"", "a\"b'c\\d")
        );
    }

    private static Stream<Arguments> numbers()
    {
        return Stream.of(
                Arguments.of("123", 123),
                Arguments.of("-0xff", -255),
                Arguments.of("0xffL", 255L),
                Arguments.of("-0xffL", -255L),
                Arguments.of("12L", 12L),
                Arguments.of("1.25", 1.25d),
                Arguments.of("1.25D", 1.25d),
                Arguments.of("1.25f", 1.25f)
        );
    }
}
