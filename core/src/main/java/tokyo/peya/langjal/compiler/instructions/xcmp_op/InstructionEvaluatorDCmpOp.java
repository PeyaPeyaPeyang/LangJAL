package tokyo.peya.langjal.compiler.instructions.xcmp_op;

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

public class InstructionEvaluatorDCmpOp extends AbstractInstructionEvaluator<JALParser.JvmInsDcmpOPContext> {
    public InstructionEvaluatorDCmpOp() {
        super(EOpcodes.DCMPG, EOpcodes.DCMPL);
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsDcmpOPContext instruction) {
        if (has(instruction.INSN_DCMPG()))
            return this.visitSingle(instruction, EOpcodes.DCMPG);
        else if (has(instruction.INSN_DCMPL()))
            return this.visitSingle(instruction, EOpcodes.DCMPL);

        throw new IllegalInstructionException("Invalid instruction: " + instruction.getText(), instruction);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction) {
        return FrameDifferenceInfo.builder(instruction)
                .popPrimitive(StackElementType.DOUBLE)
                .popPrimitive(StackElementType.DOUBLE)
                .pushPrimitive(StackElementType.INTEGER)
                .build();
    }

    @Override
    public JALParser.JvmInsDcmpOPContext map(JALParser.@NotNull InstructionContext instruction) {
        return instruction.jvmInsDcmpOP();
    }
}
