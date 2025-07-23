package tokyo.peya.langjal.compiler.instructions;

import org.antlr.v4.runtime.ParserRuleContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.InsnNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.compiler.exceptions.InternalCompileErrorException;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.JALMethodCompiler;

/**
 * Abstract base class for instruction evaluators.
 * <p>
 * Provides methods for evaluating instructions, mapping contexts, and checking applicability.
 *
 * @param <T> The type of parser rule context handled by this evaluator.
 */
public abstract class AbstractInstructionEvaluator<T extends ParserRuleContext>
{
    /**
     * Evaluates the instruction using the given compiler and context.
     *
     * @param compiler The method compiler.
     * @param ctxt     The parser rule context.
     * @return The evaluated instruction.
     */
    @NotNull
    protected abstract EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler, @NotNull T ctxt);

    /**
     * Returns the frame difference info for the given instruction.
     *
     * @param instruction The instruction info.
     * @return The frame difference info.
     */
    public abstract FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction);

    /**
     * Maps the instruction context to the specific parser rule context type.
     *
     * @param instruction The instruction context.
     * @return The mapped context, or null if not applicable.
     */
    @Nullable
    protected abstract T map(@NotNull JALParser.InstructionContext instruction);

    /**
     * Evaluates the instruction using the given compiler and instruction context.
     * Throws an exception if the instruction is not applicable or mapping fails.
     *
     * @param compiler    The method compiler.
     * @param instruction The instruction context.
     * @return The evaluated instruction.
     */
    public EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler,
                                         @NotNull JALParser.InstructionContext instruction)
    {
        if (!isApplicable(instruction))
            throw new InternalCompileErrorException(
                    "Instruction is not applicable: " + instruction.getText(),
                    instruction
            );

        T mappedContext = map(instruction);
        if (mappedContext == null)
            throw new InternalCompileErrorException(
                    "Mapped context is null for instruction: " + instruction.getText(),
                    instruction
            );

        return evaluate(compiler, mappedContext);
    }

    /**
     * Checks if this evaluator is applicable to the given instruction context.
     *
     * @param instruction The instruction context.
     * @return true if applicable, false otherwise.
     */
    public boolean isApplicable(@NotNull JALParser.InstructionContext instruction)
    {
        return map(instruction) != null;
    }

    /**
     * Creates an evaluated instruction for a single opcode.
     * Throws an exception if the instruction size does not match.
     *
     * @param ctxt   The parser rule context.
     * @param opCode The opcode.
     * @return The evaluated instruction.
     */
    public EvaluatedInstruction visitSingle(ParserRuleContext ctxt, int opCode)
    {
        EvaluatedInstruction inst = EvaluatedInstruction.of(this, new InsnNode(opCode));
        if (inst.getInstructionSize() != 1)
            throw new InternalCompileErrorException(
                    "Instruction size mismatch: expected 1, but got " + inst.getInstructionSize() + " for opcode " + opCode,
                    ctxt
            );
        return inst;
    }

    /**
     * Checks if the given context object is not null.
     *
     * @param context The context object.
     * @return true if not null, false otherwise.
     */
    protected static boolean has(@Nullable Object context)
    {
        return context != null;
    }
}
