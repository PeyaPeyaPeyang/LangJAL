package tokyo.peya.langjal.compiler.instructions.field;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.FieldInsnNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;

public class InstructionEvaluateHelperField
{
    @NotNull
    public static EvaluatedInstruction evaluate(@NotNull AbstractInstructionEvaluator<?> evaluator,
                                                @NotNull JALParser.JvmInsArgFieldRefContext ref, int opcode)
    {
        JALParser.JvmInsArgFieldRefTypeContext fieldOwner = ref.jvmInsArgFieldRefType();
        JALParser.JvmInsArgFieldRefNameContext fieldName = ref.jvmInsArgFieldRefName();
        JALParser.TypeDescriptorContext fieldType = ref.typeDescriptor();

        FieldInsnNode fieldInsn = new FieldInsnNode(
                opcode,
                fieldOwner.getText(),
                fieldName.getText(),
                fieldType.getText()
        );
        return EvaluatedInstruction.of(evaluator, fieldInsn);
    }
}
