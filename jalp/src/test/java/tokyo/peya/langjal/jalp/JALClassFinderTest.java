package tokyo.peya.langjal.jalp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JALClassFinderTest {
    @TempDir
    Path tempDir;

    @Test
    void findClassReadsDirectClassFile() throws IOException {
        byte[] bytes = {1, 2, 3};
        Path classFile = this.tempDir.resolve("Example.class");
        Files.write(classFile, bytes);

        ClassInfo info = JALClassFinder.findClass(classFile.toString(), null);

        assertEquals(classFile.toAbsolutePath().normalize(), info.classFile());
        assertEquals(3, info.size());
        assertArrayEquals(bytes, info.bytes());
        assertEquals("039058c6f2c0cb492c533b0a4d14ef77cc0f78abccced5287d84a1a2011cfb81", info.sha256());
    }

    @Test
    void findClassAddsClassExtensionForDirectCandidate() throws IOException {
        Path classFile = this.tempDir.resolve("Example.class");
        Files.write(classFile, new byte[]{4, 5});

        ClassInfo info = JALClassFinder.findClass(this.tempDir.resolve("Example").toString(), null);

        assertEquals(classFile.toAbsolutePath().normalize(), info.classFile());
        assertEquals(2, info.size());
    }

    @Test
    void findClassSearchesDirectoryClasspathByQualifiedName() throws IOException {
        byte[] bytes = {6, 7, 8};
        Path classFile = this.tempDir.resolve("pkg").resolve("Example.class");
        Files.createDirectories(classFile.getParent());
        Files.write(classFile, bytes);

        ClassInfo info = JALClassFinder.findClass("pkg.Example", this.tempDir.toString());

        assertEquals(classFile.toAbsolutePath().normalize(), info.classFile());
        assertArrayEquals(bytes, info.bytes());
    }

    @Test
    void findClassSearchesArchiveOnClasspath() throws IOException {
        byte[] bytes = {9, 10};
        Path jarFile = this.tempDir.resolve("classes.jar");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jarFile))) {
            output.putNextEntry(new JarEntry("pkg/Example.class"));
            output.write(bytes);
            output.closeEntry();
        }

        ClassInfo info = JALClassFinder.findClass("pkg.Example", jarFile.toString());

        assertEquals(Path.of(jarFile.toAbsolutePath().normalize() + "!pkg/Example.class"), info.classFile());
        assertEquals(2, info.size());
        assertArrayEquals(bytes, info.bytes());
    }

    @Test
    void findClassThrowsWhenClassCannotBeFound() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> JALClassFinder.findClass("missing.Example", this.tempDir.toString())
        );

        assertTrue(exception.getMessage().contains("missing.Example"));
    }
}
