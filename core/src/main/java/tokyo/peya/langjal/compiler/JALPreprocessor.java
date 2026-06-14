package tokyo.peya.langjal.compiler;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.exceptions.CompileErrorException;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

final class JALPreprocessor {
    private static final int MAX_EXPANSION_DEPTH = 64;

    private JALPreprocessor() {
    }

    @NotNull
    static String preprocess(@NotNull String sourceCode) throws CompileErrorException {
        Map<String, String> defines = new LinkedHashMap<>();
        StringBuilder result = new StringBuilder(sourceCode.length());
        boolean[] inBlockComment = {false};

        int line = 1;
        int index = 0;
        while (index < sourceCode.length()) {
            int lineEnd = findLineEnd(sourceCode, index);
            int nextLine = nextLineStart(sourceCode, lineEnd);
            String sourceLine = sourceCode.substring(index, lineEnd);
            String lineEnding = sourceCode.substring(lineEnd, nextLine);

            if (!inBlockComment[0] && isPreprocessorDirective(sourceLine)) {
                processDirective(defines, sourceLine, line);
                result.append(lineEnding);
            } else {
                result.append(expandLine(sourceLine, defines, inBlockComment));
                result.append(lineEnding);
            }

            index = nextLine;
            line++;
        }

        return result.toString();
    }

    private static int findLineEnd(@NotNull String sourceCode, int start) {
        int index = start;
        while (index < sourceCode.length()) {
            char c = sourceCode.charAt(index);
            if (c == '\r' || c == '\n')
                break;
            index++;
        }

        return index;
    }

    private static int nextLineStart(@NotNull String sourceCode, int lineEnd) {
        if (lineEnd >= sourceCode.length())
            return lineEnd;

        if (sourceCode.charAt(lineEnd) == '\r'
                && lineEnd + 1 < sourceCode.length()
                && sourceCode.charAt(lineEnd + 1) == '\n')
            return lineEnd + 2;

        return lineEnd + 1;
    }

    private static boolean isPreprocessorDirective(@NotNull String line) {
        int index = skipHorizontalSpaces(line, 0);
        return index < line.length() && line.charAt(index) == '#';
    }

    private static void processDirective(@NotNull Map<String, String> defines,
                                         @NotNull String line,
                                         int lineNumber) throws CompileErrorException {
        int index = skipHorizontalSpaces(line, 0);
        int directiveStart = index;
        index++;
        index = skipHorizontalSpaces(line, index);

        int directiveEnd = readIdentifierEnd(line, index);
        String directive = line.substring(index, directiveEnd);
        if (!"define".equals(directive))
            throw new CompileErrorException(
                    "Unsupported preprocessor directive: #" + directive,
                    lineNumber,
                    directiveStart,
                    line.length() - directiveStart
            );

        index = skipHorizontalSpaces(line, directiveEnd);
        int nameEnd = readIdentifierEnd(line, index);
        if (nameEnd == index)
            throw new CompileErrorException("Expected macro name after #define", lineNumber, index, 1);

        String name = line.substring(index, nameEnd);
        index = skipHorizontalSpaces(line, nameEnd);
        defines.put(name, line.substring(index));
    }

    private static int skipHorizontalSpaces(@NotNull String line, int index) {
        while (index < line.length()) {
            char c = line.charAt(index);
            if (c != ' ' && c != '\t')
                break;
            index++;
        }

        return index;
    }

    private static int readIdentifierEnd(@NotNull String line, int index) {
        if (index >= line.length() || !isIdentifierStart(line.charAt(index)))
            return index;

        index++;
        while (index < line.length() && isIdentifierPart(line.charAt(index)))
            index++;

        return index;
    }

    @NotNull
    private static String expandLine(@NotNull String line,
                                     @NotNull Map<String, String> defines,
                                     boolean @NotNull [] inBlockComment) {
        return expandLine(line, defines, inBlockComment, new HashSet<>());
    }

    @NotNull
    private static String expandLine(@NotNull String line,
                                     @NotNull Map<String, String> defines,
                                     boolean @NotNull [] inBlockComment,
                                     @NotNull Set<String> expanding) {
        StringBuilder result = new StringBuilder(line.length());
        boolean inString = false;
        boolean inLineComment = false;

        int index = 0;
        while (index < line.length()) {
            char c = line.charAt(index);

            if (inLineComment) {
                result.append(c);
                index++;
                continue;
            }

            if (inBlockComment[0]) {
                result.append(c);
                if (c == '*' && index + 1 < line.length() && line.charAt(index + 1) == '/') {
                    result.append('/');
                    inBlockComment[0] = false;
                    index += 2;
                } else {
                    index++;
                }
                continue;
            }

            if (inString) {
                result.append(c);
                if (c == '\\' && index + 1 < line.length()) {
                    result.append(line.charAt(index + 1));
                    index += 2;
                } else {
                    if (c == '"')
                        inString = false;
                    index++;
                }
                continue;
            }

            if (c == '/' && index + 1 < line.length()) {
                char next = line.charAt(index + 1);
                if (next == '/') {
                    result.append("//");
                    inLineComment = true;
                    index += 2;
                    continue;
                }

                if (next == '*') {
                    result.append("/*");
                    inBlockComment[0] = true;
                    index += 2;
                    continue;
                }
            }

            if (c == '"') {
                result.append(c);
                inString = true;
                index++;
                continue;
            }

            if (isIdentifierStart(c)) {
                int end = readIdentifierEnd(line, index);
                String identifier = line.substring(index, end);
                String replacement = defines.get(identifier);
                if (replacement == null || expanding.contains(identifier)) {
                    result.append(identifier);
                } else if (expanding.size() >= MAX_EXPANSION_DEPTH) {
                    result.append(replacement);
                } else {
                    expanding.add(identifier);
                    result.append(expandLine(replacement, defines, new boolean[]{false}, expanding));
                    expanding.remove(identifier);
                }
                index = end;
                continue;
            }

            result.append(c);
            index++;
        }

        return result.toString();
    }

    private static boolean isIdentifierStart(char c) {
        return c == '_' || c == '$' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private static boolean isIdentifierPart(char c) {
        return isIdentifierStart(c) || (c >= '0' && c <= '9');
    }
}
