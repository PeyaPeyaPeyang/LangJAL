package tokyo.peya.langjal.compiler.exceptions;

import lombok.Getter;
import org.antlr.v4.runtime.ParserRuleContext;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;

/**
 * Exception thrown when the return type of a method does not match the expected type.
 * <p>
 * This exception is typically thrown during semantic analysis when a method's return statement
 * does not conform to the declared return type of the method.
 * </p>
 */
@Getter
public class ReturnTypeMismatchedException extends CompileErrorException
{
    /**
     * The expected return type as declared in the method signature.
     */
    private final TypeDescriptor expectedType;

    /**
     * The actual return type found in the method body.
     */
    private final TypeDescriptor actualType;

    /**
     * Constructs a new ReturnTypeMismatchedException with the given method, context, and types.
     *
     * @param method       The ASM MethodNode representing the method.
     * @param node         The parser rule context where the mismatch was detected.
     * @param expectedType The expected return type.
     * @param actualType   The actual return type found.
     */
    public ReturnTypeMismatchedException(@NotNull MethodNode method,
                                         @NotNull ParserRuleContext node, TypeDescriptor expectedType,
                                         @NotNull TypeDescriptor actualType)
    {
        super(
                String.format(
                        "Return type mismatch in method '%s%s': expected '%s', but got '%s'.",
                        method.name, method.desc, expectedType, actualType
                ), node
        );
        this.expectedType = expectedType;
        this.actualType = actualType;
    }
}
