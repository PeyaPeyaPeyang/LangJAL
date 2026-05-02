package tokyo.peya.langjal.compiler.jvm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("AccessAttribute")
class AccessAttributeTest
{

    @Test
    void enumValuesHaveUniqueNamesAndFlags() {
        AccessAttribute[] values = AccessAttribute.values();

        assertEquals(16, values.length);
        assertEquals(values.length, Arrays.stream(values).map(AccessAttribute::getName).distinct().count());
        assertEquals(values.length, Arrays.stream(values).mapToInt(AccessAttribute::getAsmFlag).distinct().count());

        for (AccessAttribute attr : values) {
            assertNotNull(attr.getName());
            assertFalse(attr.getName().isBlank());
            assertTrue(attr.getAsmFlag() > 0);
            assertSame(attr, AccessAttribute.fromString(attr.getName()));
        }
    }

    @ParameterizedTest
    @CsvSource({
            "STATIC,static",
            "final,final",
            "  AbStRaCt  ,abstract",
            "InTeRfAcE,interface",
            "  volatile  ,volatile"
    })
    void fromStringAcceptsCaseAndWhitespace(String input, String expected) {
        assertEquals(expected, AccessAttribute.fromString(input).getName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "unknown", "not_an_attribute"})
    void invalidStringsFail(String input) {
        assertThrows(IllegalArgumentException.class, () -> AccessAttribute.fromString(input));
    }

    @Test
    @SuppressWarnings({"ConstantConditions", "DataFlowIssue"})
    void nullInputFails() {
        assertThrows(NullPointerException.class, () -> AccessAttribute.fromString(null));
    }

    @ParameterizedTest
    @MethodSource("allAttributes")
    void roundTripsThroughName(AccessAttribute attribute) {
        assertSame(attribute, AccessAttribute.fromString(attribute.getName()));
    }

    static Stream<AccessAttribute> allAttributes() {
        return Arrays.stream(AccessAttribute.values());
    }
}

