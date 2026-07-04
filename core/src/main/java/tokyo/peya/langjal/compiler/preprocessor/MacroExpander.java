package tokyo.peya.langjal.compiler.preprocessor;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class MacroExpander {
    private static final int MAX_EXPANSION_DEPTH = 64;

    private MacroExpander() {
    }

    @NotNull
    static String expandLine(@NotNull String line,
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

            if (PreprocessorSyntax.isIdentifierStart(c)) {
                int end = PreprocessorSyntax.readIdentifierEnd(line, index);
                String identifier = line.substring(index, end);
                Macro macro = defines.get(identifier);
                index = expandMacro(line, defines, expanding, result, end, identifier, macro);
                continue;
            }

            result.append(c);
            index++;
        }

        return result.toString();
    }

    private static int expandMacro(@NotNull String line,
                                   @NotNull Map<String, Macro> defines,
                                   @NotNull Set<String> expanding,
                                   @NotNull StringBuilder result,
                                   int end,
                                   @NotNull String identifier,
                                   Macro macro) {
        if (macro == null || expanding.contains(identifier)) {
            result.append(identifier);
            return end;
        }

        if (macro.isFunctionLike()) {
            MacroInvocation invocation = MacroInvocation.read(line, end);
            if (invocation == null) {
                result.append(identifier);
                return end;
            }

            appendExpandedReplacement(defines, expanding, result, identifier, applyMacroArguments(macro, invocation.arguments()));
            return invocation.nextIndex();
        }

        appendExpandedReplacement(defines, expanding, result, identifier, macro.replacement());
        return end;
    }

    private static void appendExpandedReplacement(@NotNull Map<String, Macro> defines,
                                                  @NotNull Set<String> expanding,
                                                  @NotNull StringBuilder result,
                                                  @NotNull String identifier,
                                                  @NotNull String replacement) {
        if (expanding.size() >= MAX_EXPANSION_DEPTH) {
            result.append(replacement);
            return;
        }

        expanding.add(identifier);
        result.append(expandLine(replacement, defines, new boolean[]{false}, expanding));
        expanding.remove(identifier);
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

            if (PreprocessorSyntax.isIdentifierStart(c)) {
                int end = PreprocessorSyntax.readIdentifierEnd(source, index);
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
}
