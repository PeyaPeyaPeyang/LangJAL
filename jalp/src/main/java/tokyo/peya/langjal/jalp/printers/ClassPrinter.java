package tokyo.peya.langjal.jalp.printers;

import tokyo.peya.langjal.compiler.jvm.ClassReferenceType;
import tokyo.peya.langjal.jalp.OutputChain;
import tokyo.peya.langjal.jalp.OutputFormatter;
import tokyo.peya.langjal.jalp.reader.JALClass;

public class ClassPrinter {
    private final OutputFormatter out;
    private final OutputFormatter innerOut;
    private final int flags;

    public ClassPrinter(OutputFormatter out, int flags) {
        this.out = out;
        this.innerOut = new OutputFormatter(out);
        this.flags = flags;
    }

    public void process(JALClass clazz) {
        if (PrinterUtils.shouldSkip(this.flags, clazz.access())) {
            return;
        }

        this.processClassMaster(clazz);

        MethodPrinter methodPrinter = new MethodPrinter(this.innerOut, this.flags);
        methodPrinter.printMethods(clazz);

        this.out.println("}");
    }

    private void processClassMaster(JALClass clazz) {
        PrinterUtils.printAccess(this.out, clazz.access(), clazz.accessAttrs())
                .output("class ")
                .output(clazz.thisName().getInternalName())
                .println();

        // attr の出力
        this.out.println(" ( ");
        OutputFormatter attrOut = new OutputFormatter(this.out);
        if (clazz.superName() != ClassReferenceType.OBJECT) {
            attrOut.chained()
                    .output("super =\"")
                    .output(clazz.superName().getInternalName())
                    .output("\",")
                    .println();
        }
        attrOut.chained()
                .output("major_version = ")
                .output(String.valueOf(clazz.majorVersion()))
                .output(",")
                .println();
        attrOut.chained()
                .output("minor_version = ")
                .output(String.valueOf(clazz.minorVersion()))
                .println();
        this.out.println(") {");
    }

}
