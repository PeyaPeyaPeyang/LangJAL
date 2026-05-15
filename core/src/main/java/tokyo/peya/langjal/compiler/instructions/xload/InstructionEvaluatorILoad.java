package tokyo.peya.langjal.compiler.instructions.xload;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.member.*;

public class InstructionEvaluatorILoad extends AbstractInstructionEvaluator<JALParser.JvmInsIloadContext> {
    public InstructionEvaluatorILoad() {
        super(Opcodes.ILOAD);
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsIloadContext instruction) {
        return InstructionEvaluateHelperXLoad.evaluate(
                this,
                locals,
                instruction.jvmInsArgLocalRef(),
                Opcodes.ILOAD,
                "iload",
                instruction.INSN_WIDE()
        );
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction) {
        VarInsnNode insn = (VarInsnNode) instruction.insn();
        return FrameDifferenceInfo.builder(instruction)
                .consumeLocalPrimitive(insn.var, StackElementType.INTEGER)
                .pushPrimitive(StackElementType.INTEGER)
                .build();
    }

    @Override
    public JALParser.JvmInsIloadContext map(JALParser.@NotNull InstructionContext instruction) {
        return instruction.jvmInsIload();
    }
}
