package tokyo.peya.langjal.compiler.exceptions.analyse;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.LabelInfo;

/**
 * Exception thrown when a jump instruction refers to an unknown or unresolved label during analysis.
 * <p>
 * Used to indicate control flow errors or incomplete bytecode.
 * </p>
 */
@Getter
public class UnknownJumpException extends ClassAnalyseException
{
    /**
     * The instruction that attempted the jump.
     */
    @NotNull
    private final InstructionInfo instruction;

    /**
     * The label that was the target of the jump, or null if unknown.
     */
    @Nullable
    private final LabelInfo toLabel;

    /**
     * Constructs a new UnknownJumpException with the given jump target and instruction.
     *
     * @param toLabel     The label that was the target of the jump, or null if unknown.
     * @param instruction The instruction that attempted the jump.
     */
    public UnknownJumpException(@Nullable LabelInfo toLabel, @NotNull InstructionInfo instruction)
    {
        super("Unknown jump target: " + (toLabel != null ? toLabel.name(): "<unknown label>") +
                      " at instruction: " + instruction);
        this.instruction = instruction;
        this.toLabel = toLabel;
    }

    /**
     * Constructs a new UnknownJumpException with a custom message.
     *
     * @param message     The detail message.
     * @param instruction The instruction that attempted the jump.
     */
    public UnknownJumpException(@NotNull String message, @NotNull InstructionInfo instruction)
    {
        super(message);
        this.instruction = instruction;
        this.toLabel = null;
    }
}
