package tokyo.peya.langjal.compiler.jvm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EOpcodes")
class EOpcodesUtilTest {

    @ParameterizedTest
    @CsvSource(
            {
                    "nop,0",
                    "aconst_null,1",
                    "iconst_0,3",
                    "bipush,16",
                    "ldc,18",
                    "getstatic,178",
                    "invokevirtual,182",
                    "return,177"
            }
    )
    void findOpcodeLocatesInstructions(String name, int expectedOpcode) {
        assertEquals(expectedOpcode, EOpcodes.findOpcode(name));
    }

    @ParameterizedTest
    @CsvSource(
            {
                    "NOP,0",
                    "ACONST_NULL,1",
                    "ICONST_0,3"
            }
    )
    void findOpcodeHandlesCaseInsensitivity(String name, int expectedOpcode) {
        assertEquals(expectedOpcode, EOpcodes.findOpcode(name));
    }

    @ParameterizedTest
    @ValueSource(strings = {"unknown", "invalid_opcode", "xyz"})
    void findOpcodeReturnsNegativeOne(String unknown) {
        assertEquals(-1, EOpcodes.findOpcode(unknown));
    }

    @ParameterizedTest
    @CsvSource(
            {
                    "0,nop",
                    "1,aconst_null",
                    "3,iconst_0",
                    "16,bipush",
                    "18,ldc",
                    "178,getstatic",
                    "182,invokevirtual",
                    "177,return"
            }
    )
    void getNameReturnsOpcodeNames(int opcode, String expectedName) {
        assertEquals(expectedName, EOpcodes.getName(opcode));
    }

    @Test
    void getNameThrowsForInvalid() {
        assertThrows(IllegalArgumentException.class, () -> EOpcodes.getName(999));
    }

    @ParameterizedTest
    @CsvSource(
            {
                    "0,1",
                    "1,1",
                    "16,2",
                    "17,3",
                    "197,4",
                    "185,5"
            }
    )
    void getOpcodeSizeReturnsSizeInBytes(int opcode, int expectedSize) {
        assertEquals(expectedSize, EOpcodes.getOpcodeSize(opcode));
    }

    @ParameterizedTest
    @ValueSource(ints = {170, 171, 196})
    void getOpcodeSizeThrowsForVariable(int opcode) {
        assertThrows(IllegalArgumentException.class, () -> EOpcodes.getOpcodeSize(opcode));
    }

    @Test
    void instructionNamesIsPopulated() {
        assertTrue(EOpcodes.INSTRUCTION_NAMES.length > 0);
        assertEquals("nop", EOpcodes.INSTRUCTION_NAMES[0]);
    }
}

