package tokyo.peya.langjal.cli;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class FileOutputter
{
    private final Path output;
    private final boolean isDirectory;
    private final boolean verbose;

    @Getter
    private final Path actualCompileOutput;

    public FileOutputter(@NotNull Path output, boolean isOutputDirectoryLike, boolean verbose)
    {
        this.output = output;
        this.isDirectory = isOutputDirectoryLike;
        this.verbose = verbose;

        this.actualCompileOutput = createActualCompileOutput(isOutputDirectoryLike, output);

        if (this.verbose)
            System.out.println("Output path: " + output + ", is directory: " + this.isDirectory);
    }

    public void finalise()
    {
        if(this.verbose)
            System.out.println("Finalising output: " + this.actualCompileOutput);

        if (this.isDirectory)
            return;  // 何もすることはない

        if (!Files.exists(this.actualCompileOutput))
        {
            System.err.println("Output file " + this.actualCompileOutput + " does not exist after compilation.");
            return;
        }

        try(FileOutputStream fos = new FileOutputStream(this.actualCompileOutput.toFile());
            ZipOutputStream zos = new ZipOutputStream(fos);
            Stream<Path> paths = Files.walk(this.actualCompileOutput.getParent()))
        {
            paths.filter(Files::isRegularFile)
                 .forEach(path -> {
                try
                {
                    String zipEntryName = this.actualCompileOutput.getParent().relativize(path).toString();
                    zos.putNextEntry(new ZipEntry(zipEntryName));
                    Files.copy(path, zos);
                    zos.closeEntry();
                }
                catch (IOException e)
                {
                    System.err.println("Failed to add file " + path + " to output archive: " + e.getMessage());
                }
            });
        }
        catch (IOException e)
        {
            System.err.println("Failed to create an output archive: " + e.getMessage());
        }
    }

    private static Path createActualCompileOutput(boolean isDirectory, @NotNull Path output)
    {
        if (isDirectory)
            return output;

        try
        {
            Path tempOutput = Files.createTempDirectory("langjal-compile-output-");
            return tempOutput.resolve(output.getFileName());
        }
        catch (IOException e)
        {
            System.err.println("Failed to normalize output path " + output + ": " + e.getMessage());
            throw new UncheckedIOException(e);
        }
    }

    public boolean prepareOutput(@NotNull Path output, boolean verbose)
    {
        if (this.isDirectory)
        {
            if (verbose)
                System.out.println("Output is a directory: " + output);
            try
            {
                Files.createDirectories(output);
            }
            catch (Exception e)
            {
                System.err.println("Failed to create output directory " + output + ": " + e.getMessage());
            }
            return true;
        }

        Path parentDir = output.getParent();
        if (!Files.exists(parentDir))
        {
            if (verbose)
                System.out.println("Creating parent directory: " + parentDir);
            try
            {
                Files.createDirectories(parentDir);
            }
            catch (Exception e)
            {
                System.err.println("Failed to create parent directory " + parentDir + ": " + e.getMessage());
                return false;
            }
            return true;
        }
        else if (!Files.isDirectory(parentDir))
        {
            System.err.println("Output path " + output + " is not a directory and its parent is not a directory either: " + parentDir);
            return false;
        }


        if (Files.exists(output))
        {
            if (verbose)
                System.out.println("Output file already exists, deleting: " + output);
            try
            {
                Files.delete(output);
            }
            catch (Exception e)
            {
                System.err.println("Failed to delete existing output file " + output + ": " + e.getMessage());
                return false;
            }
        }

        return true;
    }
}
