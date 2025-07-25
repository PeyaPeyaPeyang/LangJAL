package tokyo.peya.langjal.compiler.jvm;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    public static AccessAttributeSet fromAccess(int access)
    {
        List<AccessAttribute> attributes = new ArrayList<>();
        if ((access & EOpcodes.ACC_STATIC) != 0)  // 0x0008
            attributes.add(AccessAttribute.STATIC);
        if ((access & EOpcodes.ACC_FINAL) != 0)  // 0x0010
            attributes.add(AccessAttribute.FINAL);
        if ((access & EOpcodes.ACC_SUPER) != 0)  // 0x0020
            attributes.add(AccessAttribute.SUPER);
        if ((access & EOpcodes.ACC_SYNCHRONIZED) != 0)  // 0x0020
            attributes.add(AccessAttribute.SYNCHRONIZED);
        if ((access & EOpcodes.ACC_VOLATILE) != 0)  // 0x0040
            attributes.add(AccessAttribute.VOLATILE);
        if ((access & EOpcodes.ACC_BRIDGE) != 0)  // 0x0040
            attributes.add(AccessAttribute.BRIDGE);
        if ((access & EOpcodes.ACC_VARARGS) != 0)  // 0x0080
            attributes.add(AccessAttribute.VARARGS);
        if ((access & EOpcodes.ACC_TRANSIENT) != 0)  // 0x0080
            attributes.add(AccessAttribute.TRANSIENT);
        if ((access & EOpcodes.ACC_NATIVE) != 0)  // 0x0100
            attributes.add(AccessAttribute.NATIVE);
        if ((access & EOpcodes.ACC_INTERFACE) != 0)  // 0x0200
            attributes.add(AccessAttribute.INTERFACE);
        if ((access & EOpcodes.ACC_ABSTRACT) != 0)  // 0x0400
            attributes.add(AccessAttribute.ABSTRACT);
        if ((access & EOpcodes.ACC_STRICT) != 0)  // 0x0800
            attributes.add(AccessAttribute.STRICTFP);
        if ((access & EOpcodes.ACC_SYNTHETIC) != 0)  // 0x1000
            attributes.add(AccessAttribute.SYNTHETIC);
        if ((access & EOpcodes.ACC_ANNOTATION) != 0)  // 0x2000
            attributes.add(AccessAttribute.ANNOTATION);
        if ((access & EOpcodes.ACC_ENUM) != 0)  // 0x4000
            attributes.add(AccessAttribute.ENUM);
        if ((access & EOpcodes.ACC_MANDATED) != 0)  // 0x20000
            attributes.add(AccessAttribute.MANDATED);

        return new AccessAttributeSet(attributes);
    }
}
