package tokyo.peya.langjal.jalp.reader;

import tokyo.peya.langjal.compiler.jvm.AccessAttributeSet;
import tokyo.peya.langjal.compiler.jvm.AccessLevel;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;

public record JALField(
        AccessLevel access,
        AccessAttributeSet accessAttributeSet,
        String name,
        TypeDescriptor descriptor,
        JALAttribute[] attributes
) {
    public JALField(AccessAttributeSet accessAttributeSet, String name, TypeDescriptor descriptor, JALAttribute[] attributes) {
        this(AccessLevel.PACKAGE_PRIVATE, accessAttributeSet, name, descriptor, attributes);
    }

    public static JALField read(JALClassReader reader, JALConstantPoolEntry[] constantPool) {
        int access = reader.readUnsignedShort();
        AccessLevel accessLevel = AccessLevel.fromAccess(access);
        AccessAttributeSet accessAttrs = AccessAttributeSet.fromAccess(access);
        String name = JALClassReader.getFromConstants(
                constantPool,
                reader.readUnsignedShort(),
                entry -> entry instanceof JALConstantPoolEntry.Utf8Entry,
                entry -> ((JALConstantPoolEntry.Utf8Entry) entry).value()
        );
        TypeDescriptor descriptor = TypeDescriptor.parse(
                JALClassReader.getFromConstants(
                        constantPool,
                        reader.readUnsignedShort(),
                        entry -> entry instanceof JALConstantPoolEntry.Utf8Entry,
                        entry -> ((JALConstantPoolEntry.Utf8Entry) entry).value()
                )
        );

        JALAttribute[] attributes = JALAttribute.readAttributes(reader, constantPool);
        return new JALField(
                accessLevel,
                accessAttrs,
                name,
                descriptor,
                attributes
        );
    }
}
