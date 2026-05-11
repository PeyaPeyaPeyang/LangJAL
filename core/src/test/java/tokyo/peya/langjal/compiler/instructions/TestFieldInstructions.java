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

public abstract class TestFieldInstructions<T extends ParserRuleContext, E extends AbstractInstructionEvaluator<T>>
        extends AbstractInstructionTestCase<T, E>
{
    protected TestFieldInstructions(E evaluator, int... expectedOpcodes)
    {
        super(evaluator, expectedOpcodes);
    }

    @Override
    protected void assertInstructionEquals(AbstractInsnNode expected, AbstractInsnNode actual)
    {
        super.assertInstructionEquals(expected, actual);
        FieldInsnNode expectedField = (FieldInsnNode) expected;
        FieldInsnNode actualField = (FieldInsnNode) actual;
        assertEquals(expectedField.owner, actualField.owner);
        assertEquals(expectedField.name, actualField.name);
        assertEquals(expectedField.desc, actualField.desc);
    }

    @Nested
    static class TestGetFieldCase extends TestFieldInstructions<JALParser.JvmInsGetfieldContext, InstructionEvaluatorGetField>
    {
        TestGetFieldCase()
        {
            super(new InstructionEvaluatorGetField(), EOpcodes.GETFIELD);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    of(create(object(TypeDescriptor.className("my/Owner"))).expected(create(integerValue())),
                            "getfield my/Owner->value:I",
                            new FieldInsnNode(EOpcodes.GETFIELD, "my/Owner", "value", "I"))
            );
        }
    }

    @Nested
    static class TestPutFieldCase extends TestFieldInstructions<JALParser.JvmInsPutfieldContext, InstructionEvaluatorPutField>
    {
        TestPutFieldCase()
        {
            super(new InstructionEvaluatorPutField(), EOpcodes.PUTFIELD);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    of(create(object(TypeDescriptor.className("my/Owner")), integerValue()).expected(create()),
                            "putfield my/Owner->value:I",
                            new FieldInsnNode(EOpcodes.PUTFIELD, "my/Owner", "value", "I"))
            );
        }
    }

    @Nested
    static class TestGetStaticCase extends TestFieldInstructions<JALParser.JvmInsGetstaticContext, InstructionEvaluatorGetStatic>
    {
        TestGetStaticCase()
        {
            super(new InstructionEvaluatorGetStatic(), EOpcodes.GETSTATIC);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    of(create().expected(create(integerValue())),
                            "getstatic my/Owner->value:I",
                            new FieldInsnNode(EOpcodes.GETSTATIC, "my/Owner", "value", "I"))
            );
        }
    }

    @Nested
    static class TestPutStaticCase extends TestFieldInstructions<JALParser.JvmInsPutstaticContext, InstructionEvaluatorPutStatic>
    {
        TestPutStaticCase()
        {
            super(new InstructionEvaluatorPutStatic(), EOpcodes.PUTSTATIC);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    of(create(integerValue()).expected(create()),
                            "putstatic my/Owner->value:I",
                            new FieldInsnNode(EOpcodes.PUTSTATIC, "my/Owner", "value", "I"))
            );
        }
    }
}
