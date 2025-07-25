package tokyo.peya.langjal.importer;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.jvm.AccessAttributeSet;
import tokyo.peya.langjal.compiler.jvm.AccessLevel;
import tokyo.peya.langjal.compiler.jvm.ClassReferenceType;

/**
 * Responsible for importing and converting ASM {@link ClassNode} representations
 * into higher-level internal structures suitable for further compilation or analysis.
 * This class extracts class metadata, inheritance information, access flags, and methods,
 * reporting progress and errors via a {@link FileEvaluatingReporter}.
 */
public class ClassImporter
{
    private final FileEvaluatingReporter reporter;

    /**
     * Constructs a new {@code ClassImporter} with the specified reporter.
     *
     * @param reporter The reporter used to post informational and error messages during import.
     */
    public ClassImporter(@NotNull FileEvaluatingReporter reporter)
    {
        this.reporter = reporter;
    }

    /**
     * Imports the specified ASM {@link ClassNode}, extracting its metadata,
     * inheritance hierarchy, access flags, and methods. Each method is further
     * processed using {@link MethodImporter}.
     *
     * @param asmClass The ASM {@link ClassNode} representing the class to import.
     * @return The result of the class import, encapsulated in a {@link ClassImportResult}.
     */
    @NotNull
    public ClassImportResult importClass(@NotNull ClassNode asmClass)
    {
        this.reporter.postInfo("Importing class: " + asmClass.name);

        int majorVersion = asmClass.version & 0xFFFF0000;
        int minorVersion = asmClass.version & 0x0000FFFF;

        ClassReferenceType superType;
        if (asmClass.superName == null || asmClass.superName.isEmpty())
            superType = ClassReferenceType.OBJECT;  // null のときは， Object を継承していると推論
        else
            superType = ClassReferenceType.parse(asmClass.superName);

        ClassReferenceType[] interfaces;
        if (asmClass.interfaces.isEmpty())
            interfaces = new ClassReferenceType[0];
        else
            interfaces = asmClass.interfaces.stream()
                    .map(ClassReferenceType::parse)
                    .toArray(ClassReferenceType[]::new);

        AccessLevel access = AccessLevel.fromAccess(asmClass.access);
        AccessAttributeSet accessAttributes = AccessAttributeSet.fromAccess(asmClass.access);
        String name = asmClass.name;

        MethodImporter methodImporter = new MethodImporter(asmClass, this.reporter);
        MethodImportResult[] importedMethods = new MethodImportResult[asmClass.methods.size()];
        MethodNode[] methods = asmClass.methods.toArray(new MethodNode[0]);
        for (int i = 0; i < methods.length; i++)
        {
            MethodNode method = methods[i];
            importedMethods[i] = methodImporter.importMethod(method);
        }

        return new ClassImportResult(
                majorVersion,
                minorVersion,
                superType,
                interfaces,

                access,
                accessAttributes,
                name,
                importedMethods,

                asmClass
        );
    }
}
