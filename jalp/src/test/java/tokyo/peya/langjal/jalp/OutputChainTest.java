package tokyo.peya.langjal.jalp;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class OutputChainTest {
    @Test
    void printOutputsBufferedTextThroughFormatter() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        OutputFormatter formatter = new OutputFormatter(new OutputFormatter(new PrintStream(output)));

        OutputFormatter returned = formatter.chained()
                .output("hello")
                .output(" ")
                .righten(5, "JAL")
                .print();

        assertSame(formatter, returned);

        assertEquals("  hello   JAL", output.toString());
    }

    @Test
    void printlnOutputsBufferedTextWithLineSeparator() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        OutputFormatter formatter = new OutputFormatter(new PrintStream(output));

        formatter.chained().output("line").println();

        assertEquals("line" + System.lineSeparator(), output.toString());
    }
}
