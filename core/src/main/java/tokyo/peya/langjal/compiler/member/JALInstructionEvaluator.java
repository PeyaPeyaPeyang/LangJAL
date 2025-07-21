package tokyo.peya.langjal.compiler.member;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.exceptions.InternalCompileErrorException;
import tokyo.peya.langjal.compiler.instructions.AbstractInstructionEvaluator;
import tokyo.peya.langjal.compiler.instructions.InstructionEvaluatorANewArray;
import tokyo.peya.langjal.compiler.instructions.InstructionEvaluatorAThrow;
import tokyo.peya.langjal.compiler.instructions.InstructionEvaluatorArrayLength;
import tokyo.peya.langjal.compiler.instructions.InstructionEvaluatorBiPush;
import tokyo.peya.langjal.compiler.instructions.InstructionEvaluatorCheckCast;
import tokyo.peya.langjal.compiler.instructions.InstructionEvaluatorGoto;
import tokyo.peya.langjal.compiler.instructions.InstructionEvaluatorGotoW;
import tokyo.peya.langjal.compiler.instructions.InstructionEvaluatorInstanceOf;
import tokyo.peya.langjal.compiler.instructions.InstructionEvaluatorJsr;
import tokyo.peya.langjal.compiler.instructions.InstructionEvaluatorJsrW;
import tokyo.peya.langjal.compiler.instructions.InstructionEvaluatorLookupSwitch;
import tokyo.peya.langjal.compiler.instructions.InstructionEvaluatorMonitorEnter;
import tokyo.peya.langjal.compiler.instructions.InstructionEvaluatorMonitorExit;
import tokyo.peya.langjal.compiler.instructions.InstructionEvaluatorMultiANewArray;
import tokyo.peya.langjal.compiler.instructions.InstructionEvaluatorNew;
import tokyo.peya.langjal.compiler.instructions.InstructionEvaluatorNewArray;
import tokyo.peya.langjal.compiler.instructions.InstructionEvaluatorNop;
import tokyo.peya.langjal.compiler.instructions.InstructionEvaluatorPop;
import tokyo.peya.langjal.compiler.instructions.InstructionEvaluatorPop2;
import tokyo.peya.langjal.compiler.instructions.InstructionEvaluatorRet;
import tokyo.peya.langjal.compiler.instructions.InstructionEvaluatorReturn;
import tokyo.peya.langjal.compiler.instructions.InstructionEvaluatorSiPush;
import tokyo.peya.langjal.compiler.instructions.InstructionEvaluatorSwap;
import tokyo.peya.langjal.compiler.instructions.InstructionEvaluatorTableSwitch;
import tokyo.peya.langjal.compiler.instructions.calc.InstructionEvaluatorIInc;
import tokyo.peya.langjal.compiler.instructions.calc.InstructionEvaluatorLCmp;
import tokyo.peya.langjal.compiler.instructions.calc.xadd.InstructionEvaluatorDAdd;
import tokyo.peya.langjal.compiler.instructions.calc.xadd.InstructionEvaluatorFAdd;
import tokyo.peya.langjal.compiler.instructions.calc.xadd.InstructionEvaluatorIAdd;
import tokyo.peya.langjal.compiler.instructions.calc.xadd.InstructionEvaluatorLAdd;
import tokyo.peya.langjal.compiler.instructions.calc.xand.InstructionEvaluatorIAnd;
import tokyo.peya.langjal.compiler.instructions.calc.xand.InstructionEvaluatorLAnd;
import tokyo.peya.langjal.compiler.instructions.calc.xdiv.InstructionEvaluatorDDiv;
import tokyo.peya.langjal.compiler.instructions.calc.xdiv.InstructionEvaluatorFDiv;
import tokyo.peya.langjal.compiler.instructions.calc.xdiv.InstructionEvaluatorIDiv;
import tokyo.peya.langjal.compiler.instructions.calc.xdiv.InstructionEvaluatorLDiv;
import tokyo.peya.langjal.compiler.instructions.calc.xmul.InstructionEvaluatorDMul;
import tokyo.peya.langjal.compiler.instructions.calc.xmul.InstructionEvaluatorFMul;
import tokyo.peya.langjal.compiler.instructions.calc.xmul.InstructionEvaluatorIMul;
import tokyo.peya.langjal.compiler.instructions.calc.xmul.InstructionEvaluatorLMul;
import tokyo.peya.langjal.compiler.instructions.calc.xneg.InstructionEvaluatorDNeg;
import tokyo.peya.langjal.compiler.instructions.calc.xneg.InstructionEvaluatorFNeg;
import tokyo.peya.langjal.compiler.instructions.calc.xneg.InstructionEvaluatorINeg;
import tokyo.peya.langjal.compiler.instructions.calc.xneg.InstructionEvaluatorLNeg;
import tokyo.peya.langjal.compiler.instructions.calc.xor.InstructionEvaluatorIOr;
import tokyo.peya.langjal.compiler.instructions.calc.xor.InstructionEvaluatorLOr;
import tokyo.peya.langjal.compiler.instructions.calc.xrem.InstructionEvaluatorDRem;
import tokyo.peya.langjal.compiler.instructions.calc.xrem.InstructionEvaluatorFRem;
import tokyo.peya.langjal.compiler.instructions.calc.xrem.InstructionEvaluatorIRem;
import tokyo.peya.langjal.compiler.instructions.calc.xrem.InstructionEvaluatorLRem;
import tokyo.peya.langjal.compiler.instructions.calc.xshl.InstructionEvaluatorIShl;
import tokyo.peya.langjal.compiler.instructions.calc.xshl.InstructionEvaluatorLShl;
import tokyo.peya.langjal.compiler.instructions.calc.xshr.InstructionEvaluatorIShr;
import tokyo.peya.langjal.compiler.instructions.calc.xshr.InstructionEvaluatorLShr;
import tokyo.peya.langjal.compiler.instructions.calc.xsub.InstructionEvaluatorDSub;
import tokyo.peya.langjal.compiler.instructions.calc.xsub.InstructionEvaluatorFSub;
import tokyo.peya.langjal.compiler.instructions.calc.xsub.InstructionEvaluatorISub;
import tokyo.peya.langjal.compiler.instructions.calc.xsub.InstructionEvaluatorLSub;
import tokyo.peya.langjal.compiler.instructions.calc.xushr.InstructionEvaluatorIUShr;
import tokyo.peya.langjal.compiler.instructions.calc.xushr.InstructionEvaluatorLUShr;
import tokyo.peya.langjal.compiler.instructions.calc.xxor.InstructionEvaluatorIXOr;
import tokyo.peya.langjal.compiler.instructions.calc.xxor.InstructionEvaluatorLXOr;
import tokyo.peya.langjal.compiler.instructions.cast.InstructionEvaluatorD2F;
import tokyo.peya.langjal.compiler.instructions.cast.InstructionEvaluatorD2I;
import tokyo.peya.langjal.compiler.instructions.cast.InstructionEvaluatorD2L;
import tokyo.peya.langjal.compiler.instructions.cast.InstructionEvaluatorF2D;
import tokyo.peya.langjal.compiler.instructions.cast.InstructionEvaluatorF2I;
import tokyo.peya.langjal.compiler.instructions.cast.InstructionEvaluatorF2L;
import tokyo.peya.langjal.compiler.instructions.cast.InstructionEvaluatorI2B;
import tokyo.peya.langjal.compiler.instructions.cast.InstructionEvaluatorI2C;
import tokyo.peya.langjal.compiler.instructions.cast.InstructionEvaluatorI2D;
import tokyo.peya.langjal.compiler.instructions.cast.InstructionEvaluatorI2F;
import tokyo.peya.langjal.compiler.instructions.cast.InstructionEvaluatorI2L;
import tokyo.peya.langjal.compiler.instructions.cast.InstructionEvaluatorI2S;
import tokyo.peya.langjal.compiler.instructions.cast.InstructionEvaluatorL2D;
import tokyo.peya.langjal.compiler.instructions.cast.InstructionEvaluatorL2F;
import tokyo.peya.langjal.compiler.instructions.cast.InstructionEvaluatorL2I;
import tokyo.peya.langjal.compiler.instructions.dup.InstructionEvaluatorDup;
import tokyo.peya.langjal.compiler.instructions.dup.InstructionEvaluatorDup2;
import tokyo.peya.langjal.compiler.instructions.dup.InstructionEvaluatorDup2X1;
import tokyo.peya.langjal.compiler.instructions.dup.InstructionEvaluatorDup2X2;
import tokyo.peya.langjal.compiler.instructions.dup.InstructionEvaluatorDupX1;
import tokyo.peya.langjal.compiler.instructions.dup.InstructionEvaluatorDupX2;
import tokyo.peya.langjal.compiler.instructions.field.InstructionEvaluatorGetField;
import tokyo.peya.langjal.compiler.instructions.field.InstructionEvaluatorGetStatic;
import tokyo.peya.langjal.compiler.instructions.field.InstructionEvaluatorPutField;
import tokyo.peya.langjal.compiler.instructions.field.InstructionEvaluatorPutStatic;
import tokyo.peya.langjal.compiler.instructions.ifx.InstructionEvaluatorIfACmpOP;
import tokyo.peya.langjal.compiler.instructions.ifx.InstructionEvaluatorIfICmpOP;
import tokyo.peya.langjal.compiler.instructions.ifx.InstructionEvaluatorIfNonNull;
import tokyo.peya.langjal.compiler.instructions.ifx.InstructionEvaluatorIfNull;
import tokyo.peya.langjal.compiler.instructions.ifx.InstructionEvaluatorIfOP;
import tokyo.peya.langjal.compiler.instructions.invokex.InstructionEvaluatorInvokeDynamic;
import tokyo.peya.langjal.compiler.instructions.invokex.InstructionEvaluatorInvokeInterface;
import tokyo.peya.langjal.compiler.instructions.invokex.InstructionEvaluatorInvokeSpecial;
import tokyo.peya.langjal.compiler.instructions.invokex.InstructionEvaluatorInvokeStatic;
import tokyo.peya.langjal.compiler.instructions.invokex.InstructionEvaluatorInvokeVirtual;
import tokyo.peya.langjal.compiler.instructions.ldc.InstructionEvaluatorLDC;
import tokyo.peya.langjal.compiler.instructions.ldc.InstructionEvaluatorLDCW;
import tokyo.peya.langjal.compiler.instructions.ldc.InstructionEvaluatorLDCW2;
import tokyo.peya.langjal.compiler.instructions.xaload.InstructionEvaluatorAALoad;
import tokyo.peya.langjal.compiler.instructions.xaload.InstructionEvaluatorBALoad;
import tokyo.peya.langjal.compiler.instructions.xaload.InstructionEvaluatorCALoad;
import tokyo.peya.langjal.compiler.instructions.xaload.InstructionEvaluatorDALoad;
import tokyo.peya.langjal.compiler.instructions.xaload.InstructionEvaluatorFALoad;
import tokyo.peya.langjal.compiler.instructions.xaload.InstructionEvaluatorIALoad;
import tokyo.peya.langjal.compiler.instructions.xaload.InstructionEvaluatorLALoad;
import tokyo.peya.langjal.compiler.instructions.xaload.InstructionEvaluatorSALoad;
import tokyo.peya.langjal.compiler.instructions.xastore.InstructionEvaluatorAAStore;
import tokyo.peya.langjal.compiler.instructions.xastore.InstructionEvaluatorBAStore;
import tokyo.peya.langjal.compiler.instructions.xastore.InstructionEvaluatorCAStore;
import tokyo.peya.langjal.compiler.instructions.xastore.InstructionEvaluatorDAStore;
import tokyo.peya.langjal.compiler.instructions.xastore.InstructionEvaluatorFAStore;
import tokyo.peya.langjal.compiler.instructions.xastore.InstructionEvaluatorIAStore;
import tokyo.peya.langjal.compiler.instructions.xastore.InstructionEvaluatorLAStore;
import tokyo.peya.langjal.compiler.instructions.xastore.InstructionEvaluatorSAStore;
import tokyo.peya.langjal.compiler.instructions.xcmp_op.InstructionEvaluatorDCmpOp;
import tokyo.peya.langjal.compiler.instructions.xcmp_op.InstructionEvaluatorFCmpOp;
import tokyo.peya.langjal.compiler.instructions.xconst.InstructionEvaluatorAConstNull;
import tokyo.peya.langjal.compiler.instructions.xconst.InstructionEvaluatorDConstN;
import tokyo.peya.langjal.compiler.instructions.xconst.InstructionEvaluatorFConstN;
import tokyo.peya.langjal.compiler.instructions.xconst.InstructionEvaluatorIConstN;
import tokyo.peya.langjal.compiler.instructions.xconst.InstructionEvaluatorLConstN;
import tokyo.peya.langjal.compiler.instructions.xload.InstructionEvaluatorALoad;
import tokyo.peya.langjal.compiler.instructions.xload.InstructionEvaluatorALoadN;
import tokyo.peya.langjal.compiler.instructions.xload.InstructionEvaluatorDLoad;
import tokyo.peya.langjal.compiler.instructions.xload.InstructionEvaluatorDLoadN;
import tokyo.peya.langjal.compiler.instructions.xload.InstructionEvaluatorFLoad;
import tokyo.peya.langjal.compiler.instructions.xload.InstructionEvaluatorFLoadN;
import tokyo.peya.langjal.compiler.instructions.xload.InstructionEvaluatorILoad;
import tokyo.peya.langjal.compiler.instructions.xload.InstructionEvaluatorILoadN;
import tokyo.peya.langjal.compiler.instructions.xload.InstructionEvaluatorLLoad;
import tokyo.peya.langjal.compiler.instructions.xload.InstructionEvaluatorLLoadN;
import tokyo.peya.langjal.compiler.instructions.xreturn.InstructionEvaluatorAReturn;
import tokyo.peya.langjal.compiler.instructions.xreturn.InstructionEvaluatorDReturn;
import tokyo.peya.langjal.compiler.instructions.xreturn.InstructionEvaluatorFReturn;
import tokyo.peya.langjal.compiler.instructions.xreturn.InstructionEvaluatorIReturn;
import tokyo.peya.langjal.compiler.instructions.xreturn.InstructionEvaluatorLReturn;
import tokyo.peya.langjal.compiler.instructions.xstore.InstructionEvaluatorAStore;
import tokyo.peya.langjal.compiler.instructions.xstore.InstructionEvaluatorAStoreN;
import tokyo.peya.langjal.compiler.instructions.xstore.InstructionEvaluatorDStore;
import tokyo.peya.langjal.compiler.instructions.xstore.InstructionEvaluatorDStoreN;
import tokyo.peya.langjal.compiler.instructions.xstore.InstructionEvaluatorFStore;
import tokyo.peya.langjal.compiler.instructions.xstore.InstructionEvaluatorFStoreN;
import tokyo.peya.langjal.compiler.instructions.xstore.InstructionEvaluatorIStore;
import tokyo.peya.langjal.compiler.instructions.xstore.InstructionEvaluatorIStoreN;
import tokyo.peya.langjal.compiler.instructions.xstore.InstructionEvaluatorLStore;
import tokyo.peya.langjal.compiler.instructions.xstore.InstructionEvaluatorLStoreN;

import java.util.List;

public class JALInstructionEvaluator
{
    private static final List<AbstractInstructionEvaluator<?>> EVALUATORS = List.of(
            // ---- カテゴリ 1 ----
            new InstructionEvaluatorNop(),
            new InstructionEvaluatorAConstNull(),

            new InstructionEvaluatorIConstN(),
            new InstructionEvaluatorLConstN(),
            new InstructionEvaluatorFConstN(),
            new InstructionEvaluatorDConstN(),

            new InstructionEvaluatorILoadN(),
            new InstructionEvaluatorLLoadN(),
            new InstructionEvaluatorFLoadN(),
            new InstructionEvaluatorDLoadN(),
            new InstructionEvaluatorALoadN(),

            new InstructionEvaluatorIALoad(),
            new InstructionEvaluatorLALoad(),
            new InstructionEvaluatorFALoad(),
            new InstructionEvaluatorDALoad(),
            new InstructionEvaluatorAALoad(),
            new InstructionEvaluatorBALoad(),
            new InstructionEvaluatorCALoad(),
            new InstructionEvaluatorSALoad(),

            new InstructionEvaluatorIStoreN(),
            new InstructionEvaluatorLStoreN(),
            new InstructionEvaluatorFStoreN(),
            new InstructionEvaluatorDStoreN(),
            new InstructionEvaluatorAStoreN(),

            new InstructionEvaluatorIAStore(),
            new InstructionEvaluatorLAStore(),
            new InstructionEvaluatorFAStore(),
            new InstructionEvaluatorDAStore(),
            new InstructionEvaluatorAAStore(),
            new InstructionEvaluatorBAStore(),
            new InstructionEvaluatorCAStore(),
            new InstructionEvaluatorSAStore(),

            new InstructionEvaluatorPop(),
            new InstructionEvaluatorPop2(),

            new InstructionEvaluatorDup(),
            new InstructionEvaluatorDupX1(),
            new InstructionEvaluatorDupX2(),
            new InstructionEvaluatorDup2(),
            new InstructionEvaluatorDup2X1(),
            new InstructionEvaluatorDup2X2(),
            new InstructionEvaluatorSwap(),

            new InstructionEvaluatorIAdd(),
            new InstructionEvaluatorLAdd(),
            new InstructionEvaluatorFAdd(),
            new InstructionEvaluatorDAdd(),

            new InstructionEvaluatorISub(),
            new InstructionEvaluatorLSub(),
            new InstructionEvaluatorFSub(),
            new InstructionEvaluatorDSub(),

            new InstructionEvaluatorIMul(),
            new InstructionEvaluatorLMul(),
            new InstructionEvaluatorFMul(),
            new InstructionEvaluatorDMul(),

            new InstructionEvaluatorIDiv(),
            new InstructionEvaluatorLDiv(),
            new InstructionEvaluatorFDiv(),
            new InstructionEvaluatorDDiv(),

            new InstructionEvaluatorIRem(),
            new InstructionEvaluatorLRem(),
            new InstructionEvaluatorFRem(),
            new InstructionEvaluatorDRem(),

            new InstructionEvaluatorINeg(),
            new InstructionEvaluatorLNeg(),
            new InstructionEvaluatorFNeg(),
            new InstructionEvaluatorDNeg(),

            new InstructionEvaluatorIShl(),
            new InstructionEvaluatorLShl(),

            new InstructionEvaluatorIShr(),
            new InstructionEvaluatorLShr(),

            new InstructionEvaluatorIUShr(),
            new InstructionEvaluatorLUShr(),

            new InstructionEvaluatorIAnd(),
            new InstructionEvaluatorLAnd(),

            new InstructionEvaluatorIOr(),
            new InstructionEvaluatorLOr(),

            new InstructionEvaluatorIXOr(),
            new InstructionEvaluatorLXOr(),

            new InstructionEvaluatorI2F(),
            new InstructionEvaluatorI2L(),
            new InstructionEvaluatorI2D(),

            new InstructionEvaluatorL2I(),
            new InstructionEvaluatorL2F(),
            new InstructionEvaluatorL2D(),

            new InstructionEvaluatorF2I(),
            new InstructionEvaluatorF2L(),
            new InstructionEvaluatorF2D(),

            new InstructionEvaluatorD2I(),
            new InstructionEvaluatorD2L(),
            new InstructionEvaluatorD2F(),

            new InstructionEvaluatorI2B(),
            new InstructionEvaluatorI2C(),
            new InstructionEvaluatorI2S(),

            new InstructionEvaluatorLCmp(),

            new InstructionEvaluatorFCmpOp(),
            new InstructionEvaluatorDCmpOp(),

            new InstructionEvaluatorIReturn(),
            new InstructionEvaluatorLReturn(),
            new InstructionEvaluatorFReturn(),
            new InstructionEvaluatorDReturn(),
            new InstructionEvaluatorAReturn(),
            new InstructionEvaluatorReturn(),

            new InstructionEvaluatorArrayLength(),
            new InstructionEvaluatorAThrow(),

            new InstructionEvaluatorMonitorEnter(),
            new InstructionEvaluatorMonitorExit(),

            // ---- カテゴリ 2 ----

            new InstructionEvaluatorBiPush(),

            new InstructionEvaluatorLDC(),

            new InstructionEvaluatorILoad(),
            new InstructionEvaluatorLLoad(),
            new InstructionEvaluatorFLoad(),
            new InstructionEvaluatorDLoad(),
            new InstructionEvaluatorALoad(),

            new InstructionEvaluatorIStore(),
            new InstructionEvaluatorLStore(),
            new InstructionEvaluatorFStore(),
            new InstructionEvaluatorDStore(),
            new InstructionEvaluatorAStore(),

            new InstructionEvaluatorIInc(),

            new InstructionEvaluatorRet(),

            // ---- カテゴリ 3 ----

            new InstructionEvaluatorSiPush(),

            new InstructionEvaluatorLDCW(),
            new InstructionEvaluatorLDCW2(),

            new InstructionEvaluatorGetStatic(),
            new InstructionEvaluatorPutStatic(),
            new InstructionEvaluatorGetField(),
            new InstructionEvaluatorPutField(),

            new InstructionEvaluatorInvokeVirtual(),
            new InstructionEvaluatorInvokeSpecial(),
            new InstructionEvaluatorInvokeStatic(),
            new InstructionEvaluatorInvokeInterface(),

            new InstructionEvaluatorNew(),
            new InstructionEvaluatorNewArray(),
            new InstructionEvaluatorANewArray(),

            new InstructionEvaluatorCheckCast(),
            new InstructionEvaluatorInstanceOf(),

            new InstructionEvaluatorIfOP(),
            new InstructionEvaluatorIfICmpOP(),
            new InstructionEvaluatorIfACmpOP(),
            new InstructionEvaluatorIfNull(),
            new InstructionEvaluatorIfNonNull(),

            new InstructionEvaluatorGoto(),
            new InstructionEvaluatorJsr(),

            // ---- カテゴリ 4 ----

            new InstructionEvaluatorMultiANewArray(),

            // ---- カテゴリ 5 ----

            new InstructionEvaluatorGotoW(),
            new InstructionEvaluatorJsrW(),

            // ---- 可変長 ----
            new InstructionEvaluatorTableSwitch(),
            new InstructionEvaluatorLookupSwitch(),

            new InstructionEvaluatorInvokeDynamic()
    );

    @Nullable
    static EvaluatedInstruction evaluateInstruction(@NotNull JALMethodCompiler methodEvaluator,
                                                    @NotNull JALParser.InstructionContext instruction)
    {
        for (AbstractInstructionEvaluator<?> evaluator : EVALUATORS)
            if (evaluator.isApplicable(instruction))
                return evaluator.evaluate(methodEvaluator, instruction);

        throw new InternalCompileErrorException("Unsupported instruction: " + instruction.getText(), instruction);
    }
}
