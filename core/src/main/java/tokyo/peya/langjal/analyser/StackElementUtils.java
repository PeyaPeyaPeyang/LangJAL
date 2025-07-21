package tokyo.peya.langjal.analyser;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.analyser.stack.LocalStackElement;
import tokyo.peya.langjal.analyser.stack.NullElement;
import tokyo.peya.langjal.analyser.stack.ObjectElement;
import tokyo.peya.langjal.analyser.stack.StackElement;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.exceptions.analyse.StackElementMismatchedException;
import tokyo.peya.langjal.compiler.exceptions.analyse.StackSizeDifferentException;
import tokyo.peya.langjal.compiler.jvm.ClassReferenceType;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.member.LabelInfo;

import java.util.Collection;
import java.util.List;

@UtilityClass
public class StackElementUtils
{
    public static LocalStackElement[] cleanUpLocals(@NotNull LocalStackElement[] locals)
    {
        // ローカルスタック要素の中で，末尾から連続して続く TOP 要素を削除する。
        // ただし， TOP 要素の前がカテゴリ２の要素（LONG, DOUBLE）である場合は削除しない。
        // [I, I, TOP, TOP, TOP] -> [I, I]
        // [I, I, TOP, D, TOP, TOP] -> [I, I, TOP, D, TOP]
        int lastNonTopIndex = locals.length - 1;
        while (lastNonTopIndex >= 0 && locals[lastNonTopIndex].stackElement().type() == StackElementType.TOP)
        {
            // カテゴリ2の要素が続いている場合は削除しない
            if (lastNonTopIndex > 0)
            {
                StackElementType previousType = locals[lastNonTopIndex - 1].stackElement().type();
                if (previousType == StackElementType.LONG || previousType == StackElementType.DOUBLE)
                    break;
            }
            lastNonTopIndex--;
        }

        // 最後の非 TOP 要素のインデックスがローカルスタック要素の長さより小さい場合は，
        // そのインデックスまでの要素を新しい配列にコピーして返す。
        if (lastNonTopIndex < locals.length - 1)
        {
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


    public static LocalStackElement[] mergeLocals(@NotNull LocalStackElement[] existingLocal,
                                                  @NotNull LocalStackElement[] newLocal)
    {
        return mergeLocals(existingLocal, newLocal, Math.min(existingLocal.length, newLocal.length));
    }

    public static LocalStackElement[] mergeLocals(@NotNull LocalStackElement[] existingLocal,
                                                  @NotNull LocalStackElement[] newLocal,
                                                  int minLocalSize)
    {
        if (minLocalSize > existingLocal.length || minLocalSize > newLocal.length)
            throw new IllegalArgumentException(
                    "minLocalSize must be less than or equal to the length of both existingLocal and newLocal arrays."
            );

        LocalStackElement[] mergedLocals = new LocalStackElement[minLocalSize];
        for (int i = 0; i < minLocalSize; i++)
        {
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

    private static void checkStackSize(@NotNull LabelInfo frameLabel,
                                       @NotNull String stackType,
                                       @NotNull Collection<? extends StackElement> existingStack,
                                       @NotNull Collection<? extends StackElement> newStack)
    {
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

    public static StackElement[] mergeStack(@NotNull LabelInfo frameLabel,
                                            @NotNull StackElement[] existingStack,
                                            @NotNull StackElement[] newStack)
    {
        checkStackSize(frameLabel, "stack", List.of(existingStack), List.of(newStack));

        StackElement[] mergedStack = new StackElement[existingStack.length];
        for (int i = 0; i < newStack.length; i++)
        {
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
                                                 @NotNull Collection<? extends StackElement> stack)
    {
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

    public static void checkSameType(@NotNull StackElement element, @NotNull StackElement expectedElement)
    {
        if (element.type() != expectedElement.type())
            throw new StackElementMismatchedException(
                    element.producer(), expectedElement, element,
                    "Cannot merge stack element with different types: " +
                            element.type() + " produced by " + element.producer() +
                            " and " + expectedElement.type() + " produced by " + expectedElement.producer()
            );
    }

    public static StackElement mergeElement(@NotNull StackElement existingElement, @NotNull StackElement newElement)
    {
        StackElementUtils.checkSameType(existingElement, newElement);

        // null は任意のオブジェクト型と互換性があるのでそのまま返す
        if (newElement instanceof NullElement)
            return newElement;
        else if (existingElement instanceof NullElement)
            return existingElement;

        return switch (existingElement.type())
        {
            case TOP, INTEGER, FLOAT, LONG, DOUBLE, NULL, RETURN_ADDRESS, NOP -> existingElement;
            // オブジェクト型はマージする
            case OBJECT ->
            {
                assert newElement instanceof ObjectElement;  // #checkType() でチェック済み
                yield mergeObjects((ObjectElement) existingElement, (ObjectElement) newElement);
            }
            case UNINITIALIZED_THIS, UNINITIALIZED -> newElement;  // Uninitialized は新しい要素をそのまま返す
        };
    }

    public static ObjectElement mergeObjects(@NotNull ObjectElement existingObject, @NotNull ObjectElement newObject)
    {
        TypeDescriptor existingType = existingObject.content();
        TypeDescriptor newType = newObject.content();
        if (existingType.equals(newType)
                || existingType.getBaseType().equals(ClassReferenceType.OBJECT))
        {
            // 型が同じまたは Object 型の場合はそのまま返す
            return newObject;
        }

        if (existingType.getArrayDimensions() != newType.getArrayDimensions())
        {
            // 配列の次元が異なる場合はエラー
            throw new StackElementMismatchedException(
                    newObject.producer(), existingObject, newObject,
                    "Cannot merge object stack elements with different array dimensions: " +
                            existingType + "produced by " + existingObject.producer()
                            + " and " + newType + " produced by " + newObject.producer()
            );
        }

        if (existingType.getBaseType().equals(newType.getBaseType()))
            return newObject; // 基本型が同じならそのまま返す
        else if (existingType.getBaseType().isPrimitive() || newType.getBaseType().isPrimitive())
        {
            // ここに到達するプリミティブは，型が違うことが保証されている
            throw new StackElementMismatchedException(
                    newObject.producer(), existingObject, newObject,
                    "Cannot merge object stack elements with different primitive types: " +
                            existingType + " produced by " + existingObject.producer()
                            + " and " + newType + " produced by " + newObject.producer()
            );

        }

        // 共通のスーパークラスを求める
        int arayDimension = existingType.getArrayDimensions();  // 配列の次元は同じなのでどちらか一方を使う
        ClassReferenceType commonSuperType = getCommonSuperType(
                (ClassReferenceType) existingType.getBaseType(),  // 参照型なのは保証されている
                (ClassReferenceType) newType.getBaseType()
        );

        TypeDescriptor mergedType = new TypeDescriptor(commonSuperType, arayDimension);
        // 新しい ObjectElement を返す
        return new ObjectElement(newObject.producer(), mergedType);
    }

    public static ClassReferenceType getCommonSuperType(@NotNull ClassReferenceType type1,
                                                        @NotNull ClassReferenceType type2)
    {
        ClassLoader classLoader = ClassReferenceType.class.getClassLoader();
        Class<?> class1, class2;
        try
        {
            class1 = Class.forName(type1.getDottedName(), false, classLoader);
            class2 = Class.forName(type2.getDottedName(), false, classLoader);
        }
        catch (ClassNotFoundException e)
        {
            return type1;  // クラスが見つからない場合は，片方の型をそのまま返す
        }

        if (class1.isAssignableFrom(class2))
            return type1;  // type1 が type2 のスーパークラスなら type1 を返す
        else if (class2.isAssignableFrom(class1))
            return type2;  // type2 が type1 のスーパークラスなら type2 を返す

        if (class1.isInterface() || class2.isInterface())
            return ClassReferenceType.parse(Object.class.getName());  // インターフェースの場合は Object を返す

        do
        {
            class1 = class1.getSuperclass();
        }
        while (class1.isAssignableFrom(class2));

        // 共通のスーパークラスを返す。 Object まで到達するので，その時は Object を返す
        return ClassReferenceType.parse(class1.getName());
    }

    static String stackToString(@NotNull Collection<? extends StackElement> stack)
    {
        return stackToString(stack.toArray(new StackElement[0]));
    }

    static <T extends StackElement> String stackToString(@NotNull T[] stack)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < stack.length; i++)
        {
            sb.append(stackElementToString(stack[i]));
            if (i < stack.length - 1)
                sb.append(" | ");
        }

        sb.append("]");
        return sb.toString();
    }

    private static String stackElementToString(@NotNull StackElement element)
    {
        if (element instanceof LocalStackElement local)
            element = local.stackElement();

        return switch (element.type())
        {
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
