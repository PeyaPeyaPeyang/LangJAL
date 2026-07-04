package tokyo.peya.langjal.compiler.preprocessor;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.exceptions.CompileErrorException;

import java.util.LinkedHashMap;
import java.util.Map;

public final class JALPreprocessor {
    private JALPreprocessor() {
    }

    @NotNull
    public static String preprocess(@NotNull String sourceCode) throws CompileErrorException {
        Map<String, Macro> defines = new LinkedHashMap<>();
        StringBuilder result = new StringBuilder(sourceCode.length());
        boolean[] inBlockComment = {false};

        int line = 1;
        int index = 0;
        while (index < sourceCode.length()) {
            int lineEnd = PreprocessorSyntax.findLineEnd(sourceCode, index);
            int nextLine = PreprocessorSyntax.nextLineStart(sourceCode, lineEnd);
            String sourceLine = sourceCode.substring(index, lineEnd);
            String lineEnding = sourceCode.substring(lineEnd, nextLine);

            if (!inBlockComment[0] && PreprocessorSyntax.isPreprocessorDirective(sourceLine)) {
                ContinuedLine directive = ContinuedLine.read(sourceCode, sourceLine, lineEnding, nextLine);
                MacroDirectiveParser.process(defines, directive.text(), line);
                result.append(directive.removedLineEndings());
                index = directive.nextIndex();
                line += directive.lineCount();
            } else {
                result.append(MacroExpander.expandLine(sourceLine, defines, inBlockComment));
                result.append(lineEnding);
                index = nextLine;
                line++;
            }
        }

        return result.toString();
    }
}
