package tokyo.peya.langjal.jalp;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JALPOptionsTest {
    @Test
    void isReturnsTrueWhenFlagIsIncluded() {
        int options = JALPOptions.SHOW_HEADER | JALPOptions.SHOW_CODE;

        assertTrue(JALPOptions.is(options, JALPOptions.SHOW_HEADER));
        assertTrue(JALPOptions.is(options, JALPOptions.SHOW_CODE));
    }

    @Test
    void isReturnsFalseWhenFlagIsNotIncluded() {
        int options = JALPOptions.SHOW_HEADER;

        assertFalse(JALPOptions.is(options, JALPOptions.SHOW_CONSTANTS));
    }

    @Test
    void defaultShowsHeaderAndNonPrivateMembers() {
        assertTrue(JALPOptions.is(JALPOptions.DEFAULT, JALPOptions.SHOW_HEADER));
        assertTrue(JALPOptions.is(JALPOptions.DEFAULT, JALPOptions.SHOW_ACC_PUBLIC));
        assertTrue(JALPOptions.is(JALPOptions.DEFAULT, JALPOptions.SHOW_ACC_PROTECTED));
        assertTrue(JALPOptions.is(JALPOptions.DEFAULT, JALPOptions.SHOW_ACC_PACKAGE_PRIVATE));
        assertFalse(JALPOptions.is(JALPOptions.DEFAULT, JALPOptions.SHOW_ACC_PRIVATE));
    }

    @Test
    void verboseIncludesAllDefinedFlags() {
        for (int flag : allFlags())
            assertTrue(JALPOptions.is(JALPOptions.VERBOSE, flag));
    }

    @Test
    void zeroIncludesNoFlags() {
        for (int flag : allFlags())
            assertFalse(JALPOptions.is(0, flag));
    }

    @Test
    void isReturnsTrueWhenAnyBitInMaskIsIncluded() {
        int mask = JALPOptions.SHOW_HEADER | JALPOptions.SHOW_CODE;

        assertTrue(JALPOptions.is(JALPOptions.SHOW_HEADER, mask));
    }

    @Test
    void defaultDoesNotShowCodeOrConstants() {
        assertFalse(JALPOptions.is(JALPOptions.DEFAULT, JALPOptions.SHOW_CODE));
        assertFalse(JALPOptions.is(JALPOptions.DEFAULT, JALPOptions.SHOW_CONSTANTS));
    }

    private static List<Integer> allFlags() {
        return List.of(
                JALPOptions.SHOW_ACC_PUBLIC,
                JALPOptions.SHOW_ACC_PROTECTED,
                JALPOptions.SHOW_ACC_PACKAGE_PRIVATE,
                JALPOptions.SHOW_ACC_PRIVATE,
                JALPOptions.SHOW_CODE,
                JALPOptions.SHOW_CONSTANTS,
                JALPOptions.SHOW_HEADER
        );
    }
}
