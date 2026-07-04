package tokyo.peya.langjal.jalp;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ClassInfoTest {
    @Test
    void recordAccessorsReturnConstructorValues() {
        Path path = Path.of("Example.class");
        Instant lastModified = Instant.parse("2024-01-02T03:04:05Z");
        byte[] bytes = {0x01, 0x02};

        ClassInfo info = new ClassInfo(path, lastModified, 2, "abc123", bytes);

        assertEquals(path, info.classFile());
        assertEquals(lastModified, info.lastModified());
        assertEquals(2, info.size());
        assertEquals("abc123", info.sha256());
        assertArrayEquals(bytes, info.bytes());
    }

    @Test
    void recordsWithSameArrayInstanceAreEqual() {
        byte[] bytes = {0x01};
        ClassInfo first = new ClassInfo(Path.of("A.class"), Instant.EPOCH, 1, "hash", bytes);
        ClassInfo second = new ClassInfo(Path.of("A.class"), Instant.EPOCH, 1, "hash", bytes);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void recordsWithEqualButDifferentArrayInstancesAreNotEqual() {
        ClassInfo first = new ClassInfo(Path.of("A.class"), Instant.EPOCH, 1, "hash", new byte[] {0x01});
        ClassInfo second = new ClassInfo(Path.of("A.class"), Instant.EPOCH, 1, "hash", new byte[] {0x01});

        assertNotEquals(first, second);
    }
}
