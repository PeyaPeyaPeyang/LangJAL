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

public class InstructionEvaluatorLStoreN extends AbstractInstructionEvaluator<JALParser.JvmInsLstoreNContext>
{
    public InstructionEvaluatorLStoreN()
    {
        super(EOpcodes.LSTORE_0, EOpcodes.LSTORE_1, EOpcodes.LSTORE_2, EOpcodes.LSTORE_3);
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsLstoreNContext instruction)
    {

        JALParser.LocalDeclarationContext ins = instruction.localDeclaration();
        if (has(instruction.INSN_LSTORE_0()))
            return InstructionEvaluateHelperXStore.evaluateN(context, labels, locals, this, EOpcodes.LSTORE, 0, "J", ins);
        else if (has(instruction.INSN_LSTORE_1()))
            return InstructionEvaluateHelperXStore.evaluateN(context, labels, locals, this, EOpcodes.LSTORE, 1, "J", ins);
        else if (has(instruction.INSN_LSTORE_2()))
            return InstructionEvaluateHelperXStore.evaluateN(context, labels, locals, this, EOpcodes.LSTORE, 2, "J", ins);
        else if (has(instruction.INSN_LSTORE_3()))
            return InstructionEvaluateHelperXStore.evaluateN(context, labels, locals, this, EOpcodes.LSTORE, 3, "J", ins);

        throw new IllegalInstructionException("Unexpected instruction: " + instruction.getText(), instruction);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        VarInsnNode varInsnNode = (VarInsnNode) instruction.insn();
        return FrameDifferenceInfo.builder(instruction)
                                  .popPrimitive(StackElementType.LONG)
                                  .addLocalPrimitive(varInsnNode.var, StackElementType.LONG)
                                  .build();
    }

    @Override
    public JALParser.JvmInsLstoreNContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsLstoreN();
    }
}
