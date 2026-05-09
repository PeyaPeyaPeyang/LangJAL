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

public class InstructionEvaluatorIConstN extends AbstractInstructionEvaluator<JALParser.JvmInsIconstNContext>
{
    public InstructionEvaluatorIConstN()
    {
        super(EOpcodes.ICONST_0, EOpcodes.ICONST_1, EOpcodes.ICONST_2, EOpcodes.ICONST_3,
              EOpcodes.ICONST_4, EOpcodes.ICONST_5);
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsIconstNContext instruction)
    {
        if (has(instruction.INSN_ICONST_M1()))
            return this.visitSingle(instruction, EOpcodes.ICONST_M1);
        else if (has(instruction.INSN_ICONST_0()))
            return this.visitSingle(instruction, EOpcodes.ICONST_0);
        else if (has(instruction.INSN_ICONST_1()))
            return this.visitSingle(instruction, EOpcodes.ICONST_1);
        else if (has(instruction.INSN_ICONST_2()))
            return this.visitSingle(instruction, EOpcodes.ICONST_2);
        else if (has(instruction.INSN_ICONST_3()))
            return this.visitSingle(instruction, EOpcodes.ICONST_3);
        else if (has(instruction.INSN_ICONST_4()))
            return this.visitSingle(instruction, EOpcodes.ICONST_4);
        else if (has(instruction.INSN_ICONST_5()))
            return this.visitSingle(instruction, EOpcodes.ICONST_5);

        throw new IllegalInstructionException("Unknown instruction: " + instruction.getText(), instruction);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.builder(instruction)
                                  .pushPrimitive(StackElementType.INTEGER)
                                  .build();
    }

    @Override
    public JALParser.JvmInsIconstNContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsIconstN();
    }
}
