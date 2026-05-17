package tokyo.peya.langjal.jalp.reader;

import org.jetbrains.annotations.Nullable;
import tokyo.peya.langjal.compiler.jvm.ClassReferenceType;

public sealed interface JALAttribute {
    String name();

    static JALAttribute[] readAttributes(JALClassReader reader, JALConstantPoolEntry[] pool) {
        int count = reader.readUnsignedShort();
        JALAttribute[] attributes = new JALAttribute[count];
        for (int i = 0; i < count; i++) {
            int nameIdx = reader.readUnsignedShort();
            String name = JALClassReader.getFromConstants(
                    pool,
                    nameIdx,
                    entry -> entry instanceof JALConstantPoolEntry.Utf8Entry,
                    entry -> ((JALConstantPoolEntry.Utf8Entry) entry).value()
            );
            attributes[i] = switch (name) {
                case "ConstantValue" -> ConstantValueAttribute.read(reader, pool);
                case "Code" -> CodeAttribute.read(reader, pool);
                case "StackMapTable" -> StackMapTableAttribute.read(reader);
                case "Exceptions" -> ExceptionsAttribute.read(reader, pool);
                case "InnerClasses" -> InnerClassesAttribute.read(reader, pool);
                case "EnclosingMethod" -> EnclosingMethodAttribute.read(reader, pool);
                case "Synthetic" -> SyntheticAttribute.read(reader);
                case "Signature" -> SignatureAttribute.read(reader, pool);
                case "SourceFile" -> SourceFileAttribute.read(reader, pool);
                case "SourceDebugExtension" -> SourceDebugExtensionAttribute.read(reader);
                case "LineNumberTable" -> LineNumberTableAttribute.read(reader);
                case "LocalVariableTable" -> LocalVariableTableAttribute.read(reader, pool);
                case "LocalVariableTypeTable" -> LocalVariableTypeTableAttribute.read(reader, pool);
                case "Deprecated" -> DeprecatedAttribute.read(reader);
                case "RuntimeVisibleAnnotations" -> RuntimeVisibleAnnotationsAttribute.read(reader, pool);
                case "RuntimeInvisibleAnnotations" -> RuntimeInvisibleAnnotationsAttribute.read(reader, pool);
                case "RuntimeVisibleParameterAnnotations" -> RuntimeVisibleParameterAnnotationsAttribute.read(reader, pool);
                case "RuntimeInvisibleParameterAnnotations" -> RuntimeInvisibleParameterAnnotationsAttribute.read(reader, pool);
                case "RuntimeVisibleTypeAnnotations" -> RuntimeVisibleTypeAnnotationsAttribute.read(reader, pool);
                case "RuntimeInvisibleTypeAnnotations" -> RuntimeInvisibleTypeAnnotationsAttribute.read(reader, pool);
                case "AnnotationDefault" -> AnnotationDefaultAttribute.read(reader, pool);
                case "BootstrapMethods" -> BootstrapMethodsAttribute.read(reader, pool);
                case "MethodParameters" -> MethodParametersAttribute.read(reader, pool);
                case "Module" -> ModuleAttribute.read(reader, pool);
                case "ModulePackages" -> ModulePackagesAttribute.read(reader, pool);
                case "ModuleMainClass" -> ModuleMainClassAttribute.read(reader, pool);
                case "NestHost" -> NestHostAttribute.read(reader, pool);
                case "NestMembers" -> NestMembersAttribute.read(reader, pool);
                case "Record" -> RecordAttribute.read(reader, pool);
                case "PermittedSubclasses" -> PermittedSubclassesAttribute.read(reader, pool);
                default -> throw new IllegalStateException("Unknown attribute: " + name);
            };
        }
        return attributes;
    }

    private static int readLength(JALClassReader reader) {
        return reader.readInt();
    }

    private static void requireLength(String name, int expected, int actual) {
        if (expected != actual) {
            throw new IllegalStateException("Invalid " + name + " attribute length: " + actual + " (expected: " + expected + ")");
        }
    }

    private static String cpUtf8(JALConstantPoolEntry[] pool, int idx) {
        return JALClassReader.getFromConstants(
                pool, idx,
                entry -> entry instanceof JALConstantPoolEntry.Utf8Entry,
                entry -> ((JALConstantPoolEntry.Utf8Entry) entry).value()
        );
    }

    private static JALConstantPoolEntry.ClassEntry cpClass(JALConstantPoolEntry[] pool, int idx) {
        return JALClassReader.getFromConstants(
                pool, idx,
                entry -> entry instanceof JALConstantPoolEntry.ClassEntry,
                entry -> (JALConstantPoolEntry.ClassEntry) entry
        );
    }

    private static JALConstantPoolEntry.NameAndTypeEntry cpNameAndType(JALConstantPoolEntry[] pool, int idx) {
        return JALClassReader.getFromConstants(
                pool, idx,
                entry -> entry instanceof JALConstantPoolEntry.NameAndTypeEntry,
                entry -> (JALConstantPoolEntry.NameAndTypeEntry) entry
        );
    }

    record ConstantValueAttribute(String name, JALConstantPoolEntry constant) implements JALAttribute {
        public static ConstantValueAttribute read(JALClassReader reader, JALConstantPoolEntry[] pool) {
            int length = readLength(reader);
            requireLength("ConstantValue", 2, length);
            return new ConstantValueAttribute("ConstantValue", pool[reader.readUnsignedShort()]);
        }
    }

    record CodeAttribute(String name, int maxStack, int maxLocals, byte[] code, ExceptionHandler[] exceptionTable, JALAttribute[] attributes)
            implements JALAttribute {
        record ExceptionHandler(int startPc, int endPc, int handlerPc, JALConstantPoolEntry.ClassEntry catchType) {
        }

        public static CodeAttribute read(JALClassReader reader, JALConstantPoolEntry[] pool) {
            readLength(reader); // variable
            int maxStack = reader.readUnsignedShort();
            int maxLocals = reader.readUnsignedShort();
            byte[] code = reader.readBytes(reader.readInt());

            int exceptionTableLength = reader.readUnsignedShort();
            ExceptionHandler[] handlers = new ExceptionHandler[exceptionTableLength];
            for (int i = 0; i < exceptionTableLength; i++) {
                int startPc = reader.readUnsignedShort();
                int endPc = reader.readUnsignedShort();
                int handlerPc = reader.readUnsignedShort();
                int catchTypeIdx = reader.readUnsignedShort();
                JALConstantPoolEntry.ClassEntry catchType = catchTypeIdx == 0 ? null : cpClass(pool, catchTypeIdx);
                handlers[i] = new ExceptionHandler(startPc, endPc, handlerPc, catchType);
            }
            JALAttribute[] nested = readAttributes(reader, pool);
            return new CodeAttribute("Code", maxStack, maxLocals, code, handlers, nested);
        }

        @Nullable
        @SuppressWarnings("unchecked")
        public <T extends JALAttribute> T getAttribute(Class<T> attrClass) {
            for (JALAttribute attr : this.attributes) {
                if (attrClass.isInstance(attr)) {
                    return (T) attr;
                }
            }
            return null;
        }
    }

    record StackMapTableAttribute(String name, byte[] frames) implements JALAttribute {
        public static StackMapTableAttribute read(JALClassReader reader) {
            return new StackMapTableAttribute("StackMapTable", reader.readBytes(readLength(reader)));
        }
    }

    record ExceptionsAttribute(String name, JALConstantPoolEntry.ClassEntry[] exceptions) implements JALAttribute {
        public static ExceptionsAttribute read(JALClassReader reader, JALConstantPoolEntry[] pool) {
            int length = readLength(reader);
            int n = reader.readUnsignedShort();
            requireLength("Exceptions", 2 + 2 * n, length);
            JALConstantPoolEntry.ClassEntry[] ex = new JALConstantPoolEntry.ClassEntry[n];
            for (int i = 0; i < n; i++) ex[i] = cpClass(pool, reader.readUnsignedShort());
            return new ExceptionsAttribute("Exceptions", ex);
        }
    }

    record InnerClassesAttribute(String name, InnerClassInfo[] classes) implements JALAttribute {
        record InnerClassInfo(JALConstantPoolEntry.ClassEntry innerClass, JALConstantPoolEntry.ClassEntry outerClass, String innerName, int accessFlags) {
        }

        public static InnerClassesAttribute read(JALClassReader reader, JALConstantPoolEntry[] pool) {
            readLength(reader);
            int n = reader.readUnsignedShort();
            InnerClassInfo[] classes = new InnerClassInfo[n];
            for (int i = 0; i < n; i++) {
                int innerIdx = reader.readUnsignedShort();
                int outerIdx = reader.readUnsignedShort();
                int nameIdx = reader.readUnsignedShort();
                int accessFlags = reader.readUnsignedShort();
                classes[i] = new InnerClassInfo(
                        innerIdx == 0 ? null : cpClass(pool, innerIdx),
                        outerIdx == 0 ? null : cpClass(pool, outerIdx),
                        nameIdx == 0 ? null : cpUtf8(pool, nameIdx),
                        accessFlags
                );
            }
            return new InnerClassesAttribute("InnerClasses", classes);
        }
    }

    record EnclosingMethodAttribute(String name, JALConstantPoolEntry.ClassEntry owner, JALConstantPoolEntry.NameAndTypeEntry method)
            implements JALAttribute {
        public static EnclosingMethodAttribute read(JALClassReader reader, JALConstantPoolEntry[] pool) {
            int length = readLength(reader);
            requireLength("EnclosingMethod", 4, length);
            JALConstantPoolEntry.ClassEntry owner = cpClass(pool, reader.readUnsignedShort());
            int methodIdx = reader.readUnsignedShort();
            return new EnclosingMethodAttribute("EnclosingMethod", owner, methodIdx == 0 ? null : cpNameAndType(pool, methodIdx));
        }
    }

    record SyntheticAttribute(String name) implements JALAttribute {
        public static SyntheticAttribute read(JALClassReader reader) {
            requireLength("Synthetic", 0, readLength(reader));
            return new SyntheticAttribute("Synthetic");
        }
    }

    record SignatureAttribute(String name, String signature) implements JALAttribute {
        public static SignatureAttribute read(JALClassReader reader, JALConstantPoolEntry[] pool) {
            requireLength("Signature", 2, readLength(reader));
            return new SignatureAttribute("Signature", cpUtf8(pool, reader.readUnsignedShort()));
        }
    }

    record SourceFileAttribute(String name, String sourceFile) implements JALAttribute {
        public static SourceFileAttribute read(JALClassReader reader, JALConstantPoolEntry[] pool) {
            requireLength("SourceFile", 2, readLength(reader));
            return new SourceFileAttribute("SourceFile", cpUtf8(pool, reader.readUnsignedShort()));
        }
    }

    record SourceDebugExtensionAttribute(String name, String debugExtension) implements JALAttribute {
        public static SourceDebugExtensionAttribute read(JALClassReader reader) {
            int length = readLength(reader);
            return new SourceDebugExtensionAttribute("SourceDebugExtension", new String(reader.readBytes(length)));
        }
    }

    record LineNumberTableAttribute(String name, LineNumber[] lines) implements JALAttribute {
        public record LineNumber(int startPc, int lineNumber) {
        }

        public static LineNumberTableAttribute read(JALClassReader reader) {
            readLength(reader);
            int n = reader.readUnsignedShort();
            LineNumber[] lines = new LineNumber[n];
            for (int i = 0; i < n; i++) lines[i] = new LineNumber(reader.readUnsignedShort(), reader.readUnsignedShort());
            return new LineNumberTableAttribute("LineNumberTable", lines);
        }
    }

    record LocalVariableTableAttribute(String name, Entry[] variables) implements JALAttribute {
        record Entry(int startPc, int length, String name, String descriptor, int index) {
        }

        public static LocalVariableTableAttribute read(JALClassReader reader, JALConstantPoolEntry[] pool) {
            readLength(reader);
            int n = reader.readUnsignedShort();
            Entry[] entries = new Entry[n];
            for (int i = 0; i < n; i++) {
                entries[i] = new Entry(
                        reader.readUnsignedShort(),
                        reader.readUnsignedShort(),
                        cpUtf8(pool, reader.readUnsignedShort()),
                        cpUtf8(pool, reader.readUnsignedShort()),
                        reader.readUnsignedShort()
                );
            }
            return new LocalVariableTableAttribute("LocalVariableTable", entries);
        }
    }

    record LocalVariableTypeTableAttribute(String name, Entry[] variables) implements JALAttribute {
        record Entry(int startPc, int length, String name, String signature, int index) {
        }

        public static LocalVariableTypeTableAttribute read(JALClassReader reader, JALConstantPoolEntry[] pool) {
            readLength(reader);
            int n = reader.readUnsignedShort();
            Entry[] entries = new Entry[n];
            for (int i = 0; i < n; i++) {
                entries[i] = new Entry(
                        reader.readUnsignedShort(),
                        reader.readUnsignedShort(),
                        cpUtf8(pool, reader.readUnsignedShort()),
                        cpUtf8(pool, reader.readUnsignedShort()),
                        reader.readUnsignedShort()
                );
            }
            return new LocalVariableTypeTableAttribute("LocalVariableTypeTable", entries);
        }
    }

    record DeprecatedAttribute(String name) implements JALAttribute {
        public static DeprecatedAttribute read(JALClassReader reader) {
            requireLength("Deprecated", 0, readLength(reader));
            return new DeprecatedAttribute("Deprecated");
        }
    }

    record RuntimeVisibleAnnotationsAttribute(String name, Annotation[] annotations) implements JALAttribute {
        public static RuntimeVisibleAnnotationsAttribute read(JALClassReader reader, JALConstantPoolEntry[] pool) {
            readLength(reader);
            return new RuntimeVisibleAnnotationsAttribute("RuntimeVisibleAnnotations", readAnnotations(reader, pool));
        }
    }

    record RuntimeInvisibleAnnotationsAttribute(String name, Annotation[] annotations) implements JALAttribute {
        public static RuntimeInvisibleAnnotationsAttribute read(JALClassReader reader, JALConstantPoolEntry[] pool) {
            readLength(reader);
            return new RuntimeInvisibleAnnotationsAttribute("RuntimeInvisibleAnnotations", readAnnotations(reader, pool));
        }
    }

    record RuntimeVisibleParameterAnnotationsAttribute(String name, Annotation[][] parameterAnnotations) implements JALAttribute {
        public static RuntimeVisibleParameterAnnotationsAttribute read(JALClassReader reader, JALConstantPoolEntry[] pool) {
            readLength(reader);
            int num = reader.readUnsignedByte();
            Annotation[][] parameters = new Annotation[num][];
            for (int i = 0; i < num; i++) parameters[i] = readAnnotations(reader, pool);
            return new RuntimeVisibleParameterAnnotationsAttribute("RuntimeVisibleParameterAnnotations", parameters);
        }
    }

    record RuntimeInvisibleParameterAnnotationsAttribute(String name, Annotation[][] parameterAnnotations) implements JALAttribute {
        public static RuntimeInvisibleParameterAnnotationsAttribute read(JALClassReader reader, JALConstantPoolEntry[] pool) {
            readLength(reader);
            int num = reader.readUnsignedByte();
            Annotation[][] parameters = new Annotation[num][];
            for (int i = 0; i < num; i++) parameters[i] = readAnnotations(reader, pool);
            return new RuntimeInvisibleParameterAnnotationsAttribute("RuntimeInvisibleParameterAnnotations", parameters);
        }
    }

    record RuntimeVisibleTypeAnnotationsAttribute(String name, TypeAnnotation[] annotations) implements JALAttribute {
        public static RuntimeVisibleTypeAnnotationsAttribute read(JALClassReader reader, JALConstantPoolEntry[] pool) {
            readLength(reader);
            return new RuntimeVisibleTypeAnnotationsAttribute("RuntimeVisibleTypeAnnotations", readTypeAnnotations(reader, pool));
        }
    }

    record RuntimeInvisibleTypeAnnotationsAttribute(String name, TypeAnnotation[] annotations) implements JALAttribute {
        public static RuntimeInvisibleTypeAnnotationsAttribute read(JALClassReader reader, JALConstantPoolEntry[] pool) {
            readLength(reader);
            return new RuntimeInvisibleTypeAnnotationsAttribute("RuntimeInvisibleTypeAnnotations", readTypeAnnotations(reader, pool));
        }
    }

    record AnnotationDefaultAttribute(String name, ElementValue defaultValue) implements JALAttribute {
        public static AnnotationDefaultAttribute read(JALClassReader reader, JALConstantPoolEntry[] pool) {
            readLength(reader);
            return new AnnotationDefaultAttribute("AnnotationDefault", readElementValue(reader, pool));
        }
    }

    record BootstrapMethodsAttribute(String name, BootstrapMethod[] methods) implements JALAttribute {
        record BootstrapMethod(JALConstantPoolEntry.MethodHandleEntry bootstrapMethodRef, JALConstantPoolEntry[] arguments) {
        }

        public static BootstrapMethodsAttribute read(JALClassReader reader, JALConstantPoolEntry[] pool) {
            readLength(reader);
            int n = reader.readUnsignedShort();
            BootstrapMethod[] methods = new BootstrapMethod[n];
            for (int i = 0; i < n; i++) {
                JALConstantPoolEntry.MethodHandleEntry handle = JALClassReader.getFromConstants(
                        pool, reader.readUnsignedShort(),
                        e -> e instanceof JALConstantPoolEntry.MethodHandleEntry,
                        e -> (JALConstantPoolEntry.MethodHandleEntry) e
                );
                int argN = reader.readUnsignedShort();
                JALConstantPoolEntry[] args = new JALConstantPoolEntry[argN];
                for (int j = 0; j < argN; j++) args[j] = pool[reader.readUnsignedShort()];
                methods[i] = new BootstrapMethod(handle, args);
            }
            return new BootstrapMethodsAttribute("BootstrapMethods", methods);
        }
    }

    record MethodParametersAttribute(String name, MethodParameter[] parameters) implements JALAttribute {
        record MethodParameter(String name, int accessFlags) {
        }

        public static MethodParametersAttribute read(JALClassReader reader, JALConstantPoolEntry[] pool) {
            readLength(reader);
            int n = reader.readUnsignedByte();
            MethodParameter[] params = new MethodParameter[n];
            for (int i = 0; i < n; i++) {
                int nameIdx = reader.readUnsignedShort();
                params[i] = new MethodParameter(nameIdx == 0 ? null : cpUtf8(pool, nameIdx), reader.readUnsignedShort());
            }
            return new MethodParametersAttribute("MethodParameters", params);
        }
    }

    record ModuleAttribute(String name, String moduleName, int moduleFlags, String moduleVersion,
                           Requires[] requires, Exports[] exports, Opens[] opens, ClassReferenceType[] uses, Provides[] provides)
            implements JALAttribute {
        record Requires(String moduleName, int flags, String version) {
        }

        record Exports(String packageName, int flags, String[] toModules) {
        }

        record Opens(String packageName, int flags, String[] toModules) {
        }

        record Provides(ClassReferenceType serviceName, ClassReferenceType[] implNames) {
        }

        public static ModuleAttribute read(JALClassReader reader, JALConstantPoolEntry[] pool) {
            readLength(reader);
            String moduleName = JALClassReader.getFromConstants(pool, reader.readUnsignedShort(),
                    e -> e instanceof JALConstantPoolEntry.ModuleEntry, e -> ((JALConstantPoolEntry.ModuleEntry) e).name());
            int moduleFlags = reader.readUnsignedShort();
            int moduleVersionIdx = reader.readUnsignedShort();
            String moduleVersion = moduleVersionIdx == 0 ? null : cpUtf8(pool, moduleVersionIdx);

            int reqN = reader.readUnsignedShort();
            Requires[] requires = new Requires[reqN];
            for (int i = 0; i < reqN; i++) {
                String reqName = JALClassReader.getFromConstants(pool, reader.readUnsignedShort(),
                        e -> e instanceof JALConstantPoolEntry.ModuleEntry, e -> ((JALConstantPoolEntry.ModuleEntry) e).name());
                int flags = reader.readUnsignedShort();
                int versionIdx = reader.readUnsignedShort();
                requires[i] = new Requires(reqName, flags, versionIdx == 0 ? null : cpUtf8(pool, versionIdx));
            }

            int expN = reader.readUnsignedShort();
            Exports[] exports = new Exports[expN];
            for (int i = 0; i < expN; i++) {
                String packageName = JALClassReader.getFromConstants(pool, reader.readUnsignedShort(),
                        e -> e instanceof JALConstantPoolEntry.PackageEntry, e -> ((JALConstantPoolEntry.PackageEntry) e).name());
                int flags = reader.readUnsignedShort();
                int toN = reader.readUnsignedShort();
                String[] toModules = new String[toN];
                for (int j = 0; j < toN; j++) {
                    toModules[j] = JALClassReader.getFromConstants(pool, reader.readUnsignedShort(),
                            e -> e instanceof JALConstantPoolEntry.ModuleEntry, e -> ((JALConstantPoolEntry.ModuleEntry) e).name());
                }
                exports[i] = new Exports(packageName, flags, toModules);
            }

            int openN = reader.readUnsignedShort();
            Opens[] opens = new Opens[openN];
            for (int i = 0; i < openN; i++) {
                String packageName = JALClassReader.getFromConstants(pool, reader.readUnsignedShort(),
                        e -> e instanceof JALConstantPoolEntry.PackageEntry, e -> ((JALConstantPoolEntry.PackageEntry) e).name());
                int flags = reader.readUnsignedShort();
                int toN = reader.readUnsignedShort();
                String[] toModules = new String[toN];
                for (int j = 0; j < toN; j++) {
                    toModules[j] = JALClassReader.getFromConstants(pool, reader.readUnsignedShort(),
                            e -> e instanceof JALConstantPoolEntry.ModuleEntry, e -> ((JALConstantPoolEntry.ModuleEntry) e).name());
                }
                opens[i] = new Opens(packageName, flags, toModules);
            }

            int usesN = reader.readUnsignedShort();
            ClassReferenceType[] uses = new ClassReferenceType[usesN];
            for (int i = 0; i < usesN; i++) uses[i] = cpClass(pool, reader.readUnsignedShort()).name();

            int providesN = reader.readUnsignedShort();
            Provides[] provides = new Provides[providesN];
            for (int i = 0; i < providesN; i++) {
                ClassReferenceType service = cpClass(pool, reader.readUnsignedShort()).name();
                int implN = reader.readUnsignedShort();
                ClassReferenceType[] impls = new ClassReferenceType[implN];
                for (int j = 0; j < implN; j++) impls[j] = cpClass(pool, reader.readUnsignedShort()).name();
                provides[i] = new Provides(service, impls);
            }
            return new ModuleAttribute("Module", moduleName, moduleFlags, moduleVersion, requires, exports, opens, uses, provides);
        }
    }

    record ModulePackagesAttribute(String name, String[] packages) implements JALAttribute {
        public static ModulePackagesAttribute read(JALClassReader reader, JALConstantPoolEntry[] pool) {
            readLength(reader);
            int n = reader.readUnsignedShort();
            String[] packages = new String[n];
            for (int i = 0; i < n; i++) {
                packages[i] = JALClassReader.getFromConstants(pool, reader.readUnsignedShort(),
                        e -> e instanceof JALConstantPoolEntry.PackageEntry, e -> ((JALConstantPoolEntry.PackageEntry) e).name());
            }
            return new ModulePackagesAttribute("ModulePackages", packages);
        }
    }

    record ModuleMainClassAttribute(String name, JALConstantPoolEntry.ClassEntry mainClass) implements JALAttribute {
        public static ModuleMainClassAttribute read(JALClassReader reader, JALConstantPoolEntry[] pool) {
            requireLength("ModuleMainClass", 2, readLength(reader));
            return new ModuleMainClassAttribute("ModuleMainClass", cpClass(pool, reader.readUnsignedShort()));
        }
    }

    record NestHostAttribute(String name, JALConstantPoolEntry.ClassEntry hostClass) implements JALAttribute {
        public static NestHostAttribute read(JALClassReader reader, JALConstantPoolEntry[] pool) {
            requireLength("NestHost", 2, readLength(reader));
            return new NestHostAttribute("NestHost", cpClass(pool, reader.readUnsignedShort()));
        }
    }

    record NestMembersAttribute(String name, JALConstantPoolEntry.ClassEntry[] classes) implements JALAttribute {
        public static NestMembersAttribute read(JALClassReader reader, JALConstantPoolEntry[] pool) {
            readLength(reader);
            int n = reader.readUnsignedShort();
            JALConstantPoolEntry.ClassEntry[] classes = new JALConstantPoolEntry.ClassEntry[n];
            for (int i = 0; i < n; i++) classes[i] = cpClass(pool, reader.readUnsignedShort());
            return new NestMembersAttribute("NestMembers", classes);
        }
    }

    record RecordAttribute(String name, RecordComponent[] components) implements JALAttribute {
        record RecordComponent(String componentName, String descriptor, JALAttribute[] attributes) {
        }

        public static RecordAttribute read(JALClassReader reader, JALConstantPoolEntry[] pool) {
            readLength(reader);
            int n = reader.readUnsignedShort();
            RecordComponent[] components = new RecordComponent[n];
            for (int i = 0; i < n; i++) {
                String componentName = cpUtf8(pool, reader.readUnsignedShort());
                String descriptor = cpUtf8(pool, reader.readUnsignedShort());
                JALAttribute[] attrs = readAttributes(reader, pool);
                components[i] = new RecordComponent(componentName, descriptor, attrs);
            }
            return new RecordAttribute("Record", components);
        }
    }

    record PermittedSubclassesAttribute(String name, JALConstantPoolEntry.ClassEntry[] classes) implements JALAttribute {
        public static PermittedSubclassesAttribute read(JALClassReader reader, JALConstantPoolEntry[] pool) {
            readLength(reader);
            int n = reader.readUnsignedShort();
            JALConstantPoolEntry.ClassEntry[] classes = new JALConstantPoolEntry.ClassEntry[n];
            for (int i = 0; i < n; i++) classes[i] = cpClass(pool, reader.readUnsignedShort());
            return new PermittedSubclassesAttribute("PermittedSubclasses", classes);
        }
    }

    record Annotation(String typeDescriptor, ElementNameValuePair[] pairs) {
    }

    record ElementNameValuePair(String elementName, ElementValue value) {
    }

    sealed interface ElementValue permits ConstElementValue, EnumElementValue, ClassElementValue, AnnotationElementValue, ArrayElementValue {
        char tag();
    }

    record ConstElementValue(char tag, JALConstantPoolEntry value) implements ElementValue {
    }

    record EnumElementValue(char tag, String typeName, String constName) implements ElementValue {
    }

    record ClassElementValue(char tag, String classInfo) implements ElementValue {
    }

    record AnnotationElementValue(char tag, Annotation annotation) implements ElementValue {
    }

    record ArrayElementValue(char tag, ElementValue[] values) implements ElementValue {
    }

    record TypeAnnotation(int targetType, TargetInfo targetInfo, TypePathEntry[] typePath, Annotation annotation) {
    }

    sealed interface TargetInfo permits EmptyTarget, TypeParameterTarget, SupertypeTarget, TypeParameterBoundTarget, FormalParameterTarget,
            ThrowsTarget, LocalVarTarget, CatchTarget, OffsetTarget, TypeArgumentTarget {
    }

    record EmptyTarget() implements TargetInfo {
    }

    record TypeParameterTarget(int typeParameterIndex) implements TargetInfo {
    }

    record SupertypeTarget(int supertypeIndex) implements TargetInfo {
    }

    record TypeParameterBoundTarget(int typeParameterIndex, int boundIndex) implements TargetInfo {
    }

    record FormalParameterTarget(int formalParameterIndex) implements TargetInfo {
    }

    record ThrowsTarget(int throwsTypeIndex) implements TargetInfo {
    }

    record LocalVarTarget(LocalVarTableEntry[] table) implements TargetInfo {
    }

    record LocalVarTableEntry(int startPc, int length, int index) {
    }

    record CatchTarget(int exceptionTableIndex) implements TargetInfo {
    }

    record OffsetTarget(int offset) implements TargetInfo {
    }

    record TypeArgumentTarget(int offset, int typeArgumentIndex) implements TargetInfo {
    }

    record TypePathEntry(int kind, int argumentIndex) {
    }

    private static Annotation[] readAnnotations(JALClassReader reader, JALConstantPoolEntry[] pool) {
        int n = reader.readUnsignedShort();
        Annotation[] annotations = new Annotation[n];
        for (int i = 0; i < n; i++) annotations[i] = readAnnotation(reader, pool);
        return annotations;
    }

    private static Annotation readAnnotation(JALClassReader reader, JALConstantPoolEntry[] pool) {
        String type = cpUtf8(pool, reader.readUnsignedShort());
        int n = reader.readUnsignedShort();
        ElementNameValuePair[] pairs = new ElementNameValuePair[n];
        for (int i = 0; i < n; i++) {
            String name = cpUtf8(pool, reader.readUnsignedShort());
            pairs[i] = new ElementNameValuePair(name, readElementValue(reader, pool));
        }
        return new Annotation(type, pairs);
    }

    private static ElementValue readElementValue(JALClassReader reader, JALConstantPoolEntry[] pool) {
        char tag = (char) reader.readUnsignedByte();
        return switch (tag) {
            case 'B', 'C', 'D', 'F', 'I', 'J', 'S', 'Z', 's' -> new ConstElementValue(tag, pool[reader.readUnsignedShort()]);
            case 'e' -> new EnumElementValue(tag, cpUtf8(pool, reader.readUnsignedShort()), cpUtf8(pool, reader.readUnsignedShort()));
            case 'c' -> new ClassElementValue(tag, cpUtf8(pool, reader.readUnsignedShort()));
            case '@' -> new AnnotationElementValue(tag, readAnnotation(reader, pool));
            case '[' -> {
                int n = reader.readUnsignedShort();
                ElementValue[] values = new ElementValue[n];
                for (int i = 0; i < n; i++) values[i] = readElementValue(reader, pool);
                yield new ArrayElementValue(tag, values);
            }
            default -> throw new IllegalStateException("Unknown element value tag: " + tag);
        };
    }

    private static TypeAnnotation[] readTypeAnnotations(JALClassReader reader, JALConstantPoolEntry[] pool) {
        int n = reader.readUnsignedShort();
        TypeAnnotation[] annotations = new TypeAnnotation[n];
        for (int i = 0; i < n; i++) {
            int targetType = reader.readUnsignedByte();
            TargetInfo target = readTargetInfo(reader, targetType);
            int pathLength = reader.readUnsignedByte();
            TypePathEntry[] path = new TypePathEntry[pathLength];
            for (int j = 0; j < pathLength; j++) {
                path[j] = new TypePathEntry(reader.readUnsignedByte(), reader.readUnsignedByte());
            }
            annotations[i] = new TypeAnnotation(targetType, target, path, readAnnotation(reader, pool));
        }
        return annotations;
    }

    private static TargetInfo readTargetInfo(JALClassReader reader, int targetType) {
        return switch (targetType) {
            case 0x00, 0x01 -> new TypeParameterTarget(reader.readUnsignedByte());
            case 0x10 -> new SupertypeTarget(reader.readUnsignedShort());
            case 0x11, 0x12 -> new TypeParameterBoundTarget(reader.readUnsignedByte(), reader.readUnsignedByte());
            case 0x13, 0x14, 0x15 -> new EmptyTarget();
            case 0x16 -> new FormalParameterTarget(reader.readUnsignedByte());
            case 0x17 -> new ThrowsTarget(reader.readUnsignedShort());
            case 0x40, 0x41 -> {
                int tableLength = reader.readUnsignedShort();
                LocalVarTableEntry[] table = new LocalVarTableEntry[tableLength];
                for (int i = 0; i < tableLength; i++) {
                    table[i] = new LocalVarTableEntry(reader.readUnsignedShort(), reader.readUnsignedShort(), reader.readUnsignedShort());
                }
                yield new LocalVarTarget(table);
            }
            case 0x42 -> new CatchTarget(reader.readUnsignedShort());
            case 0x43, 0x44, 0x45, 0x46 -> new OffsetTarget(reader.readUnsignedShort());
            case 0x47, 0x48, 0x49, 0x4A, 0x4B -> new TypeArgumentTarget(reader.readUnsignedShort(), reader.readUnsignedByte());
            default -> throw new IllegalStateException("Unknown type annotation target_type: " + targetType);
        };
    }
}
