package tokyo.peya.langjal.compiler.instructions;

import tokyo.peya.langjal.analyser.stack.ObjectElement;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.instructions.utils.AbstractInstructionTestCase;
import tokyo.peya.langjal.compiler.instructions.utils.StackMachine;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;

import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.anyObject;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.floatValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.integerValue;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.StackValues.object;
import static tokyo.peya.langjal.compiler.instructions.utils.StackMachine.create;

public class TestSwap extends AbstractInstructionTestCase<JALParser.JvmInsSwapContext, InstructionEvaluatorSwap>
{
    public TestSwap()
    {
        super(new InstructionEvaluatorSwap(), EOpcodes.SWAP);
    }

    @Override
    public InstructionCase[] getValidInstructionSyntaxes()
    {
        StackMachine.StackValue obj1 = object(TypeDescriptor.parse("LMyClass;"));
        StackMachine.StackValue obj2 = object(TypeDescriptor.parse("LAnotherClass;"));

        return set(
                // Object - Primitive
                of(
                        create(obj1, integerValue()).expected(create(integerValue(), obj1)),
                        "swap",
                        EOpcodes.SWAP
                ),
                of(
                        create(integerValue(), obj2).expected(create(obj2, integerValue())),
                        "swap",
                        EOpcodes.SWAP
                ),
                // Object - Object
                of(
                        create(obj1, obj2).expected(create(obj2, obj1)),
                        "swap",
                        EOpcodes.SWAP
                ),
                of(
                        create(obj2, obj1).expected(create(obj1, obj2)),
                        "swap",
                        EOpcodes.SWAP
                ),

                // Primitive - Primitive
                of(
                        create(integerValue(), floatValue()).expected(create(floatValue(), integerValue())),
                        "swap",
                        EOpcodes.SWAP
                ),
                of(
                        create(floatValue(), integerValue()).expected(create(integerValue(), floatValue())),
                        "swap",
                        EOpcodes.SWAP
                )
        );
    }
}

