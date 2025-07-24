package tokyo.peya.langjal.cli;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.CompileSettings;

import java.util.List;

public class Main
{
    public static void main(String[] args)
    {
        OptionParser parser = createOptionParser();
        try
        {
            OptionSet options = parser.parse(args);
            if (options.has("help"))
            {
                printHelpAndExit(parser);
                return;
            }
            if (options.has("version"))
            {
                System.out.println("JAL Compiler CLI Version 1.0.0");
                return;
            }

            List<?> nonOptions = options.nonOptionArguments();
            if (nonOptions.isEmpty())
            {
                System.err.println("Error: Missing input file.");
                printHelpAndExit(parser);
                return;
            }
            String input = nonOptions.getFirst().toString();

            String output = options.valueOf("output").toString();
            boolean verbose = options.has("verbose");
            @MagicConstant(valuesFromClass = CompileSettings.class)
            int compileFlags = getCompileFlag(options);

            CompilerCLI.runCompiler(input, output, compileFlags, verbose);
        }
        catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            try
            {
                parser.printHelpOn(System.err);
            }
            catch (Exception printEx)
            {
                System.err.println("Failed to print help: " + printEx.getMessage());
            }
        }
    }

    @MagicConstant(valuesFromClass = CompileSettings.class)
    private static int getCompileFlag(@NotNull OptionSet options)
    {
        // 特殊モード：軽量設定を強制
        if (options.has("no-debug"))
            return CompileSettings.REQUIRED_ONLY;

        @MagicConstant(valuesFromClass = CompileSettings.class)
        int flags = CompileSettings.NONE;

        if (!options.has("no-line-numbers"))
            flags |= CompileSettings.INCLUDE_LINE_NUMBER_TABLE;

        if (!options.has("no-stack-frame-map"))
            flags |= CompileSettings.COMPUTE_STACK_FRAME_MAP;

        return flags;
    }

    private static void printHelpAndExit(OptionParser parser)
    {
        System.out.println("Usage: jalc.jar [options] <input>");
        System.out.println();
        System.out.println("  <input>                  input file (.jal)");
        System.out.println("Options:");

        try
        {
            parser.printHelpOn(System.out);
        }
        catch (Exception ignored) {}

        System.out.println();
        System.exit(1);
    }

    private static OptionParser createOptionParser()
    {
        OptionParser parser = new OptionParser();
        parser.acceptsAll(List.of("help", "?"), "Show this help message").forHelp();
        parser.acceptsAll(List.of("version", "v"), "Show version information").forHelp();
        parser.accepts("verbose", "Enable verbose output");
        parser.acceptsAll(List.of("output", "o"))
              .withRequiredArg()
              .ofType(String.class)
              .describedAs("output directory or .jar file")
              .defaultsTo("./");

        parser.acceptsAll(List.of("no-debug", "D"))
              .withOptionalArg()
              .ofType(Boolean.class)
              .defaultsTo(false)
              .describedAs("Disable debug information(line numbers) in the output files");
        parser.acceptsAll(List.of("no-line-numbers", "L"))
              .withOptionalArg()
              .ofType(Boolean.class)
              .defaultsTo(false)
              .describedAs("Disable line number information in the output files");
        parser.acceptsAll(List.of("no-stack-frame-map", "S"))
                .withOptionalArg()
                .ofType(Boolean.class)
                .defaultsTo(false)
                .describedAs("Disable stack frame map in the output files");

        return parser;
    }
}
