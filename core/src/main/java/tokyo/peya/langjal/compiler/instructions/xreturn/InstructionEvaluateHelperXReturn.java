package tokyo.peya.langjal.compiler.instructions.xreturn;

import org.antlr.v4.runtime.ParserRuleContext;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.compiler.exceptions.ReturnTypeMismatchedException;
import tokyo.peya.langjal.compiler.jvm.MethodDescriptor;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;

public class InstructionEvaluateHelperXReturn {
    public static void checkReturnType(@NotNull MethodNode method,
                                       @NotNull ParserRuleContext ctxt,
                                       @NotNull TypeDescriptor returningType) {
        MethodDescriptor methodDescriptor = MethodDescriptor.parse(method.desc);
        TypeDescriptor expectedReturnType = methodDescriptor.getReturnType();

        if (isCompatibleReturnType(expectedReturnType, returningType))
            return;

        throw new ReturnTypeMismatchedException(
                method,
                ctxt,
                expectedReturnType,
                returningType
        );
    }

    private static boolean isCompatibleReturnType(@NotNull TypeDescriptor expectedReturnType,
                                                  @NotNull TypeDescriptor returningType) {
        if (expectedReturnType.equals(returningType))
            return true;

        if (returningType.equals(TypeDescriptor.OBJECT))
            return expectedReturnType.isArray()
                    || expectedReturnType.getBaseType().getDescriptor().startsWith("L");

        if (expectedReturnType.isArray() || returningType.isArray())
            return false;

        if (!expectedReturnType.getBaseType().isPrimitive() || !returningType.getBaseType().isPrimitive())
            return false;

        return expectedReturnType.getBaseType().getStackElementType()
                == returningType.getBaseType().getStackElementType();
    }
}
