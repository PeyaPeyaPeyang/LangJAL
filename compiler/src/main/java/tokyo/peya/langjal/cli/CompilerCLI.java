package tokyo.peya.langjal.cli;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;

public class CompilerCLI
{
    public static String[] ALLOWED_INPUT_EXTENSIONS = {".jal"};
    public static String[] ALLOWED_OUTPUT_EXTENSIONS = {".jar", ".zip"};

    public static void runCompiler(@NotNull String input, @NotNull String output, boolean verbose)
    {
        Path inputPath = resolveAbsolutePath(input);
        Path outputPath = resolveAbsolutePath(output);

        if (verbose)
        {
            System.out.println("Input path: " + inputPath);
            System.out.println("Output path: " + outputPath);
        }

        if (!(validateInputPath(input) && validateOutputPath(output)))
            return;

        if (verbose)
            System.out.println("Starting compilation...");

        // ファイルがディレクトリの場合は，専用のコンパイラを使用
        if (Files.isDirectory(inputPath))
            DirectoryCompiler.runCompiler(inputPath, outputPath, isDirectoryLike(output), verbose);
        else
            FileCompiler.runCompiler(inputPath, outputPath, isDirectoryLike(output),  verbose);
    }

    public static boolean hasValidInputFileName(@NotNull Path path)
    {
        Path fileName = path.getFileName();
        if (fileName == null || !Files.isRegularFile(path) || fileName.toString().isEmpty())
        {
            System.err.println("Error: " + path + " is not a valid input file.");
            return false;
        }

        for (String ext : ALLOWED_INPUT_EXTENSIONS)
        {
            if (fileName.toString().endsWith(ext))
                return true;
        }

        return false;
    }

    public static boolean hasValidOutputFileName(@NotNull Path path)
    {
        Path fileName = path.getFileName();
        if (fileName == null || !Files.isRegularFile(path) || fileName.toString().isEmpty())
        {
            System.err.println("Error: " + path + " is not a valid output file.");
            return false;
        }

        for (String ext : ALLOWED_OUTPUT_EXTENSIONS)
        {
            if (fileName.toString().endsWith(ext))
                return true;
        }

        return false;
    }

    private static boolean validateInputPath(@NotNull String path)
    {
        Path resolvedPath = resolveAbsolutePath(path);
        if (!Files.exists(resolvedPath))
        {
            System.err.println("Error: Input path does not exist: " + resolvedPath);
            return false;
        }
        if (isDirectoryLike(path))
            return true;

        if (!hasValidInputFileName(resolvedPath))
        {
            System.err.println("Error: Input path must have one of the following extensions: " + String.join(", ", ALLOWED_INPUT_EXTENSIONS));
            return false;
        }
        return true;
    }

    private static boolean validateOutputPath(@NotNull String path)
    {
        Path resolvedPath = resolveAbsolutePath(path);

        if (isDirectoryLike(path))
        {
            if (Files.exists(resolvedPath) && !Files.isDirectory(resolvedPath))
            {
                System.err.println("Error: Output path must be a directory or a new file: " + resolvedPath);
                return false;
            }

            // ディレクトリの場合は一律問題なし
            return true;
        }

        if (!hasValidOutputFileName(resolvedPath))
        {
            System.err.println("Error: Output path must have one of the following extensions: " + String.join(", ", ALLOWED_OUTPUT_EXTENSIONS));
            return false;
        }
        return true;
    }


    public static boolean isDirectoryLike(@NotNull String pathName)
    {
        Path path = resolveAbsolutePath(pathName);
        if (Files.isDirectory(path))
            return true;
        else if (Files.exists(path))  // ディレクトリではないものが存在している時点でディレクトリじゃない
            return false;

        if (path.endsWith("/") || path.endsWith("\\"))
            return true;  // パスがスラッシュで終わっている場合はディレクトリとみなす

        // パスが拡張子を持たない場合はディレクトリとみなす
        String fileName = path.getFileName().toString();
        return fileName.isEmpty() || fileName.indexOf('.') < 0;
    }

    private static Path resolveAbsolutePath(@NotNull String path)
    {
        Path resolvedPath = Path.of(path);
        if (!resolvedPath.isAbsolute())
            resolvedPath = Path.of(System.getProperty("user.dir")).resolve(resolvedPath);

        return resolvedPath.normalize();
    }
}
