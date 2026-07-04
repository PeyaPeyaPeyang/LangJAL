package tokyo.peya.langjal.compiler.preprocessor;

import org.jetbrains.annotations.NotNull;

record ContinuedLine(@NotNull String text,
                     @NotNull String removedLineEndings,
                     int nextIndex,
                     int lineCount) {
    @NotNull
    static ContinuedLine read(@NotNull String sourceCode,
                              @NotNull String firstLine,
                              @NotNull String firstLineEnding,
                              int firstNextLine) {
        StringBuilder text = new StringBuilder(removeLineContinuation(firstLine));
        StringBuilder removedLineEndings = new StringBuilder(firstLineEnding);
        int index = firstNextLine;
        int lineCount = 1;

        String previousLine = firstLine;
        while (hasLineContinuation(previousLine) && index < sourceCode.length()) {
            int lineEnd = PreprocessorSyntax.findLineEnd(sourceCode, index);
            int nextLine = PreprocessorSyntax.nextLineStart(sourceCode, lineEnd);
            String sourceLine = sourceCode.substring(index, lineEnd);
            String lineEnding = sourceCode.substring(lineEnd, nextLine);

            text.append('\n');
            text.append(removeLineContinuation(sourceLine));
            removedLineEndings.append(lineEnding);

            previousLine = sourceLine;
            index = nextLine;
            lineCount++;
        }

        return new ContinuedLine(text.toString(), removedLineEndings.toString(), index, lineCount);
    }

    private static boolean hasLineContinuation(@NotNull String line) {
        int index = line.length() - 1;
        while (index >= 0) {
            char c = line.charAt(index);
            if (c != ' ' && c != '\t')
                break;
            index--;
        }

        return index >= 0 && line.charAt(index) == '\\';
    }

    @NotNull
    private static String removeLineContinuation(@NotNull String line) {
        int index = line.length() - 1;
        while (index >= 0) {
            char c = line.charAt(index);
            if (c != ' ' && c != '\t')
                break;
            index--;
        }

        if (index < 0 || line.charAt(index) != '\\')
            return line;

        return line.substring(0, index);
    }
}
