package tokyo.peya.langjal.compiler.instructions;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.exceptions.IllegalInstructionException;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.*;

public class InstructionEvaluatorRet extends AbstractInstructionEvaluator<JALParser.JvmInsRetContext> {
    public InstructionEvaluatorRet() {
        super(EOpcodes.RET);
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsRetContext instruction) {
        LocalVariableInfo local = locals.resolve(instruction.jvmInsArgLocalRef(), "ret");

        int idx = local.index();
        boolean isWide = instruction.INSN_WIDE() != null;
        if (idx >= 0xFF && !isWide)
            throw new IllegalInstructionException(
                    String.format(
                            "Local variable index %d is too large for ret instruction. Use wide variant with.",
                            idx
                    ), instruction.jvmInsArgLocalRef()
            );

        VarInsnNode insn = new VarInsnNode(EOpcodes.RET, local.index());

        int size = isWide ? 4 : 2;
        return EvaluatedInstruction.of(this, insn, size);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction) {
        return FrameDifferenceInfo.same();
    }

    @Override
    public JALParser.JvmInsRetContext map(JALParser.@NotNull InstructionContext instruction) {
        return instruction.jvmInsRet();
    }
}
