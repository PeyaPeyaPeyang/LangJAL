package tokyo.peya.langjal.jalp.reader;

import org.jetbrains.annotations.Nullable;
import tokyo.peya.langjal.compiler.jvm.AccessAttributeSet;
import tokyo.peya.langjal.compiler.jvm.AccessLevel;
import tokyo.peya.langjal.compiler.jvm.ClassReferenceType;

public record JALClass(
        int majorVersion,
        int minorVersion,

        JALConstantPoolEntry[] constants,

        AccessLevel access,
        AccessAttributeSet accessAttrs,

        ClassReferenceType thisName,
        ClassReferenceType superName,

        ClassReferenceType[] interfaces,

        JALField[] fields,
        JALMethod[] methods,

        JALAttribute[] attributes
) {
    @SuppressWarnings("unchecked")
    public <T> @Nullable T getAttribute(String name) {
        for (JALAttribute attr : this.attributes) {
            if (attr.name().equals(name)) {
                return (T) attr;
            }
        }
        return null;
    }
}
