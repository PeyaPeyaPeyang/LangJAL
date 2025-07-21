package tokyo.peya.langjal.analyser;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.analyser.stack.LocalStackElement;
import tokyo.peya.langjal.analyser.stack.StackElement;
import tokyo.peya.langjal.compiler.member.LabelInfo;

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
