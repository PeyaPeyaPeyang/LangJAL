package tokyo.peya.langjal.compiler.instructions.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;

import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractInstructionTestCase
{
    private final int[] expectedOpCodes;
    private final AbstractInstructionEvaluator<?> evaluator;

    public AbstractInstructionTestCase(AbstractInstructionEvaluator<?> evaluator, int... expectedOpCodes)
    {
        this.evaluator = evaluator;
        this.expectedOpCodes = expectedOpCodes;
    }

    @Test
    public void checkEvaluatorAcceptsExpectedOpCodes()
    {
        int[] actualOpCodes = this.evaluator.getEvaluatableOpcodes();
        for (int expectedOpCode : this.expectedOpCodes)
        {
            boolean found = false;
            for (int actualOpCode : actualOpCodes)
            {
                if (actualOpCode == expectedOpCode)
                {
                    found = true;
                    break;
                }
            }
            assertTrue(found, String.format("Expected opcode %d not found in evaluator's accepted opcodes", expectedOpCode));
        }

        Assertions.assertEquals(this.expectedOpCodes.length, actualOpCodes.length, "Evaluator accepts unexpected number of opcodes");
    }
}
