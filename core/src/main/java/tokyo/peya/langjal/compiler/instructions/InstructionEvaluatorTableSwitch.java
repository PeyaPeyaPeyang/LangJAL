package tokyo.peya.langjal.compiler.instructions;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.JALMethodCompiler;
import tokyo.peya.langjal.compiler.member.LabelInfo;
import tokyo.peya.langjal.compiler.utils.EvaluatorCommons;

import java.util.List;

public class InstructionEvaluatorTableSwitch extends AbstractInstructionEvaluator<JALParser.JvmInsTableswitchContext>
{
    @Override
    protected @NotNull EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler,
                                                     JALParser.@NotNull JvmInsTableswitchContext ctxt)
    {
        JALParser.JvmInsArgTableSwitchContext args = ctxt.jvmInsArgTableSwitch();

        int low = EvaluatorCommons.asInteger(args.NUMBER());
        JALParser.JvmInsArgTableSwitchCaseListContext caseList = args.jvmInsArgTableSwitchCaseList();
        List<JALParser.LabelNameContext> branches = caseList.labelName();
        JALParser.LabelNameContext defaultBranch = args.labelName();
        int high = low + branches.size() - 1;

        LabelNode defaultLabel = toLabel(compiler, defaultBranch);
        LabelNode[] labels = branches.stream()
                                     .map(labelName -> toLabel(compiler, labelName))
                                     .toArray(LabelNode[]::new);

        TableSwitchInsnNode tableSwitchInsn = new TableSwitchInsnNode(
                low,
                high,
                defaultLabel,
                labels
        );
        return EvaluatedInstruction.of(
                this,
                tableSwitchInsn,
                calcSize(ctxt, compiler.getInstructions().getBytecodeOffset())
        );
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction)
    {
        return FrameDifferenceInfo.builder(instruction)
                                  .popPrimitive(StackElementType.INTEGER)
                                  .build();
    }

    private LabelNode toLabel(@NotNull JALMethodCompiler evaluator, @NotNull JALParser.LabelNameContext labelName)
    {
        LabelInfo labelInfo = evaluator.getLabels().resolve(labelName);
        return labelInfo.node();
    }

    @Override
    protected JALParser.JvmInsTableswitchContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsTableswitch();
    }

    private static int calcSize(@NotNull JALParser.JvmInsTableswitchContext ctxt, long startOffset)
    {
        JALParser.JvmInsArgTableSwitchContext args = ctxt.jvmInsArgTableSwitch();
        JALParser.JvmInsArgTableSwitchCaseListContext caseList = args.jvmInsArgTableSwitchCaseList();
        List<JALParser.LabelNameContext> branches = caseList.labelName();

        int padding = (int) ((4 - (startOffset + 1) % 4) % 4);
        int numCases = branches.size();

        return 1               // opcode
                + padding         // padding to 4-byte boundary
                + 4               // default offset
                + 4               // low
                + 4               // high
                + 4 * numCases;   // jump offsets
    }
}
