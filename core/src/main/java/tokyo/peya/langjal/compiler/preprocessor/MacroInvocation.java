package tokyo.peya.langjal.compiler.preprocessor;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

record MacroInvocation(@NotNull List<String> arguments, int nextIndex) {
    static MacroInvocation read(@NotNull String line, int index) {
        index = PreprocessorSyntax.skipHorizontalSpaces(line, index);
        if (index >= line.length() || line.charAt(index) != '(')
            return null;

        index++;
        int argumentStart = index;
        int depth = 0;
        boolean inString = false;
        List<String> arguments = new ArrayList<>();

        while (index < line.length()) {
            char c = line.charAt(index);
            if (inString) {
                if (c == '\\' && index + 1 < line.length()) {
                    index += 2;
                } else {
                    if (c == '"')
                        inString = false;
                    index++;
                }
                continue;
            }

            if (c == '"') {
                inString = true;
                index++;
                continue;
            }

            if (c == '(') {
                depth++;
                index++;
                continue;
            }

            if (c == ')') {
                if (depth == 0) {
                    String argument = line.substring(argumentStart, index).trim();
                    if (!argument.isEmpty() || !arguments.isEmpty())
                        arguments.add(argument);
                    return new MacroInvocation(arguments, index + 1);
                }

                depth--;
                index++;
                continue;
            }

            if (c == ',' && depth == 0) {
                arguments.add(line.substring(argumentStart, index).trim());
                index++;
                argumentStart = index;
                continue;
            }

            index++;
        }

        return null;
    }
}
