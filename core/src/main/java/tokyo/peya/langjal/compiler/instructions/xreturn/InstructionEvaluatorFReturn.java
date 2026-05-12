package tokyo.peya.langjal.compiler.instructions.xreturn;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.AbstractSingleInstructionEvaluator;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.member.*;

public class InstructionEvaluatorFReturn extends AbstractSingleInstructionEvaluator<JALParser.JvmInsFreturnContext> {
    public InstructionEvaluatorFReturn() {
        super(EOpcodes.FRETURN);
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsFreturnContext instruction) {
        InstructionEvaluateHelperXReturn.checkReturnType(method, instruction, TypeDescriptor.FLOAT);
        return super.evaluate(context, clazz, method, instructions, labels, locals, instruction);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction) {
        return FrameDifferenceInfo.builder(instruction)
                .popPrimitive(StackElementType.FLOAT)
                .build();
    }

    @Override
    public JALParser.JvmInsFreturnContext map(JALParser.@NotNull InstructionContext instruction) {
        return instruction.jvmInsFreturn();
    }
}
