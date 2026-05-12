package tokyo.peya.langjal.compiler.instructions;

import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.jupiter.api.Nested;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.utils.AbstractInstructionTestCase;
import tokyo.peya.langjal.compiler.instructions.utils.StackMachine;
import tokyo.peya.langjal.compiler.instructions.xstore.*;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.*;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.create;

public class TestXStore {
    private abstract class XStoreTestCase<T extends ParserRuleContext, E extends AbstractInstructionEvaluator<T>>
            extends AbstractInstructionTestCase<T, E> {
        protected XStoreTestCase(E evaluator, int... expectedOpCodes) {
            super(evaluator, expectedOpCodes);
        }

        protected InstructionCase localStore(int index, StackMachine.StackValue value, String syntax, int opcode) {
            return of(
                    create(value).expected(create().set(index, value)),
                    syntax,
                    new VarInsnNode(opcode, index)
            );
        }

        protected InstructionCase localStoreWithState(int index, StackMachine.StackValue value, String syntax,
                                                      int opcode) {
            return of(
                    create(object(TypeDescriptor.className("java/lang/String")), value).set(10, integerValue())
                            .expected(create(object(
                                    TypeDescriptor.className(
                                            "java/lang/String"))).set(
                                    index,
                                    value
                            ).set(10, integerValue())),
                    syntax,
                    new VarInsnNode(opcode, index)
            );
        }

        @Override
        protected void assertInstructionEquals(AbstractInsnNode expected, AbstractInsnNode actual) {
            super.assertInstructionEquals(expected, actual);

            VarInsnNode expectedVar = (VarInsnNode) expected;
            VarInsnNode actualVar = (VarInsnNode) actual;
            assertEquals(expectedVar.var, actualVar.var, "local variable index does not match");
        }
    }

    @Nested
    class TestAStore extends XStoreTestCase<JALParser.JvmInsAstoreContext, InstructionEvaluatorAStore> {
        private static final StackMachine.StackValue OBJECT_VALUE = object(TypeDescriptor.parse("LMyClass;"));

        TestAStore() {
            super(new InstructionEvaluatorAStore(), EOpcodes.ASTORE);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    localStore(1, OBJECT_VALUE, "astore 1", Opcodes.ASTORE),
                    localStoreWithState(1, OBJECT_VALUE, "astore 1", Opcodes.ASTORE),
                    localStore(300, OBJECT_VALUE, "wide astore 300", Opcodes.ASTORE)
            );
        }
    }

    @Nested
    class TestAStoreN extends XStoreTestCase<JALParser.JvmInsAstoreNContext, InstructionEvaluatorAStoreN> {
        private static final StackMachine.StackValue OBJECT_VALUE = object(TypeDescriptor.parse("LMyClass;"));

        TestAStoreN() {
            super(
                    new InstructionEvaluatorAStoreN(),
                    EOpcodes.ASTORE_0,
                    EOpcodes.ASTORE_1,
                    EOpcodes.ASTORE_2,
                    EOpcodes.ASTORE_3
            );
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    localStore(0, OBJECT_VALUE, "astore_0", Opcodes.ASTORE),
                    localStoreWithState(0, OBJECT_VALUE, "astore_0", Opcodes.ASTORE),
                    localStore(1, OBJECT_VALUE, "astore_1", Opcodes.ASTORE),
                    localStore(2, OBJECT_VALUE, "astore_2", Opcodes.ASTORE),
                    localStore(3, OBJECT_VALUE, "astore_3", Opcodes.ASTORE)
            );
        }
    }

    @Nested
    class TestDStore extends XStoreTestCase<JALParser.JvmInsDstoreContext, InstructionEvaluatorDStore> {
        TestDStore() {
            super(new InstructionEvaluatorDStore(), EOpcodes.DSTORE);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    localStore(1, doubleValue(), "dstore 1", Opcodes.DSTORE),
                    localStoreWithState(1, doubleValue(), "dstore 1", Opcodes.DSTORE),
                    localStore(300, doubleValue(), "wide dstore 300", Opcodes.DSTORE)
            );
        }
    }

    @Nested
    class TestDStoreN extends XStoreTestCase<JALParser.JvmInsDstoreNContext, InstructionEvaluatorDStoreN> {
        TestDStoreN() {
            super(
                    new InstructionEvaluatorDStoreN(),
                    EOpcodes.DSTORE_0,
                    EOpcodes.DSTORE_1,
                    EOpcodes.DSTORE_2,
                    EOpcodes.DSTORE_3
            );
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    localStore(0, doubleValue(), "dstore_0", Opcodes.DSTORE),
                    localStoreWithState(0, doubleValue(), "dstore_0", Opcodes.DSTORE),
                    localStore(1, doubleValue(), "dstore_1", Opcodes.DSTORE),
                    localStore(2, doubleValue(), "dstore_2", Opcodes.DSTORE),
                    localStore(3, doubleValue(), "dstore_3", Opcodes.DSTORE)
            );
        }
    }

    @Nested
    class TestFStore extends XStoreTestCase<JALParser.JvmInsFstoreContext, InstructionEvaluatorFStore> {
        TestFStore() {
            super(new InstructionEvaluatorFStore(), EOpcodes.FSTORE);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    localStore(1, floatValue(), "fstore 1", Opcodes.FSTORE),
                    localStoreWithState(1, floatValue(), "fstore 1", Opcodes.FSTORE),
                    localStore(300, floatValue(), "wide fstore 300", Opcodes.FSTORE)
            );
        }
    }

    @Nested
    class TestFStoreN extends XStoreTestCase<JALParser.JvmInsFstoreNContext, InstructionEvaluatorFStoreN> {
        TestFStoreN() {
            super(
                    new InstructionEvaluatorFStoreN(),
                    EOpcodes.FSTORE_0,
                    EOpcodes.FSTORE_1,
                    EOpcodes.FSTORE_2,
                    EOpcodes.FSTORE_3
            );
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    localStore(0, floatValue(), "fstore_0", Opcodes.FSTORE),
                    localStoreWithState(0, floatValue(), "fstore_0", Opcodes.FSTORE),
                    localStore(1, floatValue(), "fstore_1", Opcodes.FSTORE),
                    localStore(2, floatValue(), "fstore_2", Opcodes.FSTORE),
                    localStore(3, floatValue(), "fstore_3", Opcodes.FSTORE)
            );
        }
    }

    @Nested
    class TestIStore extends XStoreTestCase<JALParser.JvmInsIstoreContext, InstructionEvaluatorIStore> {
        TestIStore() {
            super(new InstructionEvaluatorIStore(), EOpcodes.ISTORE);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    localStore(1, integerValue(), "istore 1", Opcodes.ISTORE),
                    localStoreWithState(1, integerValue(), "istore 1", Opcodes.ISTORE),
                    localStore(300, integerValue(), "wide istore 300", Opcodes.ISTORE)
            );
        }
    }

    @Nested
    class TestIStoreN extends XStoreTestCase<JALParser.JvmInsIstoreNContext, InstructionEvaluatorIStoreN> {
        TestIStoreN() {
            super(
                    new InstructionEvaluatorIStoreN(),
                    EOpcodes.ISTORE_0,
                    EOpcodes.ISTORE_1,
                    EOpcodes.ISTORE_2,
                    EOpcodes.ISTORE_3
            );
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    localStore(0, integerValue(), "istore_0", Opcodes.ISTORE),
                    localStoreWithState(0, integerValue(), "istore_0", Opcodes.ISTORE),
                    localStore(1, integerValue(), "istore_1", Opcodes.ISTORE),
                    localStore(2, integerValue(), "istore_2", Opcodes.ISTORE),
                    localStore(3, integerValue(), "istore_3", Opcodes.ISTORE)
            );
        }
    }

    @Nested
    class TestLStore extends XStoreTestCase<JALParser.JvmInsLstoreContext, InstructionEvaluatorLStore> {
        TestLStore() {
            super(new InstructionEvaluatorLStore(), EOpcodes.LSTORE);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    localStore(1, longValue(), "lstore 1", Opcodes.LSTORE),
                    localStoreWithState(1, longValue(), "lstore 1", Opcodes.LSTORE),
                    localStore(300, longValue(), "wide lstore 300", Opcodes.LSTORE)
            );
        }
    }

    @Nested
    class TestLStoreN extends XStoreTestCase<JALParser.JvmInsLstoreNContext, InstructionEvaluatorLStoreN> {
        TestLStoreN() {
            super(
                    new InstructionEvaluatorLStoreN(),
                    EOpcodes.LSTORE_0,
                    EOpcodes.LSTORE_1,
                    EOpcodes.LSTORE_2,
                    EOpcodes.LSTORE_3
            );
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    localStore(0, longValue(), "lstore_0", Opcodes.LSTORE),
                    localStoreWithState(0, longValue(), "lstore_0", Opcodes.LSTORE),
                    localStore(1, longValue(), "lstore_1", Opcodes.LSTORE),
                    localStore(2, longValue(), "lstore_2", Opcodes.LSTORE),
                    localStore(3, longValue(), "lstore_3", Opcodes.LSTORE)
            );
        }
    }
}
