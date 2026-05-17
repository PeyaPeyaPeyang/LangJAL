package tokyo.peya.langjal.jalp.printers;

import org.junit.jupiter.api.Test;
import tokyo.peya.langjal.compiler.jvm.ClassReferenceType;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.MethodDescriptor;
import tokyo.peya.langjal.jalp.OutputFormatter;
import tokyo.peya.langjal.jalp.reader.JALAttribute;
import tokyo.peya.langjal.jalp.reader.JALConstantPoolEntry;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CodePrinterTest {
    @Test
    void printCodePrintsNoOperandInstructionsInOrder() {
        String output = print(new byte[]{
                (byte) EOpcodes.ICONST_1,
                (byte) EOpcodes.IRETURN
        });

        assertEquals(lines("iconst_1", "ireturn"), output);
    }

    @Test
    void printCodePrintsLocalVariableAndSignedImmediateOperands() {
        String output = print(new byte[]{
                (byte) EOpcodes.ILOAD, 7,
                (byte) EOpcodes.IINC, 7, -2,
                (byte) EOpcodes.BIPUSH, -1,
                (byte) EOpcodes.SIPUSH, 1, 44,
                (byte) EOpcodes.RET, 7
        });

        assertEquals(lines(
                "iload 7",
                "iinc 7 -2",
                "bipush -1",
                "sipush 300",
                "ret 7"
        ), output);
    }

    @Test
    void printCodeFormatsLdcConstants() {
        JALConstantPoolEntry[] pool = pool(
                new JALConstantPoolEntry.IntegerEntry(123),
                new JALConstantPoolEntry.FloatEntry(1.5f),
                new JALConstantPoolEntry.LongEntry(999L),
                new JALConstantPoolEntry.DoubleEntry(2.25d),
                new JALConstantPoolEntry.StringEntry("a\n\"b"),
                classEntry("java/lang/String")
        );

        String output = print(pool, null, new byte[]{
                (byte) EOpcodes.LDC, 1,
                (byte) EOpcodes.LDC, 2,
                (byte) EOpcodes.LDC2_W, 0, 3,
                (byte) EOpcodes.LDC2_W, 0, 4,
                (byte) EOpcodes.LDC, 5,
                (byte) EOpcodes.LDC_W, 0, 6
        });

        assertEquals(lines(
                "ldc 123",
                "ldc 1.5f",
                "ldc2_w 999L",
                "ldc2_w 2.25d",
                "ldc \"a\\n\\\"b\"",
                "ldc_w java/lang/String"
        ), output);
    }

    @Test
    void printCodeFormatsClassAndArrayTypeInstructions() {
        JALConstantPoolEntry[] pool = pool(
                classEntry("java/lang/String"),
                classEntry("[Ljava/lang/String;"),
                classEntry("[[I")
        );

        String output = print(pool, null, new byte[]{
                (byte) EOpcodes.NEW, 0, 1,
                (byte) EOpcodes.ANEWARRAY, 0, 1,
                (byte) EOpcodes.CHECKCAST, 0, 1,
                (byte) EOpcodes.INSTANCEOF, 0, 2,
                (byte) EOpcodes.NEWARRAY, 10,
                (byte) EOpcodes.MULTIANEWARRAY, 0, 3, 2
        });

        assertEquals(lines(
                "new java/lang/String",
                "anewarray Ljava/lang/String;",
                "checkcast Ljava/lang/String;",
                "instanceof [java/lang/String",
                "newarray I",
                "multianewarray [[I 2"
        ), output);
    }

    @Test
    void printCodeFormatsFieldAndMethodReferences() {
        JALConstantPoolEntry.ClassEntry system = classEntry("java/lang/System");
        JALConstantPoolEntry.ClassEntry printStream = classEntry("java/io/PrintStream");
        JALConstantPoolEntry[] pool = pool(
                new JALConstantPoolEntry.FieldEntry(system, new JALConstantPoolEntry.NameAndTypeEntry("out", "Ljava/io/PrintStream;")),
                new JALConstantPoolEntry.MethodEntry(printStream, new JALConstantPoolEntry.NameAndTypeEntry("println", "(Ljava/lang/String;)V")),
                new JALConstantPoolEntry.InterfaceMethodEntry(classEntry("java/util/List"), new JALConstantPoolEntry.NameAndTypeEntry("size", "()I"))
        );

        String output = print(pool, null, new byte[]{
                (byte) EOpcodes.GETSTATIC, 0, 1,
                (byte) EOpcodes.INVOKEVIRTUAL, 0, 2,
                (byte) EOpcodes.INVOKEINTERFACE, 0, 3, 1, 0
        });

        assertEquals(lines(
                "getstatic java/lang/System->out:Ljava/io/PrintStream;",
                "invokevirtual java/io/PrintStream->println(Ljava/lang/String;)V",
                "invokeinterface java/util/List->size()I"
        ), output);
    }

    @Test
    void printCodeFormatsInvokeDynamicWithBootstrapArguments() {
        JALConstantPoolEntry.MethodEntry bootstrapTarget = new JALConstantPoolEntry.MethodEntry(
                classEntry("java/lang/invoke/StringConcatFactory"),
                new JALConstantPoolEntry.NameAndTypeEntry("makeConcatWithConstants", "()V")
        );
        JALAttribute.BootstrapMethodsAttribute bootstrapMethods = new JALAttribute.BootstrapMethodsAttribute(
                "BootstrapMethods",
                new JALAttribute.BootstrapMethodsAttribute.BootstrapMethod[]{
                        new JALAttribute.BootstrapMethodsAttribute.BootstrapMethod(
                                new JALConstantPoolEntry.MethodHandleEntry(6, bootstrapTarget),
                                new JALConstantPoolEntry[]{
                                        new JALConstantPoolEntry.StringEntry("value=\\u0001"),
                                        new JALConstantPoolEntry.IntegerEntry(10),
                                        new JALConstantPoolEntry.MethodTypeEntry(MethodDescriptor.parse("(I)V"))
                                }
                        )
                }
        );
        JALConstantPoolEntry[] pool = pool(
                new JALConstantPoolEntry.InvokeDynamicEntry(
                        0,
                        new JALConstantPoolEntry.NameAndTypeEntry("makeConcatWithConstants", "(I)Ljava/lang/String;")
                )
        );

        String output = print(pool, bootstrapMethods, new byte[]{
                (byte) EOpcodes.INVOKEDYNAMIC, 0, 1, 0, 0
        });

        assertEquals(lines(
                "invokedynamic makeConcatWithConstants (I)Ljava/lang/String; "
                + "MethodHandle|invokestatic|java/lang/invoke/StringConcatFactory->makeConcatWithConstants()V "
                + "\"value=\\\\u0001\" 10 MethodHandle|(I)V"
        ), output);
    }

    @Test
    void printCodeCreatesLabelsForBranchesAndPrintsLabelsBeforeTargets() {
        String output = print(new byte[]{
                (byte) EOpcodes.IFEQ, 0, 4,
                (byte) EOpcodes.NOP,
                (byte) EOpcodes.RETURN
        });

        assertEquals(lines(
                "ifeq isZero",
                "nop",
                "isZero:",
                "return"
        ), output);
    }

    @Test
    void printCodeUsesPathSuffixForGotoLabels() {
        String output = print(new byte[]{
                (byte) EOpcodes.GOTO, 0, 4,
                (byte) EOpcodes.NOP,
                (byte) EOpcodes.RETURN
        });

        assertEquals(lines(
                "goto Path0",
                "nop",
                "Path0:",
                "return"
        ), output);
    }

    @Test
    void printCodePrefersSpecificBranchLabelOverPathLabelForSameTarget() {
        String output = print(new byte[]{
                (byte) EOpcodes.GOTO, 0, 7,
                (byte) EOpcodes.NOP,
                (byte) EOpcodes.IFEQ, 0, 3,
                (byte) EOpcodes.RETURN
        });

        assertEquals(lines(
                "goto isZero",
                "nop",
                "ifeq isZero",
                "isZero:",
                "return"
        ), output);
    }

    @Test
    void printCodePrintsWideInstructions() {
        String output = print(new byte[]{
                (byte) EOpcodes.WIDE, (byte) EOpcodes.ILOAD, 1, 44,
                (byte) EOpcodes.WIDE, (byte) EOpcodes.IINC, 1, 44, -1, -2
        });

        assertEquals(lines(
                "wide iload 300",
                "wide iinc 300 -2"
        ), output);
    }

    @Test
    void printCodePrintsLookupSwitch() {
        String output = print(new byte[]{
                (byte) EOpcodes.LOOKUPSWITCH,
                0, 0, 0,
                0, 0, 0, 21,
                0, 0, 0, 1,
                0, 0, 0, 10,
                0, 0, 0, 20,
                (byte) EOpcodes.RETURN,
                (byte) EOpcodes.RETURN
        });

        assertEquals(lines(
                "lookupswitch { 10: Case10, default: Default }",
                "Case10:",
                "return",
                "Default:",
                "return"
        ), output);
    }

    @Test
    void printCodeUsesLineNumberMatcherToInsertBlankLines() {
        JALAttribute.LineNumberTableAttribute lineNumberTable = new JALAttribute.LineNumberTableAttribute(
                "LineNumberTable",
                new JALAttribute.LineNumberTableAttribute.LineNumber[]{
                        new JALAttribute.LineNumberTableAttribute.LineNumber(0, 10),
                        new JALAttribute.LineNumberTableAttribute.LineNumber(1, 13)
                }
        );

        String output = print(new JALConstantPoolEntry[0], null, new byte[]{
                (byte) EOpcodes.NOP,
                (byte) EOpcodes.RETURN
        }, new LineNumberMatcher(lineNumberTable));

        assertEquals(lines(
                "nop",
                "",
                "",
                "return"
        ), output);
    }

    @Test
    void printCodeThrowsForTruncatedOperand() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> print(new byte[]{(byte) EOpcodes.SIPUSH, 1})
        );

        assertEquals("Truncated operand for opcode 0x11 at pc 0", exception.getMessage());
    }

    @Test
    void printCodeThrowsForUnsupportedOpcode() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> print(new byte[]{(byte) 0xcd})
        );

        assertEquals("Unsupported JVM opcode: 0xcd", exception.getMessage());
    }

    @Test
    void printCodeThrowsForInvalidConstantPoolReferenceType() {
        JALConstantPoolEntry[] pool = pool(new JALConstantPoolEntry.IntegerEntry(1));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> print(pool, null, new byte[]{(byte) EOpcodes.GETSTATIC, 0, 1})
        );

        assertEquals(
                "Expected constant pool entry of type FieldEntry at index 1, but got IntegerEntry",
                exception.getMessage()
        );
    }

    private static String print(byte[] code) {
        return print(new JALConstantPoolEntry[0], null, code);
    }

    private static String print(
            JALConstantPoolEntry[] pool,
            JALAttribute.BootstrapMethodsAttribute bootstrapMethods,
            byte[] code
    ) {
        return print(pool, bootstrapMethods, code, new LineNumberMatcher(null));
    }

    private static String print(
            JALConstantPoolEntry[] pool,
            JALAttribute.BootstrapMethodsAttribute bootstrapMethods,
            byte[] code,
            LineNumberMatcher matcher
    ) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(output));
        try {
            new CodePrinter(new OutputFormatter(), bootstrapMethods, pool).printCode(code, matcher);
        } finally {
            System.setOut(originalOut);
        }
        return output.toString();
    }

    private static JALConstantPoolEntry[] pool(JALConstantPoolEntry... entries) {
        JALConstantPoolEntry[] pool = new JALConstantPoolEntry[entries.length + 1];
        System.arraycopy(entries, 0, pool, 1, entries.length);
        return pool;
    }

    private static JALConstantPoolEntry.ClassEntry classEntry(String name) {
        return new JALConstantPoolEntry.ClassEntry(ClassReferenceType.parse(name));
    }

    private static String lines(String... lines) {
        return String.join(System.lineSeparator(), lines) + System.lineSeparator();
    }
}
