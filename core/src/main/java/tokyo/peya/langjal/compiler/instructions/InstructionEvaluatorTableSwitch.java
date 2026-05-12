package tokyo.peya.langjal.compiler.instructions;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.*;
import tokyo.peya.langjal.compiler.utils.EvaluatorCommons;

import java.util.List;

public class InstructionEvaluatorTableSwitch extends AbstractInstructionEvaluator<JALParser.JvmInsTableswitchContext> {
    public InstructionEvaluatorTableSwitch() {
        super(EOpcodes.TABLESWITCH);
    }

    private static int calcSize(@NotNull JALParser.JvmInsTableswitchContext ctxt, long startOffset) {
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

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsTableswitchContext instruction) {
        JALParser.JvmInsArgTableSwitchContext args = instruction.jvmInsArgTableSwitch();

        int low = EvaluatorCommons.asInteger(args.NUMBER());
        JALParser.JvmInsArgTableSwitchCaseListContext caseList = args.jvmInsArgTableSwitchCaseList();
        List<JALParser.LabelNameContext> branches = caseList.labelName();
        JALParser.LabelNameContext defaultBranch = args.labelName();
        int high = low + branches.size() - 1;

        LabelNode defaultLabel = toLabel(labels, defaultBranch);
        LabelNode[] branchLabels = branches.stream()
                .map(labelName -> toLabel(labels, labelName))
                .toArray(LabelNode[]::new);

        TableSwitchInsnNode tableSwitchInsn = new TableSwitchInsnNode(
                low,
                high,
                defaultLabel,
                branchLabels
        );
        return EvaluatedInstruction.of(
                this,
                tableSwitchInsn,
                calcSize(instruction, instructions.getBytecodeOffset())
        );
    }

    @Override
    public FrameDifferenceInfo getFrameDifferenceInfo(@NotNull InstructionInfo instruction) {
        return FrameDifferenceInfo.builder(instruction)
                .popPrimitive(StackElementType.INTEGER)
                .build();
    }

    private LabelNode toLabel(@NotNull LabelsHolder labels, @NotNull JALParser.LabelNameContext labelName) {
        LabelInfo labelInfo = labels.resolve(labelName);
        return labelInfo.node();
    }

    @Override
    public JALParser.JvmInsTableswitchContext map(JALParser.@NotNull InstructionContext instruction) {
        return instruction.jvmInsTableswitch();
    }
}
