package tokyo.peya.langjal.jalp.printers;

import org.junit.jupiter.api.Test;
import tokyo.peya.langjal.jalp.reader.JALAttribute;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LineNumberMatcherTest {
    @Test
    void nullLineNumberTableNeverPlacesBlanks() {
        LineNumberMatcher matcher = new LineNumberMatcher(null);

        assertEquals(0, matcher.calcBlanksToPlace(0));
        assertEquals(0, matcher.calcBlanksToPlace(10));
    }

    @Test
    void unmatchedProgramCounterDoesNotPlaceBlanks() {
        LineNumberMatcher matcher = new LineNumberMatcher(lineNumberTable(
                line(0, 10),
                line(5, 12)
        ));

        assertEquals(0, matcher.calcBlanksToPlace(1));
    }

    @Test
    void matchedProgramCounterPlacesAtMostTwoBlanksForLineGap() {
        LineNumberMatcher matcher = new LineNumberMatcher(lineNumberTable(
                line(0, 10),
                line(5, 13),
                line(9, 16)
        ));

        assertEquals(0, matcher.calcBlanksToPlace(0));
        assertEquals(2, matcher.calcBlanksToPlace(5));
        assertEquals(2, matcher.calcBlanksToPlace(9));
    }

    @Test
    void constructorSortsLineNumbersByProgramCounter() {
        LineNumberMatcher matcher = new LineNumberMatcher(lineNumberTable(
                line(10, 15),
                line(0, 10),
                line(5, 12)
        ));

        assertEquals(0, matcher.calcBlanksToPlace(0));
        assertEquals(1, matcher.calcBlanksToPlace(5));
        assertEquals(2, matcher.calcBlanksToPlace(10));
    }

    private static JALAttribute.LineNumberTableAttribute lineNumberTable(
            JALAttribute.LineNumberTableAttribute.LineNumber... lines
    ) {
        return new JALAttribute.LineNumberTableAttribute("LineNumberTable", lines);
    }

    private static JALAttribute.LineNumberTableAttribute.LineNumber line(int startPc, int lineNumber) {
        return new JALAttribute.LineNumberTableAttribute.LineNumber(startPc, lineNumber);
    }
}
