package tokyo.peya.langjal.compiler.jvm;

import tokyo.peya.langjal.analyser.stack.StackElementType;

public interface Type
{
    boolean isPrimitive();

    String getDescriptor();

    int getCategory();

    StackElementType getStackElementType();
}
