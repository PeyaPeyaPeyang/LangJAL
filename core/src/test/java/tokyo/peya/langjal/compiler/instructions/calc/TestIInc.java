package tokyo.peya.langjal.compiler.instructions.calc;

import org.objectweb.asm.tree.IincInsnNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.utils.AbstractInstructionTestCase;
import tokyo.peya.langjal.compiler.instructions.utils.StackMachine;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;

import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.integerValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.create;

public class TestIInc extends AbstractInstructionTestCase<JALParser.JvmInsIincContext, InstructionEvaluatorIInc>
{
    public TestIInc()
    {
        super(new InstructionEvaluatorIInc(), EOpcodes.IINC);
    }

    @Override
    public InstructionCase[] getValidInstructionSyntaxes()
    {
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
                        create().set(300, integerValue())
                                .expected(create().set(300, integerValue())),
                        "wide iinc 300 300",
                        new IincInsnNode(300, 300)
                )
        );
    }
}
