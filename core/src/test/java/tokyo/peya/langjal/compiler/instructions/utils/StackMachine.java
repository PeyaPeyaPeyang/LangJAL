package tokyo.peya.langjal.compiler.instructions.utils;

import org.jetbrains.annotations.Nullable;
import org.opentest4j.AssertionFailedError;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StackMachine implements Cloneable
{
    private final List<StackValue> initialStack;
    private final Map<Integer, StackValue> locals;
    @Nullable
    private StackMachine expected;

    private StackMachine()
    {
        this.initialStack = new ArrayList<>();
        this.locals = new HashMap<>();
    }

    public static StackMachine create()
    {
        return new StackMachine();
    }

    public static StackMachine create(StackValue... initialStack)
    {
        StackMachine emulator = new StackMachine();
        emulator.push(initialStack);
        return emulator;
    }

    public StackMachine push(StackValue... value)
    {
        for (StackValue v : value) {
            this.initialStack.add(v);
            if (v instanceof LongStackValue || v instanceof DoubleStackValue) {
                // long と double はスタック上で2スロットを占有するので，もう1スロット分の Top を追加する。
                this.initialStack.add(StackValues.top());
            }
        }
        return this;
    }

    public StackMachine pop(int count)
    {
        for (int i = 0; i < count; i++)
            this.initialStack.removeLast();
        return this;
    }

    public StackMachine set(int index, StackValue value)
    {
        this.locals.put(index, value);
        return this;
    }

    public StackMachine expected(StackMachine expected) {
        if (expected.expected != null) {
            throw new IllegalArgumentException("Expected StackMachine cannot have its own expected StackMachine.");
        }

        this.expected = expected.clone();
        return this;
    }

    public EmulateResult emulate(FrameDifferenceInfo frameDifferenceInfo) {
        Emulator emulator = new Emulator(this.initialStack, this.locals);
        EmulateResult result = emulator.emulate(frameDifferenceInfo);
        if (this.expected != null) {
            this.assertStacMachineResult(result, this.expected);
        }

        return result;
    }

    private void assertStacMachineResult(EmulateResult result, StackMachine expected) {
        List<StackValue> expectedStack = expected.initialStack;
        if (result.stack().size() != expectedStack.size()) {
            throw new AssertionFailedError(
                    "Expected stack size " + expectedStack.size() + " but got " + result.stack().size(),
                    expectedStack,
                    result.stack()
            );
        }

        for (int i = 0; i < expectedStack.size(); i++) {
            StackValue expectedValue = expectedStack.get(i);
            StackValue actualValue = result.stack().get(i);
            if (expectedValue.type() != actualValue.type()) {
                throw new AssertionFailedError(
                        "Stack value type mismatch at index " + i,
                        expectedValue.type(),
                        actualValue.type()
                );
            }
        }

        for (Map.Entry<Integer, StackValue> entry : expected.locals.entrySet()) {
            int index = entry.getKey();
            StackValue expectedValue = entry.getValue();
            StackValue actualValue = result.locals().get(index);
            if (actualValue == null) {
                throw new AssertionFailedError(
                        "Missing local variable at index " + index
                );
            }
            if (expectedValue.type() != actualValue.type()) {
                throw new AssertionFailedError(
                        "Local variable type mismatch at index " + index,
                        expectedValue.type(),
                        actualValue.type()
                );
            }
        }
    }

    @Override
    @SuppressWarnings({"CloneDoesntDeclareCloneNotSupportedException", "MethodDoesntCallSuperMethod"})
    protected StackMachine clone()
    {
        StackMachine cloned = new StackMachine();
        cloned.initialStack.addAll(this.initialStack);
        cloned.locals.putAll(this.locals);
        cloned.expected = this.expected; // expected は参照をコピーするだけで十分

        return cloned;
    }

    private static class Emulator {
        private final List<StackValue> stack;
        private final Map<Integer, StackValue> locals;
        private final Map<StackElementCapsule, StackValue> shelter;

        private int currentStep;

        public Emulator(List<StackValue> initialStack, Map<Integer, StackValue> locals) {
            this.stack = new ArrayList<>(initialStack);
            this.locals = new HashMap<>(locals);

            this.shelter = new HashMap<>();
        }

        public EmulateResult emulate(FrameDifferenceInfo frameDifferenceInfo) {
            if (this.currentStep != 0) {
                throw new IllegalStateException("Emulation already started");
            }

            StackOperation[] operations = frameDifferenceInfo.getStackOperations();
            for (int i = 0; i < operations.length; i++)
            {
                this.currentStep = i;
                this.stepOne(operations[i]);
            }

            return new EmulateResult(this.stack, this.shelter, this.locals);
        }

        private void stepOne(StackOperation operation) {
            switch (operation.type()) {
                case PUSH -> push(operation.element());
                case POP -> pop(operation.element());
            }
        }

        private void push(StackElement element) {
            switch (element) {
                case ObjectElement obj -> this.stack.add(new ObjectStackValue(obj.content()));
                case StackElementCapsule capsule -> {
                    StackValue value = this.shelter.get(capsule);
                    if (value == null)
                        throw new AssertionFailedError("No value in shelter for capsule: " + capsule + " at step " + this.currentStep);
                    this.stack.add(value);
                }
                case PrimitiveElement prim -> {
                    StackValue value = switch (prim.type()) {
                        case INTEGER -> new IntegerStackValue();
                        case FLOAT -> new FloatStackValue();
                        case DOUBLE -> new DoubleStackValue();
                        case LONG -> new LongStackValue();
                        default -> throw new AssertionFailedError("Unknown primitive type: " + prim.type());
                    };
                    this.stack.add(value);
                }
                case LocalStackElement local -> {
                    StackValue value = this.locals.get(local.index());
                    if (value == null)
                        throw new AssertionFailedError("No value in locals for index: " + local.index());
                    this.stack.add(value);
                }

                case UninitializedElement ignored -> this.stack.add(new UninitializedStackValue());
                case UninitializedThisElement ignored -> this.stack.add(new UninitializedThisStackValue());
                case NullElement ignored -> this.stack.add(new NullStackValue());
                case TopElement ignored -> this.stack.add(new TopStackValue());
            }
        }

        private void pop(StackElement element) {
            if (this.stack.isEmpty()) {
                throw new AssertionFailedError("Stack underflow at step " + this.currentStep);
            }

            StackValue poppedValue = this.stack.removeLast();

            // カプセルの場合は，スタックの値をシェルターに入れる。次の PUSH で同じカプセルが来るはずなので，そのときに取り出す。
            if (element instanceof StackElementCapsule capsule) {
                this.shelter.put(capsule, poppedValue);
                return;
            }

            this.assertStackElementEquals(element, poppedValue);
        }

        private void assertStackElementEquals(StackElement element, StackValue poppedValue) {
            StackElementType expectedType = element.type();
            StackElementType actualType = poppedValue.type();

            if (expectedType != actualType) {
                throw new AssertionFailedError(
                        "Stack element type mismatch at step " + this.currentStep,
                        expectedType,
                        actualType
                );
            }

            // Object の場合はクラスも。
            if (expectedType == StackElementType.OBJECT) {
                ObjectStackValue objValue = (ObjectStackValue) poppedValue;
                ObjectElement objElement = (ObjectElement) element;
                if (!objValue.typeName().equals(objElement.content())) {
                    throw new AssertionFailedError(
                            "Object type mismatch at step " + this.currentStep,
                            objElement.content(),
                            objValue.typeName()
                    );
                }
            }
        }
    }

    public record EmulateResult(
            List<StackValue> stack,
            Map<StackElementCapsule, StackValue> shelter,
            Map<Integer, StackValue> locals
    ) {
    }

    public static class StackValues {
        private static final StackValue STACK_VALUE_INTEGER = new IntegerStackValue();
        private static final StackValue STACK_VALUE_FLOAT = new FloatStackValue();
        private static final StackValue STACK_VALUE_DOUBLE = new DoubleStackValue();
        private static final StackValue STACK_VALUE_LONG = new LongStackValue();
        private static final StackValue STACK_VALUE_NULL = new NullStackValue();
        private static final StackValue STACK_VALUE_UNINITIALIZED = new UninitializedStackValue();
        private static final StackValue STACK_VALUE_UNINITIALIZED_THIS = new UninitializedThisStackValue();
        private static final StackValue STACK_VALUE_TOP = new TopStackValue();


        public static StackValue integerValue() {
            return STACK_VALUE_INTEGER;
        }

        public static StackValue floatValue() {
            return STACK_VALUE_FLOAT;
        }

        public static StackValue doubleValue() {
            return STACK_VALUE_DOUBLE;
        }

        public static StackValue longValue() {
            return STACK_VALUE_LONG;
        }

        public static StackValue nullValue() {
            return STACK_VALUE_NULL;
        }

        public static StackValue uninitialized() {
            return STACK_VALUE_UNINITIALIZED;
        }

        public static StackValue uninitializedThis() {
            return STACK_VALUE_UNINITIALIZED_THIS;
        }

        public static StackValue top() {
            return STACK_VALUE_TOP;
        }

        public static StackValue object(TypeDescriptor typeName) {
            return new ObjectStackValue(typeName);
        }

        public static StackValue anyObject() {
            return new ObjectStackValue(TypeDescriptor.OBJECT);
        }
    }

    public sealed interface StackValue
    {
        StackElementType type();
    }

    private record IntegerStackValue() implements StackValue
    {
        @Override
        public StackElementType type() {
            return StackElementType.INTEGER;
        }
    }

    private record FloatStackValue() implements StackValue
    {
        @Override
        public StackElementType type() {
            return StackElementType.FLOAT;
        }
    }

    private record DoubleStackValue() implements StackValue
    {
        @Override
        public StackElementType type() {
            return StackElementType.DOUBLE;
        }
    }

    private record LongStackValue() implements StackValue
    {
        @Override
        public StackElementType type() {
            return StackElementType.LONG;
        }
    }

    private record NullStackValue() implements StackValue
    {
        @Override
        public StackElementType type() {
            return StackElementType.NULL;
        }
    }

    private record ObjectStackValue(TypeDescriptor typeName) implements StackValue
    {
        @Override
        public StackElementType type() {
            return StackElementType.OBJECT;
        }
    }

    private record UninitializedStackValue() implements StackValue
    {
        @Override
        public StackElementType type() {
            return StackElementType.UNINITIALIZED;
        }
    }

    private record UninitializedThisStackValue() implements StackValue
    {
        @Override
        public StackElementType type() {
            return StackElementType.UNINITIALIZED_THIS;
        }
    }

    private record TopStackValue() implements StackValue
    {
        @Override
        public StackElementType type() {
            return StackElementType.TOP;
        }
    }
}
