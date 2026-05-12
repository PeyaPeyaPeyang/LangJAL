package tokyo.peya.langjal.compiler.instructions;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.*;

public class InstructionEvaluatorJsrW extends AbstractInstructionEvaluator<JALParser.JvmInsJsrWContext> {
    public InstructionEvaluatorJsrW() {
        super(EOpcodes.JSR_W);
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsJsrWContext instruction) {
        JALParser.LabelNameContext labelNameContext = instruction.labelName();
        LabelInfo label = labels.resolve(labelNameContext);

        JumpInsnNode insn = new JumpInsnNode(EOpcodes.JSR_W, label.node());
        return EvaluatedInstruction.of(this, insn);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction) {
        return FrameDifferenceInfo.builder(instruction)
                .pushReturnAddress()
                .build();
    }

    @Override
    public JALParser.JvmInsJsrWContext map(JALParser.@NotNull InstructionContext instruction) {
        return instruction.jvmInsJsrW();
    }
}
