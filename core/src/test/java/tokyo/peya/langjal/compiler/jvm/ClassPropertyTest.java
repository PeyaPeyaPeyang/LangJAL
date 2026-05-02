package tokyo.peya.langjal.compiler.jvm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("ClassProperty")
class ClassPropertyTest {

    @ParameterizedTest
    @CsvSource({
            "major_version,MAJOR_VERSION",
            "minor_version,MINOR_VERSION",
            "super_class,SUPER_CLASS",
            "interfaces,INTERFACES",
            "unknown,UNKNOWN"
    })
    void fromStringRecognizesPropertiesCase(String input, String expectedType) {
        assertEquals(ClassProperty.valueOf(expectedType), ClassProperty.fromString(input));
    }

    @ParameterizedTest
    @CsvSource({
            "MAJOR_VERSION,major_version",
            "MINOR_VERSION,minor_version",
            "SUPER_CLASS,super_class",
            "INTERFACES,interfaces",
            "UNKNOWN,unknown"
    })
    void fromStringIsCaseInsensitive(String uppercase, String expected) {
        assertEquals(expected, ClassProperty.fromString(uppercase.toLowerCase()).getName());
    }

    @Test
    void fromStringReturnsUnknownForInvalid() {
        assertEquals(ClassProperty.UNKNOWN, ClassProperty.fromString("invalid"));
        assertEquals(ClassProperty.UNKNOWN, ClassProperty.fromString("not_a_property"));
    }

    @ParameterizedTest
    @MethodSource("allProperties")
    void allPropertiesAreRecognizable(ClassProperty property) {
        assertEquals(property, ClassProperty.fromString(property.getName()));
    }

    static Stream<ClassProperty> allProperties() {
        return Stream.of(ClassProperty.values());
    }
}

