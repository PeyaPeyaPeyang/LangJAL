package tokyo.peya.langjal.compiler.instructions.xload;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
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

public class InstructionEvaluatorDLoadN extends AbstractInstructionEvaluator<JALParser.JvmInsDloadNContext>
{
    public InstructionEvaluatorDLoadN()
    {
        super(EOpcodes.DLOAD_0, EOpcodes.DLOAD_1, EOpcodes.DLOAD_2, EOpcodes.DLOAD_3);
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsDloadNContext instruction)
    {
        if (has(instruction.INSN_DLOAD_0()))
            return InstructionEvaluateHelperXLoad.evaluateN(this, instruction, locals, EOpcodes.DLOAD, 0);
        else if (has(instruction.INSN_DLOAD_1()))
            return InstructionEvaluateHelperXLoad.evaluateN(this, instruction, locals, EOpcodes.DLOAD, 1);
        else if (has(instruction.INSN_DLOAD_2()))
            return InstructionEvaluateHelperXLoad.evaluateN(this, instruction, locals, EOpcodes.DLOAD, 2);
        else if (has(instruction.INSN_DLOAD_3()))
            return InstructionEvaluateHelperXLoad.evaluateN(this, instruction, locals, EOpcodes.DLOAD, 3);

        throw new IllegalInstructionException("Unexpected instruction: " + instruction.getText(), instruction);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.builder(instruction)
                                  .pushPrimitive(StackElementType.DOUBLE)
                                  .build();
    }

    @Override
    public JALParser.JvmInsDloadNContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsDloadN();
    }
}
