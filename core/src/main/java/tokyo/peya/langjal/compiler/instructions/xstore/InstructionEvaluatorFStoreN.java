package tokyo.peya.langjal.compiler.instructions.xstore;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.exceptions.IllegalInstructionException;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.*;

public class InstructionEvaluatorFStoreN extends AbstractInstructionEvaluator<JALParser.JvmInsFstoreNContext> {
    public InstructionEvaluatorFStoreN() {
        super(EOpcodes.FSTORE_0, EOpcodes.FSTORE_1, EOpcodes.FSTORE_2, EOpcodes.FSTORE_3);
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsFstoreNContext instruction) {
        JALParser.LocalDeclarationContext ins = instruction.localDeclaration();
        if (has(instruction.INSN_FSTORE_0()))
            return InstructionEvaluateHelperXStore.evaluateN(
                    context,
                    labels,
                    locals,
                    this,
                    EOpcodes.FSTORE,
                    0,
                    "F",
                    ins
            );
        else if (has(instruction.INSN_FSTORE_1()))
            return InstructionEvaluateHelperXStore.evaluateN(
                    context,
                    labels,
                    locals,
                    this,
                    EOpcodes.FSTORE,
                    1,
                    "F",
                    ins
            );
        else if (has(instruction.INSN_FSTORE_2()))
            return InstructionEvaluateHelperXStore.evaluateN(
                    context,
                    labels,
                    locals,
                    this,
                    EOpcodes.FSTORE,
                    2,
                    "F",
                    ins
            );
        else if (has(instruction.INSN_FSTORE_3()))
            return InstructionEvaluateHelperXStore.evaluateN(
                    context,
                    labels,
                    locals,
                    this,
                    EOpcodes.FSTORE,
                    3,
                    "F",
                    ins
            );

        throw new IllegalInstructionException("Unexpected instruction: " + instruction.getText(), instruction);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction) {
        VarInsnNode varInsnNode = (VarInsnNode) instruction.insn();
        return FrameDifferenceInfo.builder(instruction)
                .popPrimitive(StackElementType.FLOAT)
                .addLocalPrimitive(varInsnNode.var, StackElementType.FLOAT)
                .build();
    }

    @Override
    public JALParser.JvmInsFstoreNContext map(JALParser.@NotNull InstructionContext instruction) {
        return instruction.jvmInsFstoreN();
    }
}
