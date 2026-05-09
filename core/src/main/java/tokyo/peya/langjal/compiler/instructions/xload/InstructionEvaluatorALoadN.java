package tokyo.peya.langjal.compiler.instructions.xload;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementCapsule;
import tokyo.peya.langjal.compiler.exceptions.IllegalInstructionException;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.InstructionsHolder;
import tokyo.peya.langjal.compiler.member.LabelsHolder;
import tokyo.peya.langjal.compiler.member.LocalVariablesHolder;

public class InstructionEvaluatorALoadN extends AbstractInstructionEvaluator<JALParser.JvmInsAloadNContext>
{
    public InstructionEvaluatorALoadN()
    {
        super(EOpcodes.ALOAD_0, EOpcodes.ALOAD_1, EOpcodes.ALOAD_2, EOpcodes.ALOAD_3);
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsAloadNContext instruction)
    {
        if (has(instruction.INSN_ALOAD_0()))
            return InstructionEvaluateHelperXLoad.evaluateN(this, instruction, locals, EOpcodes.ALOAD, 0);
        else if (has(instruction.INSN_ALOAD_1()))
            return InstructionEvaluateHelperXLoad.evaluateN(this, instruction, locals, EOpcodes.ALOAD, 1);
        else if (has(instruction.INSN_ALOAD_2()))
            return InstructionEvaluateHelperXLoad.evaluateN(this, instruction, locals, EOpcodes.ALOAD, 2);
        else if (has(instruction.INSN_ALOAD_3()))
            return InstructionEvaluateHelperXLoad.evaluateN(this, instruction, locals, EOpcodes.ALOAD, 3);

        throw new IllegalInstructionException("Unexpected instruction: " + instruction.getText(), instruction);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        VarInsnNode insn = (VarInsnNode) instruction.insn();
        StackElementCapsule capsule = new StackElementCapsule(instruction);
        return FrameDifferenceInfo.builder(instruction)
                                  .consumeLocal(insn.var, capsule)
                                  .pushFromCapsule(capsule)
                                  .build();
    }

    @Override
    public JALParser.JvmInsAloadNContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsAloadN();
    }
}
