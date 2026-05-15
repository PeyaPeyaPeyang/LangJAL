package tokyo.peya.langjal.compiler.member;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.compiler.CompileSettings;
import tokyo.peya.langjal.compiler.JALFileCompiler;
import tokyo.peya.langjal.compiler.exceptions.CompileErrorException;
import tokyo.peya.langjal.compiler.instructions.utils.TestCompileReporter;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JALMethodCompilerDeadLocalMergeTest {
    @Test
    void allowsMergingDifferentDeadLocalTypesWhenSlotIsUnusedAfterJoin() throws CompileErrorException {
        ClassNode clazz = JALFileCompiler.compileOnly(
                """
                        public class Test {
                            public static test(I)V {
                                iload_0
                                ifeq End
                                aconst_null
                                astore_0
                            End:
                                return
                            }
                        }
                        """,
                new TestCompileReporter(),
                CompileSettings.REQUIRED_ONLY
        ).getCompiledClass();

        assertEquals(1, clazz.methods.size());
        MethodNode method = clazz.methods.getFirst();
        assertEquals("test", method.name);
    }
}
