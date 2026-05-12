package tokyo.peya.langjal.compiler.instructions.xconst;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.exceptions.IllegalInstructionException;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.*;

public class InstructionEvaluatorLConstN extends AbstractInstructionEvaluator<JALParser.JvmInsLconstNContext> {
    public InstructionEvaluatorLConstN() {
        super(EOpcodes.LCONST_0, EOpcodes.LCONST_1);
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsLconstNContext instruction) {
        if (has(instruction.INSN_LCONST_0()))
            return this.visitSingle(instruction, EOpcodes.LCONST_0);
        if (has(instruction.INSN_LCONST_1()))
            return this.visitSingle(instruction, EOpcodes.LCONST_1);

        throw new IllegalInstructionException("Unknown instruction: " + instruction.getText(), instruction);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction) {
        return FrameDifferenceInfo.builder(instruction)
                .pushPrimitive(StackElementType.LONG)
                .build();
    }

    @Override
    public JALParser.JvmInsLconstNContext map(JALParser.@NotNull InstructionContext instruction) {
        return instruction.jvmInsLconstN();
    }
}
