package tokyo.peya.langjal.compiler.preprocessor;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.exceptions.CompileErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class MacroDirectiveParser {
    private MacroDirectiveParser() {
    }

    static void process(@NotNull Map<String, Macro> defines,
                        @NotNull String line,
                        int lineNumber) throws CompileErrorException {
        int index = PreprocessorSyntax.skipHorizontalSpaces(line, 0);
        int directiveStart = index;
        index++;
        index = PreprocessorSyntax.skipHorizontalSpaces(line, index);

        int directiveEnd = PreprocessorSyntax.readIdentifierEnd(line, index);
        String directive = line.substring(index, directiveEnd);
        if (!"define".equals(directive))
            throw new CompileErrorException(
                    "Unsupported preprocessor directive: #" + directive,
                    lineNumber,
                    directiveStart,
                    line.length() - directiveStart
            );

        index = PreprocessorSyntax.skipHorizontalSpaces(line, directiveEnd);
        int nameEnd = PreprocessorSyntax.readIdentifierEnd(line, index);
        if (nameEnd == index)
            throw new CompileErrorException("Expected macro name after #define", lineNumber, index, 1);

        String name = line.substring(index, nameEnd);
        index = nameEnd;
        if (index < line.length() && line.charAt(index) == '(') {
            MacroDefinition definition = readFunctionLikeMacroDefinition(line, index, lineNumber);
            defines.put(name, new Macro(definition.parameters(), definition.replacement()));
        } else {
            index = PreprocessorSyntax.skipHorizontalSpaces(line, index);
            defines.put(name, new Macro(null, line.substring(index)));
        }
    }

    @NotNull
    private static MacroDefinition readFunctionLikeMacroDefinition(@NotNull String line,
                                                                   int index,
                                                                   int lineNumber)
            throws CompileErrorException {
        index++;
        List<String> parameters = new ArrayList<>();

        while (true) {
            index = PreprocessorSyntax.skipHorizontalSpaces(line, index);
            if (index >= line.length())
                throw new CompileErrorException("Expected ')' in macro parameter list", lineNumber, index, 1);

            if (line.charAt(index) == ')') {
                index++;
                break;
            }

            int parameterEnd = PreprocessorSyntax.readIdentifierEnd(line, index);
            if (parameterEnd == index)
                throw new CompileErrorException("Expected macro parameter name", lineNumber, index, 1);

            parameters.add(line.substring(index, parameterEnd));
            index = PreprocessorSyntax.skipHorizontalSpaces(line, parameterEnd);
            if (index >= line.length())
                throw new CompileErrorException("Expected ')' in macro parameter list", lineNumber, index, 1);

            char separator = line.charAt(index);
            if (separator == ',') {
                index++;
            } else if (separator == ')') {
                index++;
                break;
            } else {
                throw new CompileErrorException("Expected ',' or ')' in macro parameter list", lineNumber, index, 1);
            }
        }

        index = PreprocessorSyntax.skipHorizontalSpaces(line, index);
        return new MacroDefinition(parameters, line.substring(index));
    }
}
