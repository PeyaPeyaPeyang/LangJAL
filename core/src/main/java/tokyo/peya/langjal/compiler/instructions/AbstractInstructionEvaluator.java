package tokyo.peya.langjal.compiler.instructions;

import org.antlr.v4.runtime.ParserRuleContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.exceptions.InternalCompileErrorException;
import tokyo.peya.langjal.compiler.member.*;

/**
 * Abstract base class for instruction evaluators.
 * <p>
 * Provides methods for evaluating instructions, mapping contexts, and checking applicability.
 *
 * @param <T> The type of parser rule context handled by this evaluator.
 */
public abstract class AbstractInstructionEvaluator<T extends ParserRuleContext> {
    private final int[] evaluatableOpcodes;

    /**
     * Constructs an evaluator with the specified opcodes.
     *
     * @param evaluatableOpcodes The opcodes for the instruction.
     */
    public AbstractInstructionEvaluator(int... evaluatableOpcodes) {
        this.evaluatableOpcodes = evaluatableOpcodes;
    }

    /**
     * Checks if the given context object is not null.
     *
     * @param context The context object.
     * @return true if not null, false otherwise.
     */
    protected static boolean has(@Nullable Object context) {
        return context != null;
    }

    /**
     * Returns the opcodes that this evaluator can handle.
     *
     * @return An array of opcodes that this evaluator can handle.
     */
    public int[] getEvaluatableOpcodes() {
        return this.evaluatableOpcodes;
    }

    /**
     * Evaluates the instruction using the given compiler and context.
     *
     * @param context      The file evaluating reporter for error reporting and context.
     * @param clazz        The class node being compiled.
     * @param method       The method node being compiled.
     * @param instructions The instructions' holder.
     * @param labels       The labels' holder.
     * @param locals       The local variables' holder.
     * @param instruction  The parser rule context.
     * @return The evaluated instruction.
     */
    @NotNull
    public abstract EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                                  @NotNull ClassNode clazz,
                                                  @NotNull MethodNode method,
                                                  @NotNull InstructionsHolder instructions,
                                                  @NotNull LabelsHolder labels,
                                                  @NotNull LocalVariablesHolder locals, @NotNull T instruction);

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
    public abstract T map(@NotNull JALParser.InstructionContext instruction);

    /**
     * Evaluates the instruction using the given compiler and instruction context.
     * Throws an exception if the instruction is not applicable or mapping fails.
     *
     * @param instruction The instruction context.
     * @return The evaluated instruction.
     */
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions,
                                         @NotNull LabelsHolder labels, @NotNull LocalVariablesHolder locals,
                                         @NotNull JALParser.InstructionContext instruction) {
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

        return evaluate(context, clazz, method, instructions, labels, locals, mappedContext);
    }

    /**
     * Checks if this evaluator is applicable to the given instruction context.
     *
     * @param instruction The instruction context.
     * @return true if applicable, false otherwise.
     */
    public boolean isApplicable(@NotNull JALParser.InstructionContext instruction) {
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
    public EvaluatedInstruction visitSingle(ParserRuleContext ctxt, int opCode) {
        EvaluatedInstruction inst = EvaluatedInstruction.of(this, new InsnNode(opCode));
        if (inst.getInstructionSize() != 1)
            throw new InternalCompileErrorException(
                    "Instruction size mismatch: expected 1, but got " + inst.getInstructionSize() + " for opcode " + opCode,
                    ctxt
            );
        return inst;
    }
}
