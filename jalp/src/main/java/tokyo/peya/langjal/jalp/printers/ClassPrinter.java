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
                .print();


        // attr の出力
        this.out.println(" ( ");
        OutputFormatter attrOut = new OutputFormatter(this.out);
        if (clazz.superName() != ClassReferenceType.OBJECT) {
            attrOut.chained()
                    .output("super_class =\"")
                    .output(clazz.superName().getInternalName())
                    .output("\",")
                    .println();
        }
        attrOut.chained()
                .output("major_version = ")
                .output(String.valueOf(clazz.majorVersion()))
                .output(",")
                .output("  // Java " + majorToJavaVersion(clazz.majorVersion()))
                .println();
        attrOut.chained()
                .output("minor_version = ")
                .output(String.valueOf(clazz.minorVersion()))
                .println();

        // interfaces
        if (clazz.interfaces().length > 0) {
            attrOut.print("interfaces = [");
            for (int i = 0; i < clazz.interfaces().length; i++) {
                if (i > 0) {
                    attrOut.output(", ");
                }
                attrOut.chained()
                        .output("\"")
                        .output(clazz.interfaces()[i].getInternalName())
                        .output("\"")
                        .print();

                // もし最後の要素でなければ，カンマを出力
                if (i < clazz.interfaces().length - 1) {
                    attrOut.println(",");
                }
            }
            attrOut.println("]");
        }

        this.out.println(") {");
    }

    private static String majorToJavaVersion(int major) {
        // 1.8 以下は 1.x で
        // それ以降は 9
        if (major <= 52) {
            return "1." + (major - 44);
        } else {
            return String.valueOf(major - 44);
        }
    }
}
