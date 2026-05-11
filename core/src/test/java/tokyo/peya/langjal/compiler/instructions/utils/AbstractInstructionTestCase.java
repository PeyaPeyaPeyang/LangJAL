package tokyo.peya.langjal.compiler.instructions.utils;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.compiler.JALLexer;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.InstructionsHolder;
import tokyo.peya.langjal.compiler.member.LabelsHolder;
import tokyo.peya.langjal.compiler.member.LocalVariablesHolder;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractInstructionTestCase<P extends ParserRuleContext, T extends AbstractInstructionEvaluator<P>>
{
    private static final TestFileEvaluatingReporter REPORTER;

    static {
        REPORTER = new TestFileEvaluatingReporter();
    }

    private final int[] expectedOpCodes;
    private final T evaluator;

    public AbstractInstructionTestCase(T evaluator, int... expectedOpCodes)
    {
        this.evaluator = evaluator;
        this.expectedOpCodes = expectedOpCodes;
    }

    @Test
    public void checkEvaluatorAcceptsExpectedOpCodes()
    {
        int[] actualOpCodes = this.evaluator.getEvaluatableOpcodes();
        for (int expectedOpCode : this.expectedOpCodes)
        {
            boolean found = false;
            for (int actualOpCode : actualOpCodes)
            {
                if (actualOpCode == expectedOpCode)
                {
                    found = true;
                    break;
                }
            }
            assertTrue(found, String.format("Expected opcode %d not found in evaluator's accepted opcodes", expectedOpCode));
        }

        Assertions.assertEquals(this.expectedOpCodes.length, actualOpCodes.length, "Evaluator accepts unexpected number of opcodes");
    }

    public abstract InstructionCase[] getValidInstructionSyntaxes();

    @ParameterizedTest
    @MethodSource("getValidInstructionSyntaxes")
    public void testParseValidInstructions(InstructionCase instruction)
    {
        P insn = tryParseInstruction(instruction.syntax);
        ClassNode ownerClass = this.createDummyClass();
        MethodNode ownerMethod = this.createDummyMethod();
        ownerClass.methods.add(ownerMethod);
        LabelsHolder labels = this.createLabelsHolder(insn);
        InstructionsHolder instructions = new InstructionsHolder(ownerClass, ownerMethod, labels);

        LocalVariablesHolder locals = new LocalVariablesHolder(REPORTER, labels);
        instruction.situation.applyTo(locals);
        this.prepareContext(insn, ownerClass, ownerMethod, instructions, labels, locals);
        EvaluatedInstruction compiled = compile(ownerClass, ownerMethod, insn, instructions, labels, locals);

        AbstractInsnNode insnNode = compiled.insn();
        AbstractInsnNode expectedNode = instruction.expectedInstruction;
        if (expectedNode != null)
            this.assertInstructionEquals(expectedNode, insnNode);
    }

    @NotNull
    private P tryParseInstruction(String syntax)
    {
        try
        {
            P parsed = parseInstruction(syntax, this.evaluator::map);
            if (parsed == null)
            {
                fail(String.format("Valid instruction '%s' could not be parsed: map returned null", syntax));
            }

            return parsed;
        }
        catch (Exception e)
        {
            fail(String.format("Valid instruction '%s' threw an exception: %s", syntax, e.getMessage()));
            return null;  // Unreachable, but required for compilation.
        }
    }

    protected void assertInstructionEquals(AbstractInsnNode expected, AbstractInsnNode actual)
    {
        assertEquals(expected.getOpcode(), actual.getOpcode(), "Opcodes do not match");
        assertEquals(expected.getClass(), actual.getClass(), "Instruction node types do not match");
    }

    protected EvaluatedInstruction compile(ClassNode ownerClass,
                                           MethodNode ownerMethod,
                                           P instruction,
                                           InstructionsHolder instructions,
                                           LabelsHolder labels,
                                           LocalVariablesHolder locals)
    {
        return this.evaluator.evaluate(
                REPORTER,
                ownerClass,
                ownerMethod,
                instructions,
                labels,
                locals,
                    instruction
        );
    }

    @ParameterizedTest(allowZeroInvocations = true)
    @MethodSource("getValidInstructionSyntaxes")
    public void testFrameDifferenceEvaluations(InstructionCase kase)
    {
        if (this instanceof AbstractInstructionTestCase.Same<P,T>) {
            return;  // Same タイプのテストケースは，フレームの操作が無いため。
        }

        P parsed = tryParseInstruction(kase.syntax);

        ClassNode ownerClass = this.createDummyClass();
        MethodNode ownerMethod = this.createDummyMethod();
        ownerClass.methods.add(ownerMethod);
        LabelsHolder labels = this.createLabelsHolder(parsed);
        InstructionsHolder instructions = new InstructionsHolder(ownerClass, ownerMethod, labels);

        LocalVariablesHolder locals = new LocalVariablesHolder(REPORTER, labels);
        kase.situation.applyTo(locals);
        this.prepareContext(parsed, ownerClass, ownerMethod, instructions, labels, locals);
        EvaluatedInstruction compiled = this.compile(ownerClass, ownerMethod, parsed, instructions, labels, locals);

        InstructionInfo info = new InstructionInfo(
                0,
                compiled.insn(),
                ownerClass,
                ownerMethod,
                this.evaluator,
                null,
                compiled.getInstructionSize(),
                0
        );

        FrameDifferenceInfo difference = this.evaluator.getFrameDifferenceInfo(info);
        kase.situation.emulate(difference);
    }

    protected static <T extends ParserRuleContext> T parseInstruction(String instruction, Function<? super JALParser.InstructionContext, T> mapper)
    {
        return parseSource(instruction, p -> mapper.apply(p.instruction()));
    }

    protected static <T extends ParserRuleContext> T parseSource(String source, Function<? super JALParser, T> mapper)
    {
        JALLexer lexer = new JALLexer(CharStreams.fromString(source));
        TokenStream tokens = new CommonTokenStream(lexer);
        JALParser parser = new JALParser(tokens);
        return mapper.apply(parser);
    }


    public abstract static class Same<P1 extends ParserRuleContext, T1 extends AbstractInstructionEvaluator<P1>>
            extends AbstractInstructionTestCase<P1, T1>
    {
        public Same(T1 evaluator, int... expectedOpCodes)
        {
            super(evaluator, expectedOpCodes);
        }

        protected static StackMachine same() {
            return StackMachine.create().expected(StackMachine.create());
        }
    }

    protected static InstructionCase of(StackMachine situation, String syntax, AbstractInsnNode instruction)
    {
        return new InstructionCase(situation, syntax, instruction);
    }

    protected static InstructionCase of(StackMachine situation, String syntax, int instruction)
    {
        return new InstructionCase(situation, syntax, new InsnNode(instruction));
    }

    public static InstructionCase[] set(InstructionCase... cases)
    {
        return cases;
    }

    protected ClassNode createDummyClass()
    {
        ClassNode ownerClass = new ClassNode();
        ownerClass.visit(
                EOpcodes.V1_8,
                EOpcodes.ACC_PUBLIC,
                "TestDummyClass",
                null,
                "java/lang/Object",
                null
        );
        return ownerClass;
    }

    protected MethodNode createDummyMethod()
    {
        return new MethodNode(
                EOpcodes.ACC_PUBLIC,
                "testDummyMethod",
                "()V",
                null,
                null
        );
    }

    protected LabelsHolder createLabelsHolder(P instruction)
    {
        return new LabelsHolder();
    }

    protected void prepareContext(P instruction,
                                  ClassNode ownerClass,
                                  MethodNode ownerMethod,
                                  InstructionsHolder instructions,
                                  LabelsHolder labels,
                                  LocalVariablesHolder locals)
    {
    }

    public record InstructionCase(
            StackMachine situation,
            String syntax,
            AbstractInsnNode expectedInstruction
    ) {

    }
}
