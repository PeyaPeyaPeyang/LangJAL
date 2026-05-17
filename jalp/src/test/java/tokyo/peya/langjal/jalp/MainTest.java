package tokyo.peya.langjal.jalp;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {
    @Test
    void mainPrintsVersion() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        try {
            System.setOut(new PrintStream(out));
            System.setErr(new PrintStream(err));

            Main.main(new String[]{"--version"});
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }

        assertEquals("jalp v1.1.0" + System.lineSeparator(), out.toString());
        assertEquals("", err.toString());
    }

    @Test
    void shortVerboseOptionSetsVerboseFlag() throws Exception {
        OptionSet options = createOptionParser().parse("-v", "Example");

        assertEquals(JALPOptions.VERBOSE, getFlags(options));
    }

    @Test
    void noHeaderRemovesHeaderFromDefaultFlags() throws Exception {
        OptionSet options = createOptionParser().parse("--no-header", "Example");

        int flags = getFlags(options);

        assertEquals(
                JALPOptions.SHOW_ACC_PUBLIC | JALPOptions.SHOW_ACC_PROTECTED | JALPOptions.SHOW_ACC_PACKAGE_PRIVATE,
                flags
        );
    }

    @Test
    void privateCodeAndConstantsOptionsAreCombined() throws Exception {
        OptionSet options = createOptionParser().parse("--private", "-c", "--constants", "Example");

        int flags = getFlags(options);

        assertTrue(JALPOptions.is(flags, JALPOptions.SHOW_ACC_PUBLIC));
        assertTrue(JALPOptions.is(flags, JALPOptions.SHOW_ACC_PROTECTED));
        assertTrue(JALPOptions.is(flags, JALPOptions.SHOW_ACC_PACKAGE_PRIVATE));
        assertTrue(JALPOptions.is(flags, JALPOptions.SHOW_ACC_PRIVATE));
        assertTrue(JALPOptions.is(flags, JALPOptions.SHOW_CODE));
        assertTrue(JALPOptions.is(flags, JALPOptions.SHOW_CONSTANTS));
    }

    private static OptionParser createOptionParser() throws Exception {
        Method method = Main.class.getDeclaredMethod("createOptionParser");
        method.setAccessible(true);
        return (OptionParser) method.invoke(null);
    }

    private static int getFlags(OptionSet options) throws Exception {
        Method method = Main.class.getDeclaredMethod("getFlags", OptionSet.class);
        method.setAccessible(true);
        return (int) method.invoke(null, options);
    }
}
