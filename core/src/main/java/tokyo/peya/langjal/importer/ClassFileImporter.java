package tokyo.peya.langjal.importer;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Provides functionality to import Java class files from various sources,
 * such as byte arrays or file paths, and convert them into internal representations
 * for further processing or analysis. This class acts as a bridge between raw class data
 * and the higher-level import logic, reporting progress and errors via a {@link FileEvaluatingReporter}.
 */
public class ClassFileImporter
{
    private final FileEvaluatingReporter reporter;
    private final ClassImporter classImporter;

    /**
     * Constructs a new {@code ClassFileImporter} with the specified reporter.
     *
     * @param reporter The reporter used to post informational and error messages during import.
     */
    public ClassFileImporter(@NotNull FileEvaluatingReporter reporter)
    {
        this.reporter = reporter;
        this.classImporter = new ClassImporter(reporter);
    }

    /**
     * Imports a Java class from the given byte array.
     * <p>
     * The method parses the byte array using ASM, constructs an internal representation,
     * and delegates further import logic to {@link ClassImporter}.
     * </p>
     *
     * @param classBytes The byte array containing the class file data.
     * @return The result of the class import, encapsulated in a {@link ClassImportResult}.
     */
    @NotNull
    public ClassImportResult importClass(byte[] classBytes)
    {
        this.reporter.postInfo("Importing class from byte array");

        ClassReader classReader = new ClassReader(classBytes);
        ClassNode asmClass = new ClassNode();
        classReader.accept(asmClass, ClassReader.SKIP_FRAMES); // デバッグ情報は保持

        return this.classImporter.importClass(asmClass);
    }

    /**
     * Imports a Java class from the specified file path.
     * <p>
     * The method reads the class file, parses it using ASM, and delegates
     * further import logic to {@link ClassImporter}. Any I/O errors encountered
     * during reading are reported and wrapped in an {@link IllegalArgumentException}.
     * </p>
     *
     * @param classFilePath The path to the class file to import.
     * @return The result of the class import, encapsulated in a {@link ClassImportResult}.
     * @throws IllegalArgumentException If the class file cannot be read or imported.
     */
    @NotNull
    public ClassImportResult importClass(@NotNull Path classFilePath)
    {
        this.reporter.postInfo("Importing class from file: " + classFilePath);

        try
        {
            InputStream inputStream = Files.newInputStream(classFilePath);
            ClassReader classReader = new ClassReader(inputStream);
            ClassNode asmClass = new ClassNode();
            classReader.accept(asmClass, ClassReader.SKIP_FRAMES); // デバッグ情報は保持
            inputStream.close();

            return this.classImporter.importClass(asmClass);
        }
        catch (IOException e)
        {
            this.reporter.postError("Failed to import class from file: " + classFilePath);
            throw new IllegalArgumentException("Failed to import class from file: " + classFilePath, e);
        }
    }
}
