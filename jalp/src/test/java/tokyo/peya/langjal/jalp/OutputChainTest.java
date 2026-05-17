package tokyo.peya.langjal.jalp;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class OutputChainTest {
    @Test
    void printOutputsBufferedTextThroughFormatter() {
        OutputFormatter formatter = new OutputFormatter(new OutputFormatter());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));
        try {
            OutputFormatter returned = formatter.chained()
                    .output("hello")
                    .output(" ")
                    .righten(5, "JAL")
                    .print();

            assertSame(formatter, returned);
        } finally {
            System.setOut(originalOut);
        }

        assertEquals("  hello   JAL", output.toString());
    }

    @Test
    void printlnOutputsBufferedTextWithLineSeparator() {
        OutputFormatter formatter = new OutputFormatter();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));
        try {
            formatter.chained().output("line").println();
        } finally {
            System.setOut(originalOut);
        }

        assertEquals("line" + System.lineSeparator(), output.toString());
    }
}
