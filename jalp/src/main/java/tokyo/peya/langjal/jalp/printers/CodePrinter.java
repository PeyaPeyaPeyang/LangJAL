package tokyo.peya.langjal.jalp.printers;

import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.jalp.OutputFormatter;
import tokyo.peya.langjal.jalp.reader.JALConstantPoolEntry;

import java.util.HashMap;
import java.util.Map;

public class CodePrinter {
    private final OutputFormatter out;
    private final JALConstantPoolEntry[] pool;
    private String[] labelsByPc;

    public CodePrinter(OutputFormatter out, JALConstantPoolEntry[] pool) {
        this.out = out;
        this.pool = pool;
    }

    public void printCode(byte[] code, LineNumberMatcher matcher) {
        this.labelsByPc = collectLabels(code);

        for (int i = 0; i < code.length; i++) {
            // 行番号に応じた空行を出力
            this.printBlanks(i, matcher, this.noLabel(i));
            this.printLabel(i);

            int opCode = code[i] & 0xFF;  // 符号なしバイトとして扱うために0xFFでマスク
            int operandBytes = calcOperandBytes(opCode);

            // 動的（オペランドによって変わる）場合は，断片を与えてサイズを計算する
            if (operandBytes == -1) {
                int frags = Math.min(16, code.length - i - 1); // 最大16バイトの断片を取る（十分なはず）
                byte[] operandFrags = new byte[frags];
                System.arraycopy(code, i + 1, operandFrags, 0, frags);
                operandBytes = calcVariableOperandSize(opCode, operandFrags);
            }

            byte[] operand = new byte[operandBytes];
            System.arraycopy(code, i + 1, operand, 0, operandBytes);
            this.printCode(i, opCode, operand);

            i += operandBytes;
        }
    }

    private void printBlanks(int i, LineNumberMatcher matcher, boolean output) {
        int blanksToPlace = matcher.calcBlanksToPlace(i);
        if (!output) {
            return;
        }

        for (int b = 0; b < blanksToPlace; b++) {
            this.out.println("");
        }
    }

    private boolean noLabel(int pc) {
        return this.labelsByPc == null || this.labelsByPc[pc] == null;
    }

    private void printLabel(int pc) {
        if (this.noLabel(pc)) {
            return;
        }

        this.out.noIndentPrintln("%s:".formatted(this.labelsByPc[pc]));
    }

    private static String[] collectLabels(byte[] code) {
        String[] labelCandidatesByPc = new String[code.length];

        for (int i = 0; i < code.length; i++) {
            int opCode = code[i] & 0xFF;
            int operandBytes = calcOperandBytes(opCode);

            if (operandBytes == -1) {
                int frags = Math.min(16, code.length - i - 1);
                byte[] operandFrags = new byte[frags];
                System.arraycopy(code, i + 1, operandFrags, 0, frags);
                operandBytes = calcVariableOperandSize(opCode, operandFrags);
            }

            byte[] operand = new byte[operandBytes];
            System.arraycopy(code, i + 1, operand, 0, operandBytes);
            markBranchTargets(labelCandidatesByPc, i, opCode, operand);

            i += operandBytes;
        }

        String[] labelsByPc = new String[code.length];
        Map<String, Integer> labelCounts = new HashMap<>();

        for (int pc = 0; pc < labelCandidatesByPc.length; pc++) {
            String labelCandidate = labelCandidatesByPc[pc];
            if (labelCandidate != null) {
                labelsByPc[pc] = uniqueLabel(labelCandidate, labelCounts);
            }
        }

        return labelsByPc;
    }

    private static String uniqueLabel(String label, Map<String, Integer> labelCounts) {
        int count = labelCounts.getOrDefault(label, 0);
        labelCounts.put(label, count + 1);

        if (label.equals("Path") || count != 0) {
            return label + count;
        }

        return label;
    }

    private static void markBranchTargets(String[] labelsByPc, int pc, int opCode, byte[] operand) {
        switch (opCode) {
            case EOpcodes.IFEQ, EOpcodes.IFNE, EOpcodes.IFLT, EOpcodes.IFGE, EOpcodes.IFGT, EOpcodes.IFLE,
                 EOpcodes.IF_ICMPEQ, EOpcodes.IF_ICMPNE, EOpcodes.IF_ICMPLT,
                 EOpcodes.IF_ICMPGE, EOpcodes.IF_ICMPGT, EOpcodes.IF_ICMPLE,
                 EOpcodes.IF_ACMPEQ, EOpcodes.IF_ACMPNE,
                 EOpcodes.IFNULL, EOpcodes.IFNONNULL ->
                    markBranchTarget(labelsByPc, pc + getShort(operand, 0), labelNameForBranch(opCode));

            case EOpcodes.GOTO ->
                    markBranchTarget(labelsByPc, pc + getShort(operand, 0), "Path");

            case EOpcodes.JSR ->
                    markBranchTarget(labelsByPc, pc + getShort(operand, 0), "Subroutine");

            case EOpcodes.GOTO_W ->
                    markBranchTarget(labelsByPc, pc + getInt(operand, 0), "Path");

            case EOpcodes.JSR_W ->
                    markBranchTarget(labelsByPc, pc + getInt(operand, 0), "Subroutine");

            case EOpcodes.TABLESWITCH -> {
                int padding = switchPadding(pc);
                markBranchTarget(labelsByPc, pc + getInt(operand, padding), "Default");

                int low = getInt(operand, padding + 4);
                int high = getInt(operand, padding + 8);
                int p = padding + 12;

                for (int key = low; key <= high; key++) {
                    markBranchTarget(labelsByPc, pc + getInt(operand, p), "Case" + key);
                    p += 4;
                }
            }

            case EOpcodes.LOOKUPSWITCH -> {
                int padding = switchPadding(pc);
                markBranchTarget(labelsByPc, pc + getInt(operand, padding), "Default");

                int pairs = getInt(operand, padding + 4);
                int p = padding + 8;

                for (int i = 0; i < pairs; i++) {
                    int match = getInt(operand, p);
                    markBranchTarget(labelsByPc, pc + getInt(operand, p + 4), "Case" + match);
                    p += 8;
                }
            }

            default -> {
            }
        }
    }

    private static void markBranchTarget(String[] labelsByPc, int targetPc, String label) {
        if (targetPc >= 0 && targetPc < labelsByPc.length && shouldReplaceLabel(labelsByPc[targetPc], label)) {
            labelsByPc[targetPc] = label;
        }
    }

    private static boolean shouldReplaceLabel(String currentLabel, String newLabel) {
        return currentLabel == null || currentLabel.equals("Path") && !newLabel.equals("Path");
    }

    private static String labelNameForBranch(int opCode) {
        return switch (opCode) {
            case EOpcodes.IFEQ -> "isZero";
            case EOpcodes.IFNE -> "isNotZero";
            case EOpcodes.IFLT -> "isNegative";
            case EOpcodes.IFGE -> "isNotNegative";
            case EOpcodes.IFGT -> "isPositive";
            case EOpcodes.IFLE -> "isNotPositive";
            case EOpcodes.IF_ICMPEQ, EOpcodes.IF_ACMPEQ -> "isEqual";
            case EOpcodes.IF_ICMPNE, EOpcodes.IF_ACMPNE -> "isNotEqual";
            case EOpcodes.IF_ICMPLT -> "isLess";
            case EOpcodes.IF_ICMPGE -> "isGreaterOrEqual";
            case EOpcodes.IF_ICMPGT -> "isGreater";
            case EOpcodes.IF_ICMPLE -> "isLessOrEqual";
            case EOpcodes.IFNULL -> "isNull";
            case EOpcodes.IFNONNULL -> "isNotNull";
            default -> "Path";
        };
    }

    private void printCode(int pc, int opCode, byte[] operand) {
        String mnemonic = EOpcodes.getName(opCode);

        switch (opCode) {
            // no operand
            case EOpcodes.NOP, EOpcodes.ACONST_NULL,
                 EOpcodes.ICONST_M1, EOpcodes.ICONST_0, EOpcodes.ICONST_1,
                 EOpcodes.ICONST_2, EOpcodes.ICONST_3, EOpcodes.ICONST_4, EOpcodes.ICONST_5,
                 EOpcodes.LCONST_0, EOpcodes.LCONST_1,
                 EOpcodes.FCONST_0, EOpcodes.FCONST_1, EOpcodes.FCONST_2,
                 EOpcodes.DCONST_0, EOpcodes.DCONST_1,
                 EOpcodes.ALOAD_0, EOpcodes.ALOAD_1, EOpcodes.ALOAD_2, EOpcodes.ALOAD_3,
                 EOpcodes.ILOAD_0, EOpcodes.ILOAD_1, EOpcodes.ILOAD_2, EOpcodes.ILOAD_3,
                 EOpcodes.LLOAD_0, EOpcodes.LLOAD_1, EOpcodes.LLOAD_2, EOpcodes.LLOAD_3,
                 EOpcodes.FLOAD_0, EOpcodes.FLOAD_1, EOpcodes.FLOAD_2, EOpcodes.FLOAD_3,
                 EOpcodes.DLOAD_0, EOpcodes.DLOAD_1, EOpcodes.DLOAD_2, EOpcodes.DLOAD_3,
                 EOpcodes.ASTORE_0, EOpcodes.ASTORE_1, EOpcodes.ASTORE_2, EOpcodes.ASTORE_3,
                 EOpcodes.ISTORE_0, EOpcodes.ISTORE_1, EOpcodes.ISTORE_2, EOpcodes.ISTORE_3,
                 EOpcodes.LSTORE_0, EOpcodes.LSTORE_1, EOpcodes.LSTORE_2, EOpcodes.LSTORE_3,
                 EOpcodes.FSTORE_0, EOpcodes.FSTORE_1, EOpcodes.FSTORE_2, EOpcodes.FSTORE_3,
                 EOpcodes.DSTORE_0, EOpcodes.DSTORE_1, EOpcodes.DSTORE_2, EOpcodes.DSTORE_3,
                 EOpcodes.IADD, EOpcodes.LADD, EOpcodes.FADD, EOpcodes.DADD,
                 EOpcodes.ISUB, EOpcodes.LSUB, EOpcodes.FSUB, EOpcodes.DSUB,
                 EOpcodes.IMUL, EOpcodes.LMUL, EOpcodes.FMUL, EOpcodes.DMUL,
                 EOpcodes.IDIV, EOpcodes.LDIV, EOpcodes.FDIV, EOpcodes.DDIV,
                 EOpcodes.IREM, EOpcodes.LREM, EOpcodes.FREM, EOpcodes.DREM,
                 EOpcodes.INEG, EOpcodes.LNEG, EOpcodes.FNEG, EOpcodes.DNEG,
                 EOpcodes.IRETURN, EOpcodes.LRETURN, EOpcodes.FRETURN,
                 EOpcodes.DRETURN, EOpcodes.ARETURN, EOpcodes.RETURN,
                 EOpcodes.POP, EOpcodes.POP2, EOpcodes.DUP, EOpcodes.DUP_X1,
                 EOpcodes.DUP_X2, EOpcodes.DUP2, EOpcodes.DUP2_X1, EOpcodes.DUP2_X2,
                 EOpcodes.SWAP,
                 EOpcodes.IALOAD, EOpcodes.LALOAD, EOpcodes.FALOAD, EOpcodes.DALOAD,
                 EOpcodes.AALOAD, EOpcodes.BALOAD, EOpcodes.CALOAD, EOpcodes.SALOAD,
                 EOpcodes.IASTORE, EOpcodes.LASTORE, EOpcodes.FASTORE, EOpcodes.DASTORE,
                 EOpcodes.AASTORE, EOpcodes.BASTORE, EOpcodes.CASTORE, EOpcodes.SASTORE,
                 EOpcodes.ARRAYLENGTH, EOpcodes.ATHROW,
                 EOpcodes.MONITORENTER, EOpcodes.MONITOREXIT,
                 EOpcodes.I2L, EOpcodes.I2F, EOpcodes.I2D,
                 EOpcodes.L2I, EOpcodes.L2F, EOpcodes.L2D,
                 EOpcodes.F2I, EOpcodes.F2L, EOpcodes.F2D,
                 EOpcodes.D2I, EOpcodes.D2L, EOpcodes.D2F,
                 EOpcodes.I2B, EOpcodes.I2C, EOpcodes.I2S,
                 EOpcodes.LCMP, EOpcodes.FCMPL, EOpcodes.FCMPG,
                 EOpcodes.DCMPL, EOpcodes.DCMPG,
                 EOpcodes.ISHL, EOpcodes.LSHL, EOpcodes.ISHR, EOpcodes.LSHR,
                 EOpcodes.IUSHR, EOpcodes.LUSHR,
                 EOpcodes.IAND, EOpcodes.LAND, EOpcodes.IOR, EOpcodes.LOR,
                 EOpcodes.IXOR, EOpcodes.LXOR ->
                    this.out.println(mnemonic);

            // local variable
            case EOpcodes.ILOAD, EOpcodes.LLOAD, EOpcodes.FLOAD, EOpcodes.DLOAD, EOpcodes.ALOAD,
                 EOpcodes.ISTORE, EOpcodes.LSTORE, EOpcodes.FSTORE, EOpcodes.DSTORE, EOpcodes.ASTORE,
                 EOpcodes.RET ->
                    this.out.println("%s %d".formatted(mnemonic, u1(operand, 0)));

            case EOpcodes.IINC ->
                    this.out.println("iinc %d %d".formatted(u1(operand, 0), operand[1]));

            // constants
            case EOpcodes.BIPUSH ->
                    this.out.println("bipush %d".formatted(operand[0]));

            case EOpcodes.SIPUSH ->
                    this.out.println("sipush %d".formatted(getShort(operand, 0)));

            case EOpcodes.LDC, EOpcodes.LDC_W, EOpcodes.LDC2_W ->
                    this.out.println("%s %s".formatted(mnemonic, formatLdcConstant(cpIndex(opCode, operand))));

            // class/type
            case EOpcodes.NEW ->
                    this.out.println("new %s".formatted(formatClassName(u2(operand, 0))));

            case EOpcodes.ANEWARRAY ->
                    this.out.println("anewarray %s".formatted(formatObjectTypeDescriptor(u2(operand, 0))));

            case EOpcodes.CHECKCAST ->
                    this.out.println("checkcast %s".formatted(formatTypeDescriptor(u2(operand, 0))));

            case EOpcodes.INSTANCEOF ->
                    this.out.println("instanceof %s".formatted(formatTypeDescriptor(u2(operand, 0))));

            case EOpcodes.NEWARRAY ->
                    this.out.println("newarray %s".formatted(formatPrimitiveDescriptor(u1(operand, 0))));

            case EOpcodes.MULTIANEWARRAY ->
                    this.out.println("multianewarray %s %d".formatted(
                            formatTypeDescriptor(u2(operand, 0)),
                            u1(operand, 2)
                    ));

            // field refs
            case EOpcodes.GETSTATIC, EOpcodes.PUTSTATIC,
                 EOpcodes.GETFIELD, EOpcodes.PUTFIELD ->
                    this.out.println("%s %s".formatted(
                            mnemonic,
                            formatFieldRef(u2(operand, 0))
                    ));

            // method refs
            case EOpcodes.INVOKEVIRTUAL, EOpcodes.INVOKESPECIAL,
                 EOpcodes.INVOKESTATIC ->
                    this.out.println("%s %s".formatted(
                            mnemonic,
                            formatMethodRef(u2(operand, 0))
                    ));

            case EOpcodes.INVOKEINTERFACE ->
                    this.out.println("invokeinterface %s".formatted(formatMethodRef(u2(operand, 0))));

            // branch
            case EOpcodes.IFEQ, EOpcodes.IFNE, EOpcodes.IFLT, EOpcodes.IFGE, EOpcodes.IFGT, EOpcodes.IFLE,
                 EOpcodes.IF_ICMPEQ, EOpcodes.IF_ICMPNE, EOpcodes.IF_ICMPLT,
                 EOpcodes.IF_ICMPGE, EOpcodes.IF_ICMPGT, EOpcodes.IF_ICMPLE,
                 EOpcodes.IF_ACMPEQ, EOpcodes.IF_ACMPNE,
                 EOpcodes.GOTO, EOpcodes.JSR,
                 EOpcodes.IFNULL, EOpcodes.IFNONNULL -> {
                int targetPc = pc + getShort(operand, 0);
                this.out.println("%s %s".formatted(mnemonic, labelOf(targetPc)));
            }

            case EOpcodes.GOTO_W, EOpcodes.JSR_W -> {
                int targetPc = pc + getInt(operand, 0);
                this.out.println("%s %s".formatted(mnemonic, labelOf(targetPc)));
            }

            case EOpcodes.TABLESWITCH ->
                    this.printTableSwitch(pc, operand);

            case EOpcodes.LOOKUPSWITCH ->
                    this.printLookupSwitch(pc, operand);

            case EOpcodes.WIDE ->
                    this.printWide(operand);

            default ->
                    throw new IllegalArgumentException("Unsupported opcode: 0x%02x".formatted(opCode));
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T getConstantPoolEntry(JALConstantPoolEntry[] pool, int index, Class<T> expectedClass) {
        if (index <= 0 || index >= pool.length) {
            throw new IllegalArgumentException("Constant pool index out of bounds: " + index);
        }

        JALConstantPoolEntry entry = pool[index];
        if (!expectedClass.isInstance(entry)) {
            throw new IllegalArgumentException("Expected constant pool entry of type %s at index %d, but got %s".formatted(
                    expectedClass.getSimpleName(),
                    index,
                    entry.getClass().getSimpleName()
            ));
        }
        return (T) entry;
    }

    private String formatFieldRef(int cpIndex) {
        // owner/name:descriptor
        // e.g. java/lang/System->out:Ljava/io/PrintStream;
        JALConstantPoolEntry.FieldEntry ref = getConstantPoolEntry(this.pool, cpIndex, JALConstantPoolEntry.FieldEntry.class);
        JALConstantPoolEntry.NameAndTypeEntry nameAndType = ref.nameAndType();

        return "%s->%s:%s".formatted(ref.owner().name().getInternalName(), nameAndType.name(), nameAndType.descriptor());
    }

    private String formatMethodRef(int cpIndex) {
        // owner->name(descriptor)
        // e.g. java/io/PrintStream->println(Ljava/lang/String;)V
        JALConstantPoolEntry.MethodEntry ref = getConstantPoolEntry(this.pool, cpIndex, JALConstantPoolEntry.MethodEntry.class);
        JALConstantPoolEntry.NameAndTypeEntry nameAndType = ref.nameAndType();

        return "%s->%s%s" .formatted(ref.owner().name().getInternalName(), nameAndType.name(), nameAndType.descriptor());
    }

    private String formatClassName(int cpIndex) {
        // e.g. java/lang/StringBuilder
        JALConstantPoolEntry.ClassEntry classEntry = getConstantPoolEntry(this.pool, cpIndex, JALConstantPoolEntry.ClassEntry.class);
        return classEntry.name().getInternalName();
    }

    private String formatTypeDescriptor(int cpIndex) {
        String className = formatClassName(cpIndex);

        if (className.startsWith("[")) {
            return className;
        }

        return "L%s;".formatted(className);
    }

    private String formatObjectTypeDescriptor(int cpIndex) {
        String className = formatClassName(cpIndex);

        if (className.startsWith("[")) {
            throw new IllegalArgumentException("Expected object type descriptor, but got array type: " + className);
        }

        return "L%s;".formatted(className);
    }

    private String formatLdcConstant(int cpIndex) {
        JALConstantPoolEntry entry = this.pool[cpIndex];

        return switch (entry) {
            case JALConstantPoolEntry.IntegerEntry intEntry -> Integer.toString(intEntry.value());
            case JALConstantPoolEntry.FloatEntry floatEntry -> floatEntry.value() + "f";
            case JALConstantPoolEntry.LongEntry longEntry -> longEntry.value() + "L";
            case JALConstantPoolEntry.DoubleEntry doubleEntry -> doubleEntry.value() + "d";
            case JALConstantPoolEntry.StringEntry stringEntry -> quote(stringEntry.value());
            case JALConstantPoolEntry.ClassEntry classEntry -> formatClassName(cpIndex);
            default -> throw new IllegalArgumentException("Unsupported constant pool entry type for ldc: " + entry.getClass().getSimpleName());
        };
    }

    private void printXStore(int opCode, byte[] operand) {
        String mnemonic = EOpcodes.getName(opCode);
        int varIndex = operand[0] & 0xFF; // 符号なしバイトとして扱う
        this.out.println("%s %d".formatted(mnemonic, varIndex));
    }

    private void printNoOperandCode(int opCode) {
        String mnemonic = EOpcodes.getName(opCode);
        this.out.println(mnemonic);
    }

    private static int calcVariableOperandSize(int operand, byte[] operandFrags) {
        return switch (operand) {
            case 0xaa -> { // tableswitch
                int padding = (4 - (operandFrags.length % 4)) % 4;
                int baseSize = 12; // default + low + high
                yield baseSize + padding + 4 * (getInt(operandFrags, padding + 8) - getInt(operandFrags, padding + 4) + 1);
            }
            case 0xab -> { // lookupswitch
                int padding = (4 - (operandFrags.length % 4)) % 4;
                int baseSize = 8; // default + npairs
                yield baseSize + padding + 8 * getInt(operandFrags, padding + 4);
            }
            case 0xc4 -> { // wide
                int modifiedOpcode = operandFrags[0] & 0xFF;
                if (modifiedOpcode == 0x84) { // iinc
                    yield 5; // opcode + indexbyte1 + indexbyte2 + constbyte1 + constbyte2
                } else {
                    yield 3; // opcode + indexbyte1 + indexbyte2
                }
            }
            default -> throw new IllegalArgumentException(
                    "Unsupported variable-length JVM opcode: 0x%02x".formatted(operand & 0xFF)
            );
        };
    }
    private void printTableSwitch(int pc, byte[] operand) {
        int padding = switchPadding(pc);

        int defaultOffset = getInt(operand, padding);
        int low = getInt(operand, padding + 4);
        int high = getInt(operand, padding + 8);

        StringBuilder builder = new StringBuilder();
        builder.append("tableswitch ");
        builder.append(low);
        builder.append(" { ");

        int p = padding + 12;
        for (int key = low; key <= high; key++) {
            if (key != low) {
                builder.append(", ");
            }

            int offset = getInt(operand, p);
            builder.append(labelOf(pc + offset));
            p += 4;
        }

        builder.append(" } default ");
        builder.append(labelOf(pc + defaultOffset));

        this.out.println(builder.toString());
    }

    private void printLookupSwitch(int pc, byte[] operand) {
        int padding = switchPadding(pc);

        int defaultOffset = getInt(operand, padding);
        int pairs = getInt(operand, padding + 4);

        StringBuilder builder = new StringBuilder();
        builder.append("lookupswitch { ");

        int p = padding + 8;
        for (int i = 0; i < pairs; i++) {
            if (i != 0) {
                builder.append(", ");
            }

            int match = getInt(operand, p);
            int offset = getInt(operand, p + 4);

            builder.append(match);
            builder.append(": ");
            builder.append(labelOf(pc + offset));

            p += 8;
        }

        if (pairs != 0) {
            builder.append(", ");
        }

        builder.append("default: ");
        builder.append(labelOf(pc + defaultOffset));
        builder.append(" }");

        this.out.println(builder.toString());
    }

    private void printWide(byte[] operand) {
        int modifiedOpcode = u1(operand, 0);
        String mnemonic = EOpcodes.getName(modifiedOpcode);

        if (modifiedOpcode == EOpcodes.IINC) {
            this.out.println("wide iinc %d %d".formatted(
                    u2(operand, 1),
                    getShort(operand, 3)
            ));
            return;
        }

        this.out.println("wide %s %d".formatted(
                mnemonic,
                u2(operand, 1)
        ));
    }private static int cpIndex(int opCode, byte[] operand) {
        return opCode == EOpcodes.LDC ? u1(operand, 0) : u2(operand, 0);
    }

    private String labelOf(int pc) {
        if (this.labelsByPc != null && pc >= 0 && pc < this.labelsByPc.length && this.labelsByPc[pc] != null) {
            return this.labelsByPc[pc];
        }

        return "L" + pc;
    }

    private static String quote(String value) {
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                + "\"";
    }

    private static String formatPrimitiveDescriptor(int atype) {
        return switch (atype) {
            case 4 -> "Z";
            case 5 -> "C";
            case 6 -> "F";
            case 7 -> "D";
            case 8 -> "B";
            case 9 -> "S";
            case 10 -> "I";
            case 11 -> "J";
            default -> throw new IllegalArgumentException("Invalid newarray atype: " + atype);
        };
    }

    private static int u1(byte[] operand, int i) {
        return operand[i] & 0xFF;
    }

    private static int u2(byte[] operand, int i) {
        return ((operand[i] & 0xFF) << 8) | (operand[i + 1] & 0xFF);
    }

    private static int getShort(byte[] operand, int i) {
        return (short) (((operand[i] & 0xFF) << 8) | (operand[i + 1] & 0xFF));
    }

    private static int switchPadding(int pc) {
        return (4 - (pc % 4)) % 4;
    }

    private static int getInt(byte[] operandFrags, int i) {
        return ((operandFrags[i] & 0xFF) << 24) |
               ((operandFrags[i + 1] & 0xFF) << 16) |
               ((operandFrags[i + 2] & 0xFF) << 8) |
               (operandFrags[i + 3] & 0xFF);
    }

    private static int calcOperandBytes(int opcode) {
        return switch (opcode) {
            // variable length
            case 0xaa, // tableswitch
                 0xab, // lookupswitch
                 0xc4  // wide
                    -> -1;

            // 4 bytes
            case 0xb9, // invokeinterface
                 0xba, // invokedynamic
                 0xc8, // goto_w
                 0xc9  // jsr_w
                    -> 4;

            // 3 bytes
            case 0xc5  // multianewarray
                    -> 3;

            // 2 bytes
            case 0x11, // sipush
                 0x13, // ldc_w
                 0x14, // ldc2_w
                 0x84, // iinc
                 0x99, 0x9a, 0x9b, 0x9c, 0x9d, 0x9e, // if<cond>
                 0x9f, 0xa0, 0xa1, 0xa2, 0xa3, 0xa4, // if_icmp<cond>
                 0xa5, 0xa6, // if_acmp<cond>
                 0xa7, // goto
                 0xa8, // jsr
                 0xb2, // getstatic
                 0xb3, // putstatic
                 0xb4, // getfield
                 0xb5, // putfield
                 0xb6, // invokevirtual
                 0xb7, // invokespecial
                 0xb8, // invokestatic
                 0xbb, // new
                 0xbd, // anewarray
                 0xc0, // checkcast
                 0xc1, // instanceof
                 0xc6, // ifnull
                 0xc7  // ifnonnull
                    -> 2;

            // 1 byte
            case 0x10, // bipush
                 0x12, // ldc
                 0x15, // iload
                 0x16, // lload
                 0x17, // fload
                 0x18, // dload
                 0x19, // aload
                 0x36, // istore
                 0x37, // lstore
                 0x38, // fstore
                 0x39, // dstore
                 0x3a, // astore
                 0xa9, // ret
                 0xbc  // newarray
                    -> 1;

            // 0 bytes
            case 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
                 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f,
                 0x1a, 0x1b, 0x1c, 0x1d, 0x1e, 0x1f, 0x20, 0x21,
                 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29,
                 0x2a, 0x2b, 0x2c, 0x2d, 0x2e, 0x2f, 0x30, 0x31,
                 0x32, 0x33, 0x34, 0x35, 0x3b, 0x3c, 0x3d, 0x3e,
                 0x3f, 0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46,
                 0x47, 0x48, 0x49, 0x4a, 0x4b, 0x4c, 0x4d, 0x4e,
                 0x4f, 0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56,
                 0x57, 0x58, 0x59, 0x5a, 0x5b, 0x5c, 0x5d, 0x5e,
                 0x5f, 0x60, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66,
                 0x67, 0x68, 0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e,
                 0x6f, 0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76,
                 0x77, 0x78, 0x79, 0x7a, 0x7b, 0x7c, 0x7d, 0x7e,
                 0x7f, 0x80, 0x81, 0x82, 0x83, 0x85, 0x86, 0x87,
                 0x88, 0x89, 0x8a, 0x8b, 0x8c, 0x8d, 0x8e, 0x8f,
                 0x90, 0x91, 0x92, 0x93, 0x94, 0x95, 0x96, 0x97,
                 0x98, 0xac, 0xad, 0xae, 0xaf, 0xb0, 0xb1, 0xbe,
                 0xbf, 0xc2, 0xc3, 0xca, 0xcb, 0xcc
                    -> 0;

            default -> throw new IllegalArgumentException(
                    "Unsupported JVM opcode: 0x%02x".formatted(opcode & 0xFF)
            );
        };
    }
}
