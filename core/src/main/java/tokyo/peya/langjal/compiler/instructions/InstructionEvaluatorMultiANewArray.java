package tokyo.peya.langjal.compiler.instructions;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.InstructionsHolder;
import tokyo.peya.langjal.compiler.member.LabelsHolder;
import tokyo.peya.langjal.compiler.member.LocalVariablesHolder;
import tokyo.peya.langjal.compiler.utils.EvaluatorCommons;

public class InstructionEvaluatorMultiANewArray
        extends AbstractInstructionEvaluator<JALParser.JvmInsMultianewarrayContext>
{
    public InstructionEvaluatorMultiANewArray()
    {
        super(EOpcodes.MULTIANEWARRAY);
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsMultianewarrayContext instruction)
    {
        JALParser.TypeDescriptorContext typeDescriptor = instruction.typeDescriptor();
        // Ljava/lang/String; -> java.lang.String に変換
        String typeName = EvaluatorCommons.unwrapClassTypeDescriptor(typeDescriptor);
        int dimensions = EvaluatorCommons.asInteger(instruction.NUMBER());

        MultiANewArrayInsnNode insn = new MultiANewArrayInsnNode(typeName, dimensions);
        return EvaluatedInstruction.of(this, insn);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        MultiANewArrayInsnNode insn = (MultiANewArrayInsnNode) instruction.insn();

        FrameDifferenceInfo.Builder builder = FrameDifferenceInfo.builder(instruction);
        for (int i = 0; i < insn.dims; i++)
            builder.popPrimitive(StackElementType.INTEGER);

        builder.pushObjectRef(TypeDescriptor.className(insn.desc));

        return builder.build();
    }

    @Override
    public JALParser.JvmInsMultianewarrayContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsMultianewarray();
    }
}
