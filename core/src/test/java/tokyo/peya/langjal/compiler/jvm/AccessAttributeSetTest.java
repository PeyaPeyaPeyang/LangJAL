package tokyo.peya.langjal.compiler.jvm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("AccessAttributeSet")
class AccessAttributeSetTest {

    @Test
    void emptyConstantIsEmpty() {
        assertEquals(0, AccessAttributeSet.EMPTY.getAttributes().length);
    }

    @Test
    void constructorFromVarargsPreservesContent() {
        AccessAttributeSet set = new AccessAttributeSet(AccessAttribute.STATIC, AccessAttribute.FINAL);

        assertEquals(2, set.getAttributes().length);
        assertTrue(set.has(AccessAttribute.STATIC));
        assertTrue(set.has(AccessAttribute.FINAL));
    }

    @Test
    void constructorFromVarargsHandlesEmpty() {
        AccessAttributeSet set = AccessAttributeSet.EMPTY;

        assertEquals(0, set.getAttributes().length);
    }

    @ParameterizedTest
    @CsvSource({
            "STATIC",
            "FINAL",
            "ABSTRACT",
            "INTERFACE",
            "NATIVE"
    })
    void constructorFromStringNamesCreatesAttributes(String attributeName) {
        AccessAttributeSet set = new AccessAttributeSet(attributeName);

        assertEquals(1, set.getAttributes().length);
        assertTrue(set.has(AccessAttribute.valueOf(attributeName)));
    }

    @Test
    void constructorFromMultipleStringNames() {
        AccessAttributeSet set = new AccessAttributeSet("STATIC", "FINAL", "ABSTRACT");

        assertEquals(3, set.getAttributes().length);
        assertTrue(set.has(AccessAttribute.STATIC));
        assertTrue(set.has(AccessAttribute.FINAL));
        assertTrue(set.has(AccessAttribute.ABSTRACT));
    }

    @Test
    void hasReturnsFalseForAbsentAttribute() {
        AccessAttributeSet set = new AccessAttributeSet(AccessAttribute.STATIC);

        assertFalse(set.has(AccessAttribute.FINAL));
        assertFalse(set.has(AccessAttribute.ABSTRACT));
    }

    @Test
    void hasReturnsTrueForPresentAttribute() {
        AccessAttributeSet set = new AccessAttributeSet(AccessAttribute.STATIC, AccessAttribute.FINAL);

        assertTrue(set.has(AccessAttribute.STATIC));
        assertTrue(set.has(AccessAttribute.FINAL));
    }

    @Test
    void toStringFormatsAttributeNames() {
        AccessAttributeSet set = new AccessAttributeSet(AccessAttribute.STATIC);
        String result = set.toString();

        assertTrue(result.contains("static"));
    }

    @Test
    void toStringEmptySet() {
        AccessAttributeSet set = AccessAttributeSet.EMPTY;

        assertEquals("", set.toString());
    }

    @Test
    void isNormalClassReturnsTrueForOrdinaryClass() {
        AccessAttributeSet set = new AccessAttributeSet(AccessAttribute.STATIC);

        assertTrue(set.isNormalClass());
    }

    @Test
    void isNormalClassReturnsTrueForEmpty() {
        AccessAttributeSet set = AccessAttributeSet.EMPTY;

        assertTrue(set.isNormalClass());
    }

    @ParameterizedTest
    @MethodSource("specialClassAttributes")
    void isNormalClassReturnsFalseForSpecialClasses(AccessAttribute attribute) {
        AccessAttributeSet set = new AccessAttributeSet(attribute);

        assertFalse(set.isNormalClass());
    }

    static Stream<AccessAttribute> specialClassAttributes() {
        return Stream.of(
                AccessAttribute.ABSTRACT,
                AccessAttribute.INTERFACE,
                AccessAttribute.ENUM,
                AccessAttribute.ANNOTATION
        );
    }

    @Test
    void fromAccessRecognizesStaticFlag() {
        AccessAttributeSet set = AccessAttributeSet.fromAccess(EOpcodes.ACC_STATIC);

        assertTrue(set.has(AccessAttribute.STATIC));
    }

    @Test
    void fromAccessRecognizesFinalFlag() {
        AccessAttributeSet set = AccessAttributeSet.fromAccess(EOpcodes.ACC_FINAL);

        assertTrue(set.has(AccessAttribute.FINAL));
    }

    @Test
    void fromAccessRecognizesSuperFlag() {
        AccessAttributeSet set = AccessAttributeSet.fromAccess(EOpcodes.ACC_SUPER);

        assertTrue(set.has(AccessAttribute.SUPER));
    }

    @Test
    void fromAccessRecognizesSynchronizedFlag() {
        AccessAttributeSet set = AccessAttributeSet.fromAccess(EOpcodes.ACC_SYNCHRONIZED);

        assertTrue(set.has(AccessAttribute.SYNCHRONIZED));
    }

    @Test
    void fromAccessRecognizesVolatileFlag() {
        AccessAttributeSet set = AccessAttributeSet.fromAccess(EOpcodes.ACC_VOLATILE);

        assertTrue(set.has(AccessAttribute.VOLATILE));
    }

    @Test
    void fromAccessRecognizesBridgeFlag() {
        AccessAttributeSet set = AccessAttributeSet.fromAccess(EOpcodes.ACC_BRIDGE);

        assertTrue(set.has(AccessAttribute.BRIDGE));
    }

    @Test
    void fromAccessRecognizesVarargsFlag() {
        AccessAttributeSet set = AccessAttributeSet.fromAccess(EOpcodes.ACC_VARARGS);

        assertTrue(set.has(AccessAttribute.VARARGS));
    }

    @Test
    void fromAccessRecognizesTransientFlag() {
        AccessAttributeSet set = AccessAttributeSet.fromAccess(EOpcodes.ACC_TRANSIENT);

        assertTrue(set.has(AccessAttribute.TRANSIENT));
    }

    @Test
    void fromAccessRecognizesNativeFlag() {
        AccessAttributeSet set = AccessAttributeSet.fromAccess(EOpcodes.ACC_NATIVE);

        assertTrue(set.has(AccessAttribute.NATIVE));
    }

    @Test
    void fromAccessRecognizesInterfaceFlag() {
        AccessAttributeSet set = AccessAttributeSet.fromAccess(EOpcodes.ACC_INTERFACE);

        assertTrue(set.has(AccessAttribute.INTERFACE));
    }

    @Test
    void fromAccessRecognizesAbstractFlag() {
        AccessAttributeSet set = AccessAttributeSet.fromAccess(EOpcodes.ACC_ABSTRACT);

        assertTrue(set.has(AccessAttribute.ABSTRACT));
    }

    @Test
    void fromAccessRecognizesStrictFlag() {
        AccessAttributeSet set = AccessAttributeSet.fromAccess(EOpcodes.ACC_STRICT);

        assertTrue(set.has(AccessAttribute.STRICTFP));
    }

    @Test
    void fromAccessRecognizesSyntheticFlag() {
        AccessAttributeSet set = AccessAttributeSet.fromAccess(EOpcodes.ACC_SYNTHETIC);

        assertTrue(set.has(AccessAttribute.SYNTHETIC));
    }

    @Test
    void fromAccessRecognizesAnnotationFlag() {
        AccessAttributeSet set = AccessAttributeSet.fromAccess(EOpcodes.ACC_ANNOTATION);

        assertTrue(set.has(AccessAttribute.ANNOTATION));
    }

    @Test
    void fromAccessRecognizesEnumFlag() {
        AccessAttributeSet set = AccessAttributeSet.fromAccess(EOpcodes.ACC_ENUM);

        assertTrue(set.has(AccessAttribute.ENUM));
    }

    @Test
    void fromAccessRecognizesMandatedFlag() {
        AccessAttributeSet set = AccessAttributeSet.fromAccess(EOpcodes.ACC_MANDATED);

        assertTrue(set.has(AccessAttribute.MANDATED));
    }

    @Test
    void fromAccessCombinesMultipleFlags() {
        int combined = EOpcodes.ACC_STATIC | EOpcodes.ACC_FINAL | EOpcodes.ACC_SUPER;
        AccessAttributeSet set = AccessAttributeSet.fromAccess(combined);

        assertTrue(set.has(AccessAttribute.STATIC));
        assertTrue(set.has(AccessAttribute.FINAL));
        assertTrue(set.has(AccessAttribute.SUPER));
    }

    @Test
    void fromAccessHandlesZeroFlags() {
        AccessAttributeSet set = AccessAttributeSet.fromAccess(0);

        assertEquals(0, set.getAttributes().length);
    }

    @Test
    void fromAccessDoesNotRecognizeUnknownBits() {
        int unknownBits = 1 << 30;
        AccessAttributeSet set = AccessAttributeSet.fromAccess(unknownBits);

        assertEquals(0, set.getAttributes().length);
    }
}

