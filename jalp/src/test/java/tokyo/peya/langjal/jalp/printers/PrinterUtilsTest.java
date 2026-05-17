package tokyo.peya.langjal.jalp.printers;

import org.junit.jupiter.api.Test;
import tokyo.peya.langjal.compiler.jvm.AccessAttribute;
import tokyo.peya.langjal.compiler.jvm.AccessAttributeSet;
import tokyo.peya.langjal.compiler.jvm.AccessLevel;
import tokyo.peya.langjal.jalp.JALPOptions;
import tokyo.peya.langjal.jalp.OutputFormatter;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrinterUtilsTest {
    @Test
    void shouldSkipReturnsFalseWhenMatchingAccessFlagIsEnabled() {
        assertFalse(PrinterUtils.shouldSkip(JALPOptions.SHOW_ACC_PUBLIC, AccessLevel.PUBLIC));
        assertFalse(PrinterUtils.shouldSkip(JALPOptions.SHOW_ACC_PROTECTED, AccessLevel.PROTECTED));
        assertFalse(PrinterUtils.shouldSkip(JALPOptions.SHOW_ACC_PRIVATE, AccessLevel.PRIVATE));
        assertFalse(PrinterUtils.shouldSkip(JALPOptions.SHOW_ACC_PACKAGE_PRIVATE, AccessLevel.PACKAGE_PRIVATE));
    }

    @Test
    void shouldSkipReturnsTrueWhenMatchingAccessFlagIsDisabled() {
        int flags = JALPOptions.SHOW_ACC_PUBLIC;

        assertTrue(PrinterUtils.shouldSkip(flags, AccessLevel.PRIVATE));
        assertTrue(PrinterUtils.shouldSkip(flags, AccessLevel.PROTECTED));
        assertTrue(PrinterUtils.shouldSkip(flags, AccessLevel.PACKAGE_PRIVATE));
    }

    @Test
    void printAccessIncludesAccessAndAttributes() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));
        try {
            PrinterUtils.printAccess(
                    new OutputFormatter(),
                    AccessLevel.PUBLIC,
                    new AccessAttributeSet(AccessAttribute.STATIC, AccessAttribute.FINAL)
            ).output("method").print();
        } finally {
            System.setOut(originalOut);
        }

        assertEquals("public static final method", output.toString());
    }

    @Test
    void printAccessOmitsPackagePrivateAccessName() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));
        try {
            PrinterUtils.printAccess(
                    new OutputFormatter(),
                    AccessLevel.PACKAGE_PRIVATE,
                    AccessAttributeSet.EMPTY
            ).output("field").print();
        } finally {
            System.setOut(originalOut);
        }

        assertEquals("field", output.toString());
    }
}
