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

public class InstructionEvaluatorPutStatic extends AbstractInstructionEvaluator<JALParser.JvmInsPutstaticContext>
{
    @Override
    protected @NotNull EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler,
                                                     JALParser.@NotNull JvmInsPutstaticContext ctxt)
    {
        return InstructionEvaluateHelperField.evaluate(this, ctxt.jvmInsArgFieldRef(), EOpcodes.PUTSTATIC);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        FieldInsnNode fieldInsnNode = (FieldInsnNode) instruction.insn();
        return FrameDifferenceInfo.builder(instruction)
                                  .popObjectRef(TypeDescriptor.className(fieldInsnNode.owner))
                                  .pushObjectRef(TypeDescriptor.className(fieldInsnNode.desc))
                                  .build();
    }

    @Override
    protected JALParser.JvmInsPutstaticContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsPutstatic();
    }
}
