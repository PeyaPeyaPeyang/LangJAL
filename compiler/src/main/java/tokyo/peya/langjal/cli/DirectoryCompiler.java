package tokyo.peya.langjal.cli;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.CompileSettings;
import tokyo.peya.langjal.compiler.JALFileCompiler;
import tokyo.peya.langjal.compiler.exceptions.CompileErrorException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class DirectoryCompiler
{
    public static void runCompiler(@NotNull Path sourceDirectory, @NotNull Path output,
                                   boolean isOutputDirectoryLike, boolean verbose)
    {
        FileOutputter outputter = new FileOutputter(output, isOutputDirectoryLike, verbose);
        if (!outputter.prepareOutput(output, verbose))
        {
            System.err.println("Failed to prepare output directory: " + output);
            return;
        }

        Path[] sourceFiles = scanInputDirectory(sourceDirectory, verbose);
        if (sourceFiles.length == 0)
        {
            System.out.println("No source files found in directory: " + sourceDirectory);
            return;
        }

        JALCompilerReporter reporter = new JALCompilerReporter(verbose);
        JALFileCompiler compiler;
        try
        {
             compiler = new JALFileCompiler(
                    reporter,
                    outputter.getActualCompileOutput(),
                    CompileSettings.COMPUTE_STACK_FRAME_MAP);
        }
        catch (IOException e)
        {
            System.err.println("Failed to initialize compiler: " + e.getMessage());
            return;
        }

        if (verbose)
            System.out.println("Compiling " + sourceFiles.length + " source files...");

        for (Path sourceFile : sourceFiles)
        {
            if (verbose)
                System.out.println("Compiling: " + sourceFile);

            try
            {
                compiler.compile(sourceFile);
            }
            catch (CompileErrorException e)
            {
                reporter.postError("Failed to compile " + sourceFile, e, sourceFile);
                return;
            }
        }

        outputter.finalise();
        if (verbose)
            System.out.println("Compilation completed successfully.");
    }

    private static Path[] scanInputDirectory(@NotNull Path sourceDirectory, boolean verbose)
    {
        if (verbose)
            System.out.println("Scanning directory: " + sourceDirectory);
        Path[] sourceFiles = getAllCompilableSources(sourceDirectory);
        if (verbose)
        {
            System.out.println("Found " + sourceFiles.length + " source files to compile.");
            for (Path sourceFile : sourceFiles)
                System.out.println("- " + sourceFile);
        }

        return sourceFiles;
    }


    private static Path[] getAllCompilableSources(@NotNull Path sourceDirectory)
    {
        try(Stream<Path> paths = Files.walk(sourceDirectory))
        {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(CompilerCLI::hasValidInputFileName)
                    .toArray(Path[]::new);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to list source files in directory: " + sourceDirectory, e);
        }
    }
}
