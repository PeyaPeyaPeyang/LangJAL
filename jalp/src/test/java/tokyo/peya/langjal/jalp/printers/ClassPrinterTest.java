package tokyo.peya.langjal.jalp.printers;

import org.junit.jupiter.api.Test;
import tokyo.peya.langjal.compiler.jvm.AccessAttribute;
import tokyo.peya.langjal.compiler.jvm.AccessAttributeSet;
import tokyo.peya.langjal.compiler.jvm.AccessLevel;
import tokyo.peya.langjal.compiler.jvm.ClassReferenceType;
import tokyo.peya.langjal.jalp.JALPOptions;
import tokyo.peya.langjal.jalp.OutputFormatter;
import tokyo.peya.langjal.jalp.reader.JALAttribute;
import tokyo.peya.langjal.jalp.reader.JALClass;
import tokyo.peya.langjal.jalp.reader.JALConstantPoolEntry;
import tokyo.peya.langjal.jalp.reader.JALField;
import tokyo.peya.langjal.jalp.reader.JALMethod;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassPrinterTest {
    @Test
    void processSkipsClassWhenAccessLevelIsHidden() {
        JALClass clazz = newClass(
                AccessLevel.PRIVATE,
                AccessAttributeSet.EMPTY,
                ClassReferenceType.parse("Example"),
                ClassReferenceType.OBJECT
        );

        String output = captureOutput(() -> new ClassPrinter(new OutputFormatter(), JALPOptions.SHOW_ACC_PUBLIC)
                .process(clazz));

        assertEquals("", output);
    }

    @Test
    void processPrintsClassHeaderAndVersions() {
        JALClass clazz = newClass(
                AccessLevel.PUBLIC,
                new AccessAttributeSet(AccessAttribute.FINAL),
                ClassReferenceType.parse("Example"),
                ClassReferenceType.OBJECT
        );

        String output = captureOutput(() -> new ClassPrinter(new OutputFormatter(), JALPOptions.SHOW_ACC_PUBLIC)
                .process(clazz));

        assertTrue(output.contains("public final class Example ( " + System.lineSeparator()));
        assertTrue(output.contains("  major_version = 65," + System.lineSeparator()));
        assertTrue(output.contains("  minor_version = 0" + System.lineSeparator()));
        assertTrue(output.endsWith("}" + System.lineSeparator()));
    }

    @Test
    void processPrintsSuperClassWhenItIsNotObject() {
        JALClass clazz = newClass(
                AccessLevel.PUBLIC,
                AccessAttributeSet.EMPTY,
                ClassReferenceType.parse("pkg/Example"),
                ClassReferenceType.parse("pkg/Base")
        );

        String output = captureOutput(() -> new ClassPrinter(new OutputFormatter(), JALPOptions.SHOW_ACC_PUBLIC)
                .process(clazz));

        assertTrue(output.contains("  super =\"pkg/Base\"," + System.lineSeparator()));
    }

    private static JALClass newClass(
            AccessLevel access,
            AccessAttributeSet accessAttrs,
            ClassReferenceType thisName,
            ClassReferenceType superName
    ) {
        return new JALClass(
                65,
                0,
                new JALConstantPoolEntry[0],
                access,
                accessAttrs,
                thisName,
                superName,
                new ClassReferenceType[0],
                new JALField[0],
                new JALMethod[0],
                new JALAttribute[0]
        );
    }

    private static String captureOutput(Runnable action) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));
        try {
            action.run();
        } finally {
            System.setOut(originalOut);
        }
        return output.toString();
    }
}
