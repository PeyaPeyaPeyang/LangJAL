package tokyo.peya.langjal.jalp.reader;

import org.junit.jupiter.api.Test;
import tokyo.peya.langjal.compiler.jvm.AccessAttributeSet;
import tokyo.peya.langjal.compiler.jvm.AccessLevel;
import tokyo.peya.langjal.compiler.jvm.ClassReferenceType;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class JALClassTest {
    @Test
    void getAttributeReturnsFirstAttributeWithMatchingName() {
        JALAttribute.SourceFileAttribute sourceFile =
                new JALAttribute.SourceFileAttribute("SourceFile", "Example.java");
        JALClass clazz = newClass(new JALAttribute[]{
                new JALAttribute.SyntheticAttribute("Synthetic"),
                sourceFile
        });

        assertSame(sourceFile, clazz.getAttribute("SourceFile"));
    }

    @Test
    void getAttributeReturnsNullWhenNameDoesNotMatch() {
        JALClass clazz = newClass(new JALAttribute[]{
                new JALAttribute.SyntheticAttribute("Synthetic")
        });

        assertNull(clazz.getAttribute("SourceFile"));
    }

    private static JALClass newClass(JALAttribute[] attributes) {
        return new JALClass(
                65,
                0,
                new JALConstantPoolEntry[0],
                AccessLevel.PUBLIC,
                AccessAttributeSet.EMPTY,
                ClassReferenceType.parse("Example"),
                ClassReferenceType.OBJECT,
                new ClassReferenceType[0],
                new JALField[0],
                new JALMethod[0],
                attributes
        );
    }
}
