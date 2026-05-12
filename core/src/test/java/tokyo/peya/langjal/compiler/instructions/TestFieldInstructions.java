package tokyo.peya.langjal.compiler.instructions;

import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.jupiter.api.Nested;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.field.InstructionEvaluatorGetField;
import tokyo.peya.langjal.compiler.instructions.field.InstructionEvaluatorGetStatic;
import tokyo.peya.langjal.compiler.instructions.field.InstructionEvaluatorPutField;
import tokyo.peya.langjal.compiler.instructions.field.InstructionEvaluatorPutStatic;
import tokyo.peya.langjal.compiler.instructions.utils.AbstractInstructionTestCase;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.integerValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.object;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.create;

public class TestFieldInstructions {
    private abstract class FieldInstructionTestCase<T extends ParserRuleContext, E extends AbstractInstructionEvaluator<T>>
            extends AbstractInstructionTestCase<T, E> {
        protected FieldInstructionTestCase(E evaluator, int... expectedOpcodes) {
            super(evaluator, expectedOpcodes);
        }

        @Override
        protected void assertInstructionEquals(AbstractInsnNode expected, AbstractInsnNode actual) {
            super.assertInstructionEquals(expected, actual);
            FieldInsnNode expectedField = (FieldInsnNode) expected;
            FieldInsnNode actualField = (FieldInsnNode) actual;
            assertEquals(expectedField.owner, actualField.owner);
            assertEquals(expectedField.name, actualField.name);
            assertEquals(expectedField.desc, actualField.desc);
        }
    }

    @Nested
    class TestGetFieldCase
            extends FieldInstructionTestCase<JALParser.JvmInsGetfieldContext, InstructionEvaluatorGetField> {
        TestGetFieldCase() {
            super(new InstructionEvaluatorGetField(), EOpcodes.GETFIELD);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    of(
                            create(object(TypeDescriptor.className("my/Owner"))).expected(create(integerValue())),
                            "getfield my/Owner->value:I",
                            new FieldInsnNode(EOpcodes.GETFIELD, "my/Owner", "value", "I")
                    ),
                    of(
                            create(
                                    object(TypeDescriptor.className("java/lang/String")),
                                    object(TypeDescriptor.className("my/Owner"))
                            )
                                    .expected(create(
                                            object(TypeDescriptor.className("java/lang/String")),
                                            integerValue()
                                    )),
                            "getfield my/Owner->value:I",
                            new FieldInsnNode(EOpcodes.GETFIELD, "my/Owner", "value", "I")
                    )
            );
        }
    }

    @Nested
    class TestPutFieldCase
            extends FieldInstructionTestCase<JALParser.JvmInsPutfieldContext, InstructionEvaluatorPutField> {
        TestPutFieldCase() {
            super(new InstructionEvaluatorPutField(), EOpcodes.PUTFIELD);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    of(
                            create(object(TypeDescriptor.className("my/Owner")), integerValue()).expected(create()),
                            "putfield my/Owner->value:I",
                            new FieldInsnNode(EOpcodes.PUTFIELD, "my/Owner", "value", "I")
                    ),
                    of(
                            create(
                                    object(TypeDescriptor.className("java/lang/String")),
                                    object(TypeDescriptor.className("my/Owner")),
                                    integerValue()
                            )
                                    .expected(create(object(TypeDescriptor.className("java/lang/String")))),
                            "putfield my/Owner->value:I",
                            new FieldInsnNode(EOpcodes.PUTFIELD, "my/Owner", "value", "I")
                    )
            );
        }
    }

    @Nested
    class TestGetStaticCase
            extends FieldInstructionTestCase<JALParser.JvmInsGetstaticContext, InstructionEvaluatorGetStatic> {
        TestGetStaticCase() {
            super(new InstructionEvaluatorGetStatic(), EOpcodes.GETSTATIC);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    of(
                            create().expected(create(integerValue())),
                            "getstatic my/Owner->value:I",
                            new FieldInsnNode(EOpcodes.GETSTATIC, "my/Owner", "value", "I")
                    ),
                    of(
                            create(object(TypeDescriptor.className("java/lang/String")))
                                    .expected(create(
                                            object(TypeDescriptor.className("java/lang/String")),
                                            integerValue()
                                    )),
                            "getstatic my/Owner->value:I",
                            new FieldInsnNode(EOpcodes.GETSTATIC, "my/Owner", "value", "I")
                    )
            );
        }
    }

    @Nested
    class TestPutStaticCase
            extends FieldInstructionTestCase<JALParser.JvmInsPutstaticContext, InstructionEvaluatorPutStatic> {
        TestPutStaticCase() {
            super(new InstructionEvaluatorPutStatic(), EOpcodes.PUTSTATIC);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    of(
                            create(integerValue()).expected(create()),
                            "putstatic my/Owner->value:I",
                            new FieldInsnNode(EOpcodes.PUTSTATIC, "my/Owner", "value", "I")
                    ),
                    of(
                            create(object(TypeDescriptor.className("java/lang/String")), integerValue())
                                    .expected(create(object(TypeDescriptor.className("java/lang/String")))),
                            "putstatic my/Owner->value:I",
                            new FieldInsnNode(EOpcodes.PUTSTATIC, "my/Owner", "value", "I")
                    )
            );
        }
    }
}
