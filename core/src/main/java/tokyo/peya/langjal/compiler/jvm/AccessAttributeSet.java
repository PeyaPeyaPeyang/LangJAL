package tokyo.peya.langjal.compiler.jvm;

import lombok.Getter;

import java.util.Collection;

@Getter
public class AccessAttributeSet
{
    public static final AccessAttributeSet EMPTY = new AccessAttributeSet();

    private final AccessAttribute[] attributes;

    public AccessAttributeSet(Collection<AccessAttribute> attributes)
    {
        this.attributes = attributes.toArray(new AccessAttribute[0]);
    }

    public AccessAttributeSet(String... attributeNames)
    {
        this.attributes = new AccessAttribute[attributeNames.length];
        for (int i = 0; i < attributeNames.length; i++)
            this.attributes[i] = AccessAttribute.valueOf(attributeNames[i].toUpperCase());
    }

    public boolean has(AccessAttribute attribute)
    {
        for (AccessAttribute attr : this.attributes)
        {
            if (attr == attribute)
                return true;
        }
        return false;
    }

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
