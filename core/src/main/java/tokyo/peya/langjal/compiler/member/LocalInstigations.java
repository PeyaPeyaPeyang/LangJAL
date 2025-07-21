package tokyo.peya.langjal.compiler.member;

import lombok.experimental.UtilityClass;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.Nullable;
import tokyo.peya.langjal.compiler.JALParser;

@UtilityClass
public class LocalInstigations
{
    @Nullable
    public static String getLocalName(@Nullable JALParser.LocalInstigationContext instigationContext)
    {
        if (instigationContext == null)
            return null;

        TerminalNode id = instigationContext.ID();
        if (id == null)
            return null;

        return id.getText();
    }
}
