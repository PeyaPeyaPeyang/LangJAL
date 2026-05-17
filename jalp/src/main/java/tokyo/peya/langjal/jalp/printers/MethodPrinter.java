package tokyo.peya.langjal.jalp.printers;

import tokyo.peya.langjal.jalp.JALPOptions;
import tokyo.peya.langjal.jalp.OutputFormatter;
import tokyo.peya.langjal.jalp.reader.JALAttribute;
import tokyo.peya.langjal.jalp.reader.JALClass;
import tokyo.peya.langjal.jalp.reader.JALMethod;

public class MethodPrinter {
    private final int flags;
    private final OutputFormatter innerOut;

    public MethodPrinter(OutputFormatter out, int flags) {
        this.innerOut = new OutputFormatter(out);
        this.flags = flags;
    }

    public void printMethods(JALClass clazz) {
        boolean isFirst = true;
        for (JALMethod method : clazz.methods()) {
            this.printMethod(clazz, method);
            if (!isFirst) {
                this.innerOut.println("");
            }
            isFirst = false;
        }
    }

    private void printMethod(JALClass clazz, JALMethod method) {
        if (PrinterUtils.shouldSkip(this.flags, method.access())) {
            return;
        }

        PrinterUtils.printAccess(this.innerOut, method.access(), method.accessAttrs())
                .output(" ")
                .output(method.name())
                .output(method.descriptor().toString())
                .output(" {")
                .println();

        if (JALPOptions.is(this.flags, JALPOptions.SHOW_CODE)) {
            this.printCode(clazz, method);
        }
        this.innerOut.println("}");
    }

    private void printCode(JALClass clazz, JALMethod method) {
        OutputFormatter codeOut = new OutputFormatter(this.innerOut);
        JALAttribute.CodeAttribute codeAttr = method.getAttribute(JALAttribute.CodeAttribute.class);
        if (codeAttr == null) {
            codeOut.println("// No code.");
            return;
        }

        LineNumberMatcher linesMatcher = new LineNumberMatcher(codeAttr.getAttribute(JALAttribute.LineNumberTableAttribute.class));
        byte[] code = codeAttr.code();

        JALAttribute.BootstrapMethodsAttribute bootstrapMethods = clazz.getAttribute("BootstrapMethods");

        CodePrinter codePrinter = new CodePrinter(codeOut, bootstrapMethods, clazz.constants());
        codePrinter.printCode(code, linesMatcher);
    }
}
