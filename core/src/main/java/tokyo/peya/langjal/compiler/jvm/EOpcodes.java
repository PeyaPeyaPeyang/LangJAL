package tokyo.peya.langjal.compiler.jvm;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.Printer;

import java.util.Locale;

/**
 * An extended version of {@link Opcodes} that includes additional
 * opcodes and utility methods for working with JVM opcodes.
 * <p>
 * This interface provides constants for various opcodes, including
 * those for loading and storing local variables, as well as utility
 * methods to find an opcode by its name and to get the size of an opcode.
 * </p>
 *
 * @see Opcodes
 * @see Printer
 */
public interface EOpcodes extends Opcodes
{
    int ILOAD_0 = 26;
    int ILOAD_1 = 27;
    int ILOAD_2 = 28;
    int ILOAD_3 = 29;

    int LLOAD_0 = 30;
    int LLOAD_1 = 31;
    int LLOAD_2 = 32;
    int LLOAD_3 = 33;

    int FLOAD_0 = 34;
    int FLOAD_1 = 35;
    int FLOAD_2 = 36;
    int FLOAD_3 = 37;

    int DLOAD_0 = 38;
    int DLOAD_1 = 39;
    int DLOAD_2 = 40;
    int DLOAD_3 = 41;

    int ALOAD_0 = 42;
    int ALOAD_1 = 43;
    int ALOAD_2 = 44;
    int ALOAD_3 = 45;

    int ISTORE_0 = 59;
    int ISTORE_1 = 60;
    int ISTORE_2 = 61;
    int ISTORE_3 = 62;

    int LSTORE_0 = 63;
    int LSTORE_1 = 64;
    int LSTORE_2 = 65;
    int LSTORE_3 = 66;

    int FSTORE_0 = 67;
    int FSTORE_1 = 68;
    int FSTORE_2 = 69;
    int FSTORE_3 = 70;

    int DSTORE_0 = 71;
    int DSTORE_1 = 72;
    int DSTORE_2 = 73;
    int DSTORE_3 = 74;

    int ASTORE_0 = 75;
    int ASTORE_1 = 76;
    int ASTORE_2 = 77;
    int ASTORE_3 = 78;

    int LDC_W = 19;
    int LDC2_W = 20;

    int WIDE = 196;

    int GOTO_W = 200;
    int JSR_W = 201;

    /**
     * Finds the opcode value by its name.
     *
     * @param opcodeName The name of the opcode.
     * @return The opcode value, or -1 if not found.
     */
    static int findOpcode(@NotNull String opcodeName)
    {
        opcodeName = opcodeName.toUpperCase(Locale.ENGLISH);
        for (int i = 0; i < Printer.OPCODES.length; i++)
            if (Printer.OPCODES[i].equals(opcodeName))
                return i;

        return -1;
    }

    /**
     * Gets the name of the opcode for the given value.
     *
     * @param opcode The opcode value.
     * @return The opcode name in lower case.
     * @throws IllegalArgumentException if the opcode is unknown.
     */
    static String getName(int opcode)
    {
        String name = Printer.OPCODES[opcode];
        if (name == null)
            throw new IllegalArgumentException("Unknown opcode: " + opcode);
        return name.toLowerCase(Locale.ENGLISH);
    }

    /**
     * Gets the size in bytes of the instruction for the given opcode.
     *
     * @param opcode The opcode value.
     * @return The size of the instruction in bytes.
     * @throws IllegalArgumentException if the instruction size cannot be determined.
     */
    static byte getOpcodeSize(int opcode)
    {
        return switch (opcode)
        {
            case NOP, ACONST_NULL, ICONST_M1, ICONST_0,
                 ICONST_1, ICONST_2, ICONST_3, ICONST_4,
                 ICONST_5, LCONST_0, LCONST_1, FCONST_0,
                 FCONST_1, FCONST_2, DCONST_0, DCONST_1,
                 ILOAD_0, ILOAD_1, ILOAD_2, ILOAD_3,
                 LLOAD_0, LLOAD_1, LLOAD_2, LLOAD_3,
                 FLOAD_0, FLOAD_1, FLOAD_2, FLOAD_3,
                 DLOAD_0, DLOAD_1, DLOAD_2, DLOAD_3,
                 ALOAD_0, ALOAD_1, ALOAD_2, ALOAD_3,
                 IALOAD, LALOAD, FALOAD, DALOAD, AALOAD,
                 BALOAD, CALOAD, SALOAD,
                 ISTORE_0, ISTORE_1, ISTORE_2, ISTORE_3,
                 LSTORE_0, LSTORE_1, LSTORE_2, LSTORE_3,
                 FSTORE_0, FSTORE_1, FSTORE_2, FSTORE_3,
                 DSTORE_0, DSTORE_1, DSTORE_2, DSTORE_3,
                 ASTORE_0, ASTORE_1, ASTORE_2, ASTORE_3,
                 IASTORE, LASTORE,
                 FASTORE, DASTORE, AASTORE, BASTORE, CASTORE,
                 SASTORE, POP, POP2, DUP, DUP_X1, DUP_X2,
                 DUP2, DUP2_X1, DUP2_X2, SWAP, IADD,
                 LADD, FADD, DADD, ISUB, LSUB,
                 FSUB, DSUB, IMUL, LMUL, FMUL,
                 DMUL, IDIV, LDIV, FDIV, DDIV,
                 IREM, LREM, FREM, DREM, INEG,
                 LNEG, FNEG, DNEG, ISHL, LSHL,
                 ISHR, LSHR, IUSHR, LUSHR, IAND,
                 LAND, IOR, LOR, IXOR, LXOR,
                 I2L, I2F, I2D, L2I,
                 L2F, L2D, F2I, F2L, F2D,
                 D2I, D2L, D2F, I2B, I2C,
                 I2S, LCMP, FCMPL, FCMPG, DCMPL,
                 DCMPG, IRETURN, LRETURN, FRETURN,
                 DRETURN, ARETURN, RETURN, ARRAYLENGTH,
                 ATHROW, MONITORENTER, MONITOREXIT -> 1;
            case BIPUSH, LDC, ILOAD, LLOAD, FLOAD,
                 DLOAD, ALOAD, ISTORE, LSTORE,
                 FSTORE, DSTORE, ASTORE, RET, NEWARRAY -> 2;
            case SIPUSH, LDC_W, LDC2_W, GETSTATIC,
                 PUTSTATIC, GETFIELD, PUTFIELD, INVOKEVIRTUAL,
                 INVOKESPECIAL, INVOKESTATIC, NEW, ANEWARRAY,
                 CHECKCAST, INSTANCEOF, IFEQ, IFNE,
                 IFLT, IFGE, IFGT, IFLE,
                 IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE,
                 IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE,
                 IFNULL, IFNONNULL, IINC, GOTO, JSR -> 3;
            case MULTIANEWARRAY -> 4;
            case INVOKEINTERFACE, INVOKEDYNAMIC, GOTO_W, JSR_W -> 5;
            /* case TABLESWITCH, LOOKUPSWITCH, WIDE: 196 ->*/
            default -> throw new IllegalArgumentException(
                    "Unable to determine instruction size for opcode(" + opcode + "). " +
                            "This is variable-sized or not supported yet.");
        };
    }
}
