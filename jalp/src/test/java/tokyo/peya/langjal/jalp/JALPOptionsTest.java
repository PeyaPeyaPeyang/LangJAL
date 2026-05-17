package tokyo.peya.langjal.jalp;

import org.junit.jupiter.api.Test;

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
}
