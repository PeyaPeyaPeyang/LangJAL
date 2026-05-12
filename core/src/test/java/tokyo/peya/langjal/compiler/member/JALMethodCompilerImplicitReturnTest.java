package tokyo.peya.langjal.compiler.member;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.compiler.CompileSettings;
import tokyo.peya.langjal.compiler.JALFileCompiler;
import tokyo.peya.langjal.compiler.exceptions.CompileErrorException;
import tokyo.peya.langjal.compiler.instructions.utils.TestCompileReporter;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;

import java.util.Arrays;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JALMethodCompilerImplicitReturnTest
{
    @Test
    void appendsVoidReturnWhenMethodEndsWithoutExplicitReturn() throws CompileErrorException
    {
        MethodNode method = compileSingleMethod("""
                public class Test {
                    public demo()V {
                        nop
                    }
                }
                """);

        assertArrayEquals(new int[] {EOpcodes.NOP, EOpcodes.RETURN}, opcodesOf(method));
    }

    @Test
    void doesNotAppendExtraReturnWhenMethodContainsOnlyReturn() throws CompileErrorException
    {
        MethodNode method = compileSingleMethod("""
                public class Test {
                    public demo()V {
                        return
                    }
                }
                """);

        assertArrayEquals(new int[] {EOpcodes.RETURN}, opcodesOf(method));
    }

    @Test
    void doesNotAppendExtraReturnAfterExplicitReturn() throws CompileErrorException
    {
        MethodNode method = compileSingleMethod("""
                public class Test {
                    public demo()V {
                        nop
                        return
                    }
                }
                """);

        assertArrayEquals(new int[] {EOpcodes.NOP, EOpcodes.RETURN}, opcodesOf(method));
    }

    @Test
    void appendsVoidReturnAfterStackProducingInstructionsAreConsumed() throws CompileErrorException
    {
        MethodNode method = compileSingleMethod("""
                public class Test {
                    public demo()V {
                        iconst_1
                        pop
                    }
                }
                """);

        assertArrayEquals(new int[] {EOpcodes.ICONST_1, EOpcodes.POP, EOpcodes.RETURN}, opcodesOf(method));
    }

    @Test
    void appendsVoidReturnAtTrailingBranchLabelWithoutInstructions() throws CompileErrorException
    {
        MethodNode method = compileSingleMethod("""
                public class Test {
                    public demo()V {
                        iconst_0
                        ifeq Branch
                        nop
                        goto End
                    Branch:
                        nop
                    End:
                        nop
                    }
                }
                """);

        assertArrayEquals(
                new int[] {EOpcodes.ICONST_0, EOpcodes.IFEQ, EOpcodes.NOP, EOpcodes.GOTO, EOpcodes.NOP, EOpcodes.NOP, EOpcodes.RETURN},
                opcodesOf(method)
        );
    }

    @Test
    void doesNotAppendExtraReturnWhenComplexBranchesAlreadyReturn() throws CompileErrorException
    {
        MethodNode method = compileSingleMethod("""
                public class Test {
                    public demo()V {
                        iconst_0
                        ifeq Branch
                        nop
                        return
                    Branch:
                        nop
                        return
                    }
                }
                """);

        assertArrayEquals(
                new int[] {EOpcodes.ICONST_0, EOpcodes.IFEQ, EOpcodes.NOP, EOpcodes.RETURN, EOpcodes.NOP, EOpcodes.RETURN},
                opcodesOf(method)
        );
    }

    private static MethodNode compileSingleMethod(String source) throws CompileErrorException
    {
        ClassNode clazz = JALFileCompiler.compileOnly(
                source,
                new TestCompileReporter(),
                CompileSettings.REQUIRED_ONLY
        ).getCompiledClass();

        assertEquals(1, clazz.methods.size());
        return clazz.methods.getFirst();
    }

    private static int[] opcodesOf(MethodNode method)
    {
        return Arrays.stream(method.instructions.toArray())
                     .filter(Objects::nonNull)
                     .mapToInt(AbstractInsnNode::getOpcode)
                     .filter(opcode -> opcode >= 0)
                     .toArray();
    }
}
