package tokyo.peya.langjal.compiler.jvm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("AccessLevel")
class AccessLevelTest {

    static Stream<AccessLevel> allLevels() {
        return Stream.of(AccessLevel.values());
    }

    @Test
    void toStringReturnsName() {
        assertEquals("public", AccessLevel.PUBLIC.toString());
        assertEquals("protected", AccessLevel.PROTECTED.toString());
        assertEquals("private", AccessLevel.PRIVATE.toString());
        assertEquals("package-private", AccessLevel.PACKAGE_PRIVATE.toString());
    }

    @ParameterizedTest
    @CsvSource(
            {
                    "public,public",
                    "protected,protected",
                    "private,private",
                    "package-private,package-private",
                    "package,package-private"
            }
    )
    void fromStringParsesValidNames(String input, String expectedName) {
        assertEquals(expectedName, AccessLevel.fromString(input).getName());
    }

    @Test
    void fromStringAcceptsEmptyString() {
        assertEquals(AccessLevel.PACKAGE_PRIVATE, AccessLevel.fromString(""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "unknown", "xyz"})
    void fromStringThrowsForInvalidNames(String invalid) {
        assertThrows(IllegalArgumentException.class, () -> AccessLevel.fromString(invalid));
    }

    @Test
    void fromAccessRecognizesPublic() {
        assertEquals(AccessLevel.PUBLIC, AccessLevel.fromAccess(EOpcodes.ACC_PUBLIC));
    }

    @Test
    void fromAccessRecognizesProtected() {
        assertEquals(AccessLevel.PROTECTED, AccessLevel.fromAccess(EOpcodes.ACC_PROTECTED));
    }

    @Test
    void fromAccessRecognizesPrivate() {
        assertEquals(AccessLevel.PRIVATE, AccessLevel.fromAccess(EOpcodes.ACC_PRIVATE));
    }

    @Test
    void fromAccessDefaultsToPackagePrivate() {
        assertEquals(AccessLevel.PACKAGE_PRIVATE, AccessLevel.fromAccess(0));
    }

    @Test
    void fromAccessPrioritizesPublic() {
        int flags = EOpcodes.ACC_PUBLIC | EOpcodes.ACC_PROTECTED;
        assertEquals(AccessLevel.PUBLIC, AccessLevel.fromAccess(flags));
    }

    @ParameterizedTest
    @MethodSource("allLevels")
    void allLevelsHaveNames(AccessLevel level) {
        assertEquals(level.getName(), level.toString());
    }
}


