package tokyo.peya.langjal.analyser;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.analyser.stack.LocalStackElement;
import tokyo.peya.langjal.analyser.stack.ObjectElement;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.analyser.stack.TopElement;
import tokyo.peya.langjal.compiler.instructions.InstructionEvaluatorNop;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StackElementUtilsTest {
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

    @Test
    void filterDeadLocalsPreservesParameters() {
        LocalStackElement[] locals = new LocalStackElement[]{
                new LocalStackElement(
                        NOP,
                        0,
                        new ObjectElement(NOP, TypeDescriptor.parse("[Ljava/lang/String;")),
                        true
                ),
                new LocalStackElement(NOP, 1, new TopElement(NOP))
        };

        LocalStackElement[] filtered = StackElementUtils.filterDeadLocals(locals, new BitSet());

        assertEquals(1, filtered.length);
        assertTrue(filtered[0].isParameter());
        assertEquals(StackElementType.OBJECT, filtered[0].type());
    }

    @Test
    void mergeLocalsPreservesDeadParameters() {
        LocalStackElement[] existing = {
                new LocalStackElement(
                        NOP,
                        0,
                        new ObjectElement(NOP, TypeDescriptor.parse("[Ljava/lang/String;")),
                        true
                ),
                new LocalStackElement(NOP, 1, new TopElement(NOP))
        };

        LocalStackElement[] incoming = {
                new LocalStackElement(
                        NOP,
                        0,
                        new ObjectElement(NOP, TypeDescriptor.parse("[Ljava/lang/String;")),
                        true
                ),
                new LocalStackElement(NOP, 1, new TopElement(NOP))
        };

        BitSet liveLocals = new BitSet();
        LocalStackElement[] merged = StackElementUtils.mergeLocals(existing, incoming, liveLocals);

        assertEquals(1, merged.length);
        assertTrue(merged[0].isParameter());
        assertEquals(StackElementType.OBJECT, merged[0].type());
    }
}
