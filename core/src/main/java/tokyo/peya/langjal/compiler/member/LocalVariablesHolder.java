package tokyo.peya.langjal.compiler.member;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.JALParser;
import tokyo.peya.langjal.compiler.exceptions.UnknownLocalVariableException;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;
import tokyo.peya.langjal.compiler.utils.EvaluatorCommons;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LocalVariablesHolder
{
    private final FileEvaluatingReporter reporter;
    private final LabelsHolder labelsHolder;

    private final List<LocalVariableInfo> locals;

    public LocalVariablesHolder(@NotNull FileEvaluatingReporter reporter, @NotNull LabelsHolder labelsHolder)
    {
        this.reporter = reporter;
        this.labelsHolder = labelsHolder;

        this.locals = new ArrayList<>();
    }

    /* non-public */ void registerParameter(@NotNull String paramName,
                                            @NotNull TypeDescriptor type,
                                            int index)
    {
        // パラメータをローカル変数として登録
        LocalVariableInfo localVar = new LocalVariableInfo(
                paramName,
                type,
                this.labelsHolder.getGlobalStart(),
                this.labelsHolder.getGlobalEnd(),
                index,
                true
        );
        this.locals.add(localVar);
    }

    public void evaluateLocals(@NotNull MethodNode method)
    {
        if (this.locals.isEmpty())
            return;  // ローカル変数がない場合は何もしない

        this.reporter.postInfo("Finalising locals for method " + method.name + method.desc);
        for (LocalVariableInfo local : this.locals)
        {
            if (local.isParameter())
                continue;  // パラメータはローカル変数として登録しない

            LabelNode start = local.start().node();
            LabelNode end = local.end().node();

            String typeDescriptor = local.type().toString();
            // ローカル変数をメソッドに登録
            method.visitLocalVariable(
                    local.name(),
                    typeDescriptor,
                    null, // signature は未使用
                    start.getLabel(),
                    end.getLabel(),
                    local.index()
            );
        }
    }

    @Nullable
    public LocalVariableInfo resolveSafe(int localIndex)
    {
        for (LocalVariableInfo foundLocal : this.locals)  // リストのサイズと index は無関係。
            if (foundLocal.index() == localIndex)
                return foundLocal;
        return null;
    }

    public boolean isLocalLiving(@NotNull LocalVariableInfo local)
    {
        LabelInfo startLabel = local.start();
        LabelInfo endLabel = local.end();
        return this.labelsHolder.isInScope(startLabel, endLabel);
    }

    public boolean isLocalLiving(@NotNull LocalVariableInfo local, @NotNull LabelInfo atLabel)
    {
        LabelInfo startLabel = local.start();
        LabelInfo endLabel = local.end();
        return LabelsHolder.isInScope(startLabel, endLabel, atLabel);
    }

    @Nullable
    public LocalVariableInfo resolveSafe(@NotNull String localName)
    {
        for (LocalVariableInfo localVar : this.locals)
            if (localName.equals(localVar.name()) && this.isLocalLiving(localVar))
                return localVar;

        return null;
    }

    @NotNull
    public LocalVariableInfo resolve(@NotNull JALParser.JvmInsArgLocalRefContext localRef,
                                     @NotNull String callerInsn)
    {
        TerminalNode localID = localRef.ID();
        TerminalNode localNumber = localRef.NUMBER();
        if (localID != null)
        {
            String localName = localID.getText();
            // ローカル変数名を参照
            LocalVariableInfo localVar = this.resolveSafe(localName);
            if (localVar != null)
                return localVar;

            throw new UnknownLocalVariableException(
                    "Local variable with name '" + localName + "' is not defined.",
                    localName,
                    localRef
            );
        }
        else if (localNumber != null)
        {
            int localIndex = EvaluatorCommons.asInteger(localNumber);
            if (localIndex < 0)
                throw new UnknownLocalVariableException(
                        "Local variable index cannot be negative: " + localIndex,
                        String.valueOf(localIndex),
                        localRef
                );

            // ローカル変数番号を参照
            LocalVariableInfo localVar = this.resolveSafe(localIndex);
            if (localVar != null)
            {
                if (localIndex <= 3 && callerInsn.endsWith("load")) // xload 系のときに警告
                    this.warnLocalPerformance(localVar, callerInsn);
                return localVar;
            }

            throw new UnknownLocalVariableException(
                    "Local variable at index " + localIndex + " is not defined.",
                    String.valueOf(localIndex),
                    localRef
            );
        }

        throw new UnknownLocalVariableException(
                "Invalid local reference: " + localRef.getText(),
                localRef.getText(),
                localRef
        );
    }

    @Nullable
    public LocalVariableInfo resolveSafe(@NotNull JALParser.JvmInsArgLocalRefContext localRef)
    {
        TerminalNode localID = localRef.ID();
        TerminalNode localNumber = localRef.NUMBER();
        if (localID != null)
            return this.resolveSafe(localID.getText());
        else if (localNumber != null)
        {
            int localIndex = EvaluatorCommons.asInteger(localNumber);
            if (localIndex < 0)
                return null;

            // ローカル変数番号を参照
            return this.resolveSafe(localIndex);
        }

        return null;  // 無効な参照
    }

    private int getNextLocalIndex()
    {
        if (this.locals.isEmpty())
            return 0;  // 最初のローカル変数はインデックス 0 から始まる

        LocalVariableInfo maxLocalNum = this.locals.stream()
                                                   .max(Comparator.comparingInt(LocalVariableInfo::index))
                                                   .orElseThrow(
                                                           () -> new IllegalStateException(
                                                                   "No local variables registered yet.")
                                                   );

        TypeDescriptor lastType = maxLocalNum.type();
        if (lastType.getBaseType().getCategory() == 2)
        {
            // カテゴリ２の型は２スロット使用するので、次のインデックスは +2
            return maxLocalNum.index() + 2;
        }
        else
        {
            // カテゴリ１の型は１スロット使用するので、次のインデックスは +1
            return maxLocalNum.index() + 1;
        }
    }

    @Nullable
    private LocalVariableInfo checkAlreadyRegistered(
            @NotNull JALParser.JvmInsArgLocalRefContext localRef,
            @Nullable TerminalNode localID
    )
    {
        LocalVariableInfo registeredLocal = this.resolveSafe(localRef);
        if (registeredLocal == null)
            return null;  // まだ登録されていない場合は null を返す

        if (localID == null)
            return registeredLocal;
        else  // 同じ ID で登録されている場合は例外を投げる
            throw new UnknownLocalVariableException(
                    "Local variable with name '" + localID.getText() + "' is already defined as " + registeredLocal.name(),
                    localID.getText(),
                    localRef
            );
    }

    @NotNull
    public LocalVariableInfo register(
            @NotNull JALParser.JvmInsArgLocalRefContext localRef,
            @NotNull TypeDescriptor type,
            @Nullable String name,
            @Nullable LabelInfo endLabel
    )
    {
        if (endLabel == null)
            endLabel = this.labelsHolder.getGlobalEnd();  // 終了ラベルが指定されていない場合はメソッドの終了ラベルを使用

        return this.register(localRef, type, name, this.labelsHolder.getCurrentLabel(), endLabel);
    }

    @NotNull
    public LocalVariableInfo register(int idx,
                                      @NotNull TypeDescriptor type,
                                      @Nullable String name,
                                      @Nullable LabelInfo endLabel)
    {
        if (endLabel == null)
            endLabel = this.labelsHolder.getGlobalEnd();  // 終了ラベルが指定されていない場合はメソッドの終了ラベルを使用

        return this.register(idx, type, name, this.labelsHolder.getCurrentLabel(), endLabel);
    }

    @NotNull
    public LocalVariableInfo register(
            @NotNull JALParser.JvmInsArgLocalRefContext localRef,
            @NotNull TypeDescriptor type,
            @Nullable String name,
            @NotNull LabelInfo startLabel,
            @NotNull LabelInfo endLabel
    )
    {
        // this.local.size() は index と無関係。
        TerminalNode localID = localRef.ID();
        TerminalNode localNumber = localRef.NUMBER();

        LocalVariableInfo registeredLocal = checkAlreadyRegistered(localRef, localID);
        if (registeredLocal != null)
            return registeredLocal;  // すでに登録されている場合はそれを返す

        String newLocalName;
        int newLocalIndex;
        if (localID != null)
        {
            newLocalName = localID.getText();
            newLocalIndex = this.getNextLocalIndex();
        }
        else if (localNumber != null)
        {
            newLocalIndex = EvaluatorCommons.asInteger(localNumber);
            if (newLocalIndex < 0)
                throw new UnknownLocalVariableException(
                        "Local variable index cannot be negative: " + newLocalIndex,
                        String.valueOf(newLocalIndex),
                        localRef
                );

            newLocalName = name == null ? String.format("local%05d", newLocalIndex): name;

        }
        else
            throw new UnknownLocalVariableException(
                    "Invalid local reference: " + localRef.getText(),
                    localRef.getText(),
                    localRef
            );

        // 新しいローカル変数を登録
        registeredLocal = new LocalVariableInfo(
                newLocalName,
                type,
                startLabel,
                endLabel,
                newLocalIndex
        );
        this.locals.add(registeredLocal);
        // メソッドへの登録は後ほど。

        return registeredLocal;
    }

    @NotNull
    public LocalVariableInfo register(int idx,
                                      @NotNull TypeDescriptor type,
                                      @Nullable String name,
                                      @NotNull LabelInfo startLabel,
                                      @NotNull LabelInfo endLabel)
    {
        if (idx < 0)
            throw new UnknownLocalVariableException(
                    "Local variable index cannot be negative: " + idx,
                    String.valueOf(idx)
            );

        // すでに登録されているか確認
        LocalVariableInfo existingLocal = this.resolveSafe(idx);
        if (existingLocal != null)  // インデックス指定なのに，すでにあるのは問題。
            throw new UnknownLocalVariableException(
                    "Local variable at index " + idx + " is already defined as " + existingLocal.name(),
                    String.valueOf(idx)
            );

        String nameToUse = name == null ? String.format("local%05d", idx): name;
        // 新しいローカル変数を登録
        LocalVariableInfo newLocal = new LocalVariableInfo(
                nameToUse,
                type,
                startLabel,
                endLabel,
                idx
        );
        this.locals.add(newLocal);

        // メソッドへの登録は後ほど。
        return newLocal;
    }

    private void warnLocalPerformance(@NotNull LocalVariableInfo localVar, @NotNull String callerInsn)
    {
        // xLOAD_<n> が定義されているので，代わりにそっちを使ったほうが効率が良い(e.g. iload_1)
        this.reporter.postWarning(String.format(
                "Local variable %s is accessed in instruction '%s', " +
                        "but it is recommended to use %s_%d instead for better performance.",
                localVar.name(), callerInsn, localVar.name(), localVar.index()
        ));
    }

    public LocalVariableInfo[] getAvailableLocalsAt(@NotNull LabelInfo globalStart)
    {
        return this.locals.stream()
                          .filter(local -> this.isLocalLiving(local, globalStart))
                          .toArray(LocalVariableInfo[]::new);
    }

    @NotNull
    public LocalVariableInfo[] getParameters()
    {
        return this.locals.stream()
                          .filter(LocalVariableInfo::isParameter)
                          .sorted(Comparator.comparingInt(LocalVariableInfo::index))
                          .toArray(LocalVariableInfo[]::new);
    }
}
