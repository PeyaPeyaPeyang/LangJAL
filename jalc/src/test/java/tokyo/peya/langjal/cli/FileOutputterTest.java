package tokyo.peya.langjal.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileOutputterTest {
    @TempDir
    Path tempDir;

    @Test
    void validatesOutputArchiveThatDoesNotExistYet() {
        assertTrue(CompilerCLI.hasValidOutputFileName(this.tempDir.resolve("out.jar")));
    }

    @Test
    void finaliseArchivesCompiledDirectoryToRequestedOutputFile() throws IOException {
        Path output = this.tempDir.resolve("out.jar");
        FileOutputter outputter = new FileOutputter(output, false, false);
        assertTrue(outputter.prepareOutput(output, false));

        Path classFile = outputter.getActualCompileOutput().resolve("pkg").resolve("Test.class");
        Files.createDirectories(classFile.getParent());
        Files.write(classFile, new byte[]{0x01, 0x02, 0x03});

        outputter.finalise();

        assertTrue(Files.isRegularFile(output));
        try (ZipFile zip = new ZipFile(output.toFile())) {
            assertNotNull(zip.getEntry("pkg/Test.class"));
        }
    }
}
