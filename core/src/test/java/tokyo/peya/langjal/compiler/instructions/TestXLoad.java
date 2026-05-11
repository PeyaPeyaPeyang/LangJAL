package tokyo.peya.langjal.compiler.instructions;

import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.jupiter.api.Nested;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.utils.AbstractInstructionTestCase;
import tokyo.peya.langjal.compiler.instructions.utils.StackMachine;
import tokyo.peya.langjal.compiler.instructions.xload.InstructionEvaluatorALoad;
import tokyo.peya.langjal.compiler.instructions.xload.InstructionEvaluatorALoadN;
import tokyo.peya.langjal.compiler.instructions.xload.InstructionEvaluatorDLoad;
import tokyo.peya.langjal.compiler.instructions.xload.InstructionEvaluatorDLoadN;
import tokyo.peya.langjal.compiler.instructions.xload.InstructionEvaluatorFLoad;
import tokyo.peya.langjal.compiler.instructions.xload.InstructionEvaluatorFLoadN;
import tokyo.peya.langjal.compiler.instructions.xload.InstructionEvaluatorILoad;
import tokyo.peya.langjal.compiler.instructions.xload.InstructionEvaluatorILoadN;
import tokyo.peya.langjal.compiler.instructions.xload.InstructionEvaluatorLLoad;
import tokyo.peya.langjal.compiler.instructions.xload.InstructionEvaluatorLLoadN;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.doubleValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.floatValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.integerValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.longValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.object;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.create;

public class TestXLoad
{
    private abstract class XLoadTestCase<T extends ParserRuleContext, E extends AbstractInstructionEvaluator<T>>
            extends AbstractInstructionTestCase<T, E>
    {
        protected XLoadTestCase(E evaluator, int... expectedOpCodes)
        {
            super(evaluator, expectedOpCodes);
        }

        protected InstructionCase localLoad(int index, StackMachine.StackValue value, String syntax, int opcode)
        {
            return of(
                    create().set(index, value).expected(create(value).set(index, value)),
                    syntax,
                    new VarInsnNode(opcode, index)
            );
        }

        @Override
        protected void assertInstructionEquals(AbstractInsnNode expected, AbstractInsnNode actual)
        {
            super.assertInstructionEquals(expected, actual);

            VarInsnNode expectedVar = (VarInsnNode) expected;
            VarInsnNode actualVar = (VarInsnNode) actual;
            assertEquals(expectedVar.var, actualVar.var, "local variable index does not match");
        }
    }

    @Nested
    class TestALoad extends XLoadTestCase<JALParser.JvmInsAloadContext, InstructionEvaluatorALoad>
    {
        private static final StackMachine.StackValue OBJECT_VALUE = object(TypeDescriptor.parse("LMyClass;"));

        TestALoad()
        {
            super(new InstructionEvaluatorALoad(), EOpcodes.ALOAD);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    localLoad(1, OBJECT_VALUE, "aload 1", Opcodes.ALOAD),
                    localLoad(300, OBJECT_VALUE, "wide aload 300", Opcodes.ALOAD)
            );
        }
    }

    @Nested
    class TestALoadN extends XLoadTestCase<JALParser.JvmInsAloadNContext, InstructionEvaluatorALoadN>
    {
        private static final StackMachine.StackValue OBJECT_VALUE = object(TypeDescriptor.parse("LMyClass;"));

        TestALoadN()
        {
            super(new InstructionEvaluatorALoadN(), EOpcodes.ALOAD_0, EOpcodes.ALOAD_1, EOpcodes.ALOAD_2, EOpcodes.ALOAD_3);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    localLoad(0, OBJECT_VALUE, "aload_0", Opcodes.ALOAD),
                    localLoad(1, OBJECT_VALUE, "aload_1", Opcodes.ALOAD),
                    localLoad(2, OBJECT_VALUE, "aload_2", Opcodes.ALOAD),
                    localLoad(3, OBJECT_VALUE, "aload_3", Opcodes.ALOAD)
            );
        }
    }

    @Nested
    class TestDLoad extends XLoadTestCase<JALParser.JvmInsDloadContext, InstructionEvaluatorDLoad>
    {
        TestDLoad()
        {
            super(new InstructionEvaluatorDLoad(), EOpcodes.DLOAD);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    localLoad(1, doubleValue(), "dload 1", Opcodes.DLOAD),
                    localLoad(300, doubleValue(), "wide dload 300", Opcodes.DLOAD)
            );
        }
    }

    @Nested
    class TestDLoadN extends XLoadTestCase<JALParser.JvmInsDloadNContext, InstructionEvaluatorDLoadN>
    {
        TestDLoadN()
        {
            super(new InstructionEvaluatorDLoadN(), EOpcodes.DLOAD_0, EOpcodes.DLOAD_1, EOpcodes.DLOAD_2, EOpcodes.DLOAD_3);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    localLoad(0, doubleValue(), "dload_0", Opcodes.DLOAD),
                    localLoad(1, doubleValue(), "dload_1", Opcodes.DLOAD),
                    localLoad(2, doubleValue(), "dload_2", Opcodes.DLOAD),
                    localLoad(3, doubleValue(), "dload_3", Opcodes.DLOAD)
            );
        }
    }

    @Nested
    class TestFLoad extends XLoadTestCase<JALParser.JvmInsFloadContext, InstructionEvaluatorFLoad>
    {
        TestFLoad()
        {
            super(new InstructionEvaluatorFLoad(), EOpcodes.FLOAD);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    localLoad(1, floatValue(), "fload 1", Opcodes.FLOAD),
                    localLoad(300, floatValue(), "wide fload 300", Opcodes.FLOAD)
            );
        }
    }

    @Nested
    class TestFLoadN extends XLoadTestCase<JALParser.JvmInsFloadNContext, InstructionEvaluatorFLoadN>
    {
        TestFLoadN()
        {
            super(new InstructionEvaluatorFLoadN(), EOpcodes.FLOAD_0, EOpcodes.FLOAD_1, EOpcodes.FLOAD_2, EOpcodes.FLOAD_3);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    localLoad(0, floatValue(), "fload_0", Opcodes.FLOAD),
                    localLoad(1, floatValue(), "fload_1", Opcodes.FLOAD),
                    localLoad(2, floatValue(), "fload_2", Opcodes.FLOAD),
                    localLoad(3, floatValue(), "fload_3", Opcodes.FLOAD)
            );
        }
    }

    @Nested
    class TestILoad extends XLoadTestCase<JALParser.JvmInsIloadContext, InstructionEvaluatorILoad>
    {
        TestILoad()
        {
            super(new InstructionEvaluatorILoad(), EOpcodes.ILOAD);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    localLoad(1, integerValue(), "iload 1", Opcodes.ILOAD),
                    localLoad(300, integerValue(), "wide iload 300", Opcodes.ILOAD)
            );
        }
    }

    @Nested
    class TestILoadN extends XLoadTestCase<JALParser.JvmInsIloadNContext, InstructionEvaluatorILoadN>
    {
        TestILoadN()
        {
            super(new InstructionEvaluatorILoadN(), EOpcodes.ILOAD_0, EOpcodes.ILOAD_1, EOpcodes.ILOAD_2, EOpcodes.ILOAD_3);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    localLoad(0, integerValue(), "iload_0", Opcodes.ILOAD),
                    localLoad(1, integerValue(), "iload_1", Opcodes.ILOAD),
                    localLoad(2, integerValue(), "iload_2", Opcodes.ILOAD),
                    localLoad(3, integerValue(), "iload_3", Opcodes.ILOAD)
            );
        }
    }

    @Nested
    class TestLLoad extends XLoadTestCase<JALParser.JvmInsLloadContext, InstructionEvaluatorLLoad>
    {
        TestLLoad()
        {
            super(new InstructionEvaluatorLLoad(), EOpcodes.LLOAD);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    localLoad(1, longValue(), "lload 1", Opcodes.LLOAD),
                    localLoad(300, longValue(), "wide lload 300", Opcodes.LLOAD)
            );
        }
    }

    @Nested
    class TestLLoadN extends XLoadTestCase<JALParser.JvmInsLloadNContext, InstructionEvaluatorLLoadN>
    {
        TestLLoadN()
        {
            super(new InstructionEvaluatorLLoadN(), EOpcodes.LLOAD_0, EOpcodes.LLOAD_1, EOpcodes.LLOAD_2, EOpcodes.LLOAD_3);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    localLoad(0, longValue(), "lload_0", Opcodes.LLOAD),
                    localLoad(1, longValue(), "lload_1", Opcodes.LLOAD),
                    localLoad(2, longValue(), "lload_2", Opcodes.LLOAD),
                    localLoad(3, longValue(), "lload_3", Opcodes.LLOAD)
            );
        }
    }
}
