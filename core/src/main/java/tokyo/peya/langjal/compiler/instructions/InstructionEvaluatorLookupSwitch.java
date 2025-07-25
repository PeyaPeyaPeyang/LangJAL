package tokyo.peya.langjal.compiler.instructions;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.analyser.FrameDifferenceInfo;
import tokyo.peya.langjal.analyser.stack.StackElementType;
import tokyo.peya.langjal.compiler.exceptions.IllegalInstructionException;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.EvaluatedInstruction;
import tokyo.peya.langjal.compiler.member.InstructionInfo;
import tokyo.peya.langjal.compiler.member.JALMethodCompiler;
import tokyo.peya.langjal.compiler.member.LabelInfo;
import tokyo.peya.langjal.compiler.utils.EvaluatorCommons;

import java.util.LinkedList;
import java.util.List;

public class InstructionEvaluatorLookupSwitch extends AbstractInstructionEvaluator<JALParser.JvmInsLookupswitchContext>
{
    public InstructionEvaluatorLookupSwitch()
    {
        super(EOpcodes.LOOKUPSWITCH);
    }

    @Override
    protected @NotNull EvaluatedInstruction evaluate(@NotNull JALMethodCompiler compiler,
                                                     JALParser.@NotNull JvmInsLookupswitchContext ctxt)
    {
        JALParser.JvmInsArgLookupSwitchContext args = ctxt.jvmInsArgLookupSwitch();
        JALParser.JvmInsArgLookupSwitchCaseListContext caseList = args.jvmInsArgLookupSwitchCaseList();
        List<JALParser.JvmInsArgLookupSwitchCaseContext> cases = caseList.jvmInsArgLookupSwitchCase();

        List<Integer> keys = new LinkedList<>();
        List<LabelNode> labels = new LinkedList<>();
        LabelNode defaultLabel = null;

        for (JALParser.JvmInsArgLookupSwitchCaseContext c : cases)
        {
            JALParser.JvmInsArgLookupSwitchCaseNameContext caseName = c.jvmInsArgLookupSwitchCaseName();
            JALParser.LabelNameContext labelName = c.labelName();
            if (caseName.KWD_SWITCH_DEFAULT() != null)
                defaultLabel = toLabel(compiler, labelName);
            else if (caseName.NUMBER() != null)
            {
                int key = EvaluatorCommons.asInteger(caseName.NUMBER());
                keys.add(key);
                labels.add(toLabel(compiler, labelName));
            }
        }

        if (defaultLabel == null)
            throw new IllegalInstructionException("lookupswitch must have a default case", args);

        LookupSwitchInsnNode lookupSwitchInsnNode = new LookupSwitchInsnNode(
                defaultLabel,
                keys.stream().mapToInt(Integer::intValue).toArray(),
                labels.toArray(new LabelNode[0])
        );
        return EvaluatedInstruction.of(
                this,
                lookupSwitchInsnNode,
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
    protected JALParser.JvmInsLookupswitchContext map(JALParser.@NotNull InstructionContext instruction)
    {
        return instruction.jvmInsLookupswitch();
    }

    private static int calcSize(@NotNull JALParser.JvmInsLookupswitchContext ctxt, long startOffset)
    {
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
}
