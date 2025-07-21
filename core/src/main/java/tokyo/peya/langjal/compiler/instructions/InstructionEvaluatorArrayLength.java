package tokyo.peya.langjal.compiler.instructions;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.ObjectElement;
import tokyo.peya.langjal.analyser.stack.StackElementCapsule;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.exceptions.analyse.StackElementMismatchedException;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

public class InstructionEvaluatorArrayLength
        extends AbstractSingleInstructionEvaluator<JALParser.JvmInsArraylengthContext>
{
    public InstructionEvaluatorArrayLength()
    {
        super(EOpcodes.ARRAYLENGTH);
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        ObjectElement expectedElement = new ObjectElement(instruction, TypeDescriptor.parse("[Ljava/lang/Object;"));

        StackElementCapsule arrayRef = new StackElementCapsule(
                instruction, (elm) -> {
            StackElementType elementType = elm.type();
            if (elementType != StackElementType.OBJECT)
                throw new StackElementMismatchedException(instruction, expectedElement, elm);
            ObjectElement objectElement = (ObjectElement) elm;
            TypeDescriptor content = objectElement.content();
            if (!content.isArray())
                throw new StackElementMismatchedException(instruction, expectedElement, elm);

            return elm;
        }
        );
        return FrameDifferenceInfo.builder(instruction)
                                  .popToCapsule(arrayRef)
                                  .pushPrimitive(StackElementType.INTEGER)
                                  .build();
    }

    @Override
    protected JALParser.JvmInsArraylengthContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsArraylength();
    }
}
