package tokyo.peya.langjal.compiler.instructions.invokex;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.NopElement;
import tokyo.peya.langjal.analyser.stack.ObjectElement;
import tokyo.peya.langjal.analyser.stack.StackElementCapsule;
import tokyo.peya.langjal.analyser.stack.UninitializedThisElement;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.MethodDescriptor;
import tokyo.peya.langjal.compiler.jvm.PrimitiveTypes;
import tokyo.peya.langjal.compiler.jvm.Type;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.JALMethodCompiler;

public class InstructionEvaluatorInvokeSpecial
        extends AbstractInstructionEvaluator<JALParser.JvmInsInvokespecialContext>
{
    @Override
    protected @NotNull EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler,
                                                     JALParser.@NotNull JvmInsInvokespecialContext ctxt)
    {
        JALParser.JvmInsArgMethodRefContext ref = ctxt.jvmInsArgMethodRef();
        String methodName = ref.methodName().getText();

        // Owner が指定されていない場合は，命令を持つメソッドのクラスが所有者となる
        JALParser.JvmInsArgMethodRefOwnerTypeContext ownerType = ref.jvmInsArgMethodRefOwnerType();
        String ownerName = ownerType == null ? compiler.getClazz().name: ownerType.getText();

        return InstructionEvaluateHelperInvocation.evaluate(
                this,
                ownerName,
                methodName,
                ref.methodDescriptor().getText(),
                EOpcodes.INVOKESPECIAL
        );
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        MethodInsnNode method = (MethodInsnNode) instruction.insn();
        MethodDescriptor descriptor = MethodDescriptor.parse(method.desc);
        TypeDescriptor returnTypeDesc = descriptor.getReturnType();
        TypeDescriptor[] parameterTypes = descriptor.getParameterTypes();

        FrameDifferenceInfo.Builder builder = FrameDifferenceInfo.builder(instruction);
        InstructionEvaluateHelperInvocation.opArguments(
                builder,
                instruction,
                parameterTypes
        );

        opSpecialInvocation(instruction, builder, method);

        // 戻り値の型に応じてスタックを調整
        InstructionEvaluateHelperInvocation.opReturnType(
                builder,
                instruction,
                returnTypeDesc
        );
        return builder.build();
    }

    private static void opSpecialInvocation(@NotNull InstructionInfo instruction, @NotNull FrameDifferenceInfo.Builder builder,
                                     @NotNull MethodInsnNode method)
    {
        StackElementCapsule uninitialisedRefCapsule = new StackElementCapsule(
                instruction, actualElm -> {
            // UninitializedThisElement の場合は，ObjectElement をローカル変数０に入れる
            if (actualElm instanceof UninitializedThisElement)
                return new ObjectElement(
                        instruction,
                        TypeDescriptor.className(method.owner)
                );

            return actualElm; // 通常の ObjectElement であればそのまま返す
        }
        );
        if (method.name.equals("<init>"))
            builder.popToCapsule(uninitialisedRefCapsule);
        else
            builder.popObjectRef();  // 通常のメソッド呼び出し

        if (method.name.equals("<init>") && method.owner.equals(instruction.ownerClass().superName))
            builder.addLocalFromCapsule(0, uninitialisedRefCapsule);
    }

    @Override
    protected JALParser.JvmInsInvokespecialContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsInvokespecial();
    }
}
