package tokyo.peya.langjal.compiler.exceptions;

import lombok.Getter;
import org.antlr.v4.runtime.ParserRuleContext;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;

@Getter
public class ReturnTypeMismatchedException extends CompileErrorException
{
    private final TypeDescriptor expectedType;
    private final TypeDescriptor actualType;

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
