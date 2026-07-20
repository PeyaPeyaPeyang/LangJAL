package tokyo.peya.langjal.analyser;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.analyser.stack.LocalStackElement;
import tokyo.peya.langjal.analyser.stack.NullElement;
import tokyo.peya.langjal.analyser.stack.ObjectElement;
import tokyo.peya.langjal.analyser.stack.PrimitiveElement;
import tokyo.peya.langjal.analyser.stack.StackElement;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.analyser.stack.TopElement;
import tokyo.peya.langjal.compiler.exceptions.analyse.StackSizeDifferentException;
import tokyo.peya.langjal.compiler.instructions.InstructionEvaluatorNop;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.LabelInfo;

import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    private static LocalStackElement local(int index, StackElementType type) {
        return new LocalStackElement(NOP, index, new PrimitiveElement(NOP, type));
    }

    private static LocalStackElement topLocal(int index) {
        return new LocalStackElement(NOP, index, new TopElement(NOP));
    }

    @Test
    void cleanUpLocalsRemovesTrailingTopSlots() {
        LocalStackElement[] cleaned = StackElementUtils.cleanUpLocals(new LocalStackElement[]{
                local(0, StackElementType.INTEGER),
                topLocal(1),
                topLocal(2)
        });

        assertEquals(1, cleaned.length);
        assertEquals(StackElementType.INTEGER, cleaned[0].type());
    }

    @Test
    void cleanUpLocalsKeepsTopSlotAfterCategoryTwoLocal() {
        LocalStackElement[] cleaned = StackElementUtils.cleanUpLocals(new LocalStackElement[]{
                local(0, StackElementType.LONG),
                topLocal(1)
        });

        assertEquals(2, cleaned.length);
        assertEquals(StackElementType.LONG, cleaned[0].type());
        assertEquals(StackElementType.TOP, cleaned[1].type());
    }

    @Test
    void filterDeadLocalsKeepsLiveLocalsAndTopsDeadLocals() {
        BitSet liveLocals = new BitSet();
        liveLocals.set(0);

        LocalStackElement[] filtered = StackElementUtils.filterDeadLocals(new LocalStackElement[]{
                local(0, StackElementType.INTEGER),
                local(1, StackElementType.FLOAT),
                local(2, StackElementType.INTEGER)
        }, liveLocals);

        assertEquals(1, filtered.length);
        assertEquals(StackElementType.INTEGER, filtered[0].type());
    }

    @Test
    void mergeLocalsWithSamePrimitiveTypesKeepsParameterFlag() {
        LocalStackElement[] merged = StackElementUtils.mergeLocals(
                new LocalStackElement[]{
                        new LocalStackElement(NOP, 0, new PrimitiveElement(NOP, StackElementType.INTEGER), true)
                },
                new LocalStackElement[]{
                        local(0, StackElementType.INTEGER)
                }
        );

        assertEquals(1, merged.length);
        assertEquals(StackElementType.INTEGER, merged[0].type());
        assertTrue(merged[0].isParameter());
    }

    @Test
    void mergeStackKeepsSamePrimitiveTypes() {
        LabelInfo label = new LabelInfo("L0", new org.objectweb.asm.Label(), 0);

        StackElement[] merged = StackElementUtils.mergeStack(
                label,
                new StackElement[]{new PrimitiveElement(NOP, StackElementType.INTEGER)},
                new StackElement[]{new PrimitiveElement(NOP, StackElementType.INTEGER)}
        );

        assertEquals(1, merged.length);
        assertEquals(StackElementType.INTEGER, merged[0].type());
    }

    @Test
    void mergeNullAndObjectUsesObjectType() {
        ObjectElement object = new ObjectElement(NOP, TypeDescriptor.className("java/lang/String"));

        assertEquals(object, StackElementUtils.mergeElement(new NullElement(NOP), object));
        assertEquals(object, StackElementUtils.mergeElement(object, new NullElement(NOP)));
    }

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

    @Test
    void mergeObjectsKeepsObjectAsCommonTypeRegardlessOfOrder() {
        ObjectElement object = new ObjectElement(NOP, TypeDescriptor.OBJECT);
        ObjectElement string = new ObjectElement(NOP, TypeDescriptor.className("java/lang/String"));

        assertEquals(TypeDescriptor.OBJECT, StackElementUtils.mergeObjects(object, string).content());
        assertEquals(TypeDescriptor.OBJECT, StackElementUtils.mergeObjects(string, object).content());
    }

    @Test
    void mergeObjectsFindsNearestCommonSuperclass() {
        ObjectElement builder = new ObjectElement(NOP, TypeDescriptor.className("java/lang/StringBuilder"));
        ObjectElement string = new ObjectElement(NOP, TypeDescriptor.className("java/lang/String"));

        assertEquals(TypeDescriptor.OBJECT, StackElementUtils.mergeObjects(builder, string).content());
    }

    @Test
    void mergeArraysWithDifferentDimensionsFallsBackToObject() {
        ObjectElement oneDimension = new ObjectElement(NOP, TypeDescriptor.parse("[Ljava/lang/String;"));
        ObjectElement twoDimensions = new ObjectElement(NOP, TypeDescriptor.parse("[[Ljava/lang/String;"));

        assertEquals(TypeDescriptor.OBJECT, StackElementUtils.mergeObjects(oneDimension, twoDimensions).content());
    }

    @Test
    void mergeObjectArraysWithSameDimensionUsesCommonComponentSuperclass() {
        ObjectElement builders = new ObjectElement(NOP, TypeDescriptor.parse("[Ljava/lang/StringBuilder;"));
        ObjectElement strings = new ObjectElement(NOP, TypeDescriptor.parse("[Ljava/lang/String;"));

        assertEquals(TypeDescriptor.parse("[Ljava/lang/Object;"), StackElementUtils.mergeObjects(builders, strings).content());
    }

    @Test
    void mergePrimitiveArraysWithDifferentComponentTypesFallsBackToObject() {
        ObjectElement ints = new ObjectElement(NOP, TypeDescriptor.parse("[I"));
        ObjectElement longs = new ObjectElement(NOP, TypeDescriptor.parse("[J"));

        assertEquals(TypeDescriptor.OBJECT, StackElementUtils.mergeObjects(ints, longs).content());
    }

    @Test
    void mergeReferenceAndArrayFallsBackToObject() {
        ObjectElement string = new ObjectElement(NOP, TypeDescriptor.className("java/lang/String"));
        ObjectElement strings = new ObjectElement(NOP, TypeDescriptor.parse("[Ljava/lang/String;"));

        assertEquals(TypeDescriptor.OBJECT, StackElementUtils.mergeObjects(string, strings).content());
    }

    @Test
    void mergeStackRejectsDifferentStackSizes() {
        LabelInfo label = new LabelInfo("L0", new org.objectweb.asm.Label(), 0);

        assertThrows(
                StackSizeDifferentException.class,
                () -> StackElementUtils.mergeStack(
                        label,
                        new StackElement[]{new ObjectElement(NOP, TypeDescriptor.OBJECT)},
                        new StackElement[0]
                )
        );
    }
}
