package tokyo.peya.langjal.compiler;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import tokyo.peya.langjal.compiler.exceptions.ClassFinalisingException;
import tokyo.peya.langjal.compiler.exceptions.ClassWritingException;
import tokyo.peya.langjal.compiler.exceptions.CompileErrorException;
import tokyo.peya.langjal.compiler.exceptions.FileReadingException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JALFileCompiler
{
    private final CompileReporter reporter;
    private final Path outputDir;
    @MagicConstant(valuesFromClass = CompileSettings.class)
    private final int settings;

    public JALFileCompiler(@NotNull CompileReporter reporter, @NotNull Path outputDir,
                           @MagicConstant(valuesFromClass = CompileSettings.class) int settings) throws IOException
    {

        this.reporter = reporter;
        this.outputDir = outputDir;
        this.settings = settings;

        // Ensure the output directory exists
        if (!Files.exists(outputDir))
            Files.createDirectories(outputDir);
    }

    @Nullable
    public ClassNode compile(@NotNull Path inputFile) throws CompileErrorException
    {
        CharStream charStream;
        try
        {
            charStream = CharStreams.fromPath(inputFile);
        }
        catch (IOException e)
        {
            this.reporter.postError(
                    "Failed to read input file: " + inputFile.toAbsolutePath(),
                    new FileReadingException(e, inputFile),
                    inputFile
            );
            return null;
        }

        ClassNode compiled = compile(this.reporter, charStream, this.settings, inputFile).getCompiledClass();
        this.writeClass(compiled);
        return compiled;
    }

    @NotNull
    public ClassNode compile(@NotNull String sourceCode) throws CompileErrorException
    {
        CharStream charStream = CharStreams.fromString(sourceCode);
        ClassNode compiled = compile(this.reporter, charStream, this.settings, null).getCompiledClass();
        this.writeClass(compiled);
        return compiled;
    }

    private void writeClass(@NotNull ClassNode classNode) throws CompileErrorException
    {
        ClassWriter classWriter = new ClassWriter(0);
        try
        {

            classNode.accept(classWriter);
        }
        catch (Throwable e)
        {
            throw new ClassFinalisingException(e);
        }

        Path outputFile = this.outputDir.resolve(classNode.name + ".class");
        try
        {
            Files.createDirectories(outputFile.getParent());

            Files.write(outputFile, classWriter.toByteArray());
            /*
            this.outputConsumer.registerCompiledClass(
                    this.target,
                    new CompiledClass(
                            this.file.toFile(),
                            outputFile.toFile(),
                            toClassName(classNode.name),
                            binaryContent
                    )
            );*/
        }
        catch (IOException e)
        {
            throw new ClassWritingException(e);
        }
    }

    @NotNull
    public static JALClassCompiler compileOnly(@NotNull String sourceCode, @NotNull CompileReporter reporter,
                                        @MagicConstant(valuesFromClass = CompileSettings.class) int settings
    ) throws CompileErrorException
    {
        CharStream charStream = CharStreams.fromString(sourceCode);
        return compile(reporter, charStream, settings, null);
    }


    @NotNull
    private static JALClassCompiler compile(@NotNull CompileReporter reporter,
                                     @NotNull CharStream charStream,
                                     @MagicConstant(valuesFromClass = CompileSettings.class) int settings,
                                     @Nullable Path sourcePath) throws CompileErrorException
    {
        JALLexer lexer = new JALLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        JALParser parser = new JALParser(tokenStream);
        JALCompileErrorStrategy errorStrategy = new JALCompileErrorStrategy(reporter, sourcePath);
        parser.setErrorHandler(errorStrategy);

        FileEvaluatingReporter fileReporter = new FileEvaluatingReporter(reporter, sourcePath);
        fileReporter.postInfo("Compiling JAL source code");

        JALParser.RootContext tree = parser.root();
        if (errorStrategy.isError())
            throw new CompileErrorException("Failed to parse JAL source code", 0, 0, 0);

        JALParser.ClassDefinitionContext classDefinition = tree.classDefinition();
        if (classDefinition == null)
            throw new CompileErrorException("No class definition found in JAL source code", 0, 0, 0);

        String fileName = sourcePath == null ? null: sourcePath.getFileName().toString();
        JALClassCompiler classCompiler = new JALClassCompiler(fileReporter, fileName, settings);

        classCompiler.compileClassAST(classDefinition);
        return classCompiler;
    }

    private static String toClassName(@NotNull String fullQualifiedName)
    {
        // a/b/c -> c
        int lastSlashIndex = fullQualifiedName.lastIndexOf('/');
        if (lastSlashIndex == -1)
            return fullQualifiedName;
        else
            return fullQualifiedName.substring(lastSlashIndex + 1);
    }
}
