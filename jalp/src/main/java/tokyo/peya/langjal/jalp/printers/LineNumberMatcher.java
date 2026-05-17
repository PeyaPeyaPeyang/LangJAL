package tokyo.peya.langjal.jalp.printers;

import org.jetbrains.annotations.Nullable;
import tokyo.peya.langjal.jalp.reader.JALAttribute;

import java.util.Arrays;
import java.util.Comparator;

public class LineNumberMatcher {
    private final int[] startPcs;
    private final int[] relativeLineNumbers;
    private int currentRelativeLineNumber;

    public LineNumberMatcher(@Nullable JALAttribute.LineNumberTableAttribute lineNumberTable) {
        if (lineNumberTable == null) {
            this.startPcs = new int[0];
            this.relativeLineNumbers = new int[0];
            return;
        }

        JALAttribute.LineNumberTableAttribute.LineNumber[] lines =
                lineNumberTable.lines().clone();

        Arrays.sort(lines, Comparator.comparingInt(
                JALAttribute.LineNumberTableAttribute.LineNumber::startPc
        ));

        this.startPcs = new int[lines.length];
        this.relativeLineNumbers = new int[lines.length];

        int firstLine = lines.length == 0 ? 0 : lines[0].lineNumber();

        for (int i = 0; i < lines.length; i++) {
            this.startPcs[i] = lines[i].startPc();
            this.relativeLineNumbers[i] = lines[i].lineNumber() - firstLine;
        }

        this.currentRelativeLineNumber = 0;
    }

    public int calcBlanksToPlace(int currentPc) {
        if (this.startPcs.length == 0) {
            return 0;
        }

        int relativeLineNumber = -1;

        for (int i = 0; i < this.startPcs.length; i++) {
            if (this.startPcs[i] > currentPc) {
                break;
            }

            relativeLineNumber = this.relativeLineNumbers[i];
        }

        if (relativeLineNumber < 0) {
            return 0;
        }

        int blanks = relativeLineNumber - this.currentRelativeLineNumber;

        if (blanks <= 0) {
            return 0;
        }

        this.currentRelativeLineNumber = relativeLineNumber;
        return blanks;
    }
}
