package tokyo.peya.langjal.compiler.jvm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DisplayName("ClassReferenceType")
class ClassReferenceTypeTest {

    @Test
    void objectConstantIsJavaLangObject() {
        assertEquals("Ljava/lang/Object;", ClassReferenceType.OBJECT.getDescriptor());
    }

    @Test
    void isNotPrimitive() {
        assertFalse(ClassReferenceType.OBJECT.isPrimitive());
    }

    @Test
    void categoryIsOne() {
        assertEquals(1, ClassReferenceType.OBJECT.getCategory());
    }

    @ParameterizedTest
    @CsvSource({
            "java/lang/String,Ljava/lang/String;",
            "java/util/List,Ljava/util/List;",
            "MyClass,LMyClass;"
    })
    void parseCreatesDescriptor(String input, String expectedDescriptor) {
        ClassReferenceType type = ClassReferenceType.parse(input);
        assertEquals(expectedDescriptor, type.getDescriptor());
    }

    @ParameterizedTest
    @CsvSource({
            "Ljava/lang/String;,java/lang/String",
            "java/lang/String,java/lang/String"
    })
    void parseHandlesLDescriptorPrefix(String input, String expectedInternal) {
        ClassReferenceType type = ClassReferenceType.parse(input);
        assertEquals(expectedInternal, type.getInternalName());
    }

    @ParameterizedTest
    @CsvSource({
            "Ljava/lang/String;,java.lang.String",
            "java.lang.String,java.lang.String"
    })
    void parseBothSlashesAndDots(String input, String expectedDotted) {
        ClassReferenceType type = ClassReferenceType.parse(input);
        assertEquals(expectedDotted, type.getDottedName());
    }

    @ParameterizedTest
    @CsvSource({
            "Ljava/lang/String;",
            "java/lang/String",
            "java.lang.String"
    })
    void parseRemovesSemicolon(String input) {
        ClassReferenceType type = ClassReferenceType.parse(input);
        assertFalse(!type.getDescriptor().contains(";") || type.getDescriptor().endsWith(";"));
    }

    @Test
    void getInternalNameHandlesEmptyPackage() {
        ClassReferenceType type = ClassReferenceType.parse("MyClass");
        assertEquals("MyClass", type.getInternalName());
    }

    @Test
    void toStringFormatsAsDescriptor() {
        ClassReferenceType type = ClassReferenceType.parse("java/lang/String");
        assertEquals("Ljava/lang/String;", type.toString());
    }

    @Test
    void equalsWorks() {
        ClassReferenceType type1 = ClassReferenceType.parse("java/lang/String");
        ClassReferenceType type2 = ClassReferenceType.parse("java/lang/String");
        assertEquals(type1, type2);
    }

    @Test
    void hashCodeConsistent() {
        ClassReferenceType type1 = ClassReferenceType.parse("java/lang/String");
        ClassReferenceType type2 = ClassReferenceType.parse("java/lang/String");
        assertEquals(type1.hashCode(), type2.hashCode());
    }

    @Test
    void asTypeDescriptorConverts() {
        ClassReferenceType type = ClassReferenceType.OBJECT;
        TypeDescriptor descriptor = type.asTypeDescriptor();
        assertEquals("Ljava/lang/Object;", descriptor.toString());
    }
}

