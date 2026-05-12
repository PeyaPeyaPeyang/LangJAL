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

public class InstructionEvaluatorIStoreN extends AbstractInstructionEvaluator<JALParser.JvmInsIstoreNContext> {
    public InstructionEvaluatorIStoreN() {
        super(EOpcodes.ISTORE_0, EOpcodes.ISTORE_1, EOpcodes.ISTORE_2, EOpcodes.ISTORE_3);
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsIstoreNContext instruction) {
        JALParser.LocalDeclarationContext ins = instruction.localDeclaration();
        if (has(instruction.INSN_ISTORE_0()))
            return InstructionEvaluateHelperXStore.evaluateN(
                    context,
                    labels,
                    locals,
                    this,
                    EOpcodes.ISTORE,
                    0,
                    "I",
                    ins
            );
        else if (has(instruction.INSN_ISTORE_1()))
            return InstructionEvaluateHelperXStore.evaluateN(
                    context,
                    labels,
                    locals,
                    this,
                    EOpcodes.ISTORE,
                    1,
                    "I",
                    ins
            );
        else if (has(instruction.INSN_ISTORE_2()))
            return InstructionEvaluateHelperXStore.evaluateN(
                    context,
                    labels,
                    locals,
                    this,
                    EOpcodes.ISTORE,
                    2,
                    "I",
                    ins
            );
        else if (has(instruction.INSN_ISTORE_3()))
            return InstructionEvaluateHelperXStore.evaluateN(
                    context,
                    labels,
                    locals,
                    this,
                    EOpcodes.ISTORE,
                    3,
                    "I",
                    ins
            );

        throw new IllegalInstructionException("Unexpected instruction: " + instruction.getText(), instruction);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction) {
        VarInsnNode varInsnNode = (VarInsnNode) instruction.insn();
        return FrameDifferenceInfo.builder(instruction)
                .popPrimitive(StackElementType.INTEGER)
                .addLocalPrimitive(varInsnNode.var, StackElementType.INTEGER)
                .build();
    }

    @Override
    public JALParser.JvmInsIstoreNContext map(JALParser.@NotNull InstructionContext instruction) {
        return instruction.jvmInsIstoreN();
    }
}
