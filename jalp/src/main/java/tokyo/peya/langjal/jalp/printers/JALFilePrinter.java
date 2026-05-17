package tokyo.peya.langjal.jalp.printers;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import tokyo.peya.langjal.jalp.ClassInfo;
import tokyo.peya.langjal.jalp.JALClassFinder;
import tokyo.peya.langjal.jalp.OutputFormatter;
import tokyo.peya.langjal.jalp.reader.JALAttribute;
import tokyo.peya.langjal.jalp.reader.JALClass;
import tokyo.peya.langjal.jalp.reader.JALClassReader;

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
        this.classpath = classpath;
        this.flags = flags;

        this.outputs = new OutputFormatter();
        this.processor = new ClassPrinter(this.outputs, this.flags);
    }

    public void process(String input) {
        ClassInfo classInfo = JALClassFinder.findClass(input, this.classpath);
        if (classInfo.bytes().length == 0) {
            throw new IllegalArgumentException("Class file is empty: " + input);
        }

        JALClass clazz = JALClassReader.read(classInfo.bytes());

        this.printHeader(classInfo, clazz);
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
}
