package tokyo.peya.langjal.compiler.instructions;

import org.junit.jupiter.api.Nested;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.utils.AbstractInstructionTestCase;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;

import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.anyObject;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.integerValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.object;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.create;

public abstract class TestSimpleInstructions
{
    @Nested
    static class TestBiPush extends AbstractInstructionTestCase<JALParser.JvmInsBipushContext, InstructionEvaluatorBiPush>
    {
        TestBiPush()
        {
            super(new InstructionEvaluatorBiPush(), EOpcodes.BIPUSH);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    of(create().expected(create(integerValue())), "bipush 127", new org.objectweb.asm.tree.IntInsnNode(Opcodes.BIPUSH, 127)),
                    of(create().expected(create(integerValue())), "bipush -128", new org.objectweb.asm.tree.IntInsnNode(Opcodes.BIPUSH, -128))
            );
        }
    }

    @Nested
    static class TestSiPush extends AbstractInstructionTestCase<JALParser.JvmInsSipushContext, InstructionEvaluatorSiPush>
    {
        TestSiPush()
        {
            super(new InstructionEvaluatorSiPush(), EOpcodes.SIPUSH);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    of(create().expected(create(integerValue())), "sipush 32767", new org.objectweb.asm.tree.IntInsnNode(Opcodes.SIPUSH, 32767)),
                    of(create().expected(create(integerValue())), "sipush -32768", new org.objectweb.asm.tree.IntInsnNode(Opcodes.SIPUSH, -32768))
            );
        }
    }

    @Nested
    static class TestPop extends AbstractInstructionTestCase<JALParser.JvmInsPopContext, InstructionEvaluatorPop>
    {
        TestPop()
        {
            super(new InstructionEvaluatorPop(), EOpcodes.POP);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    of(create(integerValue()).expected(create()), "pop", Opcodes.POP),
                    of(create(anyObject()).expected(create()), "pop", Opcodes.POP)
            );
        }
    }

    @Nested
    static class TestPop2 extends AbstractInstructionTestCase<JALParser.JvmInsPop2Context, InstructionEvaluatorPop2>
    {
        TestPop2()
        {
            super(new InstructionEvaluatorPop2(), EOpcodes.POP2);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    of(create(integerValue(), integerValue()).expected(create()), "pop2", Opcodes.POP2),
                    of(create(anyObject(), integerValue()).expected(create()), "pop2", Opcodes.POP2)
            );
        }
    }

    @Nested
    static class TestReturn extends AbstractInstructionTestCase.Same<JALParser.JvmInsReturnContext, InstructionEvaluatorReturn>
    {
        TestReturn()
        {
            super(new InstructionEvaluatorReturn(), EOpcodes.RETURN);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(of(same(), "return", Opcodes.RETURN));
        }
    }

    @Nested
    static class TestArrayLength extends AbstractInstructionTestCase<JALParser.JvmInsArraylengthContext, InstructionEvaluatorArrayLength>
    {
        TestArrayLength()
        {
            super(new InstructionEvaluatorArrayLength(), EOpcodes.ARRAYLENGTH);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    of(create(object(TypeDescriptor.parse("[I"))).expected(create(integerValue())), "arraylength", Opcodes.ARRAYLENGTH),
                    of(create(object(TypeDescriptor.parse("[Ljava/lang/String;"))).expected(create(integerValue())), "arraylength", Opcodes.ARRAYLENGTH)
            );
        }
    }

    @Nested
    static class TestAThrow extends AbstractInstructionTestCase<JALParser.JvmInsAthrowContext, InstructionEvaluatorAThrow>
    {
        TestAThrow()
        {
            super(new InstructionEvaluatorAThrow(), EOpcodes.ATHROW);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    of(create(object(TypeDescriptor.className("java/lang/Throwable"))).expected(create()), "athrow", Opcodes.ATHROW)
            );
        }
    }

    @Nested
    static class TestMonitorEnter extends AbstractInstructionTestCase<JALParser.JvmInsMonitorenterContext, InstructionEvaluatorMonitorEnter>
    {
        TestMonitorEnter()
        {
            super(new InstructionEvaluatorMonitorEnter(), EOpcodes.MONITORENTER);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(of(create(anyObject()).expected(create()), "monitorenter", Opcodes.MONITORENTER));
        }
    }

    @Nested
    static class TestMonitorExit extends AbstractInstructionTestCase<JALParser.JvmInsMonitorexitContext, InstructionEvaluatorMonitorExit>
    {
        TestMonitorExit()
        {
            super(new InstructionEvaluatorMonitorExit(), EOpcodes.MONITOREXIT);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(of(create(anyObject()).expected(create()), "monitorexit", Opcodes.MONITOREXIT));
        }
    }

    @Nested
    static class TestNop extends AbstractInstructionTestCase.Same<JALParser.JvmInsNopContext, InstructionEvaluatorNop>
    {
        TestNop()
        {
            super(new InstructionEvaluatorNop(), EOpcodes.NOP);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(of(same(), "nop", Opcodes.NOP));
        }
    }

    @Nested
    static class TestNew extends AbstractInstructionTestCase<JALParser.JvmInsNewContext, InstructionEvaluatorNew>
    {
        TestNew()
        {
            super(new InstructionEvaluatorNew(), EOpcodes.NEW);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    of(create().expected(create(object(TypeDescriptor.className("java/lang/String")))), "new java/lang/String", new org.objectweb.asm.tree.TypeInsnNode(Opcodes.NEW, "java/lang/String"))
            );
        }
    }

    @Nested
    static class TestANewArray extends AbstractInstructionTestCase<JALParser.JvmInsAnewArrayContext, InstructionEvaluatorANewArray>
    {
        TestANewArray()
        {
            super(new InstructionEvaluatorANewArray(), EOpcodes.ANEWARRAY);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    of(create(integerValue()).expected(create(object(TypeDescriptor.className("java/lang/String")))), "anewarray Ljava/lang/String;", new org.objectweb.asm.tree.TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/String"))
            );
        }
    }

    @Nested
    static class TestNewArray extends AbstractInstructionTestCase<JALParser.JvmInsNewarrayContext, InstructionEvaluatorNewArray>
    {
        TestNewArray()
        {
            super(new InstructionEvaluatorNewArray(), EOpcodes.NEWARRAY);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    of(create(integerValue()).expected(create(object(TypeDescriptor.parse("[I")))), "newarray I", new org.objectweb.asm.tree.IntInsnNode(Opcodes.NEWARRAY, Opcodes.T_INT))
            );
        }
    }

    @Nested
    static class TestMultiANewArray extends AbstractInstructionTestCase<JALParser.JvmInsMultianewarrayContext, InstructionEvaluatorMultiANewArray>
    {
        TestMultiANewArray()
        {
            super(new InstructionEvaluatorMultiANewArray(), EOpcodes.MULTIANEWARRAY);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    of(create(integerValue(), integerValue()).expected(create(object(TypeDescriptor.parse("[[Ljava/lang/String;")))),
                            "multianewarray [[Ljava/lang/String; 2",
                            new MultiANewArrayInsnNode("[[Ljava/lang/String;", 2))
            );
        }
    }

    @Nested
    static class TestCheckCast extends AbstractInstructionTestCase<JALParser.JvmInsCheckcastContext, InstructionEvaluatorCheckCast>
    {
        TestCheckCast()
        {
            super(new InstructionEvaluatorCheckCast(), EOpcodes.CHECKCAST);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    of(create(anyObject()).expected(create(object(TypeDescriptor.className("java/lang/String")))), "checkcast Ljava/lang/String;", new org.objectweb.asm.tree.TypeInsnNode(Opcodes.CHECKCAST, "java/lang/String"))
            );
        }
    }

    @Nested
    static class TestInstanceOf extends AbstractInstructionTestCase<JALParser.JvmInsInstanceofContext, InstructionEvaluatorInstanceOf>
    {
        TestInstanceOf()
        {
            super(new InstructionEvaluatorInstanceOf(), EOpcodes.INSTANCEOF);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    of(create(anyObject()).expected(create(integerValue())), "instanceof Ljava/lang/String;", new org.objectweb.asm.tree.TypeInsnNode(Opcodes.INSTANCEOF, "java/lang/String"))
            );
        }
    }
}
