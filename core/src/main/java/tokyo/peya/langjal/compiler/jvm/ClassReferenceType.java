package tokyo.peya.langjal.compiler.jvm;

import org.jetbrains.annotations.NotNull;
import tokyo.peya.langjal.analyser.stack.StackElementType;

import java.util.Objects;

public class ClassReferenceType implements Type
{
    public static final ClassReferenceType OBJECT = new ClassReferenceType("java/lang", "Object");

    @NotNull
    private final String packageName;
    @NotNull
    private final String className;

    private ClassReferenceType(@NotNull String packageName, @NotNull String className)
    {
        this.packageName = packageName;
        this.className = className;
    }

    @Override
    public boolean isPrimitive()
    {
        return false;
    }

    @Override
    public int getCategory()
    {
        return 1; // 定数プールにあるので cat 1
    }

    @Override
    public StackElementType getStackElementType()
    {
        return StackElementType.OBJECT; // オブジェクト型なので OBJECT
    }

    @Override
    public String getDescriptor()
    {
        return "L" + this.packageName + "/" + this.className + ";";
    }

    public String getInternalName()
    {
        return this.packageName.isEmpty() ? this.className: this.packageName + "/" + this.className;
    }

    public String getDottedName()
    {
        return this.getInternalName().replace('/', '.');
    }

    public static ClassReferenceType parse(@NotNull String typeName)
    {
        if (typeName.startsWith("L"))
            typeName = typeName.substring(1);
        if (typeName.endsWith(";"))
            typeName = typeName.substring(0, typeName.length() - 1);
        if (typeName.contains("."))
            typeName = typeName.replace('.', '/'); // ドットをスラッシュに変換

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

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof ClassReferenceType that))
            return false;
        return Objects.equals(this.packageName, that.packageName)
                && Objects.equals(this.className, that.className);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.packageName, this.className);
    }
}
