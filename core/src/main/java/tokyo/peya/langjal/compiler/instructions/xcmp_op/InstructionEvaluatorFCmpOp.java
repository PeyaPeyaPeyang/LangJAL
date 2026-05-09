package tokyo.peya.langjal.compiler.instructions.xcmp_op;

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

public class InstructionEvaluatorFCmpOp extends AbstractInstructionEvaluator<JALParser.JvmInsFcmpOPContext>
{
    public InstructionEvaluatorFCmpOp()
    {
        super(EOpcodes.FCMPG, EOpcodes.FCMPL);
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsFcmpOPContext instruction)
    {
        if (has(instruction.INSN_FCMPG()))
            return this.visitSingle(instruction, EOpcodes.FCMPG);
        else if (has(instruction.INSN_FCMPL()))
            return this.visitSingle(instruction, EOpcodes.FCMPL);

        throw new IllegalInstructionException("Unknown instruction: " + instruction.getText(), instruction);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.builder(instruction)
                                  .popPrimitive(StackElementType.FLOAT)
                                  .popPrimitive(StackElementType.FLOAT)
                                  .pushPrimitive(StackElementType.INTEGER)
                                  .build();
    }

    @Override
    public JALParser.JvmInsFcmpOPContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsFcmpOP();
    }
}
