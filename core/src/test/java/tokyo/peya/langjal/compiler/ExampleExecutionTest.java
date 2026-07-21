package tokyo.peya.langjal.compiler;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.tree.ClassNode;
import tokyo.peya.langjal.compiler.instructions.utils.TestCompileReporter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExampleExecutionTest {
    private static final Path PROJECT_ROOT = findProjectRoot();
    private static final Path EXAMPLES_DIR = PROJECT_ROOT.resolve("examples");
    private static final Path OUTPUTS_DIR = EXAMPLES_DIR.resolve("outs");

    @TempDir
    Path tempDir;

    @ParameterizedTest(name = "{0}")
    @MethodSource("examples")
    void examplesProduceExpectedOutput(ExampleCase example) throws Exception {
        Path outputDir = this.tempDir.resolve(example.name());
        JALFileCompiler compiler = new JALFileCompiler(new TestCompileReporter(), outputDir, CompileSettings.FULL);
        ClassNode compiledClass = compiler.compile(Files.readString(example.source(), StandardCharsets.UTF_8));

        String actual = runCompiledClass(outputDir, compiledClass.name.replace('/', '.'));
        String expected = Files.readString(example.expectedOutput(), StandardCharsets.UTF_8);

        assertEquals(normalizeOutput(expected), normalizeOutput(actual));
    }

    static Stream<ExampleCase> examples() throws IOException {
        assertTrue(Files.isDirectory(EXAMPLES_DIR), "examples directory not found: " + EXAMPLES_DIR);
        assertTrue(Files.isDirectory(OUTPUTS_DIR), "examples/outs directory not found: " + OUTPUTS_DIR);

        try (Stream<Path> sources = Files.list(EXAMPLES_DIR)) {
            List<ExampleCase> cases = sources
                    .filter(path -> path.getFileName().toString().endsWith(".jal"))
                    .map(ExampleExecutionTest::toExampleCase)
                    .filter(example -> Files.isRegularFile(example.expectedOutput()))
                    .sorted(Comparator.comparing(ExampleCase::name))
                    .toList();

            assertTrue(!cases.isEmpty(), "No example output fixtures found");
            return cases.stream();
        }
    }

    private static ExampleCase toExampleCase(Path source) {
        String fileName = source.getFileName().toString();
        String name = fileName.substring(0, fileName.length() - ".jal".length());
        return new ExampleCase(name, source, OUTPUTS_DIR.resolve(name + ".out.txt"));
    }

    private static String runCompiledClass(Path classPath, String className) throws IOException, InterruptedException {
        Path javaExecutable = Path.of(System.getProperty("java.home"))
                .resolve("bin")
                .resolve(isWindows() ? "java.exe" : "java");

        Process process = new ProcessBuilder(
                javaExecutable.toString(),
                "-Dfile.encoding=UTF-8",
                "-cp",
                classPath.toString(),
                className
        ).redirectErrorStream(true).start();

        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exitCode = process.waitFor();

        assertEquals(0, exitCode, () -> "java exited with code " + exitCode + "\n" + output);
        return output;
    }

    private static String normalizeOutput(String output) {
        return output
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replaceAll("\\n+$", "");
    }

    private static Path findProjectRoot() {
        Path path = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (path != null) {
            if (Files.isDirectory(path.resolve("examples")) && Files.exists(path.resolve("settings.gradle.kts"))) {
                return path;
            }

            path = path.getParent();
        }

        throw new IllegalStateException("Could not find project root from " + System.getProperty("user.dir"));
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");
    }

    record ExampleCase(String name, Path source, Path expectedOutput) {
        @Override
        public String toString() {
            return this.name;
        }
    }
}
