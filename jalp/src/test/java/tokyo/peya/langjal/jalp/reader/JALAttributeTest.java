package tokyo.peya.langjal.jalp.reader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class JALAttributeTest {
    @Test
    void codeAttributeReturnsNestedAttributeByType() {
        JALAttribute.LineNumberTableAttribute lineNumberTable =
                new JALAttribute.LineNumberTableAttribute("LineNumberTable", new JALAttribute.LineNumberTableAttribute.LineNumber[0]);
        JALAttribute.CodeAttribute code = new JALAttribute.CodeAttribute(
                "Code",
                1,
                1,
                new byte[0],
                new JALAttribute.CodeAttribute.ExceptionHandler[0],
                new JALAttribute[]{new JALAttribute.SyntheticAttribute("Synthetic"), lineNumberTable}
        );

        assertSame(lineNumberTable, code.getAttribute(JALAttribute.LineNumberTableAttribute.class));
    }

    @Test
    void codeAttributeReturnsNullWhenNestedAttributeDoesNotExist() {
        JALAttribute.CodeAttribute code = new JALAttribute.CodeAttribute(
                "Code",
                1,
                1,
                new byte[0],
                new JALAttribute.CodeAttribute.ExceptionHandler[0],
                new JALAttribute[0]
        );

        assertNull(code.getAttribute(JALAttribute.LineNumberTableAttribute.class));
    }
}
