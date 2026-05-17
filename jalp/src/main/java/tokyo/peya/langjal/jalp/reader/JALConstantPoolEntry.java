package tokyo.peya.langjal.jalp.reader;

import tokyo.peya.langjal.compiler.jvm.ClassReferenceType;
import tokyo.peya.langjal.compiler.jvm.MethodDescriptor;

public sealed interface JALConstantPoolEntry {
    non-sealed interface UnresolvedConstantPoolEntry extends JALConstantPoolEntry {
        JALConstantPoolEntry resolve(JALConstantPoolEntry[] constantPool);

        static JALConstantPoolEntry resolveEntry(JALConstantPoolEntry[] constantPool, int index) {
            JALConstantPoolEntry entry = constantPool[index];
            if (entry instanceof UnresolvedConstantPoolEntry unresolved) {
                JALConstantPoolEntry resolved = unresolved.resolve(constantPool);
                constantPool[index] = resolved;
                return resolved;
            }
            return entry;
        }

        static Utf8Entry resolveUtf8(JALConstantPoolEntry[] constantPool, int index) {
            return (Utf8Entry) resolveEntry(constantPool, index);
        }

        static ClassEntry resolveClass(JALConstantPoolEntry[] constantPool, int index) {
            return (ClassEntry) resolveEntry(constantPool, index);
        }

        static NameAndTypeEntry resolveNameAndType(JALConstantPoolEntry[] constantPool, int index) {
            return (NameAndTypeEntry) resolveEntry(constantPool, index);
        }
    }

    static JALConstantPoolEntry read(JALClassReader input) {
        int tag = input.readUnsignedByte();
        // 以下，仕様書順
        return switch (tag) {
            case 7 -> ClassEntry.Unresolved.read(input);
            case 9 -> FieldEntry.Unresolved.read(input);
            case 10 -> MethodEntry.Unresolved.read(input);
            case 11 -> InterfaceMethodEntry.Unresolved.read(input);
            case 8 -> StringEntry.Unresolved.read(input);
            case 3 -> IntegerEntry.read(input);
            case 4 -> FloatEntry.read(input);
            case 5 -> LongEntry.read(input);
            case 6 -> DoubleEntry.read(input);
            case 12 -> NameAndTypeEntry.Unresolved.read(input);
            case 1 -> Utf8Entry.read(input);
            case 15 -> MethodHandleEntry.Unresolved.read(input);
            case 16 -> MethodTypeEntry.Unresolved.read(input);
            case 17 -> DynamicEntry.Unresolved.read(input);
            case 18 -> InvokeDynamicEntry.Unresolved.read(input);
            case 19 -> ModuleEntry.Unresolved.read(input);
            case 20 -> PackageEntry.Unresolved.read(input);
            default -> throw new IllegalArgumentException("Invalid constant pool tag: " + tag);
        };
    }

    // Tag: 7
    record ClassEntry(ClassReferenceType name) implements JALConstantPoolEntry {
        record Unresolved(int nameIndex) implements UnresolvedConstantPoolEntry {
            public static Unresolved read(JALClassReader input) {
                int nameIndex = input.readUnsignedShort();
                return new Unresolved(nameIndex);
            }

            @Override
            public ClassEntry resolve(JALConstantPoolEntry[] constantPool) {
                return new ClassEntry(
                        ClassReferenceType.parse(UnresolvedConstantPoolEntry.resolveUtf8(constantPool, this.nameIndex).value())
                );
            }
        }
    }

    // Tag: 9
    record FieldEntry(ClassEntry owner, NameAndTypeEntry nameAndType) implements JALConstantPoolEntry {
        record Unresolved(int classIndex, int nameAndTypeIndex) implements UnresolvedConstantPoolEntry {
            public static Unresolved read(JALClassReader input) {
                int classIndex = input.readUnsignedShort();
                int nameAndTypeIndex = input.readUnsignedShort();
                return new Unresolved(classIndex, nameAndTypeIndex);
            }

            @Override
            public FieldEntry resolve(JALConstantPoolEntry[] constantPool) {
                return new FieldEntry(
                        UnresolvedConstantPoolEntry.resolveClass(constantPool, this.classIndex),
                        UnresolvedConstantPoolEntry.resolveNameAndType(constantPool, this.nameAndTypeIndex));
            }
        }
    }

    // Tag: 10
    record MethodEntry(ClassEntry owner, NameAndTypeEntry nameAndType) implements JALConstantPoolEntry {
        record Unresolved(int classIndex, int nameAndTypeIndex) implements UnresolvedConstantPoolEntry {
            public static Unresolved read(JALClassReader input) {
                int classIndex = input.readUnsignedShort();
                int nameAndTypeIndex = input.readUnsignedShort();
                return new Unresolved(classIndex, nameAndTypeIndex);
            }

            @Override
            public MethodEntry resolve(JALConstantPoolEntry[] constantPool) {
                return new MethodEntry(
                        UnresolvedConstantPoolEntry.resolveClass(constantPool, this.classIndex),
                        UnresolvedConstantPoolEntry.resolveNameAndType(constantPool, this.nameAndTypeIndex));
            }
        }
    }

    // Tag: 11
    record InterfaceMethodEntry(ClassEntry owner, NameAndTypeEntry nameAndType) implements JALConstantPoolEntry {
        record Unresolved(int classIndex, int nameAndTypeIndex) implements UnresolvedConstantPoolEntry {
            public static Unresolved read(JALClassReader input) {
                int classIndex = input.readUnsignedShort();
                int nameAndTypeIndex = input.readUnsignedShort();
                return new Unresolved(classIndex, nameAndTypeIndex);
            }

            @Override
            public InterfaceMethodEntry resolve(JALConstantPoolEntry[] constantPool) {
                return new InterfaceMethodEntry(
                        UnresolvedConstantPoolEntry.resolveClass(constantPool, this.classIndex),
                        UnresolvedConstantPoolEntry.resolveNameAndType(constantPool, this.nameAndTypeIndex));
            }
        }
    }

    // Tag: 8
    record StringEntry(String value) implements JALConstantPoolEntry {
        record Unresolved(int stringIndex) implements UnresolvedConstantPoolEntry {
            public static Unresolved read(JALClassReader input) {
                int stringIndex = input.readUnsignedShort();
                return new Unresolved(stringIndex);
            }

            @Override
            public StringEntry resolve(JALConstantPoolEntry[] constantPool) {
                return new StringEntry(UnresolvedConstantPoolEntry.resolveUtf8(constantPool, this.stringIndex).value());
            }
        }
    }

    // Tag: 3
    record IntegerEntry(int value) implements JALConstantPoolEntry {
        public static IntegerEntry read(JALClassReader input) {
            int value = input.readInt();
            return new IntegerEntry(value);
        }
    }

    // Tag: 4
    record FloatEntry(float value) implements JALConstantPoolEntry {
        public static FloatEntry read(JALClassReader input) {
            float value = input.readFloat();
            return new FloatEntry(value);
        }
    }

    // Tag: 5
    record LongEntry(long value) implements JALConstantPoolEntry {
        public static LongEntry read(JALClassReader input) {
            long value = input.readLong();
            return new LongEntry(value);
        }
    }

    // Tag: 6
    record DoubleEntry(double value) implements JALConstantPoolEntry {
        public static DoubleEntry read(JALClassReader input) {
            double value = input.readDouble();
            return new DoubleEntry(value);
        }
    }

    // Tag: 12
    record NameAndTypeEntry(String name, String descriptor) implements JALConstantPoolEntry {
        record Unresolved(int nameIndex, int descriptorIndex) implements UnresolvedConstantPoolEntry {
            public static Unresolved read(JALClassReader input) {
                int nameIndex = input.readUnsignedShort();
                int descriptorIndex = input.readUnsignedShort();
                return new Unresolved(nameIndex, descriptorIndex);
            }

            @Override
            public NameAndTypeEntry resolve(JALConstantPoolEntry[] constantPool) {
                return new NameAndTypeEntry(
                        UnresolvedConstantPoolEntry.resolveUtf8(constantPool, this.nameIndex).value(),
                        UnresolvedConstantPoolEntry.resolveUtf8(constantPool, this.descriptorIndex).value());
            }
        }
    }

    // Tag: 1
    record Utf8Entry(String value) implements JALConstantPoolEntry {
        public static Utf8Entry read(JALClassReader input) {
            String value = input.readUTF8();
            return new Utf8Entry(value);
        }
    }

    // Tag: 15
    record MethodHandleEntry(int referenceKind, JALConstantPoolEntry reference) implements JALConstantPoolEntry {
        record Unresolved(int referenceKind, int referenceIndex) implements UnresolvedConstantPoolEntry {
            public static Unresolved read(JALClassReader input) {
                int referenceKind = input.readUnsignedByte();
                int referenceIndex = input.readUnsignedShort();
                return new Unresolved(referenceKind, referenceIndex);
            }

            @Override
            public MethodHandleEntry resolve(JALConstantPoolEntry[] constantPool) {
                return new MethodHandleEntry(
                        this.referenceKind,
                        UnresolvedConstantPoolEntry.resolveEntry(constantPool, this.referenceIndex));
            }
        }
    }

    // Tag: 16
    record MethodTypeEntry(MethodDescriptor descriptor) implements JALConstantPoolEntry {
        record Unresolved(int descriptorIndex) implements UnresolvedConstantPoolEntry {
            public static Unresolved read(JALClassReader input) {
                int descriptorIndex = input.readUnsignedShort();
                return new Unresolved(descriptorIndex);
            }

            @Override
            public MethodTypeEntry resolve(JALConstantPoolEntry[] constantPool) {
                return new MethodTypeEntry(
                        MethodDescriptor.parse(UnresolvedConstantPoolEntry.resolveUtf8(constantPool, this.descriptorIndex).value())
                );
            }
        }
    }

    // Tag: 17
    record DynamicEntry(int bootstrapMethodAttrIndex, NameAndTypeEntry nameAndType) implements JALConstantPoolEntry {
        record Unresolved(int bootstrapMethodAttrIndex, int nameAndTypeIndex) implements UnresolvedConstantPoolEntry {
            public static Unresolved read(JALClassReader input) {
                int bootstrapMethodAttrIndex = input.readUnsignedShort();
                int nameAndTypeIndex = input.readUnsignedShort();
                return new Unresolved(bootstrapMethodAttrIndex, nameAndTypeIndex);
            }

            @Override
            public DynamicEntry resolve(JALConstantPoolEntry[] constantPool) {
                return new DynamicEntry(
                        this.bootstrapMethodAttrIndex,
                        UnresolvedConstantPoolEntry.resolveNameAndType(constantPool, this.nameAndTypeIndex));
            }
        }
    }

    // Tag: 18
    record InvokeDynamicEntry(int bootstrapMethodAttrIndex, NameAndTypeEntry nameAndType)
            implements JALConstantPoolEntry {
        record Unresolved(int bootstrapMethodAttrIndex, int nameAndTypeIndex) implements UnresolvedConstantPoolEntry {
            public static Unresolved read(JALClassReader input) {
                int bootstrapMethodAttrIndex = input.readUnsignedShort();
                int nameAndTypeIndex = input.readUnsignedShort();
                return new Unresolved(bootstrapMethodAttrIndex, nameAndTypeIndex);
            }

            @Override
            public InvokeDynamicEntry resolve(JALConstantPoolEntry[] constantPool) {
                return new InvokeDynamicEntry(
                        this.bootstrapMethodAttrIndex,
                        UnresolvedConstantPoolEntry.resolveNameAndType(constantPool, this.nameAndTypeIndex));
            }
        }
    }

    // Tag: 19
    record ModuleEntry(String name) implements JALConstantPoolEntry {
        record Unresolved(int nameIndex) implements UnresolvedConstantPoolEntry {
            public static Unresolved read(JALClassReader input) {
                int nameIndex = input.readUnsignedShort();
                return new Unresolved(nameIndex);
            }

            @Override
            public ModuleEntry resolve(JALConstantPoolEntry[] constantPool) {
                return new ModuleEntry(UnresolvedConstantPoolEntry.resolveUtf8(constantPool, this.nameIndex).value());
            }
        }
    }

    // Tag: 20
    record PackageEntry(String name) implements JALConstantPoolEntry {
        record Unresolved(int nameIndex) implements UnresolvedConstantPoolEntry {
            public static Unresolved read(JALClassReader input) {
                int nameIndex = input.readUnsignedShort();
                return new Unresolved(nameIndex);
            }

            @Override
            public PackageEntry resolve(JALConstantPoolEntry[] constantPool) {
                return new PackageEntry(UnresolvedConstantPoolEntry.resolveUtf8(constantPool, this.nameIndex).value());
            }
        }
    }
}
