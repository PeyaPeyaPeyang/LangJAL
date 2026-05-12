package tokyo.peya.langjal.compiler.jvm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MethodDescriptor")
class MethodDescriptorTest {

    @ParameterizedTest
    @CsvSource(
            {
                    "(I)V",
                    "(ID)I",
                    "()V",
                    "(Ljava/lang/String;)I",
                    "(LNoPackageClass;)LAnotherClass;",
                    "([[[[[[Ljava/lang/Object;BCDFIJZZZZLjava/lang/String;)[[[[[[Ljava/lang/Object;BCDFIJZZZZLjava/lang/String;",
                    "([[LB;LC;LD;LF;LZ;LJ;L[Ljava/lang/String;)[[LB;LC;LD;LF;LZ;LJ;L[Ljava/lang/String;"
            }
    )
    void parseMethodDescriptors(String descriptor) {
        MethodDescriptor method = MethodDescriptor.parse(descriptor);
        assertEquals(descriptor, method.getDescriptorString());
    }

    @Test
    void parseNoParameters() {
        MethodDescriptor method = MethodDescriptor.parse("()V");
        assertEquals(0, method.getParameterTypes().length);
    }

    @Test
    void parseSingleParameter() {
        MethodDescriptor method = MethodDescriptor.parse("(I)V");
        assertEquals(1, method.getParameterTypes().length);
    }

    @Test
    void parseMultipleParameters() {
        MethodDescriptor method = MethodDescriptor.parse("(IDLjava/lang/String;)V");
        assertEquals(3, method.getParameterTypes().length);
    }

    @Test
    void parseRecognizesReturnType() {
        MethodDescriptor method = MethodDescriptor.parse("()I");
        assertEquals("I", method.getReturnType().toString());
    }

    @Test
    void equalsMethodDescriptors() {
        MethodDescriptor m1 = MethodDescriptor.parse("(I)V");
        MethodDescriptor m2 = MethodDescriptor.parse("(I)V");
        assertEquals(m1, m2);
    }

    @Test
    void equalToDescriptorString() {
        MethodDescriptor method = MethodDescriptor.parse("(I)V");
        assertTrue(method.equals("(I)V"));
    }

    @Test
    void equalsReturnsFalseForInvalidString() {
        MethodDescriptor method = MethodDescriptor.parse("(I)V");
        assertFalse(method.equals("invalid"));
    }

    @Test
    void equalsReturnsFalseForNullString() {
        MethodDescriptor method = MethodDescriptor.parse("(I)V");
        assertFalse(method.equals(null));
    }

    @Test
    void hashCodeConsistent() {
        MethodDescriptor m1 = MethodDescriptor.parse("(I)V");
        MethodDescriptor m2 = MethodDescriptor.parse("(I)V");
        assertEquals(m1.hashCode(), m2.hashCode());
    }

    @Test
    void parseThrowsForInvalidFormat() {
        assertThrows(Exception.class, () -> MethodDescriptor.parse("invalid"));
    }
}

