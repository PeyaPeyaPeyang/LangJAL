package tokyo.peya.langjal.compiler.instructions.invokex;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.MethodDescriptor;
import tokyo.peya.langjal.compiler.jvm.PrimitiveTypes;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public class InstructionEvaluateHelperInvocation
{
    @NotNull
    public static EvaluatedInstruction evaluate(@NotNull AbstractInstructionEvaluator<?> evaluator,
                                                @NotNull ClassNode ownerClazz,
                                                @NotNull JALParser.JvmInsArgMethodRefContext ref, int opcode)
    {
        JALParser.JvmInsArgMethodRefOwnerTypeContext methodOwner = ref.jvmInsArgMethodRefOwnerType();
        JALParser.MethodNameContext methodName = ref.methodName();
        JALParser.MethodDescriptorContext methodDescriptor = ref.methodDescriptor();
        return evaluate(
                evaluator,
                methodOwner == null ? ownerClazz.name: methodOwner.getText(),
                methodName.getText(),
                methodDescriptor.getText(),
                opcode
        );
    }

    public static FrameDifferenceInfo getFrameNormalDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        MethodInsnNode method = (MethodInsnNode) instruction.insn();
        MethodDescriptor descriptor = MethodDescriptor.parse(method.desc);
        TypeDescriptor returnType = descriptor.getReturnType();
        TypeDescriptor[] parameterTypes = descriptor.getParameterTypes();

        FrameDifferenceInfo.Builder builder = FrameDifferenceInfo.builder(instruction);
        opArguments(builder, instruction, parameterTypes);
        if (!(method.getOpcode() == EOpcodes.INVOKESTATIC || method.getOpcode() == EOpcodes.INVOKEDYNAMIC))
        {
            // インスタンスメソッドの場合は，所有者クラスのインスタンスをスタックからポップする
            builder.popObjectRef(TypeDescriptor.className(method.owner));
        }

        opReturnType(builder, instruction, returnType);
        return builder.build();
    }


    public static FrameDifferenceInfo getFrameInvokedynamicFrameDifference(@NotNull InstructionInfo instruction)
    {
        InvokeDynamicInsnNode method = (InvokeDynamicInsnNode) instruction.insn();
        MethodDescriptor descriptor = MethodDescriptor.parse(method.desc);
        TypeDescriptor returnType = descriptor.getReturnType();
        TypeDescriptor[] parameterTypes = descriptor.getParameterTypes();

        FrameDifferenceInfo.Builder builder = FrameDifferenceInfo.builder(instruction);
        opArguments(builder, instruction, parameterTypes);
        opReturnType(builder, instruction, returnType);
        return builder.build();
    }

    public static FrameDifferenceInfo.Builder opArguments(@NotNull FrameDifferenceInfo.Builder builder,
                                                          @NotNull InstructionInfo instruction,
                                                          @NotNull TypeDescriptor[] parameterTypes)
    {

        for (int i = parameterTypes.length - 1; i >= 0; i--)   // 逆順
        {
            TypeDescriptor type = parameterTypes[i];
            builder.pop(type.toStackElement(instruction));
        }

        return builder;
    }

    @NotNull
    public static FrameDifferenceInfo.Builder opReturnType(@NotNull FrameDifferenceInfo.Builder builder,
                                                           @NotNull InstructionInfo instruction,
                                                           @NotNull TypeDescriptor returnType)
    {
        if (returnType.getBaseType() != PrimitiveTypes.VOID)
            builder.push(returnType.toStackElement(instruction));
        return builder;
    }

    public static EvaluatedInstruction evaluate(@NotNull AbstractInstructionEvaluator<?> evaluator,
                                                @NotNull String ownerType,
                                                @NotNull String methodName,
                                                @NotNull String methodDescriptor, int opcode)
    {
        MethodInsnNode insn = new MethodInsnNode(
                opcode,
                ownerType,
                methodName,
                methodDescriptor
        );
        return EvaluatedInstruction.of(evaluator, insn);
    }
}
