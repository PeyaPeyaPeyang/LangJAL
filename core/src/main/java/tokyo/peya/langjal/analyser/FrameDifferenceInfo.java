package tokyo.peya.langjal.analyser;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tokyo.peya.langjal.analyser.stack.LocalStackElement;
import tokyo.peya.langjal.analyser.stack.NullElement;
import tokyo.peya.langjal.analyser.stack.ObjectElement;
import tokyo.peya.langjal.analyser.stack.PrimitiveElement;
import tokyo.peya.langjal.analyser.stack.StackElement;
import tokyo.peya.langjal.analyser.stack.StackElementCapsule;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.analyser.stack.StackOperation;
import tokyo.peya.langjal.analyser.stack.TopElement;
import tokyo.peya.langjal.analyser.stack.UninitializedElement;
import tokyo.peya.langjal.analyser.stack.UninitializedThisElement;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.LabelInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the difference in the stack frame caused by an instruction.
 * Contains label information and a list of stack operations.
 */
@Getter
public class FrameDifferenceInfo
{
    /**
     * A static instance representing no change in the frame.
     */
    private static final FrameDifferenceInfo SAME = new FrameDifferenceInfo(
            null,
            new StackOperation[0]
    );

    /**
     * The label associated with this frame difference, if any.
     */
    @Nullable
    private final LabelInfo label;

    /**
     * The stack operations that describe the frame difference.
     */
    private final StackOperation[] stackOperations;

    /**
     * Constructs a FrameDifferenceInfo with the given label and stack operations.
     *
     * @param label           The label info, or null.
     * @param stackOperations The stack operations.
     */
    private FrameDifferenceInfo(@Nullable LabelInfo label,
                                @NotNull StackOperation[] stackOperations)
    {
        this.label = label;
        this.stackOperations = stackOperations;
    }

    /**
     * Returns a static instance representing no change in the frame.
     *
     * @return The SAME instance.
     */
    @NotNull
    public static FrameDifferenceInfo same()
    {
        return SAME;
    }

    /**
     * Creates a new Builder for constructing a FrameDifferenceInfo.
     *
     * @param instruction The instruction info.
     * @return The Builder instance.
     */
    public static @NotNull Builder builder(@NotNull InstructionInfo instruction)
    {
        return new Builder(instruction);
    }

    /**
     * Builder class for FrameDifferenceInfo.
     * Provides methods to describe stack and local variable changes.
     */
    public static class Builder
    {
        @NotNull
        private final InstructionInfo instruction;

        @Nullable
        private final LabelInfo labelInfo;

        @NotNull
        private final List<StackOperation> stackOperations;

        /**
         * Constructs a Builder for the given instruction.
         *
         * @param instruction The instruction info.
         */
        public Builder(@NotNull InstructionInfo instruction)
        {
            this.instruction = instruction;
            this.labelInfo = instruction.assignedLabel();
            this.stackOperations = new ArrayList<>();
        }

        /**
         * Pushes a primitive type onto the stack.
         *
         * @param type The primitive type.
         * @return This builder.
         */
        @NotNull
        public Builder pushPrimitive(@NotNull StackElementType type)
        {
            if (!(type == StackElementType.INTEGER ||
                    type == StackElementType.FLOAT ||
                    type == StackElementType.LONG ||
                    type == StackElementType.DOUBLE))
                throw new IllegalArgumentException("Invalid primitive type: " + type);

            this.stackOperations.add(StackOperation.push(PrimitiveElement.of(this.instruction, type)));
            if (type == StackElementType.LONG || type == StackElementType.DOUBLE)
            {
                // LONG と DOUBLE はスタックに 2 つの要素を追加する。
                this.stackOperations.add(StackOperation.push(new TopElement(this.instruction)));
            }
            return this;
        }

        /**
         * Pushes a return address onto the stack.
         *
         * @return This builder.
         */
        @NotNull
        public Builder pushReturnAddress()
        {
            this.stackOperations.add(StackOperation.push(new PrimitiveElement(
                    this.instruction,
                    StackElementType.RETURN_ADDRESS
            )));
            return this;
        }

        /**
         * Pushes a null reference onto the stack.
         *
         * @return This builder.
         */
        @NotNull
        public Builder pushNull()
        {
            this.stackOperations.add(StackOperation.push(NullElement.of(this.instruction)));
            return this;
        }

        /**
         * Pushes an object reference onto the stack.
         *
         * @param reference The type descriptor.
         * @return This builder.
         */
        @NotNull
        public Builder pushObjectRef(@NotNull TypeDescriptor reference)
        {
            this.stackOperations.add(StackOperation.push(new ObjectElement(this.instruction, reference)));
            return this;
        }

        /**
         * Pushes an uninitialized value onto the stack.
         *
         * @return This builder.
         */
        @NotNull
        public Builder pushUninitialized()
        {
            this.stackOperations.add(StackOperation.push(new UninitializedElement(this.instruction)));
            return this;
        }

        /**
         * Pushes an uninitialized "this" reference onto the stack.
         *
         * @return This builder.
         */
        @NotNull
        public Builder pushUninitializedThis()
        {
            this.stackOperations.add(StackOperation.push(new UninitializedThisElement(this.instruction)));
            return this;
        }

        /**
         * Pushes a stack element onto the stack.
         *
         * @param element The stack element.
         * @return This builder.
         */
        @NotNull
        public Builder push(@NotNull StackElement element)
        {
            if (element instanceof NullElement)
                return this.pushNull();
            else if (element instanceof PrimitiveElement primitive)
                return this.pushPrimitive(primitive.type());
            else if (element instanceof ObjectElement object)
                return this.pushObjectRef(object.content());
            else if (element instanceof UninitializedElement)
                return this.pushUninitialized();
            else if (element instanceof UninitializedThisElement)
                return this.pushUninitializedThis();
            else if (element instanceof StackElementCapsule capsule)
                return this.pushFromCapsule(capsule);
            else if (element instanceof TopElement)
                throw new IllegalArgumentException("Cannot push TopElement directly to stack");
            else
                throw new IllegalArgumentException("Unknown stack element type: " + element.getClass().getName());
        }

        /**
         * Adds a local variable to the frame.
         *
         * @param local The local stack element.
         * @return This builder.
         */
        public @NotNull Builder addLocal(@NotNull LocalStackElement local)
        {
            int index = local.index();
            boolean isParameter = local.isParameter();
            if (index < 0 || index > 65535)
                throw new IllegalArgumentException("Local variable index must be between 0 and 65535, but was: " + index);

            this.stackOperations.add(StackOperation.push(new LocalStackElement(
                    this.instruction,
                    index,
                    local.stackElement(),
                    isParameter
            )));

            return this;
        }

        /**
         * Pops a stack element from the stack.
         *
         * @param element The stack element.
         * @return This builder.
         */
        @NotNull
        public Builder pop(@NotNull StackElement element)
        {
            if (element instanceof NullElement)
                return this.popNull();
            else if (element instanceof PrimitiveElement primitive)
                return this.popPrimitive(primitive.type());
            else if (element instanceof ObjectElement object)
                return this.popObjectRef(object.content());
            else if (element instanceof UninitializedElement)
                return this.popUninitialized();
            else if (element instanceof UninitializedThisElement)
                return this.popUninitializedThis();
            else if (element instanceof LocalStackElement local)
                return this.consumeLocalPrimitive(local.index(), local.type());
            else if (element instanceof StackElementCapsule capsule)
                return this.popToCapsule(capsule);
            else if (element instanceof TopElement)
                throw new IllegalArgumentException("Cannot pop TopElement directly from stack");
            else
                throw new IllegalArgumentException("Unknown stack element type: " + element.getClass().getName());
        }

        /**
         * Adds a primitive local variable.
         *
         * @param idx  The index.
         * @param type The type.
         * @return This builder.
         */
        @NotNull
        public Builder addLocalPrimitive(int idx, @NotNull StackElementType type)
        {
            if (!(type == StackElementType.INTEGER ||
                    type == StackElementType.FLOAT ||
                    type == StackElementType.LONG ||
                    type == StackElementType.DOUBLE ||
                    type == StackElementType.OBJECT))
                throw new IllegalArgumentException("Invalid local type: " + type);

            this.stackOperations.add(StackOperation.push(new LocalStackElement(
                    this.instruction,
                    idx,
                    new PrimitiveElement(this.instruction, type)
            )));

            return this;
        }

        /**
         * Adds an object local variable.
         *
         * @param idx      The index.
         * @param reference The type descriptor.
         * @return This builder.
         */
        @NotNull
        public Builder addLocalObject(int idx, @NotNull TypeDescriptor reference)
        {
            this.stackOperations.add(StackOperation.push(new LocalStackElement(
                    this.instruction,
                    idx,
                    new ObjectElement(this.instruction, reference)
            )));

            return this;
        }

        /**
         * Adds a local variable from a capsule.
         *
         * @param idx     The index.
         * @param capsule The capsule.
         * @return This builder.
         */
        @NotNull
        public Builder addLocalFromCapsule(int idx, @NotNull StackElementCapsule capsule)
        {
            this.stackOperations.add(StackOperation.push(new LocalStackElement(
                    this.instruction,
                    idx,
                    capsule
            )));

            return this;
        }

        /**
         * Adds an uninitialized local variable.
         *
         * @param idx The index.
         * @return This builder.
         */
        @NotNull
        public Builder addLocalUninitialized(int idx)
        {
            this.stackOperations.add(StackOperation.push(new LocalStackElement(
                    this.instruction,
                    idx,
                    new UninitializedElement(this.instruction)
            )));

            return this;
        }

        /**
         * Adds an uninitialized "this" reference as a local variable.
         *
         * @return This builder.
         */
        @NotNull
        public Builder addUninitializedThis()
        {
            this.stackOperations.add(StackOperation.push(new LocalStackElement(
                    this.instruction,
                    0, // "this" は常にローカル変数のインデックス 0 にある
                    new UninitializedElement(this.instruction)
            )));

            return this;
        }

        /**
         * Adds a local variable from a capsule element.
         *
         * @param i      The index.
         * @param capsule The capsule.
         * @return This builder.
         */
        @NotNull
        public Builder addFromCapsule(int i, @NotNull StackElementCapsule capsule)
        {
            this.stackOperations.add(StackOperation.push(new LocalStackElement(
                    this.instruction,
                    i,
                    capsule.getElement()
            )));

            return this;
        }

        /**
         * Consumes an uninitialized "this" reference from the local variables.
         *
         * @return This builder.
         */
        @NotNull
        public Builder consumeUninitializedThis()
        {
            this.stackOperations.add(StackOperation.pop(new LocalStackElement(
                    this.instruction,
                    0, // "this" は常にローカル変数のインデックス 0 にある
                    new UninitializedElement(this.instruction)
            )));

            return this;
        }

        /**
         * Pops a null reference from the stack.
         *
         * @return This builder.
         */
        @NotNull
        public Builder popNull()
        {
            this.stackOperations.add(StackOperation.pop(NullElement.of(this.instruction)));
            return this;
        }

        /**
         * Pops a primitive type from the stack.
         *
         * @param type The primitive type.
         * @return This builder.
         */
        @NotNull
        public Builder popPrimitive(@NotNull StackElementType type)
        {
            if (!(type == StackElementType.INTEGER ||
                    type == StackElementType.FLOAT ||
                    type == StackElementType.LONG ||
                    type == StackElementType.DOUBLE ||
                    type == StackElementType.OBJECT))
                throw new IllegalArgumentException("Invalid stack element type: " + type);

            if (type == StackElementType.LONG || type == StackElementType.DOUBLE)
            {
                // LONG と DOUBLE はスタックから 2 つの要素を消費する。
                this.stackOperations.add(StackOperation.pop(new TopElement(this.instruction)));
            }
            this.stackOperations.add(StackOperation.pop(new PrimitiveElement(this.instruction, type)));
            return this;
        }

        /**
         * Pops an object reference from the stack.
         *
         * @param reference The type descriptor.
         * @return This builder.
         */
        @NotNull
        public Builder popObjectRef(@NotNull TypeDescriptor reference)
        {
            this.stackOperations.add(StackOperation.pop(new ObjectElement(this.instruction, reference)));
            return this;
        }

        /**
         * Pushes a generic object reference onto the stack.
         *
         * @return This builder.
         */
        @NotNull
        public Builder pushObjectRef()
        {
            this.stackOperations.add(StackOperation.push(new ObjectElement(this.instruction)));
            return this;
        }

        /**
         * Pops a generic object reference from the stack.
         *
         * @return This builder.
         */
        @NotNull
        public Builder popObjectRef()
        {
            this.stackOperations.add(StackOperation.pop(new ObjectElement(this.instruction)));
            return this;
        }

        /**
         * Pops a capsule from the stack.
         *
         * @param capsule The capsule.
         * @return This builder.
         */
        @NotNull
        public Builder popToCapsule(@NotNull StackElementCapsule capsule)
        {
            // DUP や SWAP 用に， 現在の スタックの状態をカプセル化して保持する。
            this.stackOperations.add(StackOperation.pop(capsule));
            return this;
        }

        /**
         * Pushes a capsule onto the stack.
         *
         * @param capsule The capsule.
         * @return This builder.
         */
        @NotNull
        public Builder pushFromCapsule(@NotNull StackElementCapsule capsule)
        {
            // DUP や SWAP 用に， カプセル化されたスタックの状態を復元する。
            this.stackOperations.add(StackOperation.push(capsule));
            return this;
        }

        /**
         * Pops an uninitialized value from the stack.
         *
         * @return This builder.
         */
        @NotNull
        public Builder popUninitialized()
        {
            this.stackOperations.add(StackOperation.pop(new UninitializedElement(this.instruction)));
            return this;
        }

        /**
         * Pops an uninitialized "this" reference from the stack.
         *
         * @return This builder.
         */
        @NotNull
        public Builder popUninitializedThis()
        {
            this.stackOperations.add(StackOperation.pop(new UninitializedThisElement(this.instruction)));
            return this;
        }

        /**
         * Consumes a primitive local variable.
         *
         * @param idx  The index.
         * @param type The type.
         * @return This builder.
         */
        @NotNull
        public Builder consumeLocalPrimitive(int idx, @NotNull StackElementType type)
        {
            if (!(type == StackElementType.INTEGER ||
                    type == StackElementType.FLOAT ||
                    type == StackElementType.LONG ||
                    type == StackElementType.DOUBLE ||
                    type == StackElementType.OBJECT))
                throw new IllegalArgumentException("Invalid local type: " + type);

            this.stackOperations.add(StackOperation.pop(new LocalStackElement(
                    this.instruction,
                    idx,
                    new PrimitiveElement(this.instruction, type)
            )));

            return this;
        }

        /**
         * Consumes an object local variable.
         *
         * @param idx      The index.
         * @param reference The type descriptor.
         * @return This builder.
         */
        @NotNull
        public Builder consumeLocalObject(int idx, @NotNull TypeDescriptor reference)
        {
            this.stackOperations.add(StackOperation.pop(new LocalStackElement(
                    this.instruction,
                    idx,
                    new ObjectElement(this.instruction, reference)
            )));

            return this;
        }

        /**
         * Consumes an uninitialized local variable.
         *
         * @param idx The index.
         * @return This builder.
         */
        @NotNull
        public Builder consumeLocalUninitialized(int idx)
        {
            this.stackOperations.add(StackOperation.pop(new LocalStackElement(
                    this.instruction,
                    idx,
                    new UninitializedElement(this.instruction)
            )));

            return this;
        }

        /**
         * Consumes a capsule local variable.
         *
         * @param idx     The index.
         * @param capsule The capsule.
         * @return This builder.
         */
        @NotNull
        public Builder consumeLocalCapsule(int idx, @NotNull StackElementCapsule capsule)
        {
            this.stackOperations.add(StackOperation.pop(new LocalStackElement(
                    this.instruction,
                    idx,
                    capsule
            )));

            return this;
        }

        /**
         * Consumes a local variable of any stack element type.
         *
         * @param idx     The index.
         * @param element The stack element.
         * @return This builder.
         */
        @NotNull
        public Builder consumeLocal(int idx, @NotNull StackElement element)
        {
            if (element instanceof NullElement)
                return this.consumeLocalNull(idx);
            else if (element instanceof PrimitiveElement primitive)
                return this.consumeLocalPrimitive(idx, primitive.type());
            else if (element instanceof ObjectElement object)
                return this.consumeLocalObject(idx, object.content());
            else if (element instanceof UninitializedElement)
                return this.consumeLocalUninitialized(idx);
            else if (element instanceof UninitializedThisElement)
                return this.consumeUninitializedThis();
            else if (element instanceof LocalStackElement local)
                return this.consumeLocalPrimitive(local.index(), local.type());
            else if (element instanceof StackElementCapsule capsule)
                return this.consumeLocalCapsule(idx, capsule);
            else if (element instanceof TopElement)
                throw new IllegalArgumentException("Cannot consume TopElement directly from stack");
            else
                throw new IllegalArgumentException("Unknown stack element type: " + element.getClass().getName());
        }

        /**
         * Consumes a null local variable.
         *
         * @param idx The index.
         * @return This builder.
         */
        public @NotNull Builder consumeLocalNull(int idx)
        {
            this.stackOperations.add(StackOperation.pop(new LocalStackElement(
                    this.instruction,
                    idx,
                    NullElement.of(this.instruction)
            )));
            return this;
        }

        /**
         * Builds and returns the FrameDifferenceInfo.
         *
         * @return The FrameDifferenceInfo instance.
         */
        @NotNull
        public FrameDifferenceInfo build()
        {
            if (this.stackOperations.isEmpty())
                return FrameDifferenceInfo.same();

            return new FrameDifferenceInfo(
                    this.labelInfo,
                    this.stackOperations.toArray(new StackOperation[0])
            );
        }
    }
}
