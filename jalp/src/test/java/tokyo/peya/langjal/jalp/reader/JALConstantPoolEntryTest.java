package tokyo.peya.langjal.jalp.reader;

import org.junit.jupiter.api.Test;
import tokyo.peya.langjal.compiler.jvm.ClassReferenceType;
import tokyo.peya.langjal.compiler.jvm.MethodDescriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

class JALConstantPoolEntryTest {
    @Test
    void resolveEntryResolvesAndCachesUnresolvedClassEntry() {
        JALConstantPoolEntry[] constantPool = {
                null,
                new JALConstantPoolEntry.Utf8Entry("java/lang/Object"),
                new JALConstantPoolEntry.ClassEntry.Unresolved(1)
        };

        JALConstantPoolEntry resolved = JALConstantPoolEntry.UnresolvedConstantPoolEntry.resolveEntry(constantPool, 2);

        JALConstantPoolEntry.ClassEntry classEntry = assertInstanceOf(
                JALConstantPoolEntry.ClassEntry.class,
                resolved
        );
        assertEquals(ClassReferenceType.parse("java/lang/Object"), classEntry.name());
        assertSame(resolved, constantPool[2]);
    }

    @Test
    void resolveEntryReturnsAlreadyResolvedEntryAsIs() {
        JALConstantPoolEntry entry = new JALConstantPoolEntry.Utf8Entry("value");
        JALConstantPoolEntry[] constantPool = {null, entry};

        assertSame(entry, JALConstantPoolEntry.UnresolvedConstantPoolEntry.resolveEntry(constantPool, 1));
    }

    @Test
    void unresolvedNameAndTypeResolvesUtf8Names() {
        JALConstantPoolEntry[] constantPool = {
                null,
                new JALConstantPoolEntry.Utf8Entry("main"),
                new JALConstantPoolEntry.Utf8Entry("([Ljava/lang/String;)V"),
                new JALConstantPoolEntry.NameAndTypeEntry.Unresolved(1, 2)
        };

        JALConstantPoolEntry.NameAndTypeEntry entry =
                JALConstantPoolEntry.UnresolvedConstantPoolEntry.resolveNameAndType(constantPool, 3);

        assertEquals("main", entry.name());
        assertEquals("([Ljava/lang/String;)V", entry.descriptor());
    }

    @Test
    void unresolvedMethodTypeResolvesDescriptor() {
        JALConstantPoolEntry[] constantPool = {
                null,
                new JALConstantPoolEntry.Utf8Entry("(I)V"),
                new JALConstantPoolEntry.MethodTypeEntry.Unresolved(1)
        };

        JALConstantPoolEntry.MethodTypeEntry entry = (JALConstantPoolEntry.MethodTypeEntry)
                JALConstantPoolEntry.UnresolvedConstantPoolEntry.resolveEntry(constantPool, 2);

        assertEquals(MethodDescriptor.parse("(I)V"), entry.descriptor());
    }
}
