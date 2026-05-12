package tokyo.peya.langjal.compiler.instructions.xload;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementCapsule;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.member.*;

public class InstructionEvaluatorALoad extends AbstractInstructionEvaluator<JALParser.JvmInsAloadContext> {
    public InstructionEvaluatorALoad() {
        super(Opcodes.ALOAD);
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsAloadContext instruction) {
        return InstructionEvaluateHelperXLoad.evaluate(
                this,
                locals,
                instruction.jvmInsArgLocalRef(),
                Opcodes.ALOAD,
                "aload",
                instruction.INSN_WIDE()
        );
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction) {
        VarInsnNode insn = (VarInsnNode) instruction.insn();
        StackElementCapsule capsule = new StackElementCapsule(instruction);
        return FrameDifferenceInfo.builder(instruction)
                .consumeLocal(insn.var, capsule)
                .pushFromCapsule(capsule)
                .build();
    }

    @Override
    public JALParser.JvmInsAloadContext map(JALParser.@NotNull InstructionContext instruction) {
        return instruction.jvmInsAload();
    }
}
