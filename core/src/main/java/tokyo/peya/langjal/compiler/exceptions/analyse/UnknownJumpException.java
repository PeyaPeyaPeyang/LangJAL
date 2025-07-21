package tokyo.peya.langjal.compiler.exceptions.analyse;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.LabelInfo;

@Getter
public class UnknownJumpException extends ClassAnalyseException
{
    @NotNull
    private final InstructionInfo instruction;
    @Nullable
    private final LabelInfo toLabel;

    public UnknownJumpException(@Nullable LabelInfo toLabel, @NotNull InstructionInfo instruction)
    {
        super("Unknown jump target: " + (toLabel != null ? toLabel.name(): "<unknown label>") +
                      " at instruction: " + instruction);
        this.instruction = instruction;
        this.toLabel = toLabel;
    }

    public UnknownJumpException(@NotNull String message, @NotNull InstructionInfo instruction)
    {
        super(message);
        this.instruction = instruction;
        this.toLabel = null;
    }
}
