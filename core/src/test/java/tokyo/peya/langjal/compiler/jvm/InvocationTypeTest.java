package tokyo.peya.langjal.compiler.jvm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("InvocationType")
class InvocationTypeTest {

    @ParameterizedTest
    @CsvSource({
            "invokevirtual,182",
            "invokespecial,183",
            "invokestatic,184",
            "invokeinterface,185",
            "invokedynamic,186"
    })
    void fromOpcodeRecognizesAllTypes(String expectedName, int opcode) {
        InvocationType type = InvocationType.fromOpcode(opcode);
        assertEquals(expectedName, type.getName());
    }

    @Test
    void fromOpcodeThrowsForInvalidOpcode() {
        assertThrows(IllegalArgumentException.class, () -> InvocationType.fromOpcode(999));
    }

    @Test
    void fromOpcodeThrowsForNegativeOpcode() {
        assertThrows(IllegalArgumentException.class, () -> InvocationType.fromOpcode(-1));
    }

    @ParameterizedTest
    @CsvSource({
            "invokevirtual,invokevirtual",
            "invokespecial,invokespecial",
            "invokestatic,invokestatic",
            "invokeinterface,invokeinterface",
            "invokedynamic,invokedynamic"
    })
    void fromNameRecognizesAllTypes(String name, String expectedName) {
        InvocationType type = InvocationType.fromName(name);
        assertEquals(expectedName, type.getName());
    }

    @Test
    void fromNameThrowsForInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> InvocationType.fromName("unknown"));
    }

    @Test
    void fromNameThrowsForEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> InvocationType.fromName(""));
    }

    @ParameterizedTest
    @MethodSource("allTypes")
    void allTypesHaveValidOpcodes(InvocationType type) {
        assertEquals(type, InvocationType.fromOpcode(type.getOpcode()));
    }

    @ParameterizedTest
    @MethodSource("allTypes")
    void allTypesHaveValidNames(InvocationType type) {
        assertEquals(type, InvocationType.fromName(type.getName()));
    }

    static Stream<InvocationType> allTypes() {
        return Stream.of(InvocationType.values());
    }
}

