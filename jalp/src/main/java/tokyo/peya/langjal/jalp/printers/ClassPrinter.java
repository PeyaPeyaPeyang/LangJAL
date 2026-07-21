package tokyo.peya.langjal.jalp.printers;

import tokyo.peya.langjal.compiler.jvm.ClassReferenceType;
import tokyo.peya.langjal.jalp.OutputChain;
import tokyo.peya.langjal.jalp.OutputFormatter;
import tokyo.peya.langjal.jalp.reader.JALAttribute;
import tokyo.peya.langjal.jalp.reader.JALClass;
import tokyo.peya.langjal.jalp.reader.JALConstantPoolEntry;
import tokyo.peya.langjal.jalp.reader.JALField;

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

        this.printFields(clazz);

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
        if (!clazz.superName().equals(ClassReferenceType.OBJECT)) {
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

    private void printFields(JALClass clazz) {
        for (JALField field : clazz.fields()) {
            if (PrinterUtils.shouldSkip(this.flags, field.access())) {
                continue;
            }
            OutputChain line = PrinterUtils.printAccess(this.innerOut, field.access(), field.accessAttributeSet())
                    .output(field.name())
                    .output(":")
                    .output(field.descriptor().toString());
            JALAttribute.ConstantValueAttribute constantValue = getAttribute(field, JALAttribute.ConstantValueAttribute.class);
            if (constantValue != null) {
                line.output(" = ").output(formatConstantValue(constantValue.constant()));
            }
            line.output(";").println();
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends JALAttribute> T getAttribute(JALField field, Class<T> attrClass) {
        for (JALAttribute attr : field.attributes()) {
            if (attrClass.isInstance(attr)) {
                return (T) attr;
            }
        }
        return null;
    }

    private static String formatConstantValue(JALConstantPoolEntry constant) {
        return switch (constant) {
            case JALConstantPoolEntry.IntegerEntry entry -> String.valueOf(entry.value());
            case JALConstantPoolEntry.FloatEntry entry -> entry.value() + "f";
            case JALConstantPoolEntry.LongEntry entry -> entry.value() + "L";
            case JALConstantPoolEntry.DoubleEntry entry -> entry.value() + "d";
            case JALConstantPoolEntry.StringEntry entry -> quote(entry.value());
            default -> throw new IllegalArgumentException("Unsupported field constant: " + constant.getClass().getSimpleName());
        };
    }

    private static String quote(String value) {
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                + "\"";
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
