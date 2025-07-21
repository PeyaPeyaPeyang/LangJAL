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

@Getter
public class FrameDifferenceInfo
{
    private static final FrameDifferenceInfo SAME = new FrameDifferenceInfo(
            null,
            new StackOperation[0]
    );

    @Nullable
    private final LabelInfo label;

    private final StackOperation[] stackOperations;

    private FrameDifferenceInfo(@Nullable LabelInfo label,
                                @NotNull StackOperation[] stackOperations)
    {
        this.label = label;
        this.stackOperations = stackOperations;
    }

    @NotNull
    public static FrameDifferenceInfo same()
    {
        return SAME;
    }

    public static @NotNull Builder builder(@NotNull InstructionInfo instruction)
    {
        return new Builder(instruction);
    }

    public static class Builder
    {
        @NotNull
        private final InstructionInfo instruction;

        @Nullable
        private final LabelInfo labelInfo;

        @NotNull
        private final List<StackOperation> stackOperations;

        public Builder(@NotNull InstructionInfo instruction)
        {
            this.instruction = instruction;
            this.labelInfo = instruction.assignedLabel();
            this.stackOperations = new ArrayList<>();
        }

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

        @NotNull
        public Builder pushReturnAddress()
        {
            this.stackOperations.add(StackOperation.push(new PrimitiveElement(
                    this.instruction,
                    StackElementType.RETURN_ADDRESS
            )));
            return this;
        }

        @NotNull
        public Builder pushNull()
        {
            this.stackOperations.add(StackOperation.push(NullElement.of(this.instruction)));
            return this;
        }

        @NotNull
        public Builder pushObjectRef(@NotNull TypeDescriptor reference)
        {
            this.stackOperations.add(StackOperation.push(new ObjectElement(this.instruction, reference)));
            return this;
        }

        @NotNull
        public Builder pushUninitialized()
        {
            this.stackOperations.add(StackOperation.push(new UninitializedElement(this.instruction)));
            return this;
        }

        @NotNull
        public Builder pushUninitializedThis()
        {
            this.stackOperations.add(StackOperation.push(new UninitializedThisElement(this.instruction)));
            return this;
        }

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

        @NotNull
        public Builder popNull()
        {
            this.stackOperations.add(StackOperation.pop(NullElement.of(this.instruction)));
            return this;
        }

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

        @NotNull
        public Builder popObjectRef(@NotNull TypeDescriptor reference)
        {
            this.stackOperations.add(StackOperation.pop(new ObjectElement(this.instruction, reference)));
            return this;
        }

        @NotNull
        public Builder pushObjectRef() // なんでもオブジェクト
        {
            this.stackOperations.add(StackOperation.push(new ObjectElement(this.instruction)));
            return this;
        }

        @NotNull
        public Builder popObjectRef() // なんでもオブジェクト
        {
            this.stackOperations.add(StackOperation.pop(new ObjectElement(this.instruction)));
            return this;
        }

        @NotNull
        public Builder popToCapsule(@NotNull StackElementCapsule capsule)
        {
            // DUP や SWAP 用に， 現在の スタックの状態をカプセル化して保持する。
            this.stackOperations.add(StackOperation.pop(capsule));
            return this;
        }

        @NotNull
        public Builder pushFromCapsule(@NotNull StackElementCapsule capsule)
        {
            // DUP や SWAP 用に， カプセル化されたスタックの状態を復元する。
            this.stackOperations.add(StackOperation.push(capsule));
            return this;
        }

        @NotNull
        public Builder popUninitialized()
        {
            this.stackOperations.add(StackOperation.pop(new UninitializedElement(this.instruction)));
            return this;
        }

        @NotNull
        public Builder popUninitializedThis()
        {
            this.stackOperations.add(StackOperation.pop(new UninitializedThisElement(this.instruction)));
            return this;
        }

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

        public @NotNull Builder consumeLocalNull(int idx)
        {
            this.stackOperations.add(StackOperation.pop(new LocalStackElement(
                    this.instruction,
                    idx,
                    NullElement.of(this.instruction)
            )));
            return this;
        }

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
