package tokyo.peya.langjal.compiler.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import tokyo.peya.langjal.compiler.CompileSettings;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("InstructionsHolder")
class InstructionsHolderTest
{
    @Test
    void startsEmptyAndReturnsNullForOutOfRangeInstruction()
    {
        InstructionsHolder holder = newHolder(new MethodNode(), new LabelsHolder());

        assertTrue(holder.isEmpty());
        assertEquals(0, holder.getSize());
        assertEquals(0, holder.getBytecodeOffset());
        assertNull(holder.getInstruction(-1));
        assertNull(holder.getInstruction(0));
        assertThrows(IllegalStateException.class, holder::getLastInstruction);
    }

    @Test
    void addReturnAddsReturnInstructionAndAdvancesOffset()
    {
        LabelsHolder labels = new LabelsHolder();
        InstructionsHolder holder = newHolder(new MethodNode(), labels);

        InstructionInfo instruction = holder.addReturn();

        assertFalse(holder.isEmpty());
        assertEquals(EOpcodes.RETURN, instruction.opcode());
        assertEquals(0, instruction.bytecodeOffset());
        assertSame(labels.getCurrentLabel(), instruction.assignedLabel());
        assertEquals(instruction.instructionSize(), holder.getBytecodeOffset());
        assertSame(instruction, holder.getLastInstruction());
    }

    @Test
    void addInstructionUsesEvaluatedInstructionSizeAndSourceLine()
    {
        InstructionsHolder holder = newHolder(new MethodNode(), new LabelsHolder());
        LabelInfo label = label("L0", 0);
        EvaluatedInstruction evaluated = EvaluatedInstruction.of(evaluator(EOpcodes.NOP), new InsnNode(EOpcodes.NOP), 7);

        InstructionInfo instruction = holder.addInstruction(evaluated, label, 42);

        assertEquals(0, instruction.bytecodeOffset());
        assertSame(label, instruction.assignedLabel());
        assertEquals(7, instruction.instructionSize());
        assertEquals(42, instruction.sourceLine());
        assertEquals(7, holder.getBytecodeOffset());
    }

    @ParameterizedTest
    @MethodSource("variableSizedInstructions")
    void importInstructionCalculatesVariableInstructionSizes(AbstractInsnNode instructionNode, int expectedSize)
    {
        InstructionsHolder holder = newHolder(new MethodNode(), new LabelsHolder());

        InstructionInfo instruction = holder.importInstruction(instructionNode, null, 1);

        assertEquals(expectedSize, instruction.instructionSize());
        assertEquals(expectedSize, holder.getBytecodeOffset());
    }

    @Test
    void importInstructionRejectsNodesWithoutOpcode()
    {
        InstructionsHolder holder = newHolder(new MethodNode(), new LabelsHolder());

        assertThrows(IllegalArgumentException.class, () -> holder.importInstruction(new LabelNode(new Label()), null, 1));
    }

    @Test
    void finaliseInstructionsAddsLabelsLineNumbersAndInstructions()
    {
        MethodNode method = new MethodNode();
        InstructionsHolder holder = newHolder(method, new LabelsHolder());
        LabelInfo label = label("L0", 0);
        holder.addInstruction(EvaluatedInstruction.of(evaluator(EOpcodes.NOP), new InsnNode(EOpcodes.NOP)), label, 123);

        holder.finaliseInstructions(CompileSettings.INCLUDE_LINE_NUMBER_TABLE);

        assertSame(label.node(), method.instructions.get(0));
        assertInstanceOf(LineNumberNode.class, method.instructions.get(1));
        assertEquals(EOpcodes.NOP, method.instructions.get(2).getOpcode());
    }

    @Test
    void getInstructionsReturnsContiguousInstructionsUntilNextLabel()
    {
        InstructionsHolder holder = newHolder(new MethodNode(), new LabelsHolder());
        LabelInfo firstLabel = label("FIRST", 0);
        LabelInfo secondLabel = label("SECOND", 2);
        InstructionInfo first = holder.addInstruction(nop(), firstLabel, 1);
        InstructionInfo second = holder.addInstruction(nop(), null, 2);
        holder.addInstruction(nop(), secondLabel, 3);

        List<InstructionInfo> instructions = holder.getInstructions(firstLabel);

        assertEquals(List.of(first, second), instructions);
        assertThrows(UnsupportedOperationException.class, () -> instructions.add(first));
        assertThrows(IndexOutOfBoundsException.class, () -> holder.getInstructions(label("BAD", 3)));
    }

    private static InstructionsHolder newHolder(MethodNode method, LabelsHolder labels)
    {
        return new InstructionsHolder(new ClassNode(), method, labels);
    }

    private static EvaluatedInstruction nop()
    {
        return EvaluatedInstruction.of(evaluator(EOpcodes.NOP), new InsnNode(EOpcodes.NOP));
    }

    private static AbstractInstructionEvaluator<?> evaluator(int opcode)
    {
        return JALInstructionEvaluator.getEvaluatorByOpcode(opcode);
    }

    private static LabelInfo label(String name, int instructionIndex)
    {
        return new LabelInfo(name, new Label(), instructionIndex);
    }

    private static Stream<Arguments> variableSizedInstructions()
    {
        return Stream.of(
                Arguments.of(new VarInsnNode(EOpcodes.ILOAD, 254), 2),
                Arguments.of(new VarInsnNode(EOpcodes.ILOAD, 255), 4),
                Arguments.of(new IincInsnNode(255, Byte.MAX_VALUE), 3),
                Arguments.of(new IincInsnNode(256, Byte.MAX_VALUE + 1), 6)
        );
    }
}
