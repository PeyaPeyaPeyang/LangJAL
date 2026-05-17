package tokyo.peya.langjal.jalp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

public class JALClassFinder {
    private JALClassFinder() {
    }

    public static ClassInfo findClass(String input, String classpath) {
        ClassInfo classFromCandidates = findFromCandidates(createCandidates(input, classpath));
        if (classFromCandidates != null) {
            return classFromCandidates;
        }

        ClassInfo classFromClasspath = findFromClasspath(input, classpath);
        if (classFromClasspath != null) {
            return classFromClasspath;
        }

        throw new IllegalArgumentException("Class not found: " + input);
    }

    private static ClassInfo findFromCandidates(List<Path> candidates) {
        for (Path candidate : candidates) {
            ClassInfo classInfo = readClassFile(candidate);
            if (classInfo != null) {
                return classInfo;
            }
        }
        return null;
    }

    private static ClassInfo findFromClasspath(String input, String classpath) {
        String classEntry = toClassEntry(input);
        for (Path entry : parseClasspathEntries(classpath)) {
            if (Files.isDirectory(entry)) {
                ClassInfo classFromDirectory = findInDirectory(entry, classEntry);
                if (classFromDirectory != null) {
                    return classFromDirectory;
                }
                continue;
            }

            if (isArchiveFile(entry)) {
                ClassInfo classFromArchive = findInArchive(entry, classEntry);
                if (classFromArchive != null) {
                    return classFromArchive;
                }
            }
        }
        return null;
    }

    private static ClassInfo findInDirectory(Path directory, String classEntry) {
        ClassInfo classFromDirectory = readClassFile(directory.resolve(classEntry));
        if (classFromDirectory != null) {
            return classFromDirectory;
        }

        for (Path archivePath : listArchiveFiles(directory)) {
            ClassInfo classFromArchive = findInArchive(archivePath, classEntry);
            if (classFromArchive != null) {
                return classFromArchive;
            }
        }
        return null;
    }

    private static List<Path> createCandidates(String input, String classpath) {
        Set<Path> candidates = new LinkedHashSet<>();
        Path inputPath = Path.of(input);
        candidates.add(inputPath);

        if (!input.endsWith(".class")) {
            candidates.add(Path.of(input + ".class"));
        }

        String classEntry = toClassEntry(input);
        for (Path classpathEntry : parseClasspathEntries(classpath)) {
            if (Files.isDirectory(classpathEntry)) {
                candidates.add(classpathEntry.resolve(classEntry));
            }
        }

        return new ArrayList<>(candidates);
    }

    private static List<Path> parseClasspathEntries(String classpath) {
        List<Path> entries = new ArrayList<>();
        if (classpath == null || classpath.isBlank()) {
            return entries;
        }

        for (String entry : classpath.split(Pattern.quote(File.pathSeparator))) {
            if (entry.isBlank()) {
                continue;
            }
            entries.add(Path.of(entry.trim()));
        }
        return entries;
    }

    private static String toClassEntry(String input) {
        String normalized = input;
        if (normalized.endsWith(".class")) {
            normalized = normalized.substring(0, normalized.length() - ".class" .length());
        }
        normalized = normalized.replace('.', '/').replace('\\', '/');
        return normalized + ".class";
    }

    private static ClassInfo readClassFile(Path classFile) {
        if (!Files.isRegularFile(classFile)) {
            return null;
        }

        try {
            byte[] bytes = Files.readAllBytes(classFile);
            FileTime lastModified = Files.getLastModifiedTime(classFile);
            Path normalizedPath = classFile.toAbsolutePath().normalize();
            return createInfo(normalizedPath, lastModified.toInstant(), bytes);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read class file: " + classFile, e);
        }
    }

    private static boolean isArchiveFile(Path entry) {
        if (!Files.isRegularFile(entry)) {
            return false;
        }
        String fileName = entry.getFileName().toString().toLowerCase();
        return fileName.endsWith(".jar") || fileName.endsWith(".zip");
    }

    private static List<Path> listArchiveFiles(Path directory) {
        try (var paths = Files.list(directory)) {
            return paths
                    .filter(JALClassFinder::isArchiveFile)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to list classpath directory: " + directory, e);
        }
    }

    private static ClassInfo findInArchive(Path archivePath, String classEntry) {
        try (JarFile jarFile = new JarFile(archivePath.toFile())) {
            JarEntry jarEntry = jarFile.getJarEntry(classEntry);
            if (jarEntry == null) {
                return null;
            }
            try (InputStream inputStream = jarFile.getInputStream(jarEntry)) {
                byte[] bytes = inputStream.readAllBytes();
                Instant lastModified = jarEntry.getLastModifiedTime() != null
                        ? jarEntry.getLastModifiedTime().toInstant()
                        : Instant.EPOCH;
                Path pseudoPath = Path.of(archivePath.toAbsolutePath().normalize() + "!" + classEntry);
                return createInfo(pseudoPath, lastModified, bytes);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read archive: " + archivePath, e);
        }
    }

    private static ClassInfo createInfo(Path classFile, Instant lastModified, byte[] bytes) {
        return new ClassInfo(classFile, lastModified, bytes.length, toSHA256(bytes), bytes);
    }

    private static String toSHA256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte hashByte : hash) {
                builder.append(String.format("%02x", hashByte));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
