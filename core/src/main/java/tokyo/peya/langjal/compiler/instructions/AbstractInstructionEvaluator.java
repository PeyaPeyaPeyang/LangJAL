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

public abstract class AbstractInstructionEvaluator<T extends ParserRuleContext>
{
    @NotNull
    protected abstract EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler, @NotNull T ctxt);

    public abstract FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction);

    @Nullable
    protected abstract T map(@NotNull JALParser.InstructionContext instruction);

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

    public boolean isApplicable(@NotNull JALParser.InstructionContext instruction)
    {
        return map(instruction) != null;
    }

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

    protected static boolean has(@Nullable Object context)
    {
        return context != null;
    }
}
