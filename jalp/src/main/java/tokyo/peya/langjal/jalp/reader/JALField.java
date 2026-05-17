package tokyo.peya.langjal.jalp.reader;

import tokyo.peya.langjal.compiler.jvm.AccessAttributeSet;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;

public record JALField(
        AccessAttributeSet accessAttributeSet,
        String name,
        TypeDescriptor descriptor,
        JALAttribute[] attributes
) {
    public static JALField read(JALClassReader reader, JALConstantPoolEntry[] constantPool) {
        AccessAttributeSet access = AccessAttributeSet.fromAccess(reader.readUnsignedShort());
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
                access,
                name,
                descriptor,
                attributes
        );
    }
}
