package tokyo.peya.langjal.compiler.instructions;

import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.jupiter.api.Nested;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.ifx.*;
import tokyo.peya.langjal.compiler.instructions.utils.AbstractInstructionTestCase;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.InstructionsHolder;
import tokyo.peya.langjal.compiler.member.LabelsHolder;
import tokyo.peya.langjal.compiler.member.LocalVariablesHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.anyObject;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.integerValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.create;

public class TestJumpInstructions {
    private abstract class LabelJumpTestCase<T extends ParserRuleContext, E extends AbstractInstructionEvaluator<T>>
            extends AbstractInstructionTestCase<T, E> {
        protected LabelJumpTestCase(E evaluator, int... expectedOpcodes) {
            super(evaluator, expectedOpcodes);
        }

        @Override
        protected void assertInstructionEquals(AbstractInsnNode expected, AbstractInsnNode actual) {
            super.assertInstructionEquals(expected, actual);
            assertEquals(JumpInsnNode.class, actual.getClass());
        }
    }

    private abstract class LabelJumpSameTestCase<T extends ParserRuleContext, E extends AbstractInstructionEvaluator<T>>
            extends AbstractInstructionTestCase.Same<T, E> {
        protected LabelJumpSameTestCase(E evaluator, int... expectedOpcodes) {
            super(evaluator, expectedOpcodes);
        }

        @Override
        protected void assertInstructionEquals(AbstractInsnNode expected, AbstractInsnNode actual) {
            super.assertInstructionEquals(expected, actual);
            assertEquals(JumpInsnNode.class, actual.getClass());
        }
    }

    @Nested
    class TestGotoCase extends LabelJumpTestCase<JALParser.JvmInsGotoContext, InstructionEvaluatorGoto> {
        TestGotoCase() {
            super(new InstructionEvaluatorGoto(), EOpcodes.GOTO);
        }

        @Override
        protected void prepareContext(JALParser.JvmInsGotoContext instruction,
                                      org.objectweb.asm.tree.ClassNode ownerClass,
                                      org.objectweb.asm.tree.MethodNode ownerMethod, InstructionsHolder instructions,
                                      LabelsHolder labels, LocalVariablesHolder locals) {
            labels.register(instruction.labelName(), 0);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    of(create().expected(create()), "goto L1", new JumpInsnNode(EOpcodes.GOTO, null)),
                    of(
                            create(integerValue()).expected(create(integerValue())),
                            "goto L1",
                            new JumpInsnNode(EOpcodes.GOTO, null)
                    )
            );
        }
    }

    @Nested
    class TestGotoWCase extends LabelJumpTestCase<JALParser.JvmInsGotoWContext, InstructionEvaluatorGotoW> {
        TestGotoWCase() {
            super(new InstructionEvaluatorGotoW(), EOpcodes.GOTO_W);
        }

        @Override
        protected void prepareContext(JALParser.JvmInsGotoWContext instruction,
                                      org.objectweb.asm.tree.ClassNode ownerClass,
                                      org.objectweb.asm.tree.MethodNode ownerMethod, InstructionsHolder instructions,
                                      LabelsHolder labels, LocalVariablesHolder locals) {
            labels.register(instruction.labelName(), 0);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    of(create().expected(create()), "goto_w L1", new JumpInsnNode(EOpcodes.GOTO_W, null)),
                    of(
                            create(integerValue())
                                    .expected(create(integerValue())),
                            "goto_w L1",
                            new JumpInsnNode(EOpcodes.GOTO_W, null)
                    )
            );
        }
    }

    @Nested
    class TestJsrCase extends LabelJumpSameTestCase<JALParser.JvmInsJsrContext, InstructionEvaluatorJsr> {
        TestJsrCase() {
            super(new InstructionEvaluatorJsr(), EOpcodes.JSR);
        }

        @Override
        protected void prepareContext(JALParser.JvmInsJsrContext instruction,
                                      org.objectweb.asm.tree.ClassNode ownerClass,
                                      org.objectweb.asm.tree.MethodNode ownerMethod, InstructionsHolder instructions,
                                      LabelsHolder labels, LocalVariablesHolder locals) {
            labels.register(instruction.labelName(), 0);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(of(same(), "jsr L1", new JumpInsnNode(EOpcodes.JSR, null)));
        }
    }

    @Nested
    class TestJsrWCase extends LabelJumpSameTestCase<JALParser.JvmInsJsrWContext, InstructionEvaluatorJsrW> {
        TestJsrWCase() {
            super(new InstructionEvaluatorJsrW(), EOpcodes.JSR_W);
        }

        @Override
        protected void prepareContext(JALParser.JvmInsJsrWContext instruction,
                                      org.objectweb.asm.tree.ClassNode ownerClass,
                                      org.objectweb.asm.tree.MethodNode ownerMethod, InstructionsHolder instructions,
                                      LabelsHolder labels, LocalVariablesHolder locals) {
            labels.register(instruction.labelName(), 0);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(of(same(), "jsr_w L1", new JumpInsnNode(EOpcodes.JSR_W, null)));
        }
    }

    @Nested
    class TestRetCase extends AbstractInstructionTestCase<JALParser.JvmInsRetContext, InstructionEvaluatorRet> {
        TestRetCase() {
            super(new InstructionEvaluatorRet(), EOpcodes.RET);
        }

        @Override
        protected void assertInstructionEquals(AbstractInsnNode expected, AbstractInsnNode actual) {
            super.assertInstructionEquals(expected, actual);
            assertEquals(((VarInsnNode) expected).var, ((VarInsnNode) actual).var);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    of(
                            create().set(1, integerValue())
                                    .expected(create().set(1, integerValue())),
                            "ret 1",
                            new VarInsnNode(EOpcodes.RET, 1)
                    ),
                    of(
                            create().set(300, integerValue())
                                    .expected(create().set(300, integerValue())),
                            "wide ret 300",
                            new VarInsnNode(EOpcodes.RET, 300)
                    )
            );
        }
    }

    @Nested
    class TestIfOPCase extends LabelJumpTestCase<JALParser.JvmInsIfOPContext, InstructionEvaluatorIfOP> {
        TestIfOPCase() {
            super(
                    new InstructionEvaluatorIfOP(),
                    EOpcodes.IFEQ,
                    EOpcodes.IFNE,
                    EOpcodes.IFLT,
                    EOpcodes.IFGE,
                    EOpcodes.IFGT,
                    EOpcodes.IFLE
            );
        }

        @Override
        protected void prepareContext(JALParser.JvmInsIfOPContext instruction,
                                      org.objectweb.asm.tree.ClassNode ownerClass,
                                      org.objectweb.asm.tree.MethodNode ownerMethod, InstructionsHolder instructions,
                                      LabelsHolder labels, LocalVariablesHolder locals) {
            labels.register(instruction.labelName(), 0);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    of(create(integerValue()).expected(create()), "ifeq L1", new JumpInsnNode(EOpcodes.IFEQ, null)),
                    of(
                            create(integerValue(), integerValue())
                                    .expected(create(integerValue())), "ifeq L1", new JumpInsnNode(EOpcodes.IFEQ, null)
                    ),
                    of(create(integerValue()).expected(create()), "ifne L1", new JumpInsnNode(EOpcodes.IFNE, null)),
                    of(create(integerValue()).expected(create()), "iflt L1", new JumpInsnNode(EOpcodes.IFLT, null)),
                    of(create(integerValue()).expected(create()), "ifge L1", new JumpInsnNode(EOpcodes.IFGE, null)),
                    of(create(integerValue()).expected(create()), "ifgt L1", new JumpInsnNode(EOpcodes.IFGT, null)),
                    of(create(integerValue()).expected(create()), "ifle L1", new JumpInsnNode(EOpcodes.IFLE, null))
            );
        }
    }

    @Nested
    class TestIfICmpOPCase extends LabelJumpTestCase<JALParser.JvmInsIfIcmpOPContext, InstructionEvaluatorIfICmpOP> {
        TestIfICmpOPCase() {
            super(
                    new InstructionEvaluatorIfICmpOP(),
                    EOpcodes.IF_ICMPEQ,
                    EOpcodes.IF_ICMPNE,
                    EOpcodes.IF_ICMPLT,
                    EOpcodes.IF_ICMPGE,
                    EOpcodes.IF_ICMPGT,
                    EOpcodes.IF_ICMPLE
            );
        }

        @Override
        protected void prepareContext(JALParser.JvmInsIfIcmpOPContext instruction,
                                      org.objectweb.asm.tree.ClassNode ownerClass,
                                      org.objectweb.asm.tree.MethodNode ownerMethod, InstructionsHolder instructions,
                                      LabelsHolder labels, LocalVariablesHolder locals) {
            labels.register(instruction.labelName(), 0);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    of(
                            create(integerValue(), integerValue())
                                    .expected(create()), "if_icmpeq L1", new JumpInsnNode(EOpcodes.IF_ICMPEQ, null)
                    ),
                    of(
                            create(integerValue(), integerValue(), integerValue())
                                    .expected(create(integerValue())),
                            "if_icmpeq L1",
                            new JumpInsnNode(EOpcodes.IF_ICMPEQ, null)
                    ),
                    of(
                            create(integerValue(), integerValue())
                                    .expected(create()), "if_icmpne L1", new JumpInsnNode(EOpcodes.IF_ICMPNE, null)
                    ),
                    of(
                            create(integerValue(), integerValue())
                                    .expected(create()), "if_icmplt L1", new JumpInsnNode(EOpcodes.IF_ICMPLT, null)
                    ),
                    of(
                            create(integerValue(), integerValue())
                                    .expected(create()), "if_icmpge L1", new JumpInsnNode(EOpcodes.IF_ICMPGE, null)
                    ),
                    of(
                            create(integerValue(), integerValue())
                                    .expected(create()), "if_icmpgt L1", new JumpInsnNode(EOpcodes.IF_ICMPGT, null)
                    ),
                    of(
                            create(integerValue(), integerValue())
                                    .expected(create()), "if_icmple L1", new JumpInsnNode(EOpcodes.IF_ICMPLE, null)
                    )
            );
        }
    }

    @Nested
    class TestIfACmpOPCase extends LabelJumpTestCase<JALParser.JvmInsIfAcmpOPContext, InstructionEvaluatorIfACmpOP> {
        TestIfACmpOPCase() {
            super(new InstructionEvaluatorIfACmpOP(), EOpcodes.IF_ACMPEQ, EOpcodes.IF_ACMPNE);
        }

        @Override
        protected void prepareContext(JALParser.JvmInsIfAcmpOPContext instruction,
                                      org.objectweb.asm.tree.ClassNode ownerClass,
                                      org.objectweb.asm.tree.MethodNode ownerMethod, InstructionsHolder instructions,
                                      LabelsHolder labels, LocalVariablesHolder locals) {
            labels.register(instruction.labelName(), 0);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    of(
                            create(anyObject(), anyObject())
                                    .expected(create()), "if_acmpeq L1", new JumpInsnNode(EOpcodes.IF_ACMPEQ, null)
                    ),
                    of(
                            create(integerValue(), anyObject(), anyObject())
                                    .expected(create(integerValue())),
                            "if_acmpeq L1",
                            new JumpInsnNode(EOpcodes.IF_ACMPEQ, null)
                    ),
                    of(
                            create(anyObject(), anyObject())
                                    .expected(create()), "if_acmpne L1", new JumpInsnNode(EOpcodes.IF_ACMPNE, null)
                    )
            );
        }
    }

    @Nested
    class TestIfNullCase extends LabelJumpTestCase<JALParser.JvmInsIfNullContext, InstructionEvaluatorIfNull> {
        TestIfNullCase() {
            super(new InstructionEvaluatorIfNull(), EOpcodes.IFNULL);
        }

        @Override
        protected void prepareContext(JALParser.JvmInsIfNullContext instruction,
                                      org.objectweb.asm.tree.ClassNode ownerClass,
                                      org.objectweb.asm.tree.MethodNode ownerMethod, InstructionsHolder instructions,
                                      LabelsHolder labels, LocalVariablesHolder locals) {
            labels.register(instruction.labelName(), 0);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    of(create(anyObject()).expected(create()), "ifnull L1", new JumpInsnNode(EOpcodes.IFNULL, null)),
                    of(
                            create(integerValue(), anyObject())
                                    .expected(create(integerValue())),
                            "ifnull L1",
                            new JumpInsnNode(EOpcodes.IFNULL, null)
                    )
            );
        }
    }

    @Nested
    class TestIfNonNullCase extends LabelJumpTestCase<JALParser.JvmInsIfNonnullContext, InstructionEvaluatorIfNonNull> {
        TestIfNonNullCase() {
            super(new InstructionEvaluatorIfNonNull(), EOpcodes.IFNONNULL);
        }

        @Override
        protected void prepareContext(JALParser.JvmInsIfNonnullContext instruction,
                                      org.objectweb.asm.tree.ClassNode ownerClass,
                                      org.objectweb.asm.tree.MethodNode ownerMethod, InstructionsHolder instructions,
                                      LabelsHolder labels, LocalVariablesHolder locals) {
            labels.register(instruction.labelName(), 0);
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes() {
            return set(
                    of(
                            create(anyObject()).expected(create()),
                            "ifnonnull L1",
                            new JumpInsnNode(EOpcodes.IFNONNULL, null)
                    ),
                    of(
                            create(integerValue(), anyObject())
                                    .expected(create(integerValue())),
                            "ifnonnull L1",
                            new JumpInsnNode(EOpcodes.IFNONNULL, null)
                    )
            );
        }
    }
}
