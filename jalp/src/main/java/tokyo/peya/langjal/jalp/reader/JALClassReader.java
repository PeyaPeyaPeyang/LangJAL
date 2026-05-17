package tokyo.peya.langjal.jalp.reader;

import tokyo.peya.langjal.compiler.jvm.AccessAttributeSet;
import tokyo.peya.langjal.compiler.jvm.AccessLevel;
import tokyo.peya.langjal.compiler.jvm.ClassReferenceType;

import java.util.function.Function;
import java.util.function.Predicate;

public class JALClassReader {
    private final byte[] bytes;
    private int current;

    private JALClassReader(byte[] bytes) {
        this.bytes = bytes;
        this.current = 0;
    }

    public static JALClass read(byte[] bytes) {
        JALClassReader reader = new JALClassReader(bytes);
        return reader.readClass();
    }

    private JALClass readClass() {
        this.validateCAFEBABE();  // u4 magic
        int minorVersion = this.readUnsignedShort();  // u2  minor_version
        int majorVersion = this.readUnsignedShort();  // u2  major_version

        // (u2 constant_pool_count, cp_info constant_pool[constant_pool_count-1])
        JALConstantPoolEntry[] constantPool = this.readConstants();

        int access = this.readUnsignedShort();  // u2 access_flags
        AccessLevel accessLevel = AccessLevel.fromAccess(access);
        AccessAttributeSet accessAttrs = AccessAttributeSet.fromAccess(access);

        JALConstantPoolEntry.ClassEntry thisClass = this.getClassEntryFromConstants(constantPool, this.readUnsignedShort());  // u2 this_class
        JALConstantPoolEntry.ClassEntry superClass = this.getClassEntryFromConstants(constantPool, this.readUnsignedShort());  // u2 super_class

        ClassReferenceType[] interfaces = this.readInterfaces(constantPool);  // u2 interfaces_count, u2 interfaces[interfaces_count]
        JALField[] fields = this.readFields(constantPool);  // u2 fields_count, field_info fields[fields_count]
        JALMethod[] methods = this.readMethods(constantPool);  // u2 methods_count, method_info methods[methods_count]
        JALAttribute[] attributes = JALAttribute.readAttributes(this, constantPool);  // u2 attributes_count, attribute_info attributes[attributes_count]

        return new JALClass(
                majorVersion,
                minorVersion,
                constantPool,
                accessLevel,
                accessAttrs,
                thisClass.name(),
                superClass.name(),
                interfaces,
                fields,
                methods,
                attributes
        );
    }

    private void validateCAFEBABE() {
        int magic = this.readInt();
        if (magic != 0xCAFEBABE) {
            throw new IllegalArgumentException("Invalid class file: missing CAFEBABE header");
        }
    }

    private JALConstantPoolEntry[] readConstants() {
        int constantPoolCount = this.readUnsignedShort();
        JALConstantPoolEntry[] constantPool = new JALConstantPoolEntry[constantPoolCount];
        for (int i = 1; i < constantPoolCount; i++) {
            constantPool[i] = JALConstantPoolEntry.read(this);
        }

        // 未解決の参照があるので，定数プールの解決を行う
        this.resolveConstants(constantPool);
        return constantPool;
    }

    private void resolveConstants(JALConstantPoolEntry [] constantPool) {
        for (int i = 1; i < constantPool.length; i++) {
            if (constantPool[i] instanceof JALConstantPoolEntry.UnresolvedConstantPoolEntry unresolved) {
                constantPool[i] = unresolved.resolve(constantPool);
            }
        }
    }

    private String getStringFromConstants(JALConstantPoolEntry[] constantPool, int index) {
        return getFromConstants(
                constantPool,
                index,
                entry -> entry instanceof JALConstantPoolEntry.Utf8Entry,
                entry -> ((JALConstantPoolEntry.Utf8Entry) entry).value()
        );
    }

    private JALConstantPoolEntry.ClassEntry getClassEntryFromConstants(JALConstantPoolEntry[] constantPool, int index) {
        return getFromConstants(
                constantPool,
                index,
                entry -> entry instanceof JALConstantPoolEntry.ClassEntry,
                entry -> (JALConstantPoolEntry.ClassEntry) entry
        );
    }

    private ClassReferenceType[]  readInterfaces(JALConstantPoolEntry[] constantPool) {
        int interfacesCount = this.readUnsignedShort();
        ClassReferenceType[] interfaces = new ClassReferenceType[interfacesCount];
        for (int i = 0; i < interfacesCount; i++) {
            int index = this.readUnsignedShort();
            JALConstantPoolEntry.ClassEntry clazz = getFromConstants(
                    constantPool,
                    index,
                    entry -> entry instanceof JALConstantPoolEntry.ClassEntry,
                    entry -> (JALConstantPoolEntry.ClassEntry) entry
            );

            interfaces[i] = clazz.name();
        }

        return interfaces;
    }

    private JALField[] readFields(JALConstantPoolEntry[] constantPool) {
        int fieldsCount = this.readUnsignedShort();
        JALField[] fields = new JALField[fieldsCount];
        for (int i = 0; i < fieldsCount; i++) {
            fields[i] = JALField.read(this, constantPool);
        }
        return fields;
    }

    private JALMethod[] readMethods(JALConstantPoolEntry[] constantPool) {
        int methodsCount = this.readUnsignedShort();
        JALMethod[] methods = new JALMethod[methodsCount];
        for (int i = 0; i < methodsCount; i++) {
            methods[i] = JALMethod.read(this, constantPool);
        }
        return methods;
    }

    /* non-public */ static <T> T getFromConstants(JALConstantPoolEntry[] constantPool,
                                          int idx,
                                          Predicate<? super JALConstantPoolEntry> type,
                                          Function<? super JALConstantPoolEntry, T> extractor) {
        // OVER している場合はダメ
        if (idx <= 0 || idx >= constantPool.length) {
            throw new IllegalArgumentException("Invalid constant pool index: " + idx);
        }

        // 型が違う場合もダメ
        JALConstantPoolEntry entry = constantPool[idx];
        if (!type.test(entry)) {
            throw new IllegalArgumentException("Expected " + type + " at index " + idx + " but found " + entry.getClass().getSimpleName());
        }
        return extractor.apply(entry);
    }

    /* non-public */ byte[] readBytes(int length) {
        byte[] result = new byte[length];
        System.arraycopy(this.bytes, this.current, result, 0, length);
        this.current += length;
        return result;
    }

    /* non-public */ String readUTF8() {
        int length = this.readUnsignedShort();
        byte[] bytes = this.readBytes(length);
        return new String(bytes);
    }

    /* non-public */ int readUnsignedShort() {
        int byte1 = this.readByte() & 0xFF;
        int byte2 = this.readByte() & 0xFF;
        return (byte1 << 8) | byte2;
    }

    /* non-public */ int readByte() {
        return this.bytes[this.current++];
    }

    /* non-public */ int readInt() {
        int byte1 = this.readByte() & 0xFF;
        int byte2 = this.readByte() & 0xFF;
        int byte3 = this.readByte() & 0xFF;
        int byte4 = this.readByte() & 0xFF;
        return (byte1 << 24) | (byte2 << 16) | (byte3 << 8) | byte4;
    }

    /* non-public */ int readUnsignedByte() {
        return this.readByte() & 0xFF;
    }

    /* non-public */ long readLong() {
        long byte1 = this.readByte() & 0xFFL;
        long byte2 = this.readByte() & 0xFFL;
        long byte3 = this.readByte() & 0xFFL;
        long byte4 = this.readByte() & 0xFFL;
        long byte5 = this.readByte() & 0xFFL;
        long byte6 = this.readByte() & 0xFFL;
        long byte7 = this.readByte() & 0xFFL;
        long byte8 = this.readByte() & 0xFFL;
        return (byte1 << 56) | (byte2 << 48) | (byte3 << 40) | (byte4 << 32) |
               (byte5 << 24) | (byte6 << 16) | (byte7 << 8) | byte8;
    }

    /* non-public */ float readFloat() {
        return Float.intBitsToFloat(this.readInt());
    }

    /* non-public */ double readDouble() {
        return Double.longBitsToDouble(this.readLong());
    }

    /* non-public */ short readShort() {
        int byte1 = this.readByte() & 0xFF;
        int byte2 = this.readByte() & 0xFF;
        return (short) ((byte1 << 8) | byte2);
    }

    /* non-public */ char readChar() {
        int byte1 = this.readByte() & 0xFF;
        int byte2 = this.readByte() & 0xFF;
        return (char) ((byte1 << 8) | byte2);
    }

    /* non-public */ boolean readBoolean() {
        return this.readByte() != 0;
    }

    /* non-public */ void skip(int length) {
        this.current += length;
    }
}
