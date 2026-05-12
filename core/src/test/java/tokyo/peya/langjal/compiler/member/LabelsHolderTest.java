package tokyo.peya.langjal.compiler.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LabelsHolder")
class LabelsHolderTest {
    private static LabelInfo label(String name, int instructionIndex) {
        return new LabelInfo(name, new Label(), instructionIndex);
    }

    @Test
    void startsWithDefaultGlobalLabels() {
        LabelsHolder holder = new LabelsHolder();

        assertEquals("MBEGIN", holder.getGlobalStart().name());
        assertEquals("MEND", holder.getGlobalEnd().name());
        assertSame(holder.getGlobalStart(), holder.getCurrentLabel());
        assertTrue(holder.getLabels().isEmpty());
    }

    @Test
    void globalStartCanBeReplacedOnlyOnce() {
        LabelsHolder holder = new LabelsHolder();
        LabelInfo start = label("START", 0);

        holder.setGlobalStart(start);

        assertSame(start, holder.getGlobalStart());
        assertSame(start, holder.getCurrentLabel());
        assertThrows(IllegalStateException.class, () -> holder.setGlobalStart(label("OTHER", 1)));
    }

    @Test
    void importAsmLabelRegistersSortedLabelsAndResolvesByNode() {
        LabelsHolder holder = new LabelsHolder();
        LabelNode later = new LabelNode(new Label());
        LabelNode earlier = new LabelNode(new Label());

        LabelInfo laterInfo = holder.importASMLabel(later, 20);
        LabelInfo earlierInfo = holder.importASMLabel(earlier, 10);

        List<LabelInfo> labels = holder.getLabels();
        assertEquals(List.of(earlierInfo, laterInfo), labels);
        assertSame(earlierInfo, holder.getLabelByNode(earlier));
        assertSame(laterInfo, holder.resolveSafe(later.getLabel().toString()));
    }

    @Test
    void importingSameAsmLabelTwiceIsRejected() {
        LabelsHolder holder = new LabelsHolder();
        LabelNode labelNode = new LabelNode(new Label());

        holder.importASMLabel(labelNode, 0);

        assertThrows(IllegalStateException.class, () -> holder.importASMLabel(labelNode, 1));
    }

    @Test
    void finaliseAndRegisterGlobalStartAppendLabelsToMethod() {
        LabelsHolder holder = new LabelsHolder();
        MethodNode method = new MethodNode();

        holder.registerGlobalStart(method);
        holder.finalise(method);

        assertEquals(2, method.instructions.size());
        assertSame(holder.getGlobalStart().node(), method.instructions.get(0));
        assertSame(holder.getGlobalEnd().node(), method.instructions.get(1));
        assertTrue(holder.getLabels().contains(holder.getGlobalStart()));
        assertTrue(holder.getLabels().contains(holder.getGlobalEnd()));
    }

    @Test
    void scopeChecksAreInclusiveAndNextBlockUsesInstructionIndex() {
        LabelsHolder holder = new LabelsHolder();
        LabelInfo start = holder.importASMLabel(new LabelNode(new Label()), 10);
        LabelInfo middle = holder.importASMLabel(new LabelNode(new Label()), 20);
        LabelInfo end = holder.importASMLabel(new LabelNode(new Label()), 30);
        LabelInfo outside = label("OUTSIDE", 31);

        holder.setCurrentLabel(middle);

        assertTrue(holder.isInScope(start, end));
        assertTrue(LabelsHolder.isInScope(start, end, start));
        assertTrue(LabelsHolder.isInScope(start, end, end));
        assertFalse(LabelsHolder.isInScope(start, end, outside));
        assertSame(middle, holder.getNextBlock(start));
        assertSame(end, holder.getNextBlock(middle));
        assertNull(holder.getNextBlock(end));
    }

    @ParameterizedTest
    @CsvSource(
            {
                    "9, false",
                    "10, true",
                    "20, true",
                    "30, true",
                    "31, false"
            }
    )
    void staticScopeCheckIsInclusive(int currentIndex, boolean expected) {
        LabelInfo start = label("START", 10);
        LabelInfo end = label("END", 30);
        LabelInfo current = label("CURRENT", currentIndex);

        assertEquals(expected, LabelsHolder.isInScope(start, end, current));
    }

    @Test
    void labelsListIsUnmodifiable() {
        LabelsHolder holder = new LabelsHolder();

        assertNotNull(holder.getLabels());
        assertThrows(UnsupportedOperationException.class, () -> holder.getLabels().add(label("X", 1)));
    }
}
