package tokyo.peya.langjal.jalp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class OutputFormatterTest {
    @Test
    void rootFormatterDoesNotIndentOutput() {
        OutputFormatter formatter = new OutputFormatter();

        assertEquals("value", formatter.output("value"));
        assertEquals("value", formatter.noIndentOutput("value"));
        assertNull(formatter.parent());
    }

    @Test
    void childFormatterIndentsOutputByTwoSpaces() {
        OutputFormatter root = new OutputFormatter();
        OutputFormatter child = new OutputFormatter(root);

        assertEquals("  value", child.output("value"));
        assertEquals("value", child.noIndentOutput("value"));
        assertSame(root, child.parent());
    }

    @Test
    void nestedFormatterUsesParentIndentForNoIndentOutput() {
        OutputFormatter child = new OutputFormatter(new OutputFormatter());
        OutputFormatter grandChild = new OutputFormatter(child);

        assertEquals("    value", grandChild.output("value"));
        assertEquals("  value", grandChild.noIndentOutput("value"));
    }

    @Test
    void rightenPadsBeforeApplyingIndent() {
        OutputFormatter formatter = new OutputFormatter(new OutputFormatter());

        assertEquals("    value", formatter.righten(7, "value"));
        assertEquals("  value", formatter.righten(3, "value"));
    }
}
