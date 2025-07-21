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
