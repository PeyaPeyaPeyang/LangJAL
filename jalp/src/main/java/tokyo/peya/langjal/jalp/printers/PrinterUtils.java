package tokyo.peya.langjal.jalp.printers;

import tokyo.peya.langjal.compiler.jvm.AccessAttributeSet;
import tokyo.peya.langjal.compiler.jvm.AccessLevel;
import tokyo.peya.langjal.jalp.JALPOptions;
import tokyo.peya.langjal.jalp.OutputChain;
import tokyo.peya.langjal.jalp.OutputFormatter;

class PrinterUtils {
    public static OutputChain printAccess(OutputFormatter out, AccessLevel access, AccessAttributeSet attributes) {
        OutputChain outChain = out.chained();
        if (access != AccessLevel.PACKAGE_PRIVATE) {
            outChain.output(access.getName())
                    .output(" ");
        }

        return outChain.output(attributes.toString());
    }

    public static boolean shouldSkip(int flags, AccessLevel access) {
        return !switch (access) {
            case PUBLIC -> (flags & JALPOptions.SHOW_ACC_PUBLIC) != 0;
            case PROTECTED -> (flags & JALPOptions.SHOW_ACC_PROTECTED) != 0;
            case PRIVATE -> (flags & JALPOptions.SHOW_ACC_PRIVATE) != 0;
            case PACKAGE_PRIVATE -> (flags & JALPOptions.SHOW_ACC_PACKAGE_PRIVATE) != 0;
        };
    }
}
