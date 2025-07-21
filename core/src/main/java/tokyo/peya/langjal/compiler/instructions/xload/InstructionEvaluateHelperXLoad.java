package tokyo.peya.langjal.compiler.instructions.xload;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.VarInsnNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.exceptions.IllegalInstructionException;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.JALMethodCompiler;
import tokyo.peya.langjal.compiler.member.LocalVariableInfo;

public class InstructionEvaluateHelperXLoad
{
    public static @NotNull EvaluatedInstruction evaluate(@NotNull AbstractInstructionEvaluator<?> evaluator,
                                                         @NotNull JALMethodCompiler compiler,
                                                         @NotNull JALParser.JvmInsArgLocalRefContext ref,
                                                         int opcode,
                                                         @NotNull String callerInsn,
                                                         @Nullable TerminalNode wide)
    {
        LocalVariableInfo local = compiler.getLocals().resolve(ref, callerInsn);

        int idx = local.index();
        boolean isWide = wide != null;
        if (idx >= 0xFF && !isWide)
            throw new IllegalInstructionException(
                    String.format(
                    "Local variable index %d is too large for %s instruction. Use wide variant with.",
                    idx, callerInsn
                    ), ref
            );

        int size = isWide ? 4: 2;
        VarInsnNode insn = new VarInsnNode(opcode, idx);
        return EvaluatedInstruction.of(evaluator, insn, size);
    }

    public static @NotNull EvaluatedInstruction evaluateN(@NotNull AbstractInstructionEvaluator<?> evaluator,
                                                          @NotNull ParserRuleContext caller,
                                                          @NotNull JALMethodCompiler compiler, int opcode, int idx)
    {
        LocalVariableInfo local = compiler.getLocals().resolveSafe(idx);
        if (local == null)
            throw new IllegalInstructionException(
                    "Local variable with index " + idx + " is not defined in the current method context.",
                    caller
            );

        VarInsnNode insn = new VarInsnNode(opcode, idx); // ここには iload_0, iload_1, iload_2, iload_3 などの短い命令が入る
        return EvaluatedInstruction.of(evaluator, insn, 1);  // 大体で aload だが，本来は aload_X などカテ１
    }
}
