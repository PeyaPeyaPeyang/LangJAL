package tokyo.peya.langjal.compiler;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.exceptions.CompileErrorException;

import java.util.HashSet;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;

final class JALPreprocessor {
    private static final int MAX_EXPANSION_DEPTH = 64;

    private JALPreprocessor() {
    }

    @NotNull
    static String preprocess(@NotNull String sourceCode) throws CompileErrorException {
        Map<String, Macro> defines = new LinkedHashMap<>();
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
                ContinuedLine directive = readContinuedLine(sourceCode, sourceLine, lineEnding, nextLine);
                processDirective(defines, directive.text(), line);
                result.append(directive.removedLineEndings());
                index = directive.nextIndex();
                line += directive.lineCount();
            } else {
                result.append(expandLine(sourceLine, defines, inBlockComment));
                result.append(lineEnding);
                index = nextLine;
                line++;
            }
        }

        return result.toString();
    }

    @NotNull
    private static ContinuedLine readContinuedLine(@NotNull String sourceCode,
                                                   @NotNull String firstLine,
                                                   @NotNull String firstLineEnding,
                                                   int firstNextLine) {
        StringBuilder text = new StringBuilder(removeLineContinuation(firstLine));
        StringBuilder removedLineEndings = new StringBuilder(firstLineEnding);
        int index = firstNextLine;
        int lineCount = 1;

        String previousLine = firstLine;
        while (hasLineContinuation(previousLine) && index < sourceCode.length()) {
            int lineEnd = findLineEnd(sourceCode, index);
            int nextLine = nextLineStart(sourceCode, lineEnd);
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

    private static void processDirective(@NotNull Map<String, Macro> defines,
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
        index = nameEnd;
        if (index < line.length() && line.charAt(index) == '(') {
            MacroDefinition definition = readFunctionLikeMacroDefinition(line, index, lineNumber);
            defines.put(name, new Macro(definition.parameters(), definition.replacement()));
        } else {
            index = skipHorizontalSpaces(line, index);
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
            index = skipHorizontalSpaces(line, index);
            if (index >= line.length())
                throw new CompileErrorException("Expected ')' in macro parameter list", lineNumber, index, 1);

            if (line.charAt(index) == ')') {
                index++;
                break;
            }

            int parameterEnd = readIdentifierEnd(line, index);
            if (parameterEnd == index)
                throw new CompileErrorException("Expected macro parameter name", lineNumber, index, 1);

            parameters.add(line.substring(index, parameterEnd));
            index = skipHorizontalSpaces(line, parameterEnd);
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

        index = skipHorizontalSpaces(line, index);
        return new MacroDefinition(parameters, line.substring(index));
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
                                     @NotNull Map<String, Macro> defines,
                                     boolean @NotNull [] inBlockComment) {
        return expandLine(line, defines, inBlockComment, new HashSet<>());
    }

    @NotNull
    private static String expandLine(@NotNull String line,
                                     @NotNull Map<String, Macro> defines,
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
                Macro macro = defines.get(identifier);
                if (macro == null || expanding.contains(identifier)) {
                    result.append(identifier);
                    index = end;
                } else if (macro.isFunctionLike()) {
                    MacroInvocation invocation = readMacroInvocation(line, end);
                    if (invocation == null) {
                        result.append(identifier);
                        index = end;
                        continue;
                    }

                    String replacement = applyMacroArguments(macro, invocation.arguments());
                    if (expanding.size() >= MAX_EXPANSION_DEPTH) {
                        result.append(replacement);
                    } else {
                        expanding.add(identifier);
                        result.append(expandLine(replacement, defines, new boolean[]{false}, expanding));
                        expanding.remove(identifier);
                    }
                    index = invocation.nextIndex();
                } else {
                    String replacement = macro.replacement();
                    if (expanding.size() >= MAX_EXPANSION_DEPTH) {
                        result.append(replacement);
                        index = end;
                        continue;
                    }

                    expanding.add(identifier);
                    result.append(expandLine(replacement, defines, new boolean[]{false}, expanding));
                    expanding.remove(identifier);
                    index = end;
                }
                continue;
            }

            result.append(c);
            index++;
        }

        return result.toString();
    }

    private static MacroInvocation readMacroInvocation(@NotNull String line, int index) {
        index = skipHorizontalSpaces(line, index);
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

    @NotNull
    private static String applyMacroArguments(@NotNull Macro macro, @NotNull List<String> arguments) {
        List<String> parameters = macro.parameters();
        if (parameters.size() != arguments.size())
            return macro.replacement();

        Map<String, String> replacements = new LinkedHashMap<>();
        for (int i = 0; i < parameters.size(); i++)
            replacements.put(parameters.get(i), arguments.get(i));

        return replaceIdentifiers(macro.replacement(), replacements);
    }

    @NotNull
    private static String replaceIdentifiers(@NotNull String source, @NotNull Map<String, String> replacements) {
        StringBuilder result = new StringBuilder(source.length());
        boolean inString = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;

        int index = 0;
        while (index < source.length()) {
            char c = source.charAt(index);

            if (inLineComment) {
                result.append(c);
                index++;
                continue;
            }

            if (inBlockComment) {
                result.append(c);
                if (c == '*' && index + 1 < source.length() && source.charAt(index + 1) == '/') {
                    result.append('/');
                    inBlockComment = false;
                    index += 2;
                } else {
                    index++;
                }
                continue;
            }

            if (inString) {
                result.append(c);
                if (c == '\\' && index + 1 < source.length()) {
                    result.append(source.charAt(index + 1));
                    index += 2;
                } else {
                    if (c == '"')
                        inString = false;
                    index++;
                }
                continue;
            }

            if (c == '/' && index + 1 < source.length()) {
                char next = source.charAt(index + 1);
                if (next == '/') {
                    result.append("//");
                    inLineComment = true;
                    index += 2;
                    continue;
                }

                if (next == '*') {
                    result.append("/*");
                    inBlockComment = true;
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
                int end = readIdentifierEnd(source, index);
                String identifier = source.substring(index, end);
                result.append(replacements.getOrDefault(identifier, identifier));
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

    private record ContinuedLine(@NotNull String text,
                                 @NotNull String removedLineEndings,
                                 int nextIndex,
                                 int lineCount) {
    }

    private record Macro(List<String> parameters, @NotNull String replacement) {
        private boolean isFunctionLike() {
            return this.parameters != null;
        }
    }

    private record MacroDefinition(@NotNull List<String> parameters, @NotNull String replacement) {
    }

    private record MacroInvocation(@NotNull List<String> arguments, int nextIndex) {
    }
}
