package tokyo.peya.langjal.compiler.instructions;

import org.junit.jupiter.api.Nested;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.dup.InstructionEvaluatorDup;
import tokyo.peya.langjal.compiler.instructions.dup.InstructionEvaluatorDup2;
import tokyo.peya.langjal.compiler.instructions.dup.InstructionEvaluatorDup2X1;
import tokyo.peya.langjal.compiler.instructions.dup.InstructionEvaluatorDup2X2;
import tokyo.peya.langjal.compiler.instructions.dup.InstructionEvaluatorDupX1;
import tokyo.peya.langjal.compiler.instructions.dup.InstructionEvaluatorDupX2;
import tokyo.peya.langjal.compiler.instructions.utils.AbstractInstructionTestCase;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;

import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.floatValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.integerValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.object;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.create;

public abstract class TestDup
{
    @Nested
    static class TestDupCase extends AbstractInstructionTestCase<JALParser.JvmInsDupContext, InstructionEvaluatorDup>
    {
        TestDupCase()
        {
            super(new InstructionEvaluatorDup(), EOpcodes.DUP);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(of(create(integerValue()).expected(create(integerValue(), integerValue())), "dup", EOpcodes.DUP));
        }
    }

    @Nested
    static class TestDupX1Case extends AbstractInstructionTestCase<JALParser.JvmInsDupX1Context, InstructionEvaluatorDupX1>
    {
        TestDupX1Case()
        {
            super(new InstructionEvaluatorDupX1(), EOpcodes.DUP_X1);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(of(create(integerValue(), floatValue()).expected(create(floatValue(), integerValue(), floatValue())), "dup_x1", EOpcodes.DUP_X1));
        }
    }

    @Nested
    static class TestDupX2Case extends AbstractInstructionTestCase<JALParser.JvmInsDupX2Context, InstructionEvaluatorDupX2>
    {
        TestDupX2Case()
        {
            super(new InstructionEvaluatorDupX2(), EOpcodes.DUP_X2);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(of(create(object(TypeDescriptor.className("java/lang/String")), integerValue(), floatValue()).expected(create(floatValue(), object(TypeDescriptor.className("java/lang/String")), integerValue(), floatValue())), "dup_x2", EOpcodes.DUP_X2));
        }
    }

    @Nested
    static class TestDup2Case extends AbstractInstructionTestCase<JALParser.JvmInsDup2Context, InstructionEvaluatorDup2>
    {
        TestDup2Case()
        {
            super(new InstructionEvaluatorDup2(), EOpcodes.DUP2);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(of(create(integerValue(), floatValue()).expected(create(integerValue(), floatValue(), integerValue(), floatValue())), "dup2", EOpcodes.DUP2));
        }
    }

    @Nested
    static class TestDup2X1Case extends AbstractInstructionTestCase<JALParser.JvmInsDup2X1Context, InstructionEvaluatorDup2X1>
    {
        TestDup2X1Case()
        {
            super(new InstructionEvaluatorDup2X1(), EOpcodes.DUP2_X1);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(of(create(object(TypeDescriptor.className("java/lang/String")), integerValue(), floatValue()).expected(create(integerValue(), floatValue(), object(TypeDescriptor.className("java/lang/String")), integerValue(), floatValue())), "dup2_x1", EOpcodes.DUP2_X1));
        }
    }

    @Nested
    static class TestDup2X2Case extends AbstractInstructionTestCase<JALParser.JvmInsDup2X2Context, InstructionEvaluatorDup2X2>
    {
        TestDup2X2Case()
        {
            super(new InstructionEvaluatorDup2X2(), EOpcodes.DUP2_X2);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(of(create(object(TypeDescriptor.className("java/lang/Object")), object(TypeDescriptor.className("java/lang/String")), integerValue(), floatValue()).expected(create(integerValue(), floatValue(), object(TypeDescriptor.className("java/lang/Object")), object(TypeDescriptor.className("java/lang/String")), integerValue(), floatValue())), "dup2_x2", EOpcodes.DUP2_X2));
        }
    }
}
