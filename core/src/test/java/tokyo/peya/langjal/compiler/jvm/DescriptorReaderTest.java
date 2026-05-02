package tokyo.peya.langjal.compiler.jvm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("DescriptorReader")
class DescriptorReaderTest {

    @Test
    void fromStringCreatesReader() {
        DescriptorReader reader = DescriptorReader.fromString("Ljava/lang/String;");
        assertTrue(reader.hasMore());
    }

    @Test
    void fromStringTrimsWhitespace() {
        DescriptorReader reader = DescriptorReader.fromString("  I  ");
        assertEquals('I', reader.peek());
    }

    @Test
    void hasMoreReturnsTrueWhenCharactersExist() {
        DescriptorReader reader = DescriptorReader.fromString("I");
        assertTrue(reader.hasMore());
    }

    @Test
    void hasMoreReturnsFalseWhenAtEnd() {
        DescriptorReader reader = DescriptorReader.fromString("I");
        reader.read();
        assertFalse(reader.hasMore());
    }

    @Test
    void peekReturnsCharacterWithoutAdvancing() {
        DescriptorReader reader = DescriptorReader.fromString("Ljava;");
        assertEquals('L', reader.peek());
        assertEquals('L', reader.peek());
    }

    @ParameterizedTest
    @CsvSource({
            "I,I",
            "Ljava/lang/String;,L",
            "(ID)I,("
    })
    void readReturnsAndAdvances(String input, char expected) {
        DescriptorReader reader = DescriptorReader.fromString(input);
        assertEquals(expected, reader.read());
    }

    @Test
    void readAdvancesPosition() {
        DescriptorReader reader = DescriptorReader.fromString("IJ");
        reader.read();
        assertEquals('J', reader.peek());
    }

    @Test
    void expectSucceedsForMatchingCharacter() {
        DescriptorReader reader = DescriptorReader.fromString("I");
        reader.expect('I');
        assertFalse(reader.hasMore());
    }

    @Test
    void expectThrowsForMismatch() {
        DescriptorReader reader = DescriptorReader.fromString("I");
        assertThrows(IllegalArgumentException.class, () -> reader.expect('J'));
    }

    @Test
    void expectThrowsWithInformativeMessage() {
        DescriptorReader reader = DescriptorReader.fromString("I");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> reader.expect('X'));
        assertTrue(ex.getMessage().contains("Expected"));
        assertTrue(ex.getMessage().contains("X"));
        assertTrue(ex.getMessage().contains("I"));
    }

    @Test
    void canReadMultipleCharacters() {
        DescriptorReader reader = DescriptorReader.fromString("(ID)I");
        assertEquals('(', reader.read());
        assertEquals('I', reader.read());
        assertEquals('D', reader.read());
        assertEquals(')', reader.read());
    }

    @Test
    void sourceIsAccessible() {
        String descriptor = "Ljava/lang/String;";
        DescriptorReader reader = DescriptorReader.fromString(descriptor);
        assertEquals(descriptor, reader.getSource());
    }

    @Test
    void positionIsAccessible() {
        DescriptorReader reader = DescriptorReader.fromString("ABC");
        assertEquals(0, reader.getPos());
        reader.read();
        assertEquals(1, reader.getPos());
    }
}

