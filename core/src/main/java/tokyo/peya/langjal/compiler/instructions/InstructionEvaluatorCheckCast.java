package tokyo.peya.langjal.compiler.instructions;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.TypeInsnNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.JALMethodCompiler;
import tokyo.peya.langjal.compiler.utils.EvaluatorCommons;

public class InstructionEvaluatorCheckCast extends AbstractInstructionEvaluator<JALParser.JvmInsCheckcastContext>
{
    @Override
    protected @NotNull EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler,
                                                     JALParser.@NotNull JvmInsCheckcastContext ctxt)
    {
        JALParser.TypeDescriptorContext typeDescriptor = ctxt.typeDescriptor();
        // Ljava/lang/String; -> java.lang.String に変換
        String typeName = EvaluatorCommons.unwrapClassTypeDescriptor(typeDescriptor);

        TypeInsnNode type = new TypeInsnNode(EOpcodes.CHECKCAST, typeName);
        return EvaluatedInstruction.of(this, type);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        TypeInsnNode insn = (TypeInsnNode) instruction.insn();
        return FrameDifferenceInfo.builder(instruction)
                                  .popObjectRef()  // なんでも
                                  // キャスト後のオブジェクト参照をプッシュ
                                  .pushObjectRef(TypeDescriptor.className(insn.desc))
                                  .build();
    }

    @Override
    protected JALParser.JvmInsCheckcastContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsCheckcast();
    }
}
