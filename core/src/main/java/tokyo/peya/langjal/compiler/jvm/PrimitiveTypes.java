package tokyo.peya.langjal.compiler.jvm;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import tokyo.peya.langjal.analyser.stack.StackElementType;

@Getter
@AllArgsConstructor
public enum PrimitiveTypes implements Type
{
    BYTE("byte", 'B', EOpcodes.T_BYTE)
            {
                @Override
                public StackElementType getStackElementType()
                {
                    return StackElementType.INTEGER; // バイトは整数型として扱う
                }
            },
    SHORT("short", 'S', EOpcodes.T_SHORT)
            {
                @Override
                public StackElementType getStackElementType()
                {
                    return StackElementType.INTEGER; // ショートは整数型として扱う
                }
            },
    INT("int", 'I', EOpcodes.T_INT)
            {
                @Override
                public StackElementType getStackElementType()
                {
                    return StackElementType.INTEGER;
                }
            },
    LONG("long", 'J', EOpcodes.T_LONG)
            {
                @Override
                public StackElementType getStackElementType()
                {
                    return StackElementType.LONG;
                }
            },
    FLOAT("float", 'F', EOpcodes.T_FLOAT)
            {
                @Override
                public StackElementType getStackElementType()
                {
                    return StackElementType.FLOAT;
                }
            },
    DOUBLE("double", 'D', EOpcodes.T_DOUBLE)
            {
                @Override
                public StackElementType getStackElementType()
                {
                    return StackElementType.DOUBLE;
                }
            },
    BOOLEAN("boolean", 'Z', EOpcodes.T_BOOLEAN)
            {
                @Override
                public StackElementType getStackElementType()
                {
                    return StackElementType.INTEGER; // ブール値は整数型として扱う
                }
            },
    CHAR("char", 'C', EOpcodes.T_CHAR)
            {
                @Override
                public StackElementType getStackElementType()
                {
                    return StackElementType.INTEGER; // キャラクターは整数型として扱う
                }
            },
    VOID("void", 'V', -1)
            {
                @Override
                public StackElementType getStackElementType()
                {
                    return StackElementType.TOP; // 仮でTOPを返す
                }
            };

    private final String name;
    @Getter(AccessLevel.NONE)
    private final char descriptor;
    private final int asmType;

    @Override
    public boolean isPrimitive()
    {
        return true;
    }

    @Override
    public String getDescriptor()
    {
        return String.valueOf(this.descriptor);
    }

    @Override
    public int getCategory()
    {
        if (this == LONG || this == DOUBLE)
            return 2; // 長い型はカテゴリー2

        return 1; // その他のプリミティブ型はカテゴリー1
    }

    public static PrimitiveTypes fromDescriptor(char descriptorChar)
    {
        for (PrimitiveTypes type : PrimitiveTypes.values())
            if (type.descriptor == descriptorChar)
                return type; // 該当するプリミティブ型を返す

        return null; // 該当するプリミティブ型がない場合はnullを返す
    }

    @Override
    public String toString()
    {
        return String.valueOf(this.descriptor);
    }

    public static PrimitiveTypes fromASMType(int asmType)
    {
        for (PrimitiveTypes type : PrimitiveTypes.values())
            if (type.asmType == asmType)
                return type; // 該当するASM型を持つプリミティブ型を返す

        return null; // 該当するプリミティブ型がない場合はnullを返す
    }
}

