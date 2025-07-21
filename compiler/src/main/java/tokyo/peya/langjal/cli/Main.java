package tokyo.peya.langjal.cli;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

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

            CompilerCLI.runCompiler(input, output, verbose);
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
        parser.accepts("help", "Show this help message").forHelp();
        parser.accepts("version", "Show version information").forHelp();
        parser.accepts("verbose", "Enable verbose output");
        parser.accepts("output")
                .withRequiredArg()
                .ofType(String.class)
                .describedAs("output directory or .jar file")
                .defaultsTo("./");

        return parser;
    }
}
