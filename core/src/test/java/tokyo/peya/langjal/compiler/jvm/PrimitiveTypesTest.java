package tokyo.peya.langjal.compiler.jvm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PrimitiveTypes")
class PrimitiveTypesTest {

    static Stream<PrimitiveTypes> allPrimitiveTypes() {
        return Stream.of(PrimitiveTypes.values());
    }

    @ParameterizedTest
    @CsvSource(
            {
                    "byte,B",
                    "short,S",
                    "int,I",
                    "long,J",
                    "float,F",
                    "double,D",
                    "boolean,Z",
                    "char,C",
                    "void,V"
            }
    )
    void getDescriptorReturnsCorrectCharacters(String typeName, String expectedDescriptor) {
        PrimitiveTypes type = PrimitiveTypes.valueOf(typeName.toUpperCase());
        assertEquals(expectedDescriptor, type.getDescriptor());
    }

    @ParameterizedTest
    @CsvSource(
            {
                    "B,BYTE",
                    "S,SHORT",
                    "I,INT",
                    "J,LONG",
                    "F,FLOAT",
                    "D,DOUBLE",
                    "Z,BOOLEAN",
                    "C,CHAR",
                    "V,VOID"
            }
    )
    void fromDescriptorRecognizesAllTypes(char descriptor, String expectedType) {
        PrimitiveTypes type = PrimitiveTypes.fromDescriptor(descriptor);
        assertNotNull(type);
        assertEquals(PrimitiveTypes.valueOf(expectedType), type);
    }

    @Test
    void fromDescriptorReturnsNullForInvalid() {
        assertNull(PrimitiveTypes.fromDescriptor('X'));
        assertNull(PrimitiveTypes.fromDescriptor('?'));
    }

    @Test
    void getCategoryReturnsOneForMostTypes() {
        assertEquals(1, PrimitiveTypes.BYTE.getCategory());
        assertEquals(1, PrimitiveTypes.INT.getCategory());
        assertEquals(1, PrimitiveTypes.FLOAT.getCategory());
    }

    @Test
    void getCategoryReturnsTwoForLongAndDouble() {
        assertEquals(2, PrimitiveTypes.LONG.getCategory());
        assertEquals(2, PrimitiveTypes.DOUBLE.getCategory());
    }

    @ParameterizedTest
    @MethodSource("allPrimitiveTypes")
    void allTypesArePrimitive(PrimitiveTypes type) {
        assertTrue(type.isPrimitive());
    }

    @ParameterizedTest
    @CsvSource(
            {
                    "BYTE,B",
                    "SHORT,S",
                    "INT,I",
                    "LONG,J",
                    "FLOAT,F",
                    "DOUBLE,D",
                    "BOOLEAN,Z",
                    "CHAR,C",
                    "VOID,V"
            }
    )
    void toStringReturnsDescriptor(String typeName, String expectedOutput) {
        PrimitiveTypes type = PrimitiveTypes.valueOf(typeName);
        assertEquals(expectedOutput, type.toString());
    }

    @ParameterizedTest
    @MethodSource("allPrimitiveTypes")
    void fromDescriptorRoundTrips(PrimitiveTypes original) {
        PrimitiveTypes retrieved = PrimitiveTypes.fromDescriptor(original.getDescriptor().charAt(0));
        assertEquals(original, retrieved);
    }

    @ParameterizedTest
    @CsvSource(
            {
                    "BYTE,8",
                    "SHORT,9",
                    "INT,10",
                    "LONG,11",
                    "FLOAT,6",
                    "DOUBLE,7",
                    "BOOLEAN,4",
                    "CHAR,5",
                    "VOID,-1"
            }
    )
    void fromASMTypeRecognizesAllTypes(String typeName, int asmType) {
        PrimitiveTypes type = PrimitiveTypes.valueOf(typeName);
        assertEquals(type, PrimitiveTypes.fromASMType(asmType));
    }

    @Test
    void fromASMTypeReturnsNullForInvalid() {
        assertNull(PrimitiveTypes.fromASMType(999));
        assertNull(PrimitiveTypes.fromASMType(-999));
    }
}

