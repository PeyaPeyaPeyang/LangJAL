package tokyo.peya.langjal.analyser;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.analyser.stack.PrimitiveElement;
import tokyo.peya.langjal.analyser.stack.StackElement;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.instructions.InstructionEvaluatorNop;
import tokyo.peya.langjal.compiler.instructions.utils.TestCompileReporter;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.JALInstructionEvaluator;
import tokyo.peya.langjal.compiler.member.LabelInfo;
import tokyo.peya.langjal.compiler.member.LabelsHolder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InstructionSetAnalyserTest {
    private static InstructionInfo nop(LabelInfo label) {
        return insn(label, EOpcodes.NOP);
    }

    private static InstructionInfo insn(LabelInfo label, int opcode) {
        ClassNode clazz = new ClassNode();
        MethodNode method = new MethodNode();
        AbstractInstructionEvaluator<?> evaluator = opcode == EOpcodes.NOP
                ? new InstructionEvaluatorNop()
                : JALInstructionEvaluator.getEvaluatorByOpcode(opcode);
        return new InstructionInfo(
                0,
                new InsnNode(opcode),
                clazz,
                method,
                evaluator,
                label,
                EOpcodes.getOpcodeSize(opcode),
                -1
        );
    }

    private static InstructionInfo jump(LabelInfo label, int opcode, LabelNode target) {
        ClassNode clazz = new ClassNode();
        MethodNode method = new MethodNode();
        AbstractInstructionEvaluator<?> evaluator = JALInstructionEvaluator.getEvaluatorByOpcode(opcode);
        return new InstructionInfo(
                0,
                new JumpInsnNode(opcode, target),
                clazz,
                method,
                evaluator,
                label,
                EOpcodes.getOpcodeSize(opcode),
                -1
        );
    }

    private static FramePropagation propagation(LabelInfo label, StackElement... stack) {
        return new FramePropagation(
                label,
                new AnalysedInstruction[0],
                label,
                stack,
                new tokyo.peya.langjal.analyser.stack.LocalStackElement[0],
                stack.length,
                0
        );
    }

    private static InstructionSetAnalyser newAnalyser(LabelsHolder labels,
                                                      LabelInfo label,
                                                      List<InstructionInfo> instructions) {
        return new InstructionSetAnalyser(
                new FileEvaluatingReporter(new TestCompileReporter(), null),
                labels,
                label,
                instructions,
                Map.of()
        );
    }

    @Test
    void nopPreservesEmptyStackAndLocals() {
        LabelInfo label = new LabelInfo("L0", new Label(), 0);
        InstructionSetAnalyser analyser = newAnalyser(new LabelsHolder(), label, List.of(nop(label)));

        InstructionSetAnalysisResult result = analyser.analyse(propagation(label));

        assertEquals(1, result.analyzedInstructions().length);
        assertEquals(0, result.stack().length);
        assertEquals(0, result.locals().length);
        assertEquals(0, result.maxStackSize());
        assertEquals(0, result.maxLocalSize());
    }

    @Test
    void iconstPushesIntegerAndUpdatesMaxStack() {
        LabelInfo label = new LabelInfo("L0", new Label(), 0);
        InstructionSetAnalyser analyser = newAnalyser(new LabelsHolder(), label, List.of(insn(label, EOpcodes.ICONST_1)));

        InstructionSetAnalysisResult result = analyser.analyse(propagation(label));

        assertEquals(1, result.stack().length);
        assertEquals(StackElementType.INTEGER, result.stack()[0].type());
        assertEquals(1, result.maxStackSize());
    }

    @Test
    void ireturnConsumesIntegerAndStopsFallthrough() {
        LabelInfo label = new LabelInfo("L0", new Label(), 0);
        InstructionSetAnalyser analyser = newAnalyser(new LabelsHolder(), label, List.of(insn(label, EOpcodes.IRETURN)));

        InstructionSetAnalysisResult result = analyser.analyse(propagation(
                label,
                new PrimitiveElement(nop(label), StackElementType.INTEGER)
        ));

        assertEquals(0, result.stack().length);
        assertEquals(0, result.framePropagations().length);
    }

    @Test
    void repeatedAnalysisDoesNotKeepPreviousAnalysedInstructions() {
        LabelInfo label = new LabelInfo("L0", new Label(), 0);
        InstructionSetAnalyser analyser = newAnalyser(new LabelsHolder(), label, List.of(nop(label)));
        FramePropagation propagation = propagation(label);

        assertEquals(1, analyser.analyse(propagation).analyzedInstructions().length);
        assertEquals(1, analyser.analyse(propagation).analyzedInstructions().length);
    }

    @Test
    void conditionalJumpCreatesTargetAndFallthroughPropagations() {
        LabelsHolder labels = new LabelsHolder();
        LabelInfo start = labels.importASMLabel(new LabelNode(new Label()), 0);
        LabelInfo fallthrough = labels.importASMLabel(new LabelNode(new Label()), 1);
        LabelInfo branch = labels.importASMLabel(new LabelNode(new Label()), 2);
        InstructionSetAnalyser analyser = newAnalyser(
                labels,
                start,
                List.of(jump(start, EOpcodes.IFEQ, branch.node()))
        );

        InstructionSetAnalysisResult result = analyser.analyse(propagation(
                start,
                new PrimitiveElement(nop(start), StackElementType.INTEGER)
        ));

        assertEquals(2, result.framePropagations().length);
        assertTrue(List.of(result.framePropagations()).stream().anyMatch(p -> p.receiver().equals(branch)));
        assertTrue(List.of(result.framePropagations()).stream().anyMatch(p -> p.receiver().equals(fallthrough)));
    }

    @Test
    void gotoCreatesTargetPropagationWithoutFallthrough() {
        LabelsHolder labels = new LabelsHolder();
        LabelInfo start = labels.importASMLabel(new LabelNode(new Label()), 0);
        LabelInfo fallthrough = labels.importASMLabel(new LabelNode(new Label()), 1);
        LabelInfo branch = labels.importASMLabel(new LabelNode(new Label()), 2);
        InstructionSetAnalyser analyser = newAnalyser(
                labels,
                start,
                List.of(jump(start, EOpcodes.GOTO, branch.node()))
        );

        InstructionSetAnalysisResult result = analyser.analyse(propagation(start));

        assertEquals(1, result.framePropagations().length);
        assertEquals(branch, result.framePropagations()[0].receiver());
        assertTrue(List.of(result.framePropagations()).stream().noneMatch(p -> p.receiver().equals(fallthrough)));
    }
}
