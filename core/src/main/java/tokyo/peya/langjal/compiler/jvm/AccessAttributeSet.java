package tokyo.peya.langjal.compiler.jvm;

import lombok.Getter;

import java.util.Collection;

/**
 * Represents a set of access attributes for a class, method, or field.
 */
@Getter
public class AccessAttributeSet
{
    /**
     * An empty set of access attributes.
     */
    public static final AccessAttributeSet EMPTY = new AccessAttributeSet();

    /**
     * The array of access attributes in this set.
     */
    private final AccessAttribute[] attributes;

    /**
     * Constructs an AccessAttributeSet from a collection of attributes.
     *
     * @param attributes The collection of access attributes.
     */
    public AccessAttributeSet(Collection<AccessAttribute> attributes)
    {
        this.attributes = attributes.toArray(new AccessAttribute[0]);
    }

    /**
     * Constructs an AccessAttributeSet from attribute names.
     *
     * @param attributeNames The names of the attributes.
     */
    public AccessAttributeSet(String... attributeNames)
    {
        this.attributes = new AccessAttribute[attributeNames.length];
        for (int i = 0; i < attributeNames.length; i++)
            this.attributes[i] = AccessAttribute.valueOf(attributeNames[i].toUpperCase());
    }

    /**
     * Checks if the set contains the specified attribute.
     *
     * @param attribute The attribute to check.
     * @return true if present, false otherwise.
     */
    public boolean has(AccessAttribute attribute)
    {
        for (AccessAttribute attr : this.attributes)
        {
            if (attr == attribute)
                return true;
        }
        return false;
    }

    /**
     * Returns a string representation of the attribute set.
     *
     * @return A space-separated string of attribute names.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (AccessAttribute attribute : this.attributes)
        {
            if (sb.isEmpty())
                sb.append(attribute.getName());
            else
                sb.append(" ");
        }
        return sb.toString();
    }

    /**
     * Checks if the set represents a normal class (not abstract, interface, enum, or annotation).
     *
     * @return true if normal class, false otherwise.
     */
    public boolean isNormalClass()
    {
        for (AccessAttribute attribute : this.attributes)
        {
            if (attribute == AccessAttribute.ABSTRACT || attribute == AccessAttribute.INTERFACE
                    || attribute == AccessAttribute.ENUM || attribute == AccessAttribute.ANNOTATION)
                return false;
        }
        return true;
    }
}
