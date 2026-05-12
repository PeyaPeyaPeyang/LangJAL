package tokyo.peya.langjal.compiler.instructions.xstore;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementCapsule;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.exceptions.IllegalInstructionException;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.ClassReferenceType;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.*;

public class InstructionEvaluatorAStoreN extends AbstractInstructionEvaluator<JALParser.JvmInsAstoreNContext> {
    public InstructionEvaluatorAStoreN() {
        super(EOpcodes.ASTORE_0, EOpcodes.ASTORE_1, EOpcodes.ASTORE_2, EOpcodes.ASTORE_3);
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsAstoreNContext instruction) {
        String type = ClassReferenceType.OBJECT.toString();
        JALParser.LocalDeclarationContext ins = instruction.localDeclaration();
        if (has(instruction.INSN_ASTORE_0()))
            return InstructionEvaluateHelperXStore.evaluateN(
                    context,
                    labels,
                    locals,
                    this,
                    EOpcodes.ASTORE,
                    0,
                    type,
                    ins
            );
        else if (has(instruction.INSN_ASTORE_1()))
            return InstructionEvaluateHelperXStore.evaluateN(
                    context,
                    labels,
                    locals,
                    this,
                    EOpcodes.ASTORE,
                    1,
                    type,
                    ins
            );
        else if (has(instruction.INSN_ASTORE_2()))
            return InstructionEvaluateHelperXStore.evaluateN(
                    context,
                    labels,
                    locals,
                    this,
                    EOpcodes.ASTORE,
                    2,
                    type,
                    ins
            );
        else if (has(instruction.INSN_ASTORE_3()))
            return InstructionEvaluateHelperXStore.evaluateN(
                    context,
                    labels,
                    locals,
                    this,
                    EOpcodes.ASTORE,
                    3,
                    type,
                    ins
            );

        throw new IllegalInstructionException("Unexpected instruction: " + instruction.getText(), instruction);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction) {
        VarInsnNode varInsnNode = (VarInsnNode) instruction.insn();

        StackElementCapsule elementCapsule = new StackElementCapsule(instruction);
        return FrameDifferenceInfo.builder(instruction)
                .popToCapsule(elementCapsule)
                .addLocalFromCapsule(varInsnNode.var, elementCapsule)
                .build();
    }

    @Override
    public JALParser.JvmInsAstoreNContext map(JALParser.@NotNull InstructionContext instruction) {
        return instruction.jvmInsAstoreN();
    }
}
