package tokyo.peya.langjal.compiler;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.compiler.exceptions.CompileErrorException;
import tokyo.peya.langjal.compiler.instructions.utils.TestCompileReporter;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;

import java.util.Arrays;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JALPreprocessorTest {
    @Test
    void preprocessRemovesDefineLineButKeepsLineBreak() throws CompileErrorException {
        assertEquals(
                "\niconst_0\n",
                JALPreprocessor.preprocess("""
                        #define ZERO iconst_0
                        ZERO
                        """)
        );
    }

    @Test
    void preprocessExpandsNestedDefines() throws CompileErrorException {
        assertEquals(
                "\n\niconst_1\n",
                JALPreprocessor.preprocess("""
                        #define ONE ICONST
                        #define ICONST iconst_1
                        ONE
                        """)
        );
    }

    @Test
    void preprocessExpandsMultilineDefine() throws CompileErrorException {
        assertEquals(
                "\n\niconst_1 \n  pop\n",
                JALPreprocessor.preprocess(
                        "#define BODY iconst_1 \\\n"
                                + "  pop\n"
                                + "BODY\n"
                )
        );
    }

    @Test
    void preprocessReplacesOnlyWholeIdentifiers() throws CompileErrorException {
        assertEquals(
                "\nbar FOO1 _FOO $FOO\n",
                JALPreprocessor.preprocess("""
                        #define FOO bar
                        FOO FOO1 _FOO $FOO
                        """)
        );
    }

    @Test
    void preprocessDoesNotExpandInsideComments() throws CompileErrorException {
        assertEquals(
                "\n// NAME\n/* NAME */\nvalue\n",
                JALPreprocessor.preprocess("""
                        #define NAME value
                        // NAME
                        /* NAME */
                        NAME
                        """)
        );
    }

    @Test
    void preprocessDoesNotExpandInsideMultiLineBlockComment() throws CompileErrorException {
        assertEquals(
                "\n/*\nNAME\n*/\nvalue\n",
                JALPreprocessor.preprocess("""
                        #define NAME value
                        /*
                        NAME
                        */
                        NAME
                        """)
        );
    }

    @Test
    void preprocessRejectsUnsupportedDirective() {
        CompileErrorException exception = assertThrows(
                CompileErrorException.class,
                () -> JALPreprocessor.preprocess("#include Test\n")
        );

        assertEquals("Unsupported preprocessor directive: #include", exception.getDetailedMessage());
        assertEquals(1, exception.getLine());
        assertEquals(0, exception.getColumn());
    }

    @Test
    void preprocessRejectsDefineWithoutName() {
        CompileErrorException exception = assertThrows(
                CompileErrorException.class,
                () -> JALPreprocessor.preprocess("#define\n")
        );

        assertEquals("Expected macro name after #define", exception.getDetailedMessage());
        assertEquals(1, exception.getLine());
    }

    private static ClassNode compile(String source) throws CompileErrorException {
        return JALFileCompiler.compileOnly(
                source,
                new TestCompileReporter(),
                CompileSettings.REQUIRED_ONLY
        ).getCompiledClass();
    }

    private static MethodNode singleMethod(String source) throws CompileErrorException {
        ClassNode clazz = compile(source);
        assertEquals(1, clazz.methods.size());
        return clazz.methods.getFirst();
    }

    private static int[] opcodesOf(MethodNode method) {
        return Arrays.stream(method.instructions.toArray())
                .filter(Objects::nonNull)
                .mapToInt(AbstractInsnNode::getOpcode)
                .filter(opcode -> opcode >= 0)
                .toArray();
    }

    private static LdcInsnNode firstLdcOf(MethodNode method) {
        return Arrays.stream(method.instructions.toArray())
                .filter(LdcInsnNode.class::isInstance)
                .map(LdcInsnNode.class::cast)
                .findFirst()
                .orElseThrow();
    }

    @Test
    void defineExpandsInstructionMnemonic() throws CompileErrorException {
        MethodNode method = singleMethod("""
                #define PUSH_THREE iconst_3
                public class Test {
                    public demo()V {
                        PUSH_THREE
                        pop
                        return
                    }
                }
                """);

        assertArrayEquals(new int[]{EOpcodes.ICONST_3, EOpcodes.POP, EOpcodes.RETURN}, opcodesOf(method));
    }

    @Test
    void defineExpandsInstructionArgument() throws CompileErrorException {
        MethodNode method = singleMethod("""
                #define MESSAGE "Hello"
                public class Test {
                    public demo()V {
                        ldc MESSAGE
                        pop
                        return
                    }
                }
                """);

        assertEquals("Hello", firstLdcOf(method).cst);
    }

    @Test
    void defineExpandsMultipleInstructions() throws CompileErrorException {
        MethodNode method = singleMethod(
                "#define PUSH_AND_POP iconst_3 \\\n"
                        + "  pop\n"
                        + "public class Test {\n"
                        + "    public demo()V {\n"
                        + "        PUSH_AND_POP\n"
                        + "        return\n"
                        + "    }\n"
                        + "}\n"
        );

        assertArrayEquals(new int[]{EOpcodes.ICONST_3, EOpcodes.POP, EOpcodes.RETURN}, opcodesOf(method));
    }

    @Test
    void defineExpandsClassName() throws CompileErrorException {
        ClassNode clazz = compile("""
                #define CLASS_NAME Sample
                public class CLASS_NAME {
                    public demo()V {
                        return
                    }
                }
                """);

        assertEquals("Sample", clazz.name);
    }

    @Test
    void defineDoesNotExpandInsideStrings() throws CompileErrorException {
        MethodNode method = singleMethod("""
                #define MESSAGE "expanded"
                public class Test {
                    public demo()V {
                        ldc "MESSAGE"
                        pop
                        return
                    }
                }
                """);

        assertEquals("MESSAGE", firstLdcOf(method).cst);
    }
}
