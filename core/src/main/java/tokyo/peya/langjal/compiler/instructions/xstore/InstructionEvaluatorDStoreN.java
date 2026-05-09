package tokyo.peya.langjal.compiler.instructions.xstore;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.exceptions.IllegalInstructionException;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.InstructionsHolder;
import tokyo.peya.langjal.compiler.member.LabelsHolder;
import tokyo.peya.langjal.compiler.member.LocalVariablesHolder;

public class InstructionEvaluatorDStoreN extends AbstractInstructionEvaluator<JALParser.JvmInsDstoreNContext>
{
    public InstructionEvaluatorDStoreN()
    {
        super(EOpcodes.DSTORE_0, EOpcodes.DSTORE_1, EOpcodes.DSTORE_2, EOpcodes.DSTORE_3);
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsDstoreNContext instruction)
    {
        JALParser.LocalDeclarationContext ins = instruction.localDeclaration();
        if (has(instruction.INSN_DSTORE_0()))
            return InstructionEvaluateHelperXStore.evaluateN(context, labels, locals, this, EOpcodes.DSTORE, 0, "D", ins);
        else if (has(instruction.INSN_DSTORE_1()))
            return InstructionEvaluateHelperXStore.evaluateN(context, labels, locals, this, EOpcodes.DSTORE, 1, "D", ins);
        else if (has(instruction.INSN_DSTORE_2()))
            return InstructionEvaluateHelperXStore.evaluateN(context, labels, locals, this, EOpcodes.DSTORE, 2, "D", ins);
        else if (has(instruction.INSN_DSTORE_3()))
            return InstructionEvaluateHelperXStore.evaluateN(context, labels, locals, this, EOpcodes.DSTORE, 3, "D", ins);

        throw new IllegalInstructionException("Unexpected instruction: " + instruction.getText(), instruction);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        VarInsnNode varInsnNode = (VarInsnNode) instruction.insn();
        return FrameDifferenceInfo.builder(instruction)
                                  .popPrimitive(StackElementType.DOUBLE)
                                  .addLocalPrimitive(varInsnNode.var, StackElementType.DOUBLE)
                                  .build();
    }

    @Override
    public JALParser.JvmInsDstoreNContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsDstoreN();
    }
}
