package tokyo.peya.langjal.jalp.reader;

import org.jetbrains.annotations.Nullable;
import tokyo.peya.langjal.compiler.jvm.AccessAttributeSet;
import tokyo.peya.langjal.compiler.jvm.AccessLevel;
import tokyo.peya.langjal.compiler.jvm.MethodDescriptor;

public record JALMethod(
        AccessLevel access,
        AccessAttributeSet accessAttrs,
        String name,
        MethodDescriptor descriptor,
        JALAttribute[] attributes
) {
    public static JALMethod read(JALClassReader reader, JALConstantPoolEntry[] constantPool) {
        int access = reader.readUnsignedShort();
        AccessLevel accessLevel = AccessLevel.fromAccess(access);
        AccessAttributeSet accessAttrs = AccessAttributeSet.fromAccess(access);

        String name = JALClassReader.getFromConstants(
                constantPool,
                reader.readUnsignedShort(),
                entry -> entry instanceof JALConstantPoolEntry.Utf8Entry,
                entry -> ((JALConstantPoolEntry.Utf8Entry) entry).value()
        );
        MethodDescriptor descriptor = MethodDescriptor.parse(
                JALClassReader.getFromConstants(
                        constantPool,
                        reader.readUnsignedShort(),
                        entry -> entry instanceof JALConstantPoolEntry.Utf8Entry,
                        entry -> ((JALConstantPoolEntry.Utf8Entry) entry).value()
                )
        );

        JALAttribute[] attributes = JALAttribute.readAttributes(reader, constantPool);
        return new JALMethod(
                accessLevel,
                accessAttrs,
                name,
                descriptor,
                attributes
        );
    }


    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends JALAttribute> T getAttribute(Class<T> attributeClass) {
        for (JALAttribute attribute : this.attributes) {
            if (attributeClass.isInstance(attribute)) {
                return (T) attribute;
            }
        }
        return null;
    }
}
