package tokyo.peya.langjal.compiler.jvm;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.analyser.stack.ObjectElement;
import tokyo.peya.langjal.analyser.stack.PrimitiveElement;
import tokyo.peya.langjal.analyser.stack.StackElement;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

import java.util.Objects;

/**
 * Represents a type descriptor in JVM format.
 */
@Getter
public class TypeDescriptor
{
    /**
     * TypeDescriptor for java.lang.Object.
     */
    public static final TypeDescriptor OBJECT = TypeDescriptor.className("Ljava/lang/Object;");
    /**
     * TypeDescriptor for byte.
     */
    public static final TypeDescriptor BYTE = new TypeDescriptor(PrimitiveTypes.BYTE);
    /**
     * TypeDescriptor for char.
     */
    public static final TypeDescriptor CHAR = new TypeDescriptor(PrimitiveTypes.CHAR);
    /**
     * TypeDescriptor for double.
     */
    public static final TypeDescriptor DOUBLE = new TypeDescriptor(PrimitiveTypes.DOUBLE);
    /**
     * TypeDescriptor for float.
     */
    public static final TypeDescriptor FLOAT = new TypeDescriptor(PrimitiveTypes.FLOAT);
    /**
     * TypeDescriptor for long.
     */
    public static final TypeDescriptor LONG = new TypeDescriptor(PrimitiveTypes.LONG);
    /**
     * TypeDescriptor for int.
     */
    public static final TypeDescriptor INTEGER = new TypeDescriptor(PrimitiveTypes.INT);
    /**
     * TypeDescriptor for short.
     */
    public static final TypeDescriptor SHORT = new TypeDescriptor(PrimitiveTypes.SHORT);
    /**
     * TypeDescriptor for boolean.
     */
    public static final TypeDescriptor BOOLEAN = new TypeDescriptor(PrimitiveTypes.BOOLEAN);
    /**
     * TypeDescriptor for void.
     */
    public static final TypeDescriptor VOID = new TypeDescriptor(PrimitiveTypes.VOID);


    /**
     * The base type.
     */
    private final Type baseType;
    /**
     * The number of array dimensions.
     */
    private final int arrayDimensions;

    /**
     * Constructs a TypeDescriptor with base type and array dimensions.
     * @param baseType The base type.
     * @param arrayDimensions The number of array dimensions.
     */
    public TypeDescriptor(Type baseType, int arrayDimensions)
    {
        this.baseType = baseType;
        this.arrayDimensions = arrayDimensions;

        if (arrayDimensions < 0)
            throw new IllegalArgumentException("Array dimensions cannot be negative: " + arrayDimensions);
    }

    /**
     * Constructs a TypeDescriptor with base type and zero array dimensions.
     * @param baseType The base type.
     */
    public TypeDescriptor(Type baseType)
    {
        this(baseType, 0);
    }

    /**
     * Returns true if this is an array type.
     * @return True if array, false otherwise.
     */
    public boolean isArray()
    {
        return this.arrayDimensions > 0;
    }

    /**
     * Returns the JVM descriptor string for this type.
     * @return The descriptor string.
     */
    @Override
    public String toString()
    {
        return "[".repeat(Math.max(0, this.arrayDimensions)) + this.baseType.getDescriptor();
    }

    /**
     * Parses a JVM type descriptor string.
     * @param descriptor The descriptor string.
     * @return The parsed TypeDescriptor.
     */
    public static TypeDescriptor parse(String descriptor)
    {
        return parse(DescriptorReader.fromString(descriptor));
    }

    /**
     * Converts this type descriptor to a stack element.
     * @param producer The instruction info that produces the element.
     * @return The corresponding StackElement.
     */
    public StackElement toStackElement(@NotNull InstructionInfo producer)
    {
        if (this.baseType.isPrimitive())
            return new PrimitiveElement(producer, this.baseType.getStackElementType());
        else
            return new ObjectElement(producer, TypeDescriptor.parse(this.toString()));
    }

    /**
     * Checks equality with another object.
     * @param o The object to compare.
     * @return True if equal, false otherwise.
     */
    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof TypeDescriptor that)) return false;
        return getArrayDimensions() == that.getArrayDimensions() &&
                Objects.equals(
                        getBaseType(),
                        that.getBaseType()
                );
    }

    /**
     * Returns the hash code for this type descriptor.
     * @return The hash code.
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(getBaseType(), getArrayDimensions());
    }

    /**
     * Creates a TypeDescriptor from a class name.
     * @param className The JVM class name.
     * @return The corresponding TypeDescriptor.
     */
    public static TypeDescriptor className(String className)
    {
        return new TypeDescriptor(ClassReferenceType.parse(className));
    }

    static TypeDescriptor parse(DescriptorReader reader)
    {
        int dim = 0;

        // 配列の次元をカウントする
        while (reader.peek() == '[')
        {
            dim++;
            reader.read(); // skip '['
        }

        char c = reader.read(); // プリミティブ or L(オブジェクト型の識別子)

        Type type;
        if (c == 'L')
        {
            StringBuilder className = new StringBuilder();
            while (reader.peek() != ';')
            {
                className.append(reader.read());
                if (!reader.hasMore())
                    throw new IllegalArgumentException("Unterminated object type");
            }
            reader.read(); // skip ';'
            type = ClassReferenceType.parse(className.toString());
        }
        else
        {
            // プリミティブ型の処理
            type = PrimitiveTypes.fromDescriptor(c);
            if (type == null)
                throw new IllegalArgumentException("Unknown type: " + c);
        }

        return new TypeDescriptor(type, dim);
    }
}
