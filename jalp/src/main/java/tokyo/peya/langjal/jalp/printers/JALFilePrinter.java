package tokyo.peya.langjal.jalp.printers;

import tokyo.peya.langjal.jalp.ClassInfo;
import tokyo.peya.langjal.jalp.JALClassFinder;
import tokyo.peya.langjal.jalp.JALPOptions;
import tokyo.peya.langjal.jalp.OutputFormatter;
import tokyo.peya.langjal.jalp.reader.JALAttribute;
import tokyo.peya.langjal.jalp.reader.JALClass;
import tokyo.peya.langjal.jalp.reader.JALClassReader;
import tokyo.peya.langjal.jalp.reader.JALConstantPoolEntry;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class JALFilePrinter {
    private static final DateTimeFormatter LAST_MODIFIED_FORMATTER =
            DateTimeFormatter.ofPattern("d MMM uuuu", Locale.ENGLISH);

    private final String classpath;
    private final int flags;

    private final OutputFormatter outputs;
    private final ClassPrinter processor;

    public JALFilePrinter(String classpath, int flags) {
        this(new OutputFormatter(), classpath, flags);
    }

    public JALFilePrinter(OutputFormatter formatter, String classpath, int flags) {
        this.outputs = formatter;
        this.classpath = classpath;
        this.flags = flags;

        this.processor = new ClassPrinter(this.outputs, this.flags);
    }

    public void process(String input) {
        ClassInfo classInfo = JALClassFinder.findClass(input, this.classpath);
        if (classInfo.bytes().length == 0) {
            throw new IllegalArgumentException("Class file is empty: " + input);
        }

        JALClass clazz = JALClassReader.read(classInfo.bytes());

        if (JALPOptions.is(this.flags, JALPOptions.SHOW_HEADER)) {
            this.printHeader(classInfo, clazz);
        }
        if (JALPOptions.is(this.flags, JALPOptions.SHOW_CONSTANTS)) {
            this.printConstants(clazz);
        }
        this.processor.process(clazz);
    }

    private void printHeader(ClassInfo classInfo, JALClass clazz) {
        String modified = LAST_MODIFIED_FORMATTER.format(
                classInfo.lastModified().atZone(ZoneId.systemDefault())
        );
        this.outputs.println("/*");

        OutputFormatter comments = new OutputFormatter(this.outputs);

        comments.println("Decompiled by JALP (Java Assembly Language Parser)")
                .println("Class: " + classInfo.classFile().toAbsolutePath())
                .println("SHA-256 checksum: " + classInfo.sha256())
                .println("Last modified: " + modified);

        // コンパイルファイルがある場合は:
        JALAttribute.SourceFileAttribute sourceFileAttr = clazz.getAttribute("SourceFile");
        if (sourceFileAttr != null) {
            comments.println("Compiled from \"" + sourceFileAttr.sourceFile() + "\"");
        }

        this.outputs.println("*/");
    }

    private void printConstants(JALClass clazz) {
        this.outputs.println("Constant pool:");
        OutputFormatter constantsOut = new OutputFormatter(this.outputs);
        JALConstantPoolEntry[] constants = clazz.constants();
        for (int i = 1; i < constants.length; i++) {
            JALConstantPoolEntry entry = constants[i];
            if (entry == null) {
                continue;
            }
            constantsOut.println("#" + i + " = " + formatConstant(entry));
        }
    }

    private static String formatConstant(JALConstantPoolEntry entry) {
        return switch (entry) {
            case JALConstantPoolEntry.Utf8Entry utf8 -> "Utf8 " + utf8.value();
            case JALConstantPoolEntry.ClassEntry classEntry -> "Class " + classEntry.name().getInternalName();
            case JALConstantPoolEntry.StringEntry stringEntry -> "String \"" + stringEntry.value() + "\"";
            case JALConstantPoolEntry.IntegerEntry integerEntry -> "Integer " + integerEntry.value();
            case JALConstantPoolEntry.FloatEntry floatEntry -> "Float " + floatEntry.value();
            case JALConstantPoolEntry.LongEntry longEntry -> "Long " + longEntry.value();
            case JALConstantPoolEntry.DoubleEntry doubleEntry -> "Double " + doubleEntry.value();
            case JALConstantPoolEntry.NameAndTypeEntry nameAndType ->
                    "NameAndType " + nameAndType.name() + ":" + nameAndType.descriptor();
            case JALConstantPoolEntry.FieldEntry fieldEntry ->
                    "Field " + fieldEntry.owner().name().getInternalName() + "."
                            + fieldEntry.nameAndType().name() + ":" + fieldEntry.nameAndType().descriptor();
            case JALConstantPoolEntry.MethodEntry methodEntry ->
                    "Method " + methodEntry.owner().name().getInternalName() + "."
                            + methodEntry.nameAndType().name() + methodEntry.nameAndType().descriptor();
            case JALConstantPoolEntry.InterfaceMethodEntry methodEntry ->
                    "InterfaceMethod " + methodEntry.owner().name().getInternalName() + "."
                            + methodEntry.nameAndType().name() + methodEntry.nameAndType().descriptor();
            case JALConstantPoolEntry.MethodHandleEntry handleEntry ->
                    "MethodHandle " + handleEntry.referenceKind() + " " + formatConstant(handleEntry.reference());
            case JALConstantPoolEntry.MethodTypeEntry methodType -> "MethodType " + methodType.descriptor();
            case JALConstantPoolEntry.DynamicEntry dynamicEntry ->
                    "Dynamic " + dynamicEntry.bootstrapMethodAttrIndex() + " "
                            + dynamicEntry.nameAndType().name() + dynamicEntry.nameAndType().descriptor();
            case JALConstantPoolEntry.InvokeDynamicEntry dynamicEntry ->
                    "InvokeDynamic " + dynamicEntry.bootstrapMethodAttrIndex() + " "
                            + dynamicEntry.nameAndType().name() + dynamicEntry.nameAndType().descriptor();
            case JALConstantPoolEntry.ModuleEntry moduleEntry -> "Module " + moduleEntry.name();
            case JALConstantPoolEntry.PackageEntry packageEntry -> "Package " + packageEntry.name();
            case JALConstantPoolEntry.UnresolvedConstantPoolEntry unresolved ->
                    "Unresolved " + unresolved.getClass().getSimpleName();
        };
    }
}
