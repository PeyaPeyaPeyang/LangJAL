package tokyo.peya.langjal.jalp.printers;

import org.junit.jupiter.api.Test;
import tokyo.peya.langjal.compiler.jvm.AccessAttributeSet;
import tokyo.peya.langjal.compiler.jvm.AccessLevel;
import tokyo.peya.langjal.compiler.jvm.ClassReferenceType;
import tokyo.peya.langjal.compiler.jvm.MethodDescriptor;
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

class MethodPrinterTest {
    @Test
    void printMethodsPrintsVisibleMethodSignature() {
        JALClass clazz = newClass(new JALMethod[]{
                newMethod(AccessLevel.PUBLIC, "run", "()V", new JALAttribute[0])
        });

        String output = captureOutput(() -> new MethodPrinter(new OutputFormatter(), JALPOptions.SHOW_ACC_PUBLIC)
                .printMethods(clazz));

        assertEquals("  public run()V {" + System.lineSeparator()
                     + "  }" + System.lineSeparator(), output);
    }

    @Test
    void printMethodsSkipsHiddenAccessLevel() {
        JALClass clazz = newClass(new JALMethod[]{
                newMethod(AccessLevel.PRIVATE, "hidden", "()V", new JALAttribute[0])
        });

        String output = captureOutput(() -> new MethodPrinter(new OutputFormatter(), JALPOptions.SHOW_ACC_PUBLIC)
                .printMethods(clazz));

        assertEquals("", output);
    }

    @Test
    void printMethodsShowsNoCodeWhenRequestedAndCodeAttributeIsMissing() {
        JALClass clazz = newClass(new JALMethod[]{
                newMethod(AccessLevel.PUBLIC, "run", "()V", new JALAttribute[0])
        });
        int flags = JALPOptions.SHOW_ACC_PUBLIC | JALPOptions.SHOW_CODE;

        String output = captureOutput(() -> new MethodPrinter(new OutputFormatter(), flags).printMethods(clazz));

        assertEquals("  public run()V {" + System.lineSeparator()
                     + "    // No code." + System.lineSeparator()
                     + "  }" + System.lineSeparator(), output);
    }

    private static JALClass newClass(JALMethod[] methods) {
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
                methods,
                new JALAttribute[0]
        );
    }

    private static JALMethod newMethod(
            AccessLevel access,
            String name,
            String descriptor,
            JALAttribute[] attributes
    ) {
        return new JALMethod(
                access,
                AccessAttributeSet.EMPTY,
                name,
                MethodDescriptor.parse(descriptor),
                attributes
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
