package tokyo.peya.langjal.compiler.jvm;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Represents a method descriptor as defined in the JVM specification.
 * <p>
 * In JVM bytecode, a method descriptor encodes the parameter types and return type of a method
 * using a compact string format. This descriptor is used in class files to describe methods
 * for linking, reflection, and invocation.
 * <br>
 * The format is: <code>(ParameterDescriptor...)ReturnDescriptor</code>
 * <br>
 * For example, a method <code>int add(int a, double b)</code> has the descriptor <code>(ID)I</code>:
 * <ul>
 *   <li><code>(</code> and <code>)</code> enclose the parameter descriptors</li>
 *   <li><code>I</code> stands for <code>int</code>, <code>D</code> for <code>double</code></li>
 *   <li>The return type <code>I</code> is <code>int</code></li>
 * </ul>
 * <br>
 * Reference: <a href="https://docs.oracle.com/javase/specs/jvms/se24/html/jvms-4.html#jvms-4.3.3">JVM Spec 4.3.3: Method Descriptors</a>
 */
@Getter
public class MethodDescriptor
{
    /**
     * The return type of the method.
     */
    private final TypeDescriptor returnType;
    /**
     * The parameter types of the method.
     */
    private final TypeDescriptor[] parameterTypes;
    /**
     * The JVM descriptor string.
     */
    private final String descriptorString;

    private MethodDescriptor(TypeDescriptor returnType, TypeDescriptor[] parameterTypes, String descriptorString)
    {
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
        this.descriptorString = descriptorString;
    }

    /**
     * Returns the JVM descriptor string for this method.
     * @return The descriptor string.
     */
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

    /**
     * Checks equality with another object.
     * @param o The object to compare.
     * @return True if equal, false otherwise.
     */
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

    /**
     * Checks equality with a descriptor string.
     * @param s The descriptor string.
     * @return True if equal, false otherwise.
     */
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

    /**
     * Returns the hash code for this method descriptor.
     * @return The hash code.
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(getReturnType(), Arrays.hashCode(getParameterTypes()), getDescriptorString());
    }

    /**
     * Parses a JVM method descriptor string.
     * @param descriptor The descriptor string.
     * @return The parsed MethodDescriptor.
     */
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
