package tokyo.peya.langjal.analyser;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.analyser.stack.LocalStackElement;
import tokyo.peya.langjal.analyser.stack.StackElement;
import tokyo.peya.langjal.compiler.member.LabelInfo;

/**
 * Represents the stack frame at a specific label (block) in JVM bytecode.
 * <p>
 * Contains the label, the current operand stack, and the local variable array at that point.
 * <br>
 * Used for frame merging and verification during bytecode analysis.
 * <br>
 * Example:
 * <pre>
 * InstructionSetFrame frame = new InstructionSetFrame(label, stack, locals);
 * System.out.println("Frame at label: " + frame.label().name());
 * </pre>
 *
 * @param label  The label for this frame.
 * @param stack  The operand stack at this point.
 * @param locals The local variable array at this point.
 */
public record InstructionSetFrame(
        @NotNull
        LabelInfo label,
        @NotNull
        StackElement[] stack,
        @NotNull
        LocalStackElement[] locals
)
{
}
