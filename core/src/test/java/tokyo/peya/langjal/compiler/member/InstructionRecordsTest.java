package tokyo.peya.langjal.compiler.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("member records")
class InstructionRecordsTest
{
    @Test
    void labelInfoCreatesNodeForSameAsmLabelAndReadableString()
    {
        Label label = new Label();
        LabelInfo info = new LabelInfo("ENTRY", label, 3);

        assertSame(label, info.node().getLabel());
        assertSame(info.node(), label.info);
        assertEquals("ENTRY (idx: 3)", info.toString());
    }

    @Test
    void evaluatedInstructionUsesOpcodeSizeUnlessCustomSizeIsProvided()
    {
        AbstractInstructionEvaluator<?> evaluator = evaluator(EOpcodes.NOP);

        assertEquals(EOpcodes.getOpcodeSize(EOpcodes.NOP),
                     EvaluatedInstruction.of(evaluator, new InsnNode(EOpcodes.NOP)).getInstructionSize());
        assertEquals(9, EvaluatedInstruction.of(evaluator, new InsnNode(EOpcodes.NOP), 9).getInstructionSize());
    }

    @ParameterizedTest
    @ValueSource(ints = {
            EOpcodes.TABLESWITCH,
            EOpcodes.LOOKUPSWITCH,
            EOpcodes.WIDE,
            EOpcodes.INVOKEDYNAMIC
    })
    void evaluatedInstructionRequiresCustomSizeForVariableLengthOpcodes(int opcode)
    {
        AbstractInstructionEvaluator<?> evaluator = evaluator(EOpcodes.NOP);

        assertThrows(IllegalArgumentException.class,
                     () -> EvaluatedInstruction.of(evaluator, new InsnNode(opcode), 0));
    }

    @Test
    void instructionInfoExposesOpcodeStringAndEqualityIgnoresOwnerAndSourceLine()
    {
        AbstractInstructionEvaluator<?> evaluator = evaluator(EOpcodes.NOP);
        LabelInfo label = new LabelInfo("L0", new Label(), 0);
        InsnNode insn = new InsnNode(EOpcodes.NOP);
        InstructionInfo first = new InstructionInfo(0, insn, new ClassNode(), new MethodNode(), evaluator, label, 1, 10);
        InstructionInfo sameInstruction = new InstructionInfo(0, insn, new ClassNode(), new MethodNode(), evaluator, label, 1, 99);
        InstructionInfo differentOffset = new InstructionInfo(1, insn, new ClassNode(), new MethodNode(), evaluator, label, 1, 10);

        assertEquals(EOpcodes.NOP, first.opcode());
        assertEquals("nop at 0 with label L0 (idx: 0)", first.toString());
        assertEquals(first, sameInstruction);
        assertNotEquals(first, differentOffset);
    }

    private static AbstractInstructionEvaluator<?> evaluator(int opcode)
    {
        return JALInstructionEvaluator.getEvaluatorByOpcode(opcode);
    }
}
