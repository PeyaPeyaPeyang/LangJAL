package tokyo.peya.langjal.compiler.jvm;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.analyser.stack.ObjectElement;
import tokyo.peya.langjal.analyser.stack.PrimitiveElement;
import tokyo.peya.langjal.analyser.stack.StackElement;
import tokyo.peya.langjal.compiler.member.InstructionInfo;

import java.util.Objects;

@Getter
public class TypeDescriptor
{
    public static final TypeDescriptor OBJECT = TypeDescriptor.className("Ljava/lang/Object;");

    public static final TypeDescriptor BYTE = new TypeDescriptor(PrimitiveTypes.BYTE);
    public static final TypeDescriptor CHAR = new TypeDescriptor(PrimitiveTypes.CHAR);
    public static final TypeDescriptor DOUBLE = new TypeDescriptor(PrimitiveTypes.DOUBLE);
    public static final TypeDescriptor FLOAT = new TypeDescriptor(PrimitiveTypes.FLOAT);
    public static final TypeDescriptor LONG = new TypeDescriptor(PrimitiveTypes.LONG);
    public static final TypeDescriptor INTEGER = new TypeDescriptor(PrimitiveTypes.INT);
    public static final TypeDescriptor SHORT = new TypeDescriptor(PrimitiveTypes.SHORT);
    public static final TypeDescriptor BOOLEAN = new TypeDescriptor(PrimitiveTypes.BOOLEAN);
    public static final TypeDescriptor VOID = new TypeDescriptor(PrimitiveTypes.VOID);


    private final Type baseType;
    private final int arrayDimensions;

    public TypeDescriptor(Type baseType, int arrayDimensions)
    {
        this.baseType = baseType;
        this.arrayDimensions = arrayDimensions;

        if (arrayDimensions < 0)
            throw new IllegalArgumentException("Array dimensions cannot be negative: " + arrayDimensions);
    }

    public TypeDescriptor(Type baseType)
    {
        this(baseType, 0);
    }

    public boolean isArray()
    {
        return this.arrayDimensions > 0;
    }

    @Override
    public String toString()
    {
        return "[".repeat(Math.max(0, this.arrayDimensions)) + this.baseType.getDescriptor();
    }

    public static TypeDescriptor parse(String descriptor)
    {
        return parse(DescriptorReader.fromString(descriptor));
    }

    public StackElement toStackElement(@NotNull InstructionInfo producer)
    {
        if (this.baseType.isPrimitive())
            return new PrimitiveElement(producer, this.baseType.getStackElementType());
        else
            return new ObjectElement(producer, TypeDescriptor.parse(this.toString()));
    }

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

    @Override
    public int hashCode()
    {
        return Objects.hash(getBaseType(), getArrayDimensions());
    }

    public static TypeDescriptor className(String className)
    {
        return new TypeDescriptor(ClassReferenceType.parse(className));
    }
}
