package tokyo.peya.langjal.analyser.stack;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StackElementType
{
    NOP("NOP", -2),
    RETURN_ADDRESS("RETURN_ADDRESS", -1),
    TOP("TOP", 0),
    INTEGER("INTEGER", 1),
    FLOAT("FLOAT", 2),
    LONG("LONG", 3),
    DOUBLE("DOUBLE", 4),
    NULL("NULL", 5),
    UNINITIALIZED_THIS("UNINITIALIZED_THIS", 6),
    OBJECT("OBJECT", 7),
    UNINITIALIZED("UNINITIALIZED", 8);
    private final String name;
    private final int opcode;
}
