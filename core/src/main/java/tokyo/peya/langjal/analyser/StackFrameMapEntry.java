package tokyo.peya.langjal.analyser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.FrameNode;
import tokyo.peya.langjal.analyser.stack.LocalStackElement;
import tokyo.peya.langjal.analyser.stack.StackElement;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.LabelInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single entry in the stack frame map for JVM bytecode verification.
 * <p>
 * Encodes the frame type, label, previous label, changed stack and locals for ASM's FrameNode.
 * Used to generate stack map frames for control flow transitions.
 * <br>
 * <b>Usage Example:</b>
 * <pre>{@code
 * StackFrameMapEntry entry = StackFrameMapEntry.full(prevFrame, nextFrame, stack, locals);
 * FrameNode node = entry.toASMFrameNode();
 * }</pre>
 *
 * @param type          The frame type.
 * @param label         The label for this frame.
 * @param previousLabel The previous label.
 * @param changedStack  The changed stack elements.
 * @param changedLocals The changed local elements.
 */
public record StackFrameMapEntry(
        @NotNull
        FrameType type,
        @NotNull
        LabelInfo label,
        @NotNull
        LabelInfo previousLabel,
        @NotNull
        StackElement[] changedStack,
        @NotNull
        StackElement[] changedLocals
)
{
    /**
     * Converts this entry to an ASM FrameNode.
     * @return The FrameNode for ASM.
     */
    public FrameNode toASMFrameNode()
    {
        return new FrameNode(
                this.type.getOpcode(),
                this.changedLocals.length,
                toASMStackElements(this.changedLocals),
                this.changedStack.length,
                toASMStackElements(this.changedStack)
        );
    }

    /**
     * Creates a SAME frame entry (no changes).
     * @param previousFrame The previous frame.
     * @param nextFrame The next frame.
     * @return The SAME frame entry.
     */
    public static StackFrameMapEntry same(
            @NotNull InstructionSetFrame previousFrame,
            @NotNull InstructionSetFrame nextFrame
    )
    {
        return new StackFrameMapEntry(
                FrameType.SAME,
                nextFrame.label(),
                previousFrame.label(),
                new StackElement[0],
                new StackElement[0]
        );
    }

    /**
     * Creates a SAME_LOCALS_1_STACK_ITEM frame entry.
     * @param previous The previous frame.
     * @param next The next frame.
     * @param stackItem The stack item.
     * @return The frame entry.
     */
    public static StackFrameMapEntry sameLocals1StackItem(@NotNull InstructionSetFrame previous,
                                                          @NotNull InstructionSetFrame next,
                                                          @NotNull StackElement stackItem)
    {
        return new StackFrameMapEntry(
                FrameType.SAME_LOCALS_1_STACK_ITEM,
                next.label(),
                previous.label(),
                new StackElement[]{stackItem},
                new StackElement[0]
        );
    }

    /**
     * Creates a CHOP frame entry (removes locals).
     * @param previous The previous frame.
     * @param next The next frame.
     * @param choppedStack The chopped locals.
     * @return The frame entry.
     * @throws IllegalArgumentException if choppedStack.length > 4
     */
    public static StackFrameMapEntry chop(@NotNull InstructionSetFrame previous,
                                          @NotNull InstructionSetFrame next,
                                          @NotNull LocalStackElement[] choppedStack)
    {
        if (choppedStack.length > 4)
            throw new IllegalArgumentException("Chopped stack cannot have more than 4 elements, got: " + choppedStack.length);

        return new StackFrameMapEntry(
                FrameType.CHOP,
                next.label(),
                previous.label(),
                new StackElement[0],
                choppedStack
        );
    }

    /**
     * Creates an APPEND frame entry (adds locals).
     * @param previous The previous frame.
     * @param next The next frame.
     * @param appendedLocals The appended locals.
     * @return The frame entry.
     * @throws IllegalArgumentException if appendedLocals.length > 4
     */
    public static StackFrameMapEntry append(@NotNull InstructionSetFrame previous,
                                            @NotNull InstructionSetFrame next,
                                            @NotNull LocalStackElement[] appendedLocals)
    {
        if (appendedLocals.length > 4)
            throw new IllegalArgumentException("Appended locals cannot have more than 4 elements, got: " + appendedLocals.length);

        return new StackFrameMapEntry(
                FrameType.APPEND,
                next.label(),
                previous.label(),
                new StackElement[0],
                appendedLocals
        );
    }

    /**
     * Creates a FULL frame entry (full stack and locals).
     * @param previous The previous frame.
     * @param next The next frame.
     * @param nextStack The stack for the next frame.
     * @param nextLocals The locals for the next frame.
     * @return The frame entry.
     */
    public static StackFrameMapEntry full(@NotNull InstructionSetFrame previous,
                                          @NotNull InstructionSetFrame next,
                                          @NotNull StackElement[] nextStack,
                                          @NotNull LocalStackElement[] nextLocals)
    {
        return new StackFrameMapEntry(
                FrameType.FULL_FRAME,
                next.label(),
                previous.label(),
                nextStack,
                nextLocals
        );
    }

    private static Object[] toASMStackElements(@NotNull StackElement[] elements)
    {
        List<Object> asmElements = new ArrayList<>(elements.length);
        StackElementType prevType = null;
        for (StackElement element : elements)
        {
            StackElementType type = element.type();
            if ((prevType == StackElementType.DOUBLE || prevType == StackElementType.LONG)
                    && type == StackElementType.TOP)
                continue;  // LONG/DOUBLE は１要素で表す（ASMの仕様）

            asmElements.add(element.toASMStackElement());
            prevType = type;
        }

        return asmElements.toArray(new Object[0]);
    }

    @Getter
    @AllArgsConstructor
    public enum FrameType
    {
        SAME(EOpcodes.F_SAME),
        SAME_LOCALS_1_STACK_ITEM(EOpcodes.F_SAME1),
        // SAME_LOCALS_1_STACK_ITEM_EXTENDED(EOpcodes.F_SAME1),
        CHOP(EOpcodes.F_CHOP),
        // SAME_FRAME_EXTENDED(EOpcodes.F_SAME),
        APPEND(EOpcodes.F_APPEND),
        FULL_FRAME(EOpcodes.F_FULL);

        private final int opcode;
    }
}
