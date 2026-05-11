package tokyo.peya.langjal.compiler.instructions;

import org.junit.jupiter.api.Nested;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.utils.AbstractInstructionTestCase;
import tokyo.peya.langjal.compiler.instructions.xcmp_op.InstructionEvaluatorDCmpOp;
import tokyo.peya.langjal.compiler.instructions.xcmp_op.InstructionEvaluatorFCmpOp;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;

import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.doubleValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.floatValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.integerValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.create;

public abstract class TestXCmpOp
{
    @Nested
    static class TestDCmpOpCase extends AbstractInstructionTestCase<JALParser.JvmInsDcmpOPContext, InstructionEvaluatorDCmpOp>
    {
        TestDCmpOpCase()
        {
            super(new InstructionEvaluatorDCmpOp(), EOpcodes.DCMPG, EOpcodes.DCMPL);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    of(create(doubleValue(), doubleValue()).expected(create(integerValue())), "dcmpg", EOpcodes.DCMPG),
                    of(create(doubleValue(), doubleValue()).expected(create(integerValue())), "dcmpl", EOpcodes.DCMPL)
            );
        }
    }

    @Nested
    static class TestFCmpOpCase extends AbstractInstructionTestCase<JALParser.JvmInsFcmpOPContext, InstructionEvaluatorFCmpOp>
    {
        TestFCmpOpCase()
        {
            super(new InstructionEvaluatorFCmpOp(), EOpcodes.FCMPG, EOpcodes.FCMPL);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    of(create(floatValue(), floatValue()).expected(create(integerValue())), "fcmpg", EOpcodes.FCMPG),
                    of(create(floatValue(), floatValue()).expected(create(integerValue())), "fcmpl", EOpcodes.FCMPL)
            );
        }
    }
}
