package tokyo.peya.langjal.compiler.jvm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("EOpcodes")
class EOpcodesTest {

    @ParameterizedTest
    @CsvSource({
            "nop,0",
            "aconst_null,1",
            "iconst_0,3",
            "bipush,16",
            "ldc,18",
            "getstatic,178",
            "invokevirtual,182",
            "return,177"
    })
    void findOpcodeLocatesValidInstructions(String name, int expected) {
        assertEquals(expected, EOpcodes.findOpcode(name));
    }

    @ParameterizedTest
    @CsvSource({
            "NOP,0",
            "ACONST_NULL,1",
            "ICONST_0,3"
    })
    void findOpcodeIsCaseInsensitive(String name, int expected) {
        assertEquals(expected, EOpcodes.findOpcode(name));
    }

    @ParameterizedTest
    @ValueSource(strings = {"unknown", "invalid_opcode", "xyz"})
    void findOpcodeReturnsNegativeOneForUnknown(String unknown) {
        assertEquals(-1, EOpcodes.findOpcode(unknown));
    }

    @ParameterizedTest
    @CsvSource({
            "0,nop",
            "1,aconst_null",
            "3,iconst_0",
            "16,bipush",
            "18,ldc",
            "178,getstatic",
            "182,invokevirtual",
            "177,return"
    })
    void getNameReturnsLowercaseNames(int opcode, String expected) {
        assertEquals(expected, EOpcodes.getName(opcode));
    }

    @Test
    void getNameThrowsForInvalidOpcode() {
        assertThrows(IllegalArgumentException.class, () -> EOpcodes.getName(999));
    }

    @Test
    void getNameThrowsForNegativeOpcode() {
        assertThrows(Exception.class, () -> EOpcodes.getName(-1));
    }

    @ParameterizedTest
    @CsvSource({
            "0,1",
            "1,1",
            "3,1",
            "89,1",
            "16,2",
            "17,3",
            "18,2",
            "197,4",
            "185,5"
    })
    void getOpcodeSizeReturnsCorrectSize(int opcode, int expectedSize) {
        assertEquals(expectedSize, EOpcodes.getOpcodeSize(opcode));
    }

    @ParameterizedTest
    @ValueSource(ints = {170, 171, 196})
    void getOpcodeSizeThrowsForVariableSized(int opcode) {
        assertThrows(IllegalArgumentException.class, () -> EOpcodes.getOpcodeSize(opcode));
    }

    @Test
    void instructionNamesArrayIsPopulated() {
        assertTrue(EOpcodes.INSTRUCTION_NAMES.length > 0);
        assertEquals("nop", EOpcodes.INSTRUCTION_NAMES[0]);
    }
}



