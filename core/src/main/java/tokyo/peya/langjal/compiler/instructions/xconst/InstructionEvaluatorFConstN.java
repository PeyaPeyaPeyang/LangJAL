package tokyo.peya.langjal.compiler.instructions.xconst;

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

public class InstructionEvaluatorFConstN extends AbstractInstructionEvaluator<JALParser.JvmInsFconstNContext>
{
    public InstructionEvaluatorFConstN()
    {
        super(EOpcodes.FCONST_0, EOpcodes.FCONST_1, EOpcodes.FCONST_2);
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsFconstNContext instruction)
    {
        if (has(instruction.INSN_FCONST_0()))
            return this.visitSingle(instruction, EOpcodes.FCONST_0);
        else if (has(instruction.INSN_FCONST_1()))
            return this.visitSingle(instruction, EOpcodes.FCONST_1);
        else if (has(instruction.INSN_FCONST_2()))
            return this.visitSingle(instruction, EOpcodes.FCONST_2);

        throw new IllegalInstructionException("Unexpected instruction: " + instruction.getText(), instruction);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.builder(instruction)
                                  .pushPrimitive(StackElementType.FLOAT)
                                  .build();
    }

    @Override
    public JALParser.JvmInsFconstNContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsFconstN();
    }
}
