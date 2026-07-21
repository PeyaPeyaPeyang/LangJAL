package tokyo.peya.langjal.analyser;

import org.junit.jupiter.api.Test;
import tokyo.peya.langjal.analyser.stack.ObjectElement;
import tokyo.peya.langjal.compiler.CompileSettings;
import tokyo.peya.langjal.compiler.JALFileCompiler;
import tokyo.peya.langjal.compiler.exceptions.CompileErrorException;
import tokyo.peya.langjal.compiler.instructions.utils.TestCompileReporter;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.member.JALMethodCompiler;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MethodAnalyserTest {
    private static JALMethodCompiler compileSingleMethod(String source) throws CompileErrorException {
        var compiler = JALFileCompiler.compileOnly(
                source,
                new TestCompileReporter(),
                CompileSettings.NONE
        );

        assertEquals(1, compiler.getMethodCompilers().size());
        return compiler.getMethodCompilers().getFirst();
    }

    private static MethodAnalysisResult analyse(String source) throws CompileErrorException {
        return compileSingleMethod(source).analyseMethod();
    }

    @Test
    void emptyStaticVoidMethodHasNoStackOrLocals() throws CompileErrorException {
        MethodAnalysisResult result = analyse("""
                public class Test {
                    public static demo()V {
                        return
                    }
                }
                """);

        assertEquals(0, result.maxStack());
        assertEquals(0, result.maxLocals());
    }

    @Test
    void instanceVoidMethodUsesThisLocal() throws CompileErrorException {
        MethodAnalysisResult result = analyse("""
                public class Test {
                    public demo()V {
                        return
                    }
                }
                """);

        assertEquals(0, result.maxStack());
        assertEquals(1, result.maxLocals());
    }

    @Test
    void loadsAndReturnsSingleIntParameter() throws CompileErrorException {
        MethodAnalysisResult result = analyse("""
                public class Test {
                    public static demo(I)I {
                        iload_0
                        ireturn
                    }
                }
                """);

        assertEquals(1, result.maxStack());
        assertEquals(1, result.maxLocals());
    }

    @Test
    void storesAndReloadsSingleIntLocal() throws CompileErrorException {
        MethodAnalysisResult result = analyse("""
                public class Test {
                    public static demo()I {
                        iconst_1
                        istore_0
                        iload_0
                        ireturn
                    }
                }
                """);

        assertEquals(1, result.maxStack());
        assertEquals(1, result.maxLocals());
    }

    @Test
    void pushesAndPopsOrdinaryPrimitiveValues() throws CompileErrorException {
        MethodAnalysisResult result = analyse("""
                public class Test {
                    public static demo()V {
                        iconst_1
                        fconst_1
                        pop
                        pop
                        return
                    }
                }
                """);

        assertEquals(2, result.maxStack());
        assertEquals(0, result.maxLocals());
    }

    @Test
    void returnsNullAsObjectReference() throws CompileErrorException {
        MethodAnalysisResult result = analyse("""
                public class Test {
                    public static demo()Ljava/lang/Object; {
                        aconst_null
                        areturn
                    }
                }
                """);

        assertEquals(1, result.maxStack());
        assertEquals(0, result.maxLocals());
    }

    @Test
    void labelledFallthroughCreatesNextBlockPropagation() throws CompileErrorException {
        MethodAnalysisResult result = analyse("""
                public class Test {
                    public static demo()V {
                        nop
                    Next:
                        return
                    }
                }
                """);

        assertTrue(Arrays.stream(result.propagations()).anyMatch(p -> p.receiver().name().equals("Next")));
    }

    @Test
    void exceptionHandlerKeepsLiveLocalsDefinedInProtectedRange() {
        assertDoesNotThrow(() -> analyse("""
                public class Test {
                    public static demo()V {
                    TryStart: [~ TryEnd, java/lang/Throwable: Catch]
                        iconst_1
                        istore 0 [I -> value]
                        aconst_null
                        athrow
                    TryEnd:
                        return
                    Catch:
                        iload value
                        pop
                        return
                    }
                }
                """));
    }

    @Test
    void computesMaxStackUsingCategoryTwoStackSlots() throws CompileErrorException {
        MethodAnalysisResult result = analyse("""
                public class Test {
                    public static demo()V {
                        lconst_0
                        dconst_0
                        pop2
                        pop2
                        return
                    }
                }
                """);

        assertEquals(4, result.maxStack());
    }

    @Test
    void computesInitialMaxLocalsUsingCategoryTwoParameters() throws CompileErrorException {
        MethodAnalysisResult result = analyse("""
                public class Test {
                    public static demo(JDI)V {
                        return
                    }
                }
                """);

        assertEquals(5, result.maxLocals());
    }

    @Test
    void createsPropagationsForConditionalBranchAndFallthrough() throws CompileErrorException {
        MethodAnalysisResult result = analyse("""
                public class Test {
                    public static demo(I)V {
                        iload_0
                        ifeq Branch
                        nop
                    Branch:
                        return
                    }
                }
                """);

        assertTrue(Arrays.stream(result.propagations()).anyMatch(p -> p.receiver().name().equals("Branch")));
        assertTrue(Arrays.stream(result.propagations()).anyMatch(p -> p.sender().name().equals("MBEGIN")));
    }

    @Test
    void propagatesPrimitiveArraysAsObjectStackElements() throws CompileErrorException {
        MethodAnalysisResult result = analyse("""
                public class Test {
                    public static demo(I)[I {
                        iload_0
                        newarray I
                        goto End
                    End:
                        areturn
                    }
                }
                """);

        Optional<FramePropagation> endPropagation = Arrays.stream(result.propagations())
                .filter(p -> p.receiver().name().equals("End"))
                .findFirst();

        assertTrue(endPropagation.isPresent());
        assertEquals(1, endPropagation.get().stack().length);
        assertTrue(endPropagation.get().stack()[0] instanceof ObjectElement);
        assertEquals(TypeDescriptor.parse("[I"), ((ObjectElement) endPropagation.get().stack()[0]).content());
    }

    @Test
    void ignoresInstructionsAfterTerminalInstructionInSameBlock() throws CompileErrorException {
        MethodAnalysisResult result = analyse("""
                public class Test {
                    public static demo()V {
                        return
                        iconst_1
                        iconst_1
                        iconst_1
                        pop
                        pop
                        pop
                    }
                }
                """);

        assertEquals(0, result.maxStack());
    }
}
