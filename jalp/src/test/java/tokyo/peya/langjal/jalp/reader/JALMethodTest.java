package tokyo.peya.langjal.jalp.reader;

import org.junit.jupiter.api.Test;
import tokyo.peya.langjal.compiler.jvm.AccessLevel;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class JALMethodTest {
    @Test
    void getAttributeReturnsFirstMatchingAttribute() {
        JALAttribute.CodeAttribute code = new JALAttribute.CodeAttribute(
                "Code",
                1,
                1,
                new byte[0],
                new JALAttribute.CodeAttribute.ExceptionHandler[0],
                new JALAttribute[0]
        );
        JALMethod method = new JALMethod(
                AccessLevel.PUBLIC,
                null,
                "method",
                null,
                new JALAttribute[]{new JALAttribute.SyntheticAttribute("Synthetic"), code}
        );

        assertSame(code, method.getAttribute(JALAttribute.CodeAttribute.class));
    }

    @Test
    void getAttributeReturnsNullWhenNoAttributeMatches() {
        JALMethod method = new JALMethod(
                AccessLevel.PUBLIC,
                null,
                "method",
                null,
                new JALAttribute[]{new JALAttribute.SyntheticAttribute("Synthetic")}
        );

        assertNull(method.getAttribute(JALAttribute.CodeAttribute.class));
    }
}
