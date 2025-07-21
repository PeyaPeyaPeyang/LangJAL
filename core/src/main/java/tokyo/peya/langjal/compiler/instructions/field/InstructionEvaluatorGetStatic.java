package tokyo.peya.langjal.compiler.instructions.field;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.FieldInsnNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.JALMethodCompiler;

public class InstructionEvaluatorGetStatic extends AbstractInstructionEvaluator<JALParser.JvmInsGetstaticContext>
{
    @Override
    protected @NotNull EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler,
                                                     JALParser.@NotNull JvmInsGetstaticContext ctxt)
    {
        return InstructionEvaluateHelperField.evaluate(this, ctxt.jvmInsArgFieldRef(), EOpcodes.GETSTATIC);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        FieldInsnNode fieldInsnNode = (FieldInsnNode) instruction.insn();
        return FrameDifferenceInfo.builder(instruction)
                                  .pushObjectRef(TypeDescriptor.className(fieldInsnNode.desc))
                                  .build();
    }

    @Override
    protected JALParser.JvmInsGetstaticContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsGetstatic();
    }
}
