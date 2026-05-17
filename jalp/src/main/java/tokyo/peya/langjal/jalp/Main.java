package tokyo.peya.langjal.jalp;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.jalp.printers.JALFilePrinter;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        OptionParser parser = createOptionParser();
        try {
            OptionSet options = parser.parse(args);
            if (options.has("help")) {
                printHelpAndExit(parser);
                return;
            }
            if (options.has("version")) {
                System.out.println("jalp v1.1.0");
                return;
            }

            List<?> nonOptions = options.nonOptionArguments();
            if (nonOptions.isEmpty()) {
                System.err.println("Error: Missing input file.");
                printHelpAndExit(parser);
                return;
            }
            String input = nonOptions.getFirst().toString();
            String classpath = options.hasArgument("cp")
                    ? options.valueOf("cp").toString()
                    : "";
            int flags = getFlags(options);

            JALFilePrinter bootstrap = new JALFilePrinter(classpath, flags);
            bootstrap.process(input);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            try {
                parser.printHelpOn(System.err);
            } catch (Exception printEx) {
                System.err.println("Failed to print help: " + printEx.getMessage());
            }
        }
    }

    private static int getFlags(@NotNull OptionSet options) {
        int flags = 0;

        if (options.has("public")) {
            flags |= JALPOptions.SHOW_ACC_PUBLIC;
        }

        if (options.has("protected")) {
            flags |= JALPOptions.SHOW_ACC_PUBLIC | JALPOptions.SHOW_ACC_PROTECTED;
        }

        if (options.has("package")) {
            flags |= JALPOptions.SHOW_ACC_PUBLIC | JALPOptions.SHOW_ACC_PROTECTED | JALPOptions.SHOW_ACC_PACKAGE_PRIVATE;
        }

        if (options.has("private") || options.has("p")) {
            flags |= JALPOptions.SHOW_ACC_PUBLIC | JALPOptions.SHOW_ACC_PROTECTED | JALPOptions.SHOW_ACC_PACKAGE_PRIVATE | JALPOptions.SHOW_ACC_PRIVATE;
        }

        if (options.has("c")) {
            flags |= JALPOptions.SHOW_CODE;
        }

        if (options.has("constants")) {
            flags |= JALPOptions.SHOW_CONSTANTS;
        }

        if (options.has("verbose") || options.has("v")) {
            flags |= JALPOptions.VERBOSE;
        }

        if (flags == 0) {
            flags = JALPOptions.DEFAULT;
        }

        if (options.has("no-header")) {
            flags &= ~JALPOptions.SHOW_HEADER;
        }

        return flags;
    }

    private static void printHelpAndExit(OptionParser parser) {
        System.out.println("Usage: jalp [options] <input>");
        System.out.println();
        System.out.println("Example:");
        System.out.println("  jalp -p -c MyClass");
        System.out.println();
        System.out.println("Options:");

        try {
            parser.printHelpOn(System.out);
        } catch (Exception ignored) {
        }

        System.out.println();
        System.exit(1);
    }

    private static OptionParser createOptionParser() {
        OptionParser parser = new OptionParser();
        parser.acceptsAll(List.of("help", "?"), "Show this help message").forHelp();
        parser.acceptsAll(List.of("version", "v"), "Show version information").forHelp();
        parser.accepts("public", "Show public classes and members");
        parser.accepts("protected", "Show public and protected classes and members");
        parser.accepts("package", "Show public, protected, and package-private classes and members");
        parser.acceptsAll(List.of("private", "p"), "Show all classes and members");
        parser.acceptsAll(List.of("cp", "classpath"), "Class path for class lookup")
                .withRequiredArg()
                .describedAs("path");
        parser.accepts("c", "Show code instructions");
        parser.accepts("constants", "Show constant pool entries");
        parser.acceptsAll(List.of("verbose", "v"), "Show verbose output (includes line numbers)");
        return parser;
    }
}
