package tokyo.peya.langjal.importer;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.compiler.jvm.AccessAttributeSet;
import tokyo.peya.langjal.compiler.jvm.AccessLevel;
import tokyo.peya.langjal.compiler.jvm.MethodDescriptor;
import tokyo.peya.langjal.compiler.member.LabelsHolder;
import tokyo.peya.langjal.compiler.member.LocalVariablesHolder;
import tokyo.peya.langjal.compiler.member.TryCatchDirectivesHolder;

/**
 * Represents the result of importing a Java method, encapsulating
 * its access flags, name, descriptor, labels, local variables, try-catch directives,
 * and the original ASM {@link MethodNode}.
 * <p>
 * This record serves as a comprehensive container for all relevant method information
 * extracted during the import process.
 * </p>
 *
 * @param access             The access level of the method (e.g., public, private).
 * @param accessAttributes   The set of access attributes (e.g., static, final).
 * @param name               The name of the method.
 * @param descriptor         The descriptor of the method, including parameter and return types.
 * @param labels             The holder for all labels used in the method.
 * @param locals             The holder for all local variables declared in the method.
 * @param tryCatchDirectives The holder for all try-catch directives present in the method.
 * @param method             The original ASM {@link MethodNode} representing the method.
 */
public record MethodImportResult(
        @NotNull
        AccessLevel access,
        @NotNull
        AccessAttributeSet accessAttributes,
        @NotNull
        String name,
        @NotNull
        MethodDescriptor descriptor,
        @NotNull
        LabelsHolder labels,
        @NotNull
        LocalVariablesHolder locals,
        @NotNull
        TryCatchDirectivesHolder tryCatchDirectives,

        @NotNull
        MethodNode method
)
{
}
