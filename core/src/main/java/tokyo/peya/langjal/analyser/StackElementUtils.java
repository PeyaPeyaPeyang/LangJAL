package tokyo.peya.langjal.analyser;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tokyo.peya.langjal.analyser.stack.*;
import tokyo.peya.langjal.compiler.exceptions.analyse.StackElementMismatchedException;
import tokyo.peya.langjal.compiler.exceptions.analyse.StackSizeDifferentException;
import tokyo.peya.langjal.compiler.jvm.ClassReferenceType;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.member.LabelInfo;

import java.util.BitSet;
import java.util.Collection;
import java.util.List;

/**
 * Utility class for manipulating and merging JVM stack and local variable elements during bytecode analysis.
 * <p>
 * Provides static methods for cleaning up, merging, and comparing stack and local variable arrays,
 * as well as for finding common super types and converting stack elements to string representations.
 * <br>
 * These utilities are essential for stack frame verification and merging in JVM analysis.
 * <br>
 * <b>Usage Example:</b>
 * <pre>{@code
 * LocalStackElement[] cleaned = StackElementUtils.cleanUpLocals(locals);
 * StackElement[] mergedStack = StackElementUtils.mergeStack(label, stack1, stack2);
 * }</pre>
 */
@UtilityClass
public class StackElementUtils {
    /**
     * Removes trailing TOP elements from the local variable array, except when preceded by category 2 elements.
     * For example:
     * <ul>
     *   <li>[I, I, TOP, TOP, TOP] -> [I, I]</li>
     *   <li>[I, I, TOP, D, TOP, TOP] -> [I, I, TOP, D, TOP]</li>
     * </ul>
     *
     * @param locals The local variable array.
     * @return A cleaned local variable array.
     */
    public static LocalStackElement[] cleanUpLocals(@NotNull LocalStackElement[] locals) {
        // ローカルスタック要素の中で，末尾から連続して続く TOP 要素を削除する。
        // ただし， TOP 要素の前がカテゴリ２の要素（LONG, DOUBLE）である場合は削除しない。
        // [I, I, TOP, TOP, TOP] -> [I, I]
        // [I, I, TOP, D, TOP, TOP] -> [I, I, TOP, D, TOP]
        int lastNonTopIndex = locals.length - 1;
        while (lastNonTopIndex >= 0 && locals[lastNonTopIndex].stackElement().type() == StackElementType.TOP) {
            // カテゴリ2の要素が続いている場合は削除しない
            if (lastNonTopIndex > 0) {
                StackElementType previousType = locals[lastNonTopIndex - 1].stackElement().type();
                if (previousType == StackElementType.LONG || previousType == StackElementType.DOUBLE)
                    break;
            }
            lastNonTopIndex--;
        }

        // 最後の非 TOP 要素のインデックスがローカルスタック要素の長さより小さい場合は，
        // そのインデックスまでの要素を新しい配列にコピーして返す。
        if (lastNonTopIndex < locals.length - 1) {
            LocalStackElement[] cleanedLocals = new LocalStackElement[lastNonTopIndex + 1];
            System.arraycopy(locals, 0, cleanedLocals, 0, lastNonTopIndex + 1);
            return cleanedLocals;
        }
        // すべての要素が TOP 要素である場合は，空の配列を返す
        else if (lastNonTopIndex < 0)
            return new LocalStackElement[0];

        // それ以外の場合は，元の配列をそのまま返す
        return locals;
    }

    /**
     * Merges two local variable arrays, checking type consistency and removing trailing TOP elements.
     *
     * @param existingLocal The first local variable array.
     * @param newLocal      The second local variable array.
     * @return The merged local variable array.
     */
    public static LocalStackElement[] mergeLocals(@NotNull LocalStackElement[] existingLocal,
                                                  @NotNull LocalStackElement[] newLocal) {
        return mergeLocals(existingLocal, newLocal, Math.min(existingLocal.length, newLocal.length));
    }

    public static LocalStackElement[] mergeLocals(@NotNull LocalStackElement[] existingLocal,
                                                  @NotNull LocalStackElement[] newLocal,
                                                  @Nullable BitSet liveLocals) {
        int maxLocalSize = Math.max(existingLocal.length, newLocal.length);
        LocalStackElement[] mergedLocals = new LocalStackElement[maxLocalSize];
        for (int i = 0; i < maxLocalSize; i++) {
            if (liveLocals != null && !liveLocals.get(i)) {
                LocalStackElement source = i < newLocal.length ? newLocal[i] : existingLocal[i];
                mergedLocals[i] = preserveParameterOrTop(source, i);
                continue;
            }

            if (i >= existingLocal.length || i >= newLocal.length)
                throw new IllegalArgumentException("Missing live local slot at index " + i);

            LocalStackElement existingLocalElement = existingLocal[i];
            LocalStackElement newLocalElement = newLocal[i];
            StackElement existingElement = existingLocalElement.stackElement();
            StackElement newElement = newLocalElement.stackElement();
            checkSameType(existingElement, newElement);
            mergedLocals[i] = new LocalStackElement(
                    existingLocalElement.producer(),
                    i,
                    mergeElement(existingElement, newElement),
                    existingLocalElement.isParameter() || newLocalElement.isParameter()
            );
        }

        return cleanUpLocals(mergedLocals);
    }

    /**
     * Merges two local variable arrays up to the specified minimum size.
     *
     * @param existingLocal The first local variable array.
     * @param newLocal      The second local variable array.
     * @param minLocalSize  The minimum size to merge.
     * @return The merged local variable array.
     */
    public static LocalStackElement[] mergeLocals(@NotNull LocalStackElement[] existingLocal,
                                                  @NotNull LocalStackElement[] newLocal,
                                                  int minLocalSize) {
        if (minLocalSize > existingLocal.length || minLocalSize > newLocal.length)
            throw new IllegalArgumentException(
                    "minLocalSize must be less than or equal to the length of both existingLocal and newLocal arrays."
            );

        LocalStackElement[] mergedLocals = new LocalStackElement[minLocalSize];
        for (int i = 0; i < minLocalSize; i++) {
            LocalStackElement existingLocalElement = existingLocal[i];
            LocalStackElement newLocalElement = newLocal[i];
            StackElement existingElement = existingLocalElement.stackElement();
            StackElement newElement = newLocalElement.stackElement();
            // 既存のスタック要素と新しいスタック要素の型が一致しない場合は例外を投げる
            checkSameType(existingElement, newElement);
            // ローカル要素をマージする
            mergedLocals[i] = new LocalStackElement(
                    existingLocalElement.producer(),
                    i,
                    mergeElement(existingElement, newElement),
                    existingLocalElement.isParameter() || newLocalElement.isParameter()
            );
        }

        // ローカル変数の末尾に連続する TOP 要素を削除する
        return cleanUpLocals(mergedLocals);
    }

    public static LocalStackElement[] filterDeadLocals(@NotNull LocalStackElement[] locals,
                                                       @Nullable BitSet liveLocals) {
        if (locals.length == 0)
            return locals;
        else if (liveLocals == null)
            return cleanUpLocals(locals);

        LocalStackElement[] filteredLocals = new LocalStackElement[locals.length];
        for (int i = 0; i < locals.length; i++) {
            LocalStackElement local = locals[i];
            if (liveLocals.get(i) || local.isParameter())
                filteredLocals[i] = local;
            else
                filteredLocals[i] = toTopLocal(local, i);
        }

        return cleanUpLocals(filteredLocals);
    }

    private static @NotNull LocalStackElement preserveParameterOrTop(@NotNull LocalStackElement local, int index) {
        return local.isParameter() ? local : toTopLocal(local, index);
    }

    private static @NotNull LocalStackElement toTopLocal(@NotNull LocalStackElement local, int index) {
        TopElement top = new TopElement(local.producer());
        return new LocalStackElement(local.producer(), index, top, local.isParameter());
    }

    private static void checkStackSize(@NotNull LabelInfo frameLabel,
                                       @NotNull String stackType,
                                       @NotNull Collection<? extends StackElement> existingStack,
                                       @NotNull Collection<? extends StackElement> newStack) {
        if (existingStack.size() != newStack.size())
            throw new StackSizeDifferentException(
                    String.format(getSizeMismatchMessage(
                            frameLabel,
                            stackType,
                            existingStack.size(),
                            newStack.size(),
                            existingStack,
                            newStack
                    )),
                    frameLabel,
                    existingStack.toArray(new StackElement[0]),
                    newStack.toArray(new StackElement[0])
            );
    }

    /**
     * Merges two stack arrays, checking type consistency.
     * Throws if stack sizes or types differ.
     *
     * @param frameLabel    The label for error reporting.
     * @param existingStack The first stack array.
     * @param newStack      The second stack array.
     * @return The merged stack array.
     */
    public static StackElement[] mergeStack(@NotNull LabelInfo frameLabel,
                                            @NotNull StackElement[] existingStack,
                                            @NotNull StackElement[] newStack) {
        checkStackSize(frameLabel, "stack", List.of(existingStack), List.of(newStack));

        StackElement[] mergedStack = new StackElement[existingStack.length];
        for (int i = 0; i < newStack.length; i++) {
            StackElement existingElement = existingStack[i];
            StackElement newElement = newStack[i];
            // 既存のスタック要素と新しいスタック要素の型が一致しない場合は例外を投げる
            checkSameType(existingElement, newElement);

            // スタック要素をマージする
            mergedStack[i] = mergeElement(existingElement, newElement);
        }

        return mergedStack;
    }

    private static String getSizeMismatchMessage(@NotNull LabelInfo label,
                                                 @NotNull String stackType,
                                                 int expectedSize, int actualSize,
                                                 @NotNull Collection<? extends StackElement> expectedStack,
                                                 @NotNull Collection<? extends StackElement> stack) {
        return String.format(
                """
                        Expected %s size %d at label '%s', but got %d.
                         This error indicates that in instruction set '%s', which is reached from several instruction jump routes,
                        the size of the %s stack for each route is different and the stacks are not consistent.
                         JVM must not differ in the number of stacks and their element types
                        at the destination of the jump.
                        
                        Current stack: %s
                        Applying stack: %s""",
                stackType, expectedSize, label.name(), actualSize,
                label.name(), stackType, stackToString(stack), stackToString(expectedStack)
        );
    }

    /**
     * Checks if two stack elements have the same type, throws if not.
     *
     * @param element         The first stack element.
     * @param expectedElement The second stack element.
     * @throws StackElementMismatchedException if types differ.
     */
    public static void checkSameType(@NotNull StackElement element, @NotNull StackElement expectedElement) {
        if (isNullAndObjectCompatible(element, expectedElement))
            return;

        if (element.type() != expectedElement.type())
            throw new StackElementMismatchedException(
                    element.producer(), expectedElement, element,
                    "Cannot merge stack element with different types: " +
                            element.type() + " produced by " + element.producer() +
                            " and " + expectedElement.type() + " produced by " + expectedElement.producer()
            );
    }

    /**
     * Merges two stack elements, handling object types and nulls.
     * For object types, finds a common super type if needed.
     *
     * @param existingElement The first stack element.
     * @param newElement      The second stack element.
     * @return The merged stack element.
     */
    public static StackElement mergeElement(@NotNull StackElement existingElement, @NotNull StackElement newElement) {
        StackElementUtils.checkSameType(existingElement, newElement);

        // null は任意の参照型に代入可能なので merge 後は参照型側を採用する
        if (existingElement instanceof NullElement && newElement instanceof ObjectElement)
            return newElement;
        else if (existingElement instanceof ObjectElement && newElement instanceof NullElement)
            return existingElement;
        else if (newElement instanceof NullElement)
            return newElement;
        else if (existingElement instanceof NullElement)
            return existingElement;

        return switch (existingElement.type()) {
            case TOP, INTEGER, FLOAT, LONG, DOUBLE, NULL, RETURN_ADDRESS, NOP -> existingElement;
            // オブジェクト型はマージする
            case OBJECT -> {
                assert newElement instanceof ObjectElement;  // #checkType() でチェック済み
                yield mergeObjects((ObjectElement) existingElement, (ObjectElement) newElement);
            }
            case UNINITIALIZED_THIS, UNINITIALIZED -> newElement;  // Uninitialized は新しい要素をそのまま返す
        };
    }

    public static void checkAssignableTo(@NotNull StackElement actualElement,
                                         @NotNull StackElement expectedElement) {
        if (actualElement instanceof NullElement && expectedElement instanceof ObjectElement)
            return;

        if (actualElement.type() != expectedElement.type())
            throw new StackElementMismatchedException(
                    actualElement.producer(), expectedElement, actualElement,
                    "Cannot assign stack element with type " + actualElement.type() +
                            " to expected type " + expectedElement.type()
            );

        if (!((actualElement instanceof ObjectElement actualObject) 
                && (expectedElement instanceof ObjectElement expectedObject)))
            return;

        if (!isObjectAssignableTo(actualObject.content(), expectedObject.content()))
            throw new StackElementMismatchedException(
                    actualElement.producer(), expectedElement, actualElement,
                    "Cannot assign object " + actualObject.content() +
                            " to expected object " + expectedObject.content()
            );
    }

    private static boolean isObjectAssignableTo(@NotNull TypeDescriptor actualType,
                                                @NotNull TypeDescriptor expectedType) {
        if (actualType.equals(expectedType))
            return true;

        if (expectedType.equals(TypeDescriptor.OBJECT))
            return true;

        if (actualType.isArray() || expectedType.isArray())
            return isArrayAssignableTo(actualType, expectedType);

        if (actualType.getBaseType().isPrimitive() || expectedType.getBaseType().isPrimitive())
            return false;

        ClassReferenceType commonSuperType = getCommonSuperType(
                (ClassReferenceType) actualType.getBaseType(),
                (ClassReferenceType) expectedType.getBaseType()
        );
        return commonSuperType.equals(expectedType.getBaseType());
    }

    private static boolean isArrayAssignableTo(@NotNull TypeDescriptor actualType,
                                               @NotNull TypeDescriptor expectedType) {
        if (!actualType.isArray())
            return false;
        if (expectedType.equals(TypeDescriptor.OBJECT))
            return true;
        if (!expectedType.isArray())
            return false;
        if (actualType.getArrayDimensions() != expectedType.getArrayDimensions())
            return false;
        if (actualType.getBaseType().equals(expectedType.getBaseType()))
            return true;  // 注意
        if (actualType.getBaseType().isPrimitive() || expectedType.getBaseType().isPrimitive())
            return false;

        ClassReferenceType commonSuperType = getCommonSuperType(
                (ClassReferenceType) actualType.getBaseType(),
                (ClassReferenceType) expectedType.getBaseType()
        );
        return commonSuperType.equals(expectedType.getBaseType());
    }

    private static boolean isNullAndObjectCompatible(@NotNull StackElement element,
                                                     @NotNull StackElement expectedElement) {
        return (element instanceof NullElement && expectedElement instanceof ObjectElement)
                || (element instanceof ObjectElement && expectedElement instanceof NullElement);
    }

    /**
     * Merges two object stack elements, finding their common super type if needed.
     *
     * @param existingObject The first object element.
     * @param newObject      The second object element.
     * @return The merged object element.
     */
    public static ObjectElement mergeObjects(@NotNull ObjectElement existingObject, @NotNull ObjectElement newObject) {
        TypeDescriptor existingType = existingObject.content();
        TypeDescriptor newType = newObject.content();
        if (existingType.equals(newType))
            return newObject;

        TypeDescriptor mergedType = getCommonReferenceType(existingType, newType);
        return new ObjectElement(newObject.producer(), mergedType);
    }

    private static @NotNull TypeDescriptor getCommonReferenceType(@NotNull TypeDescriptor existingType,
                                                                  @NotNull TypeDescriptor newType) {
        if (existingType.getBaseType().equals(ClassReferenceType.OBJECT)
                || newType.getBaseType().equals(ClassReferenceType.OBJECT))
            return TypeDescriptor.OBJECT;

        if (existingType.isArray() || newType.isArray())
            return getCommonArrayType(existingType, newType);

        if (existingType.getBaseType().isPrimitive() || newType.getBaseType().isPrimitive()) {
            throw new IllegalArgumentException(
                    "Cannot merge object stack elements with different primitive types: " +
                            existingType + " and " + newType
            );
        }

        ClassReferenceType commonSuperType = getCommonSuperType(
                (ClassReferenceType) existingType.getBaseType(),
                (ClassReferenceType) newType.getBaseType()
        );
        return new TypeDescriptor(commonSuperType);
    }

    private static @NotNull TypeDescriptor getCommonArrayType(@NotNull TypeDescriptor existingType,
                                                              @NotNull TypeDescriptor newType) {
        if (!(existingType.isArray() && newType.isArray()))
            return TypeDescriptor.OBJECT;

        if (existingType.getArrayDimensions() != newType.getArrayDimensions())
            return TypeDescriptor.OBJECT;

        if (existingType.getBaseType().equals(newType.getBaseType()))
            return existingType;

        if (existingType.getBaseType().isPrimitive() || newType.getBaseType().isPrimitive())
            return TypeDescriptor.OBJECT;

        ClassReferenceType commonSuperType = getCommonSuperType(
                (ClassReferenceType) existingType.getBaseType(),
                (ClassReferenceType) newType.getBaseType()
        );
        return new TypeDescriptor(commonSuperType, existingType.getArrayDimensions());
    }

    /**
     * Finds the common super type of two class reference types.
     * If either is an interface, returns Object.
     *
     * @param type1 The first class reference type.
     * @param type2 The second class reference type.
     * @return The common super type.
     */
    public static ClassReferenceType getCommonSuperType(@NotNull ClassReferenceType type1,
                                                        @NotNull ClassReferenceType type2) {
        ClassLoader classLoader = ClassReferenceType.class.getClassLoader();
        Class<?> class1, class2;
        try {
            class1 = Class.forName(type1.getDottedName(), false, classLoader);
            class2 = Class.forName(type2.getDottedName(), false, classLoader);
        } catch (ClassNotFoundException e) {
            return type1;  // クラスが見つからない場合は，片方の型をそのまま返す
        }

        if (class1.isAssignableFrom(class2))
            return type1;  // type1 が type2 のスーパークラスなら type1 を返す
        else if (class2.isAssignableFrom(class1))
            return type2;  // type2 が type1 のスーパークラスなら type2 を返す

        if (class1.isInterface() || class2.isInterface())
            return ClassReferenceType.parse(Object.class.getName());  // インターフェースの場合は Object を返す

        while (!class1.isAssignableFrom(class2)) {
            Class<?> newClass1 = class1.getSuperclass();
            if (newClass1 == null)
                return ClassReferenceType.OBJECT;
            class1 = newClass1;
        }

        // 共通のスーパークラスを返す。 Object まで到達するので，その時は Object を返す
        return ClassReferenceType.parse(class1.getName());
    }

    static String stackToString(@NotNull Collection<? extends StackElement> stack) {
        return stackToString(stack.toArray(new StackElement[0]));
    }

    static <T extends StackElement> String stackToString(@NotNull T[] stack) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < stack.length; i++) {
            sb.append(stackElementToString(stack[i]));
            if (i < stack.length - 1)
                sb.append(" | ");
        }

        sb.append("]");
        return sb.toString();
    }

    private static String stackElementToString(@NotNull StackElement element) {
        if (element instanceof LocalStackElement local)
            element = local.stackElement();

        return switch (element.type()) {
            case TOP -> "T";
            case UNINITIALIZED_THIS -> "U_T";
            case INTEGER -> "I";
            case FLOAT -> "F";
            case LONG -> "L";
            case DOUBLE -> "D";
            case OBJECT -> ((ObjectElement) element).content().toString();
            case NULL -> " ";
            case RETURN_ADDRESS -> "RA";
            case UNINITIALIZED -> "U";
            case NOP -> "NOP";
        };
    }
}
