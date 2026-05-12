package tokyo.peya.langjal.compiler.instructions;

import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.jupiter.api.Nested;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.invokex.*;
import tokyo.peya.langjal.compiler.instructions.utils.AbstractInstructionTestCase;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.integerValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.object;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.create;

public class TestInvokeInstructions {
    private abstract class MethodInvokeTestCase<T extends ParserRuleContext, E extends AbstractInstructionEvaluator<T>>
            extends AbstractInstructionTestCase<T, E> {
        protected MethodInvokeTestCase(E evaluator, int... expectedOpcodes) {
            super(evaluator, expectedOpcodes);
        }

        @Override
        protected void assertInstructionEquals(AbstractInsnNode expected, AbstractInsnNode actual) {
            super.assertInstructionEquals(expected, actual);
            MethodInsnNode expectedMethod = (MethodInsnNode) expected;
            MethodInsnNode actualMethod = (MethodInsnNode) actual;
            assertEquals(expectedMethod.owner, actualMethod.owner);
            assertEquals(expectedMethod.name, actualMethod.name);
            assertEquals(expectedMethod.desc, actualMethod.desc);
        }
    }

    @Nested
    class TestInvokeVirtualCase
            extends MethodInvokeTestCase<JALParser.JvmInsInvokevirtualContext, InstructionEvaluatorInvokeVirtual> {
        TestInvokeVirtualCase() {
            super(new InstructionEvaluatorInvokeVirtual(), EOpcodes.INVOKEVIRTUAL);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    of(
                            create(object(TypeDescriptor.className("my/Owner")), integerValue())
                                    .expected(create(object(TypeDescriptor.className("java/lang/String")))),
                            "invokevirtual my/Owner->call(I)Ljava/lang/String;",
                            new MethodInsnNode(EOpcodes.INVOKEVIRTUAL, "my/Owner", "call", "(I)Ljava/lang/String;")
                    ),
                    of(
                            create(integerValue(), object(TypeDescriptor.className("my/Owner")), integerValue())
                                    .expected(create(
                                            integerValue(),
                                            object(TypeDescriptor.className("java/lang/String"))
                                    )),
                            "invokevirtual my/Owner->call(I)Ljava/lang/String;",
                            new MethodInsnNode(EOpcodes.INVOKEVIRTUAL, "my/Owner", "call", "(I)Ljava/lang/String;")
                    )
            );
        }
    }

    @Nested
    class TestInvokeStaticCase
            extends MethodInvokeTestCase<JALParser.JvmInsInvokestaticContext, InstructionEvaluatorInvokeStatic> {
        TestInvokeStaticCase() {
            super(new InstructionEvaluatorInvokeStatic(), EOpcodes.INVOKESTATIC);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    of(
                            create(integerValue()).expected(create(object(TypeDescriptor.className("java/lang/String")))),
                            "invokestatic my/Owner->call(I)Ljava/lang/String;",
                            new MethodInsnNode(EOpcodes.INVOKESTATIC, "my/Owner", "call", "(I)Ljava/lang/String;")
                    ),
                    of(
                            create(object(TypeDescriptor.className("java/lang/Object")), integerValue())
                                    .expected(create(
                                            object(TypeDescriptor.className("java/lang/Object")),
                                            object(TypeDescriptor.className("java/lang/String"))
                                    )),
                            "invokestatic my/Owner->call(I)Ljava/lang/String;",
                            new MethodInsnNode(EOpcodes.INVOKESTATIC, "my/Owner", "call", "(I)Ljava/lang/String;")
                    )
            );
        }
    }

    @Nested
    class TestInvokeSpecialCase
            extends MethodInvokeTestCase<JALParser.JvmInsInvokespecialContext, InstructionEvaluatorInvokeSpecial> {
        TestInvokeSpecialCase() {
            super(new InstructionEvaluatorInvokeSpecial(), EOpcodes.INVOKESPECIAL);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    of(
                            create(object(TypeDescriptor.className("my/Owner")), integerValue()).expected(create()),
                            "invokespecial my/Owner->call(I)V",
                            new MethodInsnNode(EOpcodes.INVOKESPECIAL, "my/Owner", "call", "(I)V")
                    ),
                    of(
                            create(
                                    object(TypeDescriptor.className("java/lang/Object")),
                                    object(TypeDescriptor.className("my/Owner")),
                                    integerValue()
                            )
                                    .expected(create(object(TypeDescriptor.className("java/lang/Object")))),
                            "invokespecial my/Owner->call(I)V",
                            new MethodInsnNode(EOpcodes.INVOKESPECIAL, "my/Owner", "call", "(I)V")
                    )
            );
        }
    }

    @Nested
    class TestInvokeInterfaceCase
            extends MethodInvokeTestCase<JALParser.JvmInsInvokeinterfaceContext, InstructionEvaluatorInvokeInterface> {
        TestInvokeInterfaceCase() {
            super(new InstructionEvaluatorInvokeInterface(), EOpcodes.INVOKEINTERFACE);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    of(
                            create(object(TypeDescriptor.className("my/Service")), integerValue())
                                    .expected(create(object(TypeDescriptor.className("java/lang/String")))),
                            "invokeinterface my/Service->call(I)Ljava/lang/String;",
                            new MethodInsnNode(EOpcodes.INVOKEINTERFACE, "my/Service", "call", "(I)Ljava/lang/String;")
                    ),
                    of(
                            create(integerValue(), object(TypeDescriptor.className("my/Service")), integerValue())
                                    .expected(create(
                                            integerValue(),
                                            object(TypeDescriptor.className("java/lang/String"))
                                    )),
                            "invokeinterface my/Service->call(I)Ljava/lang/String;",
                            new MethodInsnNode(EOpcodes.INVOKEINTERFACE, "my/Service", "call", "(I)Ljava/lang/String;")
                    )
            );
        }
    }

    @Nested
    class TestInvokeDynamicCase
            extends AbstractInstructionTestCase<JALParser.JvmInsInvokedynamicContext, InstructionEvaluatorInvokeDynamic> {
        TestInvokeDynamicCase() {
            super(new InstructionEvaluatorInvokeDynamic(), EOpcodes.INVOKEDYNAMIC);
        }

        @Override
        protected void assertInstructionEquals(AbstractInsnNode expected, AbstractInsnNode actual) {
            super.assertInstructionEquals(expected, actual);
            InvokeDynamicInsnNode expectedInsn = (InvokeDynamicInsnNode) expected;
            InvokeDynamicInsnNode actualInsn = (InvokeDynamicInsnNode) actual;
            assertEquals(expectedInsn.name, actualInsn.name);
            assertEquals(expectedInsn.desc, actualInsn.desc);
            assertEquals(expectedInsn.bsm, actualInsn.bsm);
            assertArrayEquals(expectedInsn.bsmArgs, actualInsn.bsmArgs);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            Handle handle = new Handle(
                    EOpcodes.H_INVOKESTATIC,
                    "my/Bootstrap",
                    "bootstrap",
                    "()Ljava/lang/invoke/CallSite;",
                    false
            );

            return set(
                    of(
                            create(integerValue()).expected(create(object(TypeDescriptor.className("java/lang/String")))),
                            "invokedynamic dyn(I)Ljava/lang/String; MethodHandle|invokestatic|my/Bootstrap->bootstrap()Ljava/lang/invoke/CallSite; MethodType|(I)Ljava/lang/String; 7",
                            new InvokeDynamicInsnNode(
                                    "dyn",
                                    "(I)Ljava/lang/String;",
                                    handle,
                                    Type.getMethodType("(I)Ljava/lang/String;"),
                                    7
                            )
                    ),
                    of(
                            create(object(TypeDescriptor.className("java/lang/Object")), integerValue())
                                    .expected(create(
                                            object(TypeDescriptor.className("java/lang/Object")),
                                            object(TypeDescriptor.className("java/lang/String"))
                                    )),
                            "invokedynamic dyn(I)Ljava/lang/String; MethodHandle|invokestatic|my/Bootstrap->bootstrap()Ljava/lang/invoke/CallSite; MethodType|(I)Ljava/lang/String; 7",
                            new InvokeDynamicInsnNode(
                                    "dyn",
                                    "(I)Ljava/lang/String;",
                                    handle,
                                    Type.getMethodType("(I)Ljava/lang/String;"),
                                    7
                            )
                    )
            );
        }
    }
}
