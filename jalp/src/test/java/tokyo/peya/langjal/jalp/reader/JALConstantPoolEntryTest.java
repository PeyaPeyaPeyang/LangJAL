package tokyo.peya.langjal.jalp.reader;

import org.junit.jupiter.api.Test;
import tokyo.peya.langjal.compiler.jvm.ClassReferenceType;
import tokyo.peya.langjal.compiler.jvm.MethodDescriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    void unresolvedStringResolvesUtf8Value() {
        JALConstantPoolEntry[] constantPool = {
                null,
                new JALConstantPoolEntry.Utf8Entry("hello"),
                new JALConstantPoolEntry.StringEntry.Unresolved(1)
        };

        JALConstantPoolEntry.StringEntry entry = (JALConstantPoolEntry.StringEntry)
                JALConstantPoolEntry.UnresolvedConstantPoolEntry.resolveEntry(constantPool, 2);

        assertEquals("hello", entry.value());
        assertSame(entry, constantPool[2]);
    }

    @Test
    void unresolvedFieldResolvesOwnerAndNameAndType() {
        JALConstantPoolEntry[] constantPool = {
                null,
                new JALConstantPoolEntry.Utf8Entry("pkg/Owner"),
                new JALConstantPoolEntry.ClassEntry.Unresolved(1),
                new JALConstantPoolEntry.Utf8Entry("value"),
                new JALConstantPoolEntry.Utf8Entry("I"),
                new JALConstantPoolEntry.NameAndTypeEntry.Unresolved(3, 4),
                new JALConstantPoolEntry.FieldEntry.Unresolved(2, 5)
        };

        JALConstantPoolEntry.FieldEntry entry = (JALConstantPoolEntry.FieldEntry)
                JALConstantPoolEntry.UnresolvedConstantPoolEntry.resolveEntry(constantPool, 6);

        assertEquals(ClassReferenceType.parse("pkg/Owner"), entry.owner().name());
        assertEquals("value", entry.nameAndType().name());
        assertEquals("I", entry.nameAndType().descriptor());
    }

    @Test
    void unresolvedMethodResolvesOwnerAndNameAndType() {
        JALConstantPoolEntry[] constantPool = memberReferencePool(new JALConstantPoolEntry.MethodEntry.Unresolved(2, 5));

        JALConstantPoolEntry.MethodEntry entry = (JALConstantPoolEntry.MethodEntry)
                JALConstantPoolEntry.UnresolvedConstantPoolEntry.resolveEntry(constantPool, 6);

        assertEquals(ClassReferenceType.parse("pkg/Owner"), entry.owner().name());
        assertEquals("run", entry.nameAndType().name());
        assertEquals("()V", entry.nameAndType().descriptor());
    }

    @Test
    void unresolvedInterfaceMethodResolvesOwnerAndNameAndType() {
        JALConstantPoolEntry[] constantPool = memberReferencePool(new JALConstantPoolEntry.InterfaceMethodEntry.Unresolved(2, 5));

        JALConstantPoolEntry.InterfaceMethodEntry entry = (JALConstantPoolEntry.InterfaceMethodEntry)
                JALConstantPoolEntry.UnresolvedConstantPoolEntry.resolveEntry(constantPool, 6);

        assertEquals(ClassReferenceType.parse("pkg/Owner"), entry.owner().name());
        assertEquals("run", entry.nameAndType().name());
        assertEquals("()V", entry.nameAndType().descriptor());
    }

    @Test
    void unresolvedMethodHandleResolvesReferencedEntry() {
        JALConstantPoolEntry[] constantPool = memberReferencePool(new JALConstantPoolEntry.MethodEntry.Unresolved(2, 5));
        constantPool = new JALConstantPoolEntry[] {
                constantPool[0],
                constantPool[1],
                constantPool[2],
                constantPool[3],
                constantPool[4],
                constantPool[5],
                constantPool[6],
                new JALConstantPoolEntry.MethodHandleEntry.Unresolved(6, 6)
        };

        JALConstantPoolEntry.MethodHandleEntry entry = (JALConstantPoolEntry.MethodHandleEntry)
                JALConstantPoolEntry.UnresolvedConstantPoolEntry.resolveEntry(constantPool, 7);

        assertEquals(6, entry.referenceKind());
        assertInstanceOf(JALConstantPoolEntry.MethodEntry.class, entry.reference());
    }

    @Test
    void unresolvedDynamicResolvesNameAndType() {
        JALConstantPoolEntry[] constantPool = dynamicPool(new JALConstantPoolEntry.DynamicEntry.Unresolved(3, 3));

        JALConstantPoolEntry.DynamicEntry entry = (JALConstantPoolEntry.DynamicEntry)
                JALConstantPoolEntry.UnresolvedConstantPoolEntry.resolveEntry(constantPool, 4);

        assertEquals(3, entry.bootstrapMethodAttrIndex());
        assertEquals("factory", entry.nameAndType().name());
        assertEquals("()Ljava/lang/Object;", entry.nameAndType().descriptor());
    }

    @Test
    void unresolvedInvokeDynamicResolvesNameAndType() {
        JALConstantPoolEntry[] constantPool = dynamicPool(new JALConstantPoolEntry.InvokeDynamicEntry.Unresolved(4, 3));

        JALConstantPoolEntry.InvokeDynamicEntry entry = (JALConstantPoolEntry.InvokeDynamicEntry)
                JALConstantPoolEntry.UnresolvedConstantPoolEntry.resolveEntry(constantPool, 4);

        assertEquals(4, entry.bootstrapMethodAttrIndex());
        assertEquals("factory", entry.nameAndType().name());
        assertEquals("()Ljava/lang/Object;", entry.nameAndType().descriptor());
    }

    @Test
    void unresolvedModuleResolvesUtf8Name() {
        JALConstantPoolEntry[] constantPool = {
                null,
                new JALConstantPoolEntry.Utf8Entry("module.name"),
                new JALConstantPoolEntry.ModuleEntry.Unresolved(1)
        };

        JALConstantPoolEntry.ModuleEntry entry = (JALConstantPoolEntry.ModuleEntry)
                JALConstantPoolEntry.UnresolvedConstantPoolEntry.resolveEntry(constantPool, 2);

        assertEquals("module.name", entry.name());
    }

    @Test
    void unresolvedPackageResolvesUtf8Name() {
        JALConstantPoolEntry[] constantPool = {
                null,
                new JALConstantPoolEntry.Utf8Entry("pkg/name"),
                new JALConstantPoolEntry.PackageEntry.Unresolved(1)
        };

        JALConstantPoolEntry.PackageEntry entry = (JALConstantPoolEntry.PackageEntry)
                JALConstantPoolEntry.UnresolvedConstantPoolEntry.resolveEntry(constantPool, 2);

        assertEquals("pkg/name", entry.name());
    }

    @Test
    void resolveUtf8ThrowsWhenEntryIsWrongType() {
        JALConstantPoolEntry[] constantPool = {
                null,
                new JALConstantPoolEntry.IntegerEntry(1)
        };

        assertThrows(
                ClassCastException.class,
                () -> JALConstantPoolEntry.UnresolvedConstantPoolEntry.resolveUtf8(constantPool, 1)
        );
    }

    private static JALConstantPoolEntry[] memberReferencePool(JALConstantPoolEntry unresolvedMember) {
        return new JALConstantPoolEntry[] {
                null,
                new JALConstantPoolEntry.Utf8Entry("pkg/Owner"),
                new JALConstantPoolEntry.ClassEntry.Unresolved(1),
                new JALConstantPoolEntry.Utf8Entry("run"),
                new JALConstantPoolEntry.Utf8Entry("()V"),
                new JALConstantPoolEntry.NameAndTypeEntry.Unresolved(3, 4),
                unresolvedMember
        };
    }

    private static JALConstantPoolEntry[] dynamicPool(JALConstantPoolEntry unresolvedDynamic) {
        return new JALConstantPoolEntry[] {
                null,
                new JALConstantPoolEntry.Utf8Entry("factory"),
                new JALConstantPoolEntry.Utf8Entry("()Ljava/lang/Object;"),
                new JALConstantPoolEntry.NameAndTypeEntry.Unresolved(1, 2),
                unresolvedDynamic
        };
    }
}
