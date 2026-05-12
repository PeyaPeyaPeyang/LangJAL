package tokyo.peya.langjal.compiler.instructions.calc;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.exceptions.IllegalInstructionException;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.*;
import tokyo.peya.langjal.compiler.utils.EvaluatorCommons;

public class InstructionEvaluatorIInc extends AbstractInstructionEvaluator<JALParser.JvmInsIincContext> {
    public InstructionEvaluatorIInc() {
        super(EOpcodes.IINC);
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsIincContext instruction) {
        JALParser.JvmInsArgLocalRefContext ref = instruction.jvmInsArgLocalRef();
        LocalVariableInfo local = locals.resolve(ref, "iinc");

        int idx = local.index();
        boolean isWide = instruction.INSN_WIDE() != null;
        int increment = EvaluatorCommons.asInteger(instruction.NUMBER());

        if (!isWide) {
            if (idx >= 0xFF)
                throw new IllegalInstructionException(
                        String.format(
                                "Local variable index %d is too large for iinc instruction. Use wide variant with.",
                                idx
                        ), ref
                );
            else if (increment < Byte.MIN_VALUE || increment > Byte.MAX_VALUE)
                throw new IllegalInstructionException(
                        String.format(
                                "Increment value %d is out of range for iinc instruction. Use wide variant with.",
                                increment
                        ), ref
                );
        }

        int size = isWide ? 6 : 3;
        IincInsnNode insn = new IincInsnNode(idx, increment);
        return EvaluatedInstruction.of(this, insn, size);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction) {
        IincInsnNode insn = (IincInsnNode) instruction.insn();
        return FrameDifferenceInfo.builder(instruction)
                .consumeLocalPrimitive(insn.var, StackElementType.INTEGER)
                .addLocalPrimitive(insn.var, StackElementType.INTEGER)
                .build();

    }

    @Override
    public JALParser.JvmInsIincContext map(JALParser.@NotNull InstructionContext instruction) {
        return instruction.jvmInsIinc();
    }
}
