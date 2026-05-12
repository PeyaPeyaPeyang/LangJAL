package tokyo.peya.langjal.compiler.jvm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TypeDescriptor")
class TypeDescriptorTest {

    @Test
    void primitiveConstantsExist() {
        assertEquals("B", TypeDescriptor.BYTE.toString());
        assertEquals("I", TypeDescriptor.INTEGER.toString());
        assertEquals("V", TypeDescriptor.VOID.toString());
    }

    @Test
    void objectConstantIsJavaLangObject() {
        assertEquals("Ljava/lang/Object;", TypeDescriptor.OBJECT.toString());
    }

    @ParameterizedTest
    @CsvSource(
            {
                    "I,false",
                    "[I,true",
                    "[[Ljava/lang/String;,true"
            }
    )
    void isArrayDetectsArrayTypes(String descriptor, boolean expected) {
        TypeDescriptor type = TypeDescriptor.parse(descriptor);
        assertEquals(expected, type.isArray());
    }

    @ParameterizedTest
    @CsvSource(
            {
                    "I,I",
                    "Ljava/lang/String;,Ljava/lang/String;",
                    "[I,[I",
                    "[[D,[[D"
            }
    )
    void parseAndToStringRoundTrip(String descriptor, String expected) {
        TypeDescriptor type = TypeDescriptor.parse(descriptor);
        assertEquals(expected, type.toString());
    }

    @ParameterizedTest
    @CsvSource(
            {
                    "I,0",
                    "[I,1",
                    "[[Ljava/lang/String;,2",
                    "[[[D,3"
            }
    )
    void arrayDimensionsAreTracked(String descriptor, int expectedDimensions) {
        TypeDescriptor type = TypeDescriptor.parse(descriptor);
        assertEquals(expectedDimensions, type.getArrayDimensions());
    }

    @Test
    void classNameCreatesClassReference() {
        TypeDescriptor type = TypeDescriptor.className("Ljava/lang/String;");
        assertEquals("Ljava/lang/String;", type.toString());
    }

    @Test
    void constructorWithNegativeDimensionsThrows() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TypeDescriptor(PrimitiveTypes.INT, -1)
        );
    }

    @Test
    void equalsTypeDescriptors() {
        TypeDescriptor t1 = TypeDescriptor.parse("I");
        TypeDescriptor t2 = TypeDescriptor.parse("I");
        assertEquals(t1, t2);
    }

    @Test
    void notEqualsForDifferentTypes() {
        TypeDescriptor t1 = TypeDescriptor.parse("I");
        TypeDescriptor t2 = TypeDescriptor.parse("J");
        assertNotEquals(t1, t2);
    }

    @Test
    void notEqualsForDifferentDimensions() {
        TypeDescriptor t1 = TypeDescriptor.parse("I");
        TypeDescriptor t2 = TypeDescriptor.parse("[I");
        assertNotEquals(t1, t2);
    }

    @Test
    void hashCodeConsistent() {
        TypeDescriptor t1 = TypeDescriptor.parse("I");
        TypeDescriptor t2 = TypeDescriptor.parse("I");
        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @ParameterizedTest
    @CsvSource(
            {
                    "B",
                    "S",
                    "I",
                    "J",
                    "F",
                    "D",
                    "Z",
                    "C",
                    "V"
            }
    )
    void parsePrimitiveTypes(String descriptor) {
        TypeDescriptor type = TypeDescriptor.parse(descriptor);
        assertTrue(type.getBaseType().isPrimitive());
        assertEquals(descriptor, type.toString());
    }

    @ParameterizedTest
    @CsvSource(
            {
                    "Ljava/lang/String;",
                    "Ljava/lang/Object;",
                    "Ljava/util/List;"
            }
    )
    void parseClassReferences(String descriptor) {
        TypeDescriptor type = TypeDescriptor.parse(descriptor);
        assertFalse(type.getBaseType().isPrimitive());
    }

    @Test
    void parseMultipleDimensions() {
        TypeDescriptor type = TypeDescriptor.parse("[[[I");
        assertEquals(3, type.getArrayDimensions());
        assertEquals("[[[I", type.toString());
    }

    @Test
    void getCategoryForPrimitive() {
        assertEquals(1, TypeDescriptor.INTEGER.getBaseType().getCategory());
        assertEquals(2, TypeDescriptor.LONG.getBaseType().getCategory());
    }

    @Test
    void getCategoryForReference() {
        assertEquals(1, TypeDescriptor.OBJECT.getBaseType().getCategory());
    }
}

