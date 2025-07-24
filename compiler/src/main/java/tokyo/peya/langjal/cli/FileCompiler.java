package tokyo.peya.langjal.cli;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.CompileSettings;
import tokyo.peya.langjal.compiler.JALFileCompiler;
import tokyo.peya.langjal.compiler.exceptions.CompileErrorException;

import java.io.IOException;
import java.nio.file.Path;

public class FileCompiler
{
    public static void runCompiler(@NotNull Path sourceFile, @NotNull Path output,
                                   boolean isOutputDirectoryLike, boolean verbose)
    {
        FileOutputter outputter = new FileOutputter(output, isOutputDirectoryLike, verbose);
        if (!outputter.prepareOutput(output, verbose))
        {
            System.err.println("Failed to prepare output directory: " + output);
            return;
        }

        JALCompilerReporter reporter = new JALCompilerReporter(verbose);
        JALFileCompiler compiler;
        try
        {
            compiler = new JALFileCompiler(
                    reporter,
                    outputter.getActualCompileOutput(),
                    CompileSettings.FULL);
        }
        catch (IOException e)
        {
            System.err.println("Failed to initialize compiler: " + e.getMessage());
            return;
        }

        if (!validateInputFile(sourceFile))
            return;

        try
        {
            compiler.compile(sourceFile);
        }
        catch (CompileErrorException e)
        {
            reporter.postError("Failed to compile " + sourceFile, e, sourceFile);
            return;
        }


        outputter.finalise();
        if (verbose)
            System.out.println("Compilation completed successfully.");
    }

    private static boolean validateInputFile(@NotNull Path sourceFile)
    {
        if (!sourceFile.toString().endsWith(".jal"))
        {
            System.err.println("Input file must have a .jal extension: " + sourceFile);
            return false;
        }
        if (!sourceFile.toFile().exists())
        {
            System.err.println("Input file does not exist: " + sourceFile);
            return false;
        }

        return true;
    }
}
