package tokyo.peya.langjal.compiler.instructions;

import org.junit.jupiter.api.Nested;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.utils.AbstractInstructionTestCase;
import tokyo.peya.langjal.compiler.member.InstructionsHolder;
import tokyo.peya.langjal.compiler.member.LabelsHolder;
import tokyo.peya.langjal.compiler.member.LocalVariablesHolder;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.integerValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.create;

public class TestSwitchInstructions
{
    @Nested
    static class TestLookupSwitchCase extends AbstractInstructionTestCase<JALParser.JvmInsLookupswitchContext, InstructionEvaluatorLookupSwitch>
    {
        TestLookupSwitchCase()
        {
            super(new InstructionEvaluatorLookupSwitch(), EOpcodes.LOOKUPSWITCH);
        }

        @Override
        protected void prepareContext(JALParser.JvmInsLookupswitchContext instruction, org.objectweb.asm.tree.ClassNode ownerClass, org.objectweb.asm.tree.MethodNode ownerMethod, InstructionsHolder instructions, LabelsHolder labels, LocalVariablesHolder locals)
        {
            for (JALParser.JvmInsArgLookupSwitchCaseContext kase : instruction.jvmInsArgLookupSwitch().jvmInsArgLookupSwitchCaseList().jvmInsArgLookupSwitchCase())
                labels.register(kase.labelName(), 0);
        }

        @Override
        protected void assertInstructionEquals(AbstractInsnNode expected, AbstractInsnNode actual)
        {
            super.assertInstructionEquals(expected, actual);
            LookupSwitchInsnNode expectedInsn = (LookupSwitchInsnNode) expected;
            LookupSwitchInsnNode actualInsn = (LookupSwitchInsnNode) actual;
            assertEquals(expectedInsn.keys, actualInsn.keys);
            assertEquals(expectedInsn.labels.size(), actualInsn.labels.size());
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    of(create(integerValue()).expected(create()),
                            "lookupswitch { 1:L1, 5:L2, default:L3 }",
                            new LookupSwitchInsnNode(null, new int[]{1, 5}, new org.objectweb.asm.tree.LabelNode[]{null, null}))
            );
        }
    }

    @Nested
    static class TestTableSwitchCase extends AbstractInstructionTestCase<JALParser.JvmInsTableswitchContext, InstructionEvaluatorTableSwitch>
    {
        TestTableSwitchCase()
        {
            super(new InstructionEvaluatorTableSwitch(), EOpcodes.TABLESWITCH);
        }

        @Override
        protected void prepareContext(JALParser.JvmInsTableswitchContext instruction, org.objectweb.asm.tree.ClassNode ownerClass, org.objectweb.asm.tree.MethodNode ownerMethod, InstructionsHolder instructions, LabelsHolder labels, LocalVariablesHolder locals)
        {
            for (JALParser.LabelNameContext labelName : instruction.jvmInsArgTableSwitch().jvmInsArgTableSwitchCaseList().labelName())
                labels.register(labelName, 0);
            labels.register(instruction.jvmInsArgTableSwitch().labelName(), 0);
        }

        @Override
        protected void assertInstructionEquals(AbstractInsnNode expected, AbstractInsnNode actual)
        {
            super.assertInstructionEquals(expected, actual);
            TableSwitchInsnNode expectedInsn = (TableSwitchInsnNode) expected;
            TableSwitchInsnNode actualInsn = (TableSwitchInsnNode) actual;
            assertEquals(expectedInsn.min, actualInsn.min);
            assertEquals(expectedInsn.max, actualInsn.max);
            assertEquals(expectedInsn.labels.size(), actualInsn.labels.size());
        }

        @Override
        public InstructionCase[] getValidInstructionSyntaxes()
        {
            return set(
                    of(create(integerValue()).expected(create()),
                            "tableswitch 3 {L1, L2, L3} default L4",
                            new TableSwitchInsnNode(3, 5, null, new org.objectweb.asm.tree.LabelNode[]{null, null, null}))
            );
        }
    }
}
