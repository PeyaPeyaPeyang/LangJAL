package tokyo.peya.langjal.compiler.instructions;

import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.jupiter.api.Nested;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.utils.AbstractInstructionTestCase;
import tokyo.peya.langjal.compiler.instructions.xreturn.InstructionEvaluatorAReturn;
import tokyo.peya.langjal.compiler.instructions.xreturn.InstructionEvaluatorDReturn;
import tokyo.peya.langjal.compiler.instructions.xreturn.InstructionEvaluatorFReturn;
import tokyo.peya.langjal.compiler.instructions.xreturn.InstructionEvaluatorIReturn;
import tokyo.peya.langjal.compiler.instructions.xreturn.InstructionEvaluatorLReturn;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;

import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.doubleValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.floatValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.integerValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.longValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.object;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.create;

public class TestXReturn
{
    private abstract class XReturnTestCase<T extends ParserRuleContext, E extends AbstractInstructionEvaluator<T>>
            extends AbstractInstructionTestCase<T, E>
    {
        private final String methodDescriptor;

        protected XReturnTestCase(E evaluator, String methodDescriptor, int... expectedOpcodes)
        {
            super(evaluator, expectedOpcodes);
            this.methodDescriptor = methodDescriptor;
        }

        @Override
        protected MethodNode createDummyMethod()
        {
            return new MethodNode(
                    EOpcodes.ACC_PUBLIC,
                    "testDummyMethod",
                    this.methodDescriptor,
                    null,
                    null
            );
        }
    }

    @Nested
    class TestIReturnCase extends XReturnTestCase<JALParser.JvmInsIreturnContext, InstructionEvaluatorIReturn>
    {
        TestIReturnCase()
        {
            super(new InstructionEvaluatorIReturn(), "()I", EOpcodes.IRETURN);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(of(create(integerValue()).expected(create()), "ireturn", EOpcodes.IRETURN));
        }
    }

    @Nested
    class TestLReturnCase extends XReturnTestCase<JALParser.JvmInsLreturnContext, InstructionEvaluatorLReturn>
    {
        TestLReturnCase()
        {
            super(new InstructionEvaluatorLReturn(), "()J", EOpcodes.LRETURN);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(of(create(longValue()).expected(create()), "lreturn", EOpcodes.LRETURN));
        }
    }

    @Nested
    class TestFReturnCase extends XReturnTestCase<JALParser.JvmInsFreturnContext, InstructionEvaluatorFReturn>
    {
        TestFReturnCase()
        {
            super(new InstructionEvaluatorFReturn(), "()F", EOpcodes.FRETURN);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(of(create(floatValue()).expected(create()), "freturn", EOpcodes.FRETURN));
        }
    }

    @Nested
    class TestDReturnCase extends XReturnTestCase<JALParser.JvmInsDreturnContext, InstructionEvaluatorDReturn>
    {
        TestDReturnCase()
        {
            super(new InstructionEvaluatorDReturn(), "()D", EOpcodes.DRETURN);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(of(create(doubleValue()).expected(create()), "dreturn", EOpcodes.DRETURN));
        }
    }

    @Nested
    class TestAReturnCase extends XReturnTestCase<JALParser.JvmInsAreturnContext, InstructionEvaluatorAReturn>
    {
        TestAReturnCase()
        {
            super(new InstructionEvaluatorAReturn(), "()Ljava/lang/String;", EOpcodes.ARETURN);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(of(create(object(TypeDescriptor.className("java/lang/String"))).expected(create()), "areturn", EOpcodes.ARETURN));
        }
    }
}
