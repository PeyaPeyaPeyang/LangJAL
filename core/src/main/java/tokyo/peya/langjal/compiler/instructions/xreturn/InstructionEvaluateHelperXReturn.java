package tokyo.peya.langjal.compiler.instructions.xreturn;

import org.antlr.v4.runtime.ParserRuleContext;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.compiler.exceptions.ReturnTypeMismatchedException;
import tokyo.peya.langjal.compiler.jvm.MethodDescriptor;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;

public class InstructionEvaluateHelperXReturn
{
    public static void checkReturnType(@NotNull MethodNode method,
                                       @NotNull ParserRuleContext ctxt,
                                       @NotNull TypeDescriptor returningType)
    {
        MethodDescriptor methodDescriptor = MethodDescriptor.parse(method.desc);
        TypeDescriptor expectedReturnType = methodDescriptor.getReturnType();

        if (returningType.equals(TypeDescriptor.OBJECT))
        {
            if (expectedReturnType.getBaseType().getDescriptor().startsWith("L"))
                return;

        }

        if (!expectedReturnType.equals(returningType))
            throw new ReturnTypeMismatchedException(
                    method,
                    ctxt,
                    expectedReturnType,
                    returningType
            );
    }
}
