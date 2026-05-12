package tokyo.peya.langjal.compiler.instructions.utils;

import tokyo.peya.langjal.compiler.FileEvaluatingReporter;

import java.nio.file.Path;

public class TestFileEvaluatingReporter extends FileEvaluatingReporter {
    public TestFileEvaluatingReporter() {
        super(new TestCompileReporter(), Path.of("testFilePath.jal"));
    }
}
