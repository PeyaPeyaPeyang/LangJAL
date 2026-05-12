package tokyo.peya.langjal.compiler.instructions;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.exceptions.IllegalInstructionException;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.*;
import tokyo.peya.langjal.compiler.utils.EvaluatorCommons;

import java.util.LinkedList;
import java.util.List;

public class InstructionEvaluatorLookupSwitch extends AbstractInstructionEvaluator<JALParser.JvmInsLookupswitchContext> {
    public InstructionEvaluatorLookupSwitch() {
        super(EOpcodes.LOOKUPSWITCH);
    }

    private static int calcSize(@NotNull JALParser.JvmInsLookupswitchContext ctxt, long startOffset) {
        JALParser.JvmInsArgLookupSwitchContext args = ctxt.jvmInsArgLookupSwitch();
        JALParser.JvmInsArgLookupSwitchCaseListContext caseList = args.jvmInsArgLookupSwitchCaseList();
        List<JALParser.JvmInsArgLookupSwitchCaseContext> cases = caseList.jvmInsArgLookupSwitchCase();

        int nPairs = 0;
        for (JALParser.JvmInsArgLookupSwitchCaseContext c : cases)
            if (c.jvmInsArgLookupSwitchCaseName().KWD_SWITCH_DEFAULT() == null)
                nPairs++;

        int padding = (int) ((4 - (startOffset + 1) % 4) % 4);

        return 1              // opcode
                + padding        // align to 4-byte boundary
                + 4              // default offset
                + 4              // nPairs
                + 8 * nPairs;    // key + offset per pair
    }

    @Override
    @NotNull
    public EvaluatedInstruction evaluate(@NotNull FileEvaluatingReporter context,
                                         @NotNull ClassNode clazz, @NotNull MethodNode method,
                                         @NotNull InstructionsHolder instructions, @NotNull LabelsHolder labels,
                                         @NotNull LocalVariablesHolder locals,
                                         JALParser.@NotNull JvmInsLookupswitchContext instruction) {
        JALParser.JvmInsArgLookupSwitchContext args = instruction.jvmInsArgLookupSwitch();
        JALParser.JvmInsArgLookupSwitchCaseListContext caseList = args.jvmInsArgLookupSwitchCaseList();
        List<JALParser.JvmInsArgLookupSwitchCaseContext> cases = caseList.jvmInsArgLookupSwitchCase();

        List<Integer> keys = new LinkedList<>();
        List<LabelNode> branches = new LinkedList<>();
        LabelNode defaultLabel = null;

        for (JALParser.JvmInsArgLookupSwitchCaseContext c : cases) {
            JALParser.JvmInsArgLookupSwitchCaseNameContext caseName = c.jvmInsArgLookupSwitchCaseName();
            JALParser.LabelNameContext labelName = c.labelName();
            if (caseName.KWD_SWITCH_DEFAULT() != null)
                defaultLabel = toLabel(labels, labelName);
            else if (caseName.NUMBER() != null) {
                int key = EvaluatorCommons.asInteger(caseName.NUMBER());
                keys.add(key);
                branches.add(toLabel(labels, labelName));
            }
        }

        if (defaultLabel == null)
            throw new IllegalInstructionException("lookupswitch must have a default case", args);

        LookupSwitchInsnNode lookupSwitchInsnNode = new LookupSwitchInsnNode(
                defaultLabel,
                keys.stream().mapToInt(Integer::intValue).toArray(),
                branches.toArray(new LabelNode[0])
        );
        return EvaluatedInstruction.of(
                this,
                lookupSwitchInsnNode,
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
    public JALParser.JvmInsLookupswitchContext map(JALParser.@NotNull InstructionContext instruction) {
        return instruction.jvmInsLookupswitch();
    }
}
