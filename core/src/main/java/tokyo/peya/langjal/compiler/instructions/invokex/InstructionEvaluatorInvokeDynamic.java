package tokyo.peya.langjal.compiler.instructions.invokex;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.compiler.exceptions.IllegalInstructionException;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.JALMethodCompiler;
import tokyo.peya.langjal.compiler.utils.EvaluatorCommons;

import java.util.List;

public class InstructionEvaluatorInvokeDynamic
        extends AbstractInstructionEvaluator<JALParser.JvmInsInvokedynamicContext>
{
    @Override
    protected @NotNull EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler,
                                                     JALParser.@NotNull JvmInsInvokedynamicContext ctxt)
    {
        String methodName = ctxt.methodName().getText();
        String methodDesc = ctxt.methodDescriptor().getText();
        Handle bootstrapMethod = toHandle(ctxt.jvmInsArgInvokeDynamicMethodTypeMethodHandle());
        List<JALParser.JvmInsArgInvokeDynamicRefContext> args = ctxt.jvmInsArgInvokeDynamicRef();
        List<Object> bootstrapArgs = args.stream()
                                         .map(InstructionEvaluatorInvokeDynamic::evaluateBootstrapArg)
                                         .toList();

        InvokeDynamicInsnNode insn = new InvokeDynamicInsnNode(
                methodName,
                methodDesc,
                bootstrapMethod,
                bootstrapArgs.toArray(new Object[0])
        );
        return EvaluatedInstruction.of(this, insn, calcSize(ctxt));
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return InstructionEvaluateHelperInvocation.getFrameInvokedynamicFrameDifference(instruction);
    }

    @Override
    protected JALParser.JvmInsInvokedynamicContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsInvokedynamic();
    }

    private static int calcSize(JALParser.JvmInsInvokedynamicContext ctxt)
    {
        int size = 0;
        if (ctxt.jvmInsArgInvokeDynamicMethodTypeMethodHandle() != null)
            size += 1; // Handle
        for (JALParser.JvmInsArgInvokeDynamicRefContext arg : ctxt.jvmInsArgInvokeDynamicRef())
        {
            if (arg.jvmInsArgInvokeDynamicMethodType() != null)
                size += 1; // Method type
            else if (arg.jvmInsArgScalarType() != null)
                size += 1; // Scalar type
        }
        return size;
    }

    private static Object evaluateBootstrapArg(JALParser.JvmInsArgInvokeDynamicRefContext arg)
    {
        if (arg.jvmInsArgScalarType() != null)
        {
            JALParser.JvmInsArgScalarTypeContext scalarType = arg.jvmInsArgScalarType();
            return EvaluatorCommons.evaluateScalar(scalarType);
        }
        else if (arg.jvmInsArgInvokeDynamicMethodType() != null)
        {
            String desc = arg.jvmInsArgInvokeDynamicMethodType().methodDescriptor().getText();
            return Type.getMethodType(desc);
        }
        else if (arg.jvmInsArgInvokeDynamicMethodTypeMethodHandle() != null)
        {
            JALParser.JvmInsArgInvokeDynamicMethodTypeMethodHandleContext handle
                    = arg.jvmInsArgInvokeDynamicMethodTypeMethodHandle();
            return toHandle(handle);
        }
        else
            throw new IllegalInstructionException("Invalid bootstrap argument type: " + arg.getText(), arg);
    }

    private static Handle toHandle(JALParser.JvmInsArgInvokeDynamicMethodTypeMethodHandleContext handle)
    {
        JALParser.JvmInsArgMethodRefContext ref = handle.jvmInsArgMethodRef();
        String ownerType = ref.jvmInsArgMethodRefOwnerType().getText();
        String methodName = ref.methodName().getText();
        String methodDesc = ref.methodDescriptor().getText();
        int tag = toTag(handle.jvmInsArgInvokeDynamicMethodHandleType());

        return new Handle(
                tag,
                ownerType,
                methodName,
                methodDesc,
                tag == EOpcodes.H_INVOKEINTERFACE
        );
    }

    private static int toTag(JALParser.JvmInsArgInvokeDynamicMethodHandleTypeContext handle)
    {
        if (handle.INSN_GETFIELD() != null)
            return EOpcodes.H_GETFIELD;
        else if (handle.INSN_GETSTATIC() != null)
            return EOpcodes.H_GETSTATIC;
        else if (handle.INSN_PUTFIELD() != null)
            return EOpcodes.H_PUTFIELD;
        else if (handle.INSN_PUTSTATIC() != null)
            return EOpcodes.H_PUTSTATIC;
        else if (handle.INSN_INVOKEVIRTUAL() != null)
            return EOpcodes.H_INVOKEVIRTUAL;
        else if (handle.INSN_INVOKESTATIC() != null)
            return EOpcodes.H_INVOKESTATIC;
        else if (handle.INSN_INVOKESPECIAL() != null)
            return EOpcodes.H_INVOKESPECIAL;
        else if (handle.KWD_METHOD_HANDLE_TAG_NEWINVOKE() != null)
            return EOpcodes.H_NEWINVOKESPECIAL;
        else if (handle.INSN_INVOKEINTERFACE() != null)
            return EOpcodes.H_INVOKEINTERFACE;
        else
            throw new IllegalInstructionException("Unknown method handle type: " + handle.getText(), handle);
    }
}
