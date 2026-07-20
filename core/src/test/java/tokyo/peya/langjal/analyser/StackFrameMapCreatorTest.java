package tokyo.peya.langjal.analyser;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.analyser.stack.LocalStackElement;
import tokyo.peya.langjal.analyser.stack.ObjectElement;
import tokyo.peya.langjal.analyser.stack.PrimitiveElement;
import tokyo.peya.langjal.analyser.stack.StackElement;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.analyser.stack.TopElement;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.instructions.InstructionEvaluatorNop;
import tokyo.peya.langjal.compiler.instructions.utils.TestCompileReporter;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.LabelInfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StackFrameMapCreatorTest {
    private static final InstructionInfo NOP = new InstructionInfo(
            new InstructionEvaluatorNop(),
            new ClassNode(),
            new MethodNode(),
            EOpcodes.NOP,
            0,
            null,
            0,
            -1
    );

    private static LocalStackElement local(int index, StackElementType type) {
        return new LocalStackElement(NOP, index, new PrimitiveElement(NOP, type));
    }

    private static LocalStackElement topLocal(int index) {
        return new LocalStackElement(NOP, index, new TopElement(NOP));
    }

    private static StackElement stack(StackElementType type) {
        return new PrimitiveElement(NOP, type);
    }

    private static ObjectElement object(String descriptor) {
        return new ObjectElement(NOP, TypeDescriptor.parse(descriptor));
    }

    private static FramePropagation propagation(LabelInfo label, StackElement[] stack, LocalStackElement... locals) {
        return new FramePropagation(
                label,
                new AnalysedInstruction[0],
                label,
                stack,
                locals,
                stack.length,
                locals.length
        );
    }

    private static FramePropagation propagation(LabelInfo label, LocalStackElement... locals) {
        return propagation(label, new StackElement[0], locals);
    }

    private static StackFrameMapEntry[] createEntries(FramePropagation... propagations) {
        StackFrameMapCreator creator = new StackFrameMapCreator(
                new FileEvaluatingReporter(new TestCompileReporter(), null),
                new MethodNode()
        );
        creator.updateFrames(propagations);
        return creator.createStackFrameMap();
    }

    @Test
    void createsNoEntriesWhenThereIsOnlyOneFrame() {
        LabelInfo only = new LabelInfo("L0", new Label(), 0);

        StackFrameMapEntry[] entries = createEntries(propagation(only));

        assertEquals(0, entries.length);
    }

    @Test
    void appendFrameRequiresExistingLocalPrefixToMatch() {
        LabelInfo previous = new LabelInfo("L0", new Label(), 0);
        LabelInfo next = new LabelInfo("L1", new Label(), 1);
        StackFrameMapEntry[] entries = createEntries(
                propagation(previous, local(0, StackElementType.INTEGER)),
                propagation(next, local(0, StackElementType.FLOAT), local(1, StackElementType.INTEGER))
        );

        assertEquals(1, entries.length);
        assertEquals(StackFrameMapEntry.FrameType.FULL_FRAME, entries[0].type());
    }

    @Test
    void chopFrameRequiresRemainingLocalPrefixToMatch() {
        LabelInfo previous = new LabelInfo("L0", new Label(), 0);
        LabelInfo next = new LabelInfo("L1", new Label(), 1);
        StackFrameMapEntry[] entries = createEntries(
                propagation(previous, local(0, StackElementType.INTEGER), local(1, StackElementType.INTEGER)),
                propagation(next, local(0, StackElementType.FLOAT))
        );

        assertEquals(1, entries.length);
        assertEquals(StackFrameMapEntry.FrameType.FULL_FRAME, entries[0].type());
    }

    @Test
    void createsSameFrameWhenStackAndLocalsAreUnchanged() {
        LabelInfo previous = new LabelInfo("L0", new Label(), 0);
        LabelInfo next = new LabelInfo("L1", new Label(), 1);

        StackFrameMapEntry[] entries = createEntries(
                propagation(previous, local(0, StackElementType.INTEGER)),
                propagation(next, local(0, StackElementType.INTEGER))
        );

        assertEquals(StackFrameMapEntry.FrameType.SAME, entries[0].type());
    }

    @Test
    void createsSameLocalsOneStackItemFrameWhenOnlyOneStackItemAppears() {
        LabelInfo previous = new LabelInfo("L0", new Label(), 0);
        LabelInfo next = new LabelInfo("L1", new Label(), 1);

        StackFrameMapEntry[] entries = createEntries(
                propagation(previous, local(0, StackElementType.INTEGER)),
                propagation(next, new StackElement[]{object("Ljava/lang/String;")}, local(0, StackElementType.INTEGER))
        );

        assertEquals(StackFrameMapEntry.FrameType.SAME_LOCALS_1_STACK_ITEM, entries[0].type());
        assertEquals(1, entries[0].changedStack().length);
    }

    @Test
    void createsAppendFrameWhenOnlyTrailingLocalsAreAdded() {
        LabelInfo previous = new LabelInfo("L0", new Label(), 0);
        LabelInfo next = new LabelInfo("L1", new Label(), 1);

        StackFrameMapEntry[] entries = createEntries(
                propagation(previous, local(0, StackElementType.INTEGER)),
                propagation(next, local(0, StackElementType.INTEGER), local(1, StackElementType.FLOAT))
        );

        assertEquals(StackFrameMapEntry.FrameType.APPEND, entries[0].type());
        assertEquals(1, entries[0].changedLocals().length);
    }

    @Test
    void createsAppendFrameWhenTwoTrailingLocalsAreAdded() {
        LabelInfo previous = new LabelInfo("L0", new Label(), 0);
        LabelInfo next = new LabelInfo("L1", new Label(), 1);

        StackFrameMapEntry[] entries = createEntries(
                propagation(previous, local(0, StackElementType.INTEGER)),
                propagation(next,
                        local(0, StackElementType.INTEGER),
                        local(1, StackElementType.FLOAT),
                        local(2, StackElementType.INTEGER))
        );

        assertEquals(StackFrameMapEntry.FrameType.APPEND, entries[0].type());
        assertEquals(2, entries[0].changedLocals().length);
    }

    @Test
    void createsChopFrameWhenOnlyTrailingLocalsAreRemoved() {
        LabelInfo previous = new LabelInfo("L0", new Label(), 0);
        LabelInfo next = new LabelInfo("L1", new Label(), 1);

        StackFrameMapEntry[] entries = createEntries(
                propagation(previous, local(0, StackElementType.INTEGER), local(1, StackElementType.FLOAT)),
                propagation(next, local(0, StackElementType.INTEGER))
        );

        assertEquals(StackFrameMapEntry.FrameType.CHOP, entries[0].type());
        assertEquals(1, entries[0].changedLocals().length);
    }

    @Test
    void createsChopFrameWhenTwoTrailingLocalsAreRemoved() {
        LabelInfo previous = new LabelInfo("L0", new Label(), 0);
        LabelInfo next = new LabelInfo("L1", new Label(), 1);

        StackFrameMapEntry[] entries = createEntries(
                propagation(previous,
                        local(0, StackElementType.INTEGER),
                        local(1, StackElementType.FLOAT),
                        local(2, StackElementType.INTEGER)),
                propagation(next, local(0, StackElementType.INTEGER))
        );

        assertEquals(StackFrameMapEntry.FrameType.CHOP, entries[0].type());
        assertEquals(2, entries[0].changedLocals().length);
    }

    @Test
    void createsFullFrameWhenMiddleLocalChanges() {
        LabelInfo previous = new LabelInfo("L0", new Label(), 0);
        LabelInfo next = new LabelInfo("L1", new Label(), 1);

        StackFrameMapEntry[] entries = createEntries(
                propagation(previous,
                        local(0, StackElementType.INTEGER),
                        local(1, StackElementType.FLOAT),
                        local(2, StackElementType.INTEGER)),
                propagation(next,
                        local(0, StackElementType.INTEGER),
                        local(1, StackElementType.INTEGER),
                        local(2, StackElementType.INTEGER))
        );

        assertEquals(StackFrameMapEntry.FrameType.FULL_FRAME, entries[0].type());
    }

    @Test
    void createsFullFrameWhenStackHasMultipleItems() {
        LabelInfo previous = new LabelInfo("L0", new Label(), 0);
        LabelInfo next = new LabelInfo("L1", new Label(), 1);

        StackFrameMapEntry[] entries = createEntries(
                propagation(previous),
                propagation(next, new StackElement[]{stack(StackElementType.INTEGER), object("Ljava/lang/String;")})
        );

        assertEquals(StackFrameMapEntry.FrameType.FULL_FRAME, entries[0].type());
    }

    @Test
    void frameNodeOmitsTopAfterCategoryTwoLocalsAndStackItems() {
        LabelInfo previous = new LabelInfo("L0", new Label(), 0);
        LabelInfo next = new LabelInfo("L1", new Label(), 1);
        StackFrameMapEntry entry = StackFrameMapEntry.full(
                new InstructionSetFrame(previous, new StackElement[0], new LocalStackElement[0]),
                new InstructionSetFrame(next, new StackElement[0], new LocalStackElement[0]),
                new StackElement[]{stack(StackElementType.LONG), new TopElement(NOP)},
                new LocalStackElement[]{local(0, StackElementType.DOUBLE), topLocal(1)}
        );

        FrameNode frame = entry.toASMFrameNode();

        assertEquals(EOpcodes.F_FULL, frame.type);
        assertEquals(1, frame.local.size());
        assertEquals(1, frame.stack.size());
        assertTrue(frame.local.contains(EOpcodes.DOUBLE));
        assertTrue(frame.stack.contains(EOpcodes.LONG));
    }
}
