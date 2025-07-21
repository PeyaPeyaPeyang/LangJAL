package tokyo.peya.langjal.compiler.utils;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.exceptions.IllegalValueException;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;

import java.util.function.Function;

public class EvaluatorCommons
{
    public static int asAccessLevel(@Nullable JALParser.AccessLevelContext accessLevel)
    {
        if (accessLevel == null)
            return 0;

        if (accessLevel.KWD_ACC_PUBLIC() != null)
            return EOpcodes.ACC_PUBLIC;
        if (accessLevel.KWD_ACC_PRIVATE() != null)
            return EOpcodes.ACC_PRIVATE;
        if (accessLevel.KWD_ACC_PROTECTED() != null)
            return EOpcodes.ACC_PROTECTED;

        throw new IllegalArgumentException("Unknown access level: " + accessLevel.getText());
    }

    public static String asString(@NotNull TerminalNode node)
    {
        String text = node.getText();
        if (text == null || text.isEmpty())
            return null;

        if (text.startsWith("\"") && text.endsWith("\""))
            text = text.substring(1, text.length() - 1);
        else if (text.startsWith("'") && text.endsWith("'"))
            text = text.substring(1, text.length() - 1);

        return text.replace("\\\"", "\"")
                   .replace("\\'", "'")
                   .replace("\\\\", "\\");
    }

    public static int asInteger(@NotNull TerminalNode node)
    {
        Number number = toNumber(node);
        if (number == null)
            throw new IllegalValueException("Invalid integer value: " + node.getText(), node);
        return number.intValue();
    }

    public static Number toNumber(@Nullable TerminalNode number)
    {
        if (number == null || number.getText() == null || number.getText().isEmpty())
            return null;

        try
        {
            return toNumber(number.getText());
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalValueException(e.getMessage(), number);
        }
    }

    public static Number toNumber(@Nullable String numberString)
    {
        if (numberString == null || numberString.isEmpty())
            return null;

        String type = getNumberType(numberString);
        Function<String, ? extends Number> parseFunction = getNumberParsingFunction(type);
        if (parseFunction == null)
            throw new IllegalArgumentException("Unknown number type: " + type + " for number: " + numberString);

        if (!type.startsWith("may-"))
        {
            // "may-" で始まらない場合は，接尾辞がついているので，取り除く
            numberString = numberString.replaceAll("[fFdDlL]$", "");
        }
        if (type.endsWith("-hex"))
            numberString = numberString.substring(2);

        try
        {
            return parseFunction.apply(numberString);
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Invalid number format: " + numberString + " for type: " + type, e);
        }
    }

    public static boolean isNumber(@Nullable String number)
    {
        try
        {
            toNumber(number);
            return true;
        }
        catch (IllegalArgumentException e)
        {
            return false;
        }
    }

    private static Function<String, ? extends Number> getNumberParsingFunction(@NotNull String type)
    {
        return switch (type)
        {
            case "float" -> Float::parseFloat;
            case "double", "may-double" -> Double::parseDouble;
            case "long" -> Long::parseLong;
            case "long-hex" -> s -> Long.parseLong(s, 16);
            case "int", "may-int" -> Integer::parseInt;
            case "may-int-hex" -> s -> Integer.parseInt(s, 16);
            default -> null; // null を返すことで fallback させる

        };
    }

    public static String getNumberType(String number)
    {
        if (number == null || number.isEmpty())
            return null;

        if (number.startsWith("0x") || number.startsWith("-0x"))
        {
            if (number.endsWith("l") || number.endsWith("L"))
                return "hex-long";
            else
                return "may-int-hex";
        }

        if (number.endsWith("f") || number.endsWith("F"))
            return "float";
        else if (number.endsWith("d") || number.endsWith("D"))
            return "double";
        else if (number.endsWith("l") || number.endsWith("L"))
            return "long";
        else if (number.contains("."))
            return "may-double";
        else
            return "may-int";
    }

    public static String unwrapClassTypeDescriptor(@NotNull JALParser.TypeDescriptorContext typeDescriptor)
    {
        String typeName = typeDescriptor.getText();
        if (typeName.startsWith("L") && typeName.endsWith(";"))
            return typeName.substring(1, typeName.length() - 1);
        else if (typeName.startsWith("[L") && typeName.endsWith(";"))
            return typeName.substring(2, typeName.length() - 1);
        else
            throw new IllegalValueException("Invalid class type descriptor: " + typeName, typeDescriptor);
    }

    public static boolean toBoolean(@NotNull TerminalNode value)
    {

        String valueText = value.getText();
        if ("true".equalsIgnoreCase(valueText) || "1".equals(valueText))
            return true;
        else if ("false".equalsIgnoreCase(valueText) || "0".equals(valueText))
            return false;
        else
            throw new IllegalValueException("Invalid boolean value: " + valueText, value);
    }

    public static Object evaluateScalar(JALParser.JvmInsArgScalarTypeContext scalar)
    {
        if (scalar.NUMBER() != null)
            return toNumber(scalar.NUMBER());
        else if (scalar.STRING() != null)
        {
            String value = scalar.STRING().getText();
            return value.substring(1, value.length() - 1); // Remove quotes
        }
        else if (scalar.BOOLEAN() != null)
            return toBoolean(scalar.BOOLEAN());
        else
            throw new IllegalValueException("Unknown scalar type: " + scalar.getText(), scalar);
    }
}
