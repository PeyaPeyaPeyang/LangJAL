package tokyo.peya.langjal.compiler.instructions;

import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.jupiter.api.Nested;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.utils.AbstractInstructionTestCase;
import tokyo.peya.langjal.compiler.instructions.utils.StackMachine;
import tokyo.peya.langjal.compiler.instructions.xastore.InstructionEvaluatorAAStore;
import tokyo.peya.langjal.compiler.instructions.xastore.InstructionEvaluatorBAStore;
import tokyo.peya.langjal.compiler.instructions.xastore.InstructionEvaluatorCAStore;
import tokyo.peya.langjal.compiler.instructions.xastore.InstructionEvaluatorDAStore;
import tokyo.peya.langjal.compiler.instructions.xastore.InstructionEvaluatorFAStore;
import tokyo.peya.langjal.compiler.instructions.xastore.InstructionEvaluatorIAStore;
import tokyo.peya.langjal.compiler.instructions.xastore.InstructionEvaluatorLAStore;
import tokyo.peya.langjal.compiler.instructions.xastore.InstructionEvaluatorSAStore;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;

import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.doubleValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.floatValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.integerValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.longValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.object;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.create;

public class TestXAStore
{
    private abstract class XAStoreTestCase<T extends ParserRuleContext, E extends AbstractInstructionEvaluator<T>>
            extends AbstractInstructionTestCase<T, E>
    {
        protected XAStoreTestCase(E evaluator, int... expectedOpcodes)
        {
            super(evaluator, expectedOpcodes);
        }

        protected InstructionCase store(StackMachine.StackValue arrayType,
                                        StackMachine.StackValue storedType,
                                        String syntax,
                                        int opcode)
        {
            return of(
                    create(arrayType, integerValue(), storedType).expected(create()),
                    syntax,
                    opcode
            );
        }

        protected InstructionCase storeWithBase(StackMachine.StackValue arrayType,
                                                StackMachine.StackValue storedType,
                                                String syntax,
                                                int opcode)
        {
            return of(
                    create(object(TypeDescriptor.className("java/lang/String")), arrayType, integerValue(), storedType)
                            .expected(create(object(TypeDescriptor.className("java/lang/String")))),
                    syntax,
                    opcode
            );
        }
    }

    @Nested
    class TestAAStoreCase extends XAStoreTestCase<JALParser.JvmInsAastoreContext, InstructionEvaluatorAAStore>
    {
        TestAAStoreCase()
        {
            super(new InstructionEvaluatorAAStore(), EOpcodes.AASTORE);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    store(object(TypeDescriptor.parse("[Ljava/lang/String;")), object(TypeDescriptor.className("java/lang/String")), "aastore", EOpcodes.AASTORE),
                    storeWithBase(object(TypeDescriptor.parse("[Ljava/lang/String;")), object(TypeDescriptor.className("java/lang/String")), "aastore", EOpcodes.AASTORE)
            );
        }
    }

    @Nested
    class TestBAStoreCase extends XAStoreTestCase<JALParser.JvmInsBastoreContext, InstructionEvaluatorBAStore>
    {
        TestBAStoreCase()
        {
            super(new InstructionEvaluatorBAStore(), EOpcodes.BASTORE);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    store(object(TypeDescriptor.parse("[B")), integerValue(), "bastore", EOpcodes.BASTORE),
                    storeWithBase(object(TypeDescriptor.parse("[B")), integerValue(), "bastore", EOpcodes.BASTORE)
            );
        }
    }

    @Nested
    class TestCAStoreCase extends XAStoreTestCase<JALParser.JvmInsCastoreContext, InstructionEvaluatorCAStore>
    {
        TestCAStoreCase()
        {
            super(new InstructionEvaluatorCAStore(), EOpcodes.CASTORE);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    store(object(TypeDescriptor.parse("[C")), integerValue(), "castore", EOpcodes.CASTORE),
                    storeWithBase(object(TypeDescriptor.parse("[C")), integerValue(), "castore", EOpcodes.CASTORE)
            );
        }
    }

    @Nested
    class TestDAStoreCase extends XAStoreTestCase<JALParser.JvmInsDastoreContext, InstructionEvaluatorDAStore>
    {
        TestDAStoreCase()
        {
            super(new InstructionEvaluatorDAStore(), EOpcodes.DASTORE);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    store(object(TypeDescriptor.parse("[D")), doubleValue(), "dastore", EOpcodes.DASTORE),
                    storeWithBase(object(TypeDescriptor.parse("[D")), doubleValue(), "dastore", EOpcodes.DASTORE)
            );
        }
    }

    @Nested
    class TestFAStoreCase extends XAStoreTestCase<JALParser.JvmInsFastoreContext, InstructionEvaluatorFAStore>
    {
        TestFAStoreCase()
        {
            super(new InstructionEvaluatorFAStore(), EOpcodes.FASTORE);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    store(object(TypeDescriptor.parse("[F")), floatValue(), "fastore", EOpcodes.FASTORE),
                    storeWithBase(object(TypeDescriptor.parse("[F")), floatValue(), "fastore", EOpcodes.FASTORE)
            );
        }
    }

    @Nested
    class TestIAStoreCase extends XAStoreTestCase<JALParser.JvmInsIastoreContext, InstructionEvaluatorIAStore>
    {
        TestIAStoreCase()
        {
            super(new InstructionEvaluatorIAStore(), EOpcodes.IASTORE);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    store(object(TypeDescriptor.parse("[I")), integerValue(), "iastore", EOpcodes.IASTORE),
                    storeWithBase(object(TypeDescriptor.parse("[I")), integerValue(), "iastore", EOpcodes.IASTORE)
            );
        }
    }

    @Nested
    class TestLAStoreCase extends XAStoreTestCase<JALParser.JvmInsLastoreContext, InstructionEvaluatorLAStore>
    {
        TestLAStoreCase()
        {
            super(new InstructionEvaluatorLAStore(), EOpcodes.LASTORE);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    store(object(TypeDescriptor.parse("[J")), longValue(), "lastore", EOpcodes.LASTORE),
                    storeWithBase(object(TypeDescriptor.parse("[J")), longValue(), "lastore", EOpcodes.LASTORE)
            );
        }
    }

    @Nested
    class TestSAStoreCase extends XAStoreTestCase<JALParser.JvmInsSastoreContext, InstructionEvaluatorSAStore>
    {
        TestSAStoreCase()
        {
            super(new InstructionEvaluatorSAStore(), EOpcodes.SASTORE);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    store(object(TypeDescriptor.parse("[S")), integerValue(), "sastore", EOpcodes.SASTORE),
                    storeWithBase(object(TypeDescriptor.parse("[S")), integerValue(), "sastore", EOpcodes.SASTORE)
            );
        }
    }
}
