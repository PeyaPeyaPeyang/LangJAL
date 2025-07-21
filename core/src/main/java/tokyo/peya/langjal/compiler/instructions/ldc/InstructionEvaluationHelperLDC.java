package tokyo.peya.langjal.compiler.instructions.ldc;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.LdcInsnNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.exceptions.IllegalInstructionException;
import tokyo.peya.langjal.compiler.exceptions.InternalCompileErrorException;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.utils.EvaluatorCommons;

public class InstructionEvaluationHelperLDC
{
    public static final int LDC = 0;
    public static final int LDC_W = 1;
    public static final int LDC2_W = 2;

    public static @NotNull EvaluatedInstruction evaluate(@NotNull AbstractInstructionEvaluator<?> evaluator,
                                                         JALParser.@NotNull JvmInsArgScalarTypeContext scalar,
                                                         int ldcType)

    {
        if (ldcType < LDC || ldcType > LDC2_W)
            throw new InternalCompileErrorException("Invalid LDC type: " + ldcType, scalar);

        LdcInsnNode ldcInsnNode;
        TerminalNode number = scalar.NUMBER();
        TerminalNode string = scalar.STRING();
        if (string != null)
        {
            if (ldcType == LDC2_W || ldcType == LDC_W)
                throw new IllegalInstructionException(
                        "ldc2_w cannot be used with string literals, please use ldc or ldc_w instead.",
                        scalar
                );

            String value = string.getText();
            value = value.substring(1, value.length() - 1); // Remove quotes
            ldcInsnNode = new LdcInsnNode(value);
            return EvaluatedInstruction.of(evaluator, ldcInsnNode);
        }
        else if (number == null)
            throw new IllegalInstructionException(
                    "ldc instruction requires a number or string literal, but found: " + scalar.getText(),
                    scalar
            );

        // assert number != null;

        Number numberValue = EvaluatorCommons.toNumber(number.getText());
        if (numberValue == null)
            throw new IllegalInstructionException("Invalid number value: " + number.getText(), number);

        String numberType = EvaluatorCommons.getNumberType(number.getText());
        boolean isCategory2 = numberType.equals("double") || numberType.equals("long") || numberType.equals("hex-long");
        if (ldcType == LDC2_W && !isCategory2)
            throw new IllegalInstructionException(
                    "ldc2_w can only be used with double or long values, but found: " + numberType, number
            );
        else if (ldcType == LDC && isCategory2)
            throw new IllegalInstructionException(
                    "ldc cannot be used with double or long values, please use ldc2_w instead.",
                    number
            );

        int instructionSize = ldcType == LDC ? 1: (ldcType == LDC_W ? 2: 3);
        ldcInsnNode = new LdcInsnNode(numberValue);
        return EvaluatedInstruction.of(evaluator, ldcInsnNode, instructionSize);
    }

    public static FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        LdcInsnNode ldcInsn = (LdcInsnNode) instruction.insn();
        Object value = ldcInsn.cst;

        FrameDifferenceInfo.Builder builder = FrameDifferenceInfo.builder(instruction);
        if (value instanceof String)
            builder.pushObjectRef(TypeDescriptor.className("java/lang/String"));
        else if (value instanceof Integer || value instanceof Character ||
                value instanceof Byte || value instanceof Short)
            builder.pushPrimitive(StackElementType.INTEGER);
        else if (value instanceof Long)
            builder.pushPrimitive(StackElementType.LONG);
        else if (value instanceof Float)
            builder.pushPrimitive(StackElementType.FLOAT);
        else if (value instanceof Double)
            builder.pushPrimitive(StackElementType.DOUBLE);
        else if (value instanceof Type type)
        {
            switch (type.getSort())
            {
                case Type.OBJECT:
                case Type.ARRAY:
                    // → java.lang.Class インスタンスを push
                    builder.pushObjectRef(TypeDescriptor.className("java/lang/Class"));
                    break;
                case Type.METHOD:
                    // → java.lang.invoke.MethodType インスタンスを push
                    builder.pushObjectRef(TypeDescriptor.className("java/lang/invoke/MethodType"));
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected Type sort in ldc: " + type.getSort());
            }
        }
        else if (value instanceof Handle handle)
            builder.pushObjectRef(TypeDescriptor.className("java/lang/invoke/MethodHandle"));
        else
            throw new InternalCompileErrorException(
                    "Unsupported constant type in ldc: " + value.getClass().getName(),
                    new IllegalArgumentException("Unsupported constant type in ldc: " + value.getClass().getName())
            );

        return builder.build();
    }
}
