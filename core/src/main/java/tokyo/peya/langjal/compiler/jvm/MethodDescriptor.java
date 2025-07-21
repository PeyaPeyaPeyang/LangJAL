package tokyo.peya.langjal.compiler.jvm;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
public class MethodDescriptor
{
    private final TypeDescriptor returnType;
    private final TypeDescriptor[] parameterTypes;

    private final String descriptorString;

    private MethodDescriptor(TypeDescriptor returnType, TypeDescriptor[] parameterTypes, String descriptorString)
    {
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
        this.descriptorString = descriptorString;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (TypeDescriptor type : this.parameterTypes)
            sb.append(type);
        sb.append(')').append(this.returnType.toString());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof MethodDescriptor that))
            return false;

        return Objects.equals(this.getReturnType(), that.getReturnType()) &&
                Arrays.equals(this.getParameterTypes(), that.getParameterTypes()) &&
                Objects.equals(this.getDescriptorString(), that.getDescriptorString());
    }

    public boolean equals(String s)
    {
        if (s == null || s.isEmpty())
            return false;
        if (this.getDescriptorString().equals(s))
            return true;

        try
        {
            MethodDescriptor other = MethodDescriptor.parse(s);
            return this.equals(other);
        }
        catch (Exception e)
        {
            return false;
        }
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getReturnType(), Arrays.hashCode(getParameterTypes()), getDescriptorString());
    }

    public static MethodDescriptor parse(String descriptor)
    {
        DescriptorReader reader = DescriptorReader.fromString(descriptor);

        reader.expect('(');
        List<TypeDescriptor> parameters = new ArrayList<>();

        while (reader.peek() != ')')
            parameters.add(TypeDescriptor.parse(reader));

        reader.expect(')');

        TypeDescriptor returnType = TypeDescriptor.parse(reader);

        return new MethodDescriptor(returnType, parameters.toArray(new TypeDescriptor[0]), descriptor);
    }
}
