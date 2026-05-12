package tokyo.peya.langjal.compiler.instructions.calc;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.utils.AbstractInstructionTestCase;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.integerValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.create;

public class TestIInc extends AbstractInstructionTestCase<JALParser.JvmInsIincContext, InstructionEvaluatorIInc> {
    public TestIInc() {
        super(new InstructionEvaluatorIInc(), EOpcodes.IINC);
    }

    @Override
    public InstructionCase[] getValidInstructionSyntaxes() {
        return set(
                of(
                        create().set(1, integerValue())
                                .expected(create().set(1, integerValue())),
                        "iinc 1 1",
                        new IincInsnNode(1, 1)
                ),
                of(
                        create().set(2, integerValue())
                                .expected(create().set(2, integerValue())),
                        "iinc 2 10",
                        new IincInsnNode(2, 10)
                ),
                of(
                        create(integerValue()).set(2, integerValue()).set(10, integerValue())
                                .expected(create(integerValue()).set(2, integerValue())
                                        .set(10, integerValue())),
                        "iinc 2 -5",
                        new IincInsnNode(2, -5)
                ),
                of(
                        create().set(300, integerValue())
                                .expected(create().set(300, integerValue())),
                        "wide iinc 300 300",
                        new IincInsnNode(300, 300)
                )
        );
    }

    @Override
    protected void assertInstructionEquals(AbstractInsnNode expected, AbstractInsnNode actual) {
        super.assertInstructionEquals(expected, actual);

        IincInsnNode expectedIinc = (IincInsnNode) expected;
        IincInsnNode actualIinc = (IincInsnNode) actual;
        assertEquals(expectedIinc.var, actualIinc.var, "IINC local variable index does not match");
        assertEquals(expectedIinc.incr, actualIinc.incr, "IINC increment does not match");
    }
}
