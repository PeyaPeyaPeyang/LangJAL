package tokyo.peya.langjal.compiler.preprocessor;

import org.jetbrains.annotations.NotNull;

final class PreprocessorSyntax {
    private PreprocessorSyntax() {
    }

    static int findLineEnd(@NotNull String sourceCode, int start) {
        int index = start;
        while (index < sourceCode.length()) {
            char c = sourceCode.charAt(index);
            if (c == '\r' || c == '\n')
                break;
            index++;
        }

        return index;
    }

    static int nextLineStart(@NotNull String sourceCode, int lineEnd) {
        if (lineEnd >= sourceCode.length())
            return lineEnd;

        if (sourceCode.charAt(lineEnd) == '\r'
                && lineEnd + 1 < sourceCode.length()
                && sourceCode.charAt(lineEnd + 1) == '\n')
            return lineEnd + 2;

        return lineEnd + 1;
    }

    static boolean isPreprocessorDirective(@NotNull String line) {
        int index = skipHorizontalSpaces(line, 0);
        return index < line.length() && line.charAt(index) == '#';
    }

    static int skipHorizontalSpaces(@NotNull String line, int index) {
        while (index < line.length()) {
            char c = line.charAt(index);
            if (c != ' ' && c != '\t')
                break;
            index++;
        }

        return index;
    }

    static int readIdentifierEnd(@NotNull String line, int index) {
        if (index >= line.length() || !isIdentifierStart(line.charAt(index)))
            return index;

        index++;
        while (index < line.length() && isIdentifierPart(line.charAt(index)))
            index++;

        return index;
    }

    static boolean isIdentifierStart(char c) {
        return c == '_' || c == '$' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private static boolean isIdentifierPart(char c) {
        return isIdentifierStart(c) || (c >= '0' && c <= '9');
    }
}
