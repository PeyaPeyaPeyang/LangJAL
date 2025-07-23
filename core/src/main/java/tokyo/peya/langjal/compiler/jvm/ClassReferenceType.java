package tokyo.peya.langjal.compiler.jvm;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.analyser.stack.StackElementType;

import java.util.Objects;

/**
 * Represents a reference type for a class in the JVM type system.
 * Stores package and class name information and provides utility methods for descriptors and names.
 */
public class ClassReferenceType implements Type
{
    /**
     * Reference to the java/lang/Object type.
     */
    public static final ClassReferenceType OBJECT = new ClassReferenceType("java/lang", "Object");

    @NotNull
    private final String packageName;
    @NotNull
    private final String className;

    /**
     * Constructs a ClassReferenceType with the specified package and class name.
     *
     * @param packageName the package name (slash-separated)
     * @param className   the class name
     */
    private ClassReferenceType(@NotNull String packageName, @NotNull String className)
    {
        this.packageName = packageName;
        this.className = className;
    }

    /**
     * Returns false, as class reference types are not primitive.
     *
     * @return false
     */
    @Override
    public boolean isPrimitive()
    {
        return false;
    }

    /**
     * Returns the category for this type (always 1 for reference types).
     *
     * @return 1
     */
    @Override
    public int getCategory()
    {
        return 1; // Reference types are always category 1
    }

    /**
     * Returns the stack element type for this class reference (OBJECT).
     *
     * @return StackElementType.OBJECT
     */
    @Override
    public StackElementType getStackElementType()
    {
        return StackElementType.OBJECT;
    }

    /**
     * Returns the JVM descriptor string for this class reference.
     *
     * @return the descriptor string
     */
    @Override
    public String getDescriptor()
    {
        return "L" + this.packageName + "/" + this.className + ";";
    }

    /**
     * Returns the internal JVM name for this class (slash-separated).
     *
     * @return the internal name
     */
    public String getInternalName()
    {
        return this.packageName.isEmpty() ? this.className: this.packageName + "/" + this.className;
    }

    /**
     * Returns the dotted name for this class (dot-separated).
     *
     * @return the dotted name
     */
    public String getDottedName()
    {
        return this.getInternalName().replace('/', '.');
    }

    /**
     * Parses a type name string and returns a corresponding ClassReferenceType.
     *
     * @param typeName the type name to parse
     * @return the ClassReferenceType instance
     */
    public static ClassReferenceType parse(@NotNull String typeName)
    {
        if (typeName.startsWith("L"))
            typeName = typeName.substring(1);
        if (typeName.endsWith(";"))
            typeName = typeName.substring(0, typeName.length() - 1);
        if (typeName.contains("."))
            typeName = typeName.replace('.', '/');

        String[] parts = typeName.split("/");
        if (parts.length == 1)
            return new ClassReferenceType("", parts[0]);
        else
        {
            String[] packageParts = new String[parts.length - 1];
            System.arraycopy(parts, 0, packageParts, 0, parts.length - 1);
            String packageName = String.join("/", packageParts);
            String className = parts[parts.length - 1];
            return new ClassReferenceType(packageName, className);
        }
    }

    /**
     * Checks equality with another object.
     *
     * @param o the object to compare
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof ClassReferenceType that))
            return false;
        return Objects.equals(this.packageName, that.packageName)
                && Objects.equals(this.className, that.className);
    }

    /**
     * Returns the hash code for this class reference type.
     *
     * @return the hash code
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(this.packageName, this.className);
    }
}
