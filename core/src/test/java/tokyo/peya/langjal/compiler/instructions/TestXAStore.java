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

public abstract class TestXAStore<T extends ParserRuleContext, E extends AbstractInstructionEvaluator<T>>
        extends AbstractInstructionTestCase<T, E>
{
    protected TestXAStore(E evaluator, int... expectedOpcodes)
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

    @Nested
    static class TestAAStoreCase extends TestXAStore<JALParser.JvmInsAastoreContext, InstructionEvaluatorAAStore>
    {
        TestAAStoreCase()
        {
            super(new InstructionEvaluatorAAStore(), EOpcodes.AASTORE);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    store(object(TypeDescriptor.parse("[Ljava/lang/String;")), object(TypeDescriptor.className("java/lang/String")), "aastore", EOpcodes.AASTORE)
            );
        }
    }

    @Nested
    static class TestBAStoreCase extends TestXAStore<JALParser.JvmInsBastoreContext, InstructionEvaluatorBAStore>
    {
        TestBAStoreCase()
        {
            super(new InstructionEvaluatorBAStore(), EOpcodes.BASTORE);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(store(object(TypeDescriptor.parse("[B")), integerValue(), "bastore", EOpcodes.BASTORE));
        }
    }

    @Nested
    static class TestCAStoreCase extends TestXAStore<JALParser.JvmInsCastoreContext, InstructionEvaluatorCAStore>
    {
        TestCAStoreCase()
        {
            super(new InstructionEvaluatorCAStore(), EOpcodes.CASTORE);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(store(object(TypeDescriptor.parse("[C")), integerValue(), "castore", EOpcodes.CASTORE));
        }
    }

    @Nested
    static class TestDAStoreCase extends TestXAStore<JALParser.JvmInsDastoreContext, InstructionEvaluatorDAStore>
    {
        TestDAStoreCase()
        {
            super(new InstructionEvaluatorDAStore(), EOpcodes.DASTORE);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(store(object(TypeDescriptor.parse("[D")), doubleValue(), "dastore", EOpcodes.DASTORE));
        }
    }

    @Nested
    static class TestFAStoreCase extends TestXAStore<JALParser.JvmInsFastoreContext, InstructionEvaluatorFAStore>
    {
        TestFAStoreCase()
        {
            super(new InstructionEvaluatorFAStore(), EOpcodes.FASTORE);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(store(object(TypeDescriptor.parse("[F")), floatValue(), "fastore", EOpcodes.FASTORE));
        }
    }

    @Nested
    static class TestIAStoreCase extends TestXAStore<JALParser.JvmInsIastoreContext, InstructionEvaluatorIAStore>
    {
        TestIAStoreCase()
        {
            super(new InstructionEvaluatorIAStore(), EOpcodes.IASTORE);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(store(object(TypeDescriptor.parse("[I")), integerValue(), "iastore", EOpcodes.IASTORE));
        }
    }

    @Nested
    static class TestLAStoreCase extends TestXAStore<JALParser.JvmInsLastoreContext, InstructionEvaluatorLAStore>
    {
        TestLAStoreCase()
        {
            super(new InstructionEvaluatorLAStore(), EOpcodes.LASTORE);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(store(object(TypeDescriptor.parse("[J")), longValue(), "lastore", EOpcodes.LASTORE));
        }
    }

    @Nested
    static class TestSAStoreCase extends TestXAStore<JALParser.JvmInsSastoreContext, InstructionEvaluatorSAStore>
    {
        TestSAStoreCase()
        {
            super(new InstructionEvaluatorSAStore(), EOpcodes.SASTORE);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(store(object(TypeDescriptor.parse("[S")), integerValue(), "sastore", EOpcodes.SASTORE));
        }
    }
}
