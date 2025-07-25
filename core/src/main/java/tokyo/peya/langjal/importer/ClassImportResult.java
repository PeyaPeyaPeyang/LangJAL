package tokyo.peya.langjal.importer;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import tokyo.peya.langjal.compiler.jvm.AccessAttributeSet;
import tokyo.peya.langjal.compiler.jvm.AccessLevel;
import tokyo.peya.langjal.compiler.jvm.ClassReferenceType;

/**
 * Represents the result of importing a Java class file, encapsulating
 * its metadata, inheritance information, access flags, implemented interfaces,
 * imported methods, and the original ASM {@link ClassNode}.
 * <p>
 * This record serves as a comprehensive container for all relevant class information
 * extracted during the import process.
 * </p>
 *
 * @param majorVersion      The major version of the class file format.
 * @param minorVersion      The minor version of the class file format.
 * @param superType         The type reference of the superclass.
 * @param interfaces        The array of type references for implemented interfaces.
 * @param access            The access level of the class (e.g., public, private).
 * @param accessAttributes  The set of access attributes (e.g., static, final).
 * @param name              The internal name of the class.
 * @param methods           The array of imported methods for the class.
 * @param asmClassNode      The original ASM {@link ClassNode} representing the class.
 */
public record ClassImportResult(
        int majorVersion,
        int minorVersion,
        @NotNull
        ClassReferenceType superType,
        @NotNull
        ClassReferenceType[] interfaces,

        @NotNull
        AccessLevel access,
        @NotNull
        AccessAttributeSet accessAttributes,
        @NotNull
        String name,
        @NotNull
        MethodImportResult[] methods,  // TODO: FIELD

        @NotNull
        ClassNode asmClassNode
)
{
}
