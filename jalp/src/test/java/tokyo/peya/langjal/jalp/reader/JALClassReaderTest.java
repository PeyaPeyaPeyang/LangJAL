package tokyo.peya.langjal.jalp.reader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JALClassReaderTest {
    @Test
    void readRejectsBytesWithoutClassFileMagic() {
        byte[] bytes = {0, 0, 0, 0};

        assertThrows(IllegalArgumentException.class, () -> JALClassReader.read(bytes));
    }

    @Test
    void getFromConstantsReturnsExtractedValueForMatchingEntry() {
        JALConstantPoolEntry[] constantPool = {
                null,
                new JALConstantPoolEntry.Utf8Entry("value")
        };

        String value = JALClassReader.getFromConstants(
                constantPool,
                1,
                entry -> entry instanceof JALConstantPoolEntry.Utf8Entry,
                entry -> ((JALConstantPoolEntry.Utf8Entry) entry).value()
        );

        assertEquals("value", value);
    }

    @Test
    void getFromConstantsRejectsInvalidIndex() {
        JALConstantPoolEntry[] constantPool = {
                null,
                new JALConstantPoolEntry.Utf8Entry("value")
        };

        assertThrows(IllegalArgumentException.class, () -> JALClassReader.getFromConstants(
                constantPool,
                0,
                entry -> true,
                entry -> entry
        ));
        assertThrows(IllegalArgumentException.class, () -> JALClassReader.getFromConstants(
                constantPool,
                2,
                entry -> true,
                entry -> entry
        ));
    }

    @Test
    void getFromConstantsRejectsMismatchedEntryType() {
        JALConstantPoolEntry[] constantPool = {
                null,
                new JALConstantPoolEntry.IntegerEntry(1)
        };

        assertThrows(IllegalArgumentException.class, () -> JALClassReader.getFromConstants(
                constantPool,
                1,
                entry -> entry instanceof JALConstantPoolEntry.Utf8Entry,
                entry -> entry
        ));
    }
}
