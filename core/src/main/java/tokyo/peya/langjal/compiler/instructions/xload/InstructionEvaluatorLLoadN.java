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

public class InstructionEvaluatorLLoadN extends AbstractInstructionEvaluator<JALParser.JvmInsLloadNContext>
{
    public InstructionEvaluatorLLoadN()
    {
        super(EOpcodes.LLOAD_0, EOpcodes.LLOAD_1, EOpcodes.LLOAD_2, EOpcodes.LLOAD_3);
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsLloadNContext instruction)
    {
        if (has(instruction.INSN_LLOAD_0()))
            return InstructionEvaluateHelperXLoad.evaluateN(this, instruction, locals, EOpcodes.LLOAD, 0);
        else if (has(instruction.INSN_LLOAD_1()))
            return InstructionEvaluateHelperXLoad.evaluateN(this, instruction, locals, EOpcodes.LLOAD, 1);
        else if (has(instruction.INSN_LLOAD_2()))
            return InstructionEvaluateHelperXLoad.evaluateN(this, instruction, locals, EOpcodes.LLOAD, 2);
        else if (has(instruction.INSN_LLOAD_3()))
            return InstructionEvaluateHelperXLoad.evaluateN(this, instruction, locals, EOpcodes.LLOAD, 3);

        throw new IllegalInstructionException("Unexpected instruction: " + instruction.getText(), instruction);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.builder(instruction)
                                  .pushPrimitive(StackElementType.LONG)
                                  .build();
    }

    @Override
    public JALParser.JvmInsLloadNContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsLloadN();
    }
}
