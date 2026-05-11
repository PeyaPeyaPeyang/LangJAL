package tokyo.peya.langjal.compiler.member;

import org.antlr.v4.runtime.ParserRuleContext;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import tokyo.peya.langjal.compiler.CompileReporter;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.exceptions.CompileErrorException;
import tokyo.peya.langjal.compiler.exceptions.UnknownLocalVariableException;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("LocalVariablesHolder")
class LocalVariablesHolderTest
{
    @Test
    void registersAndResolvesLocalsByIndexAndLivingName()
    {
        LabelsHolder labels = new LabelsHolder();
        LocalVariablesHolder holder = newHolder(labels);
        LabelInfo start = label("START", 10);
        LabelInfo end = label("END", 20);
        LabelInfo outside = label("OUTSIDE", 21);

        LocalVariableInfo local = holder.register(2, TypeDescriptor.INTEGER, "value", start, end);

        assertSame(local, holder.resolveSafe(2));
        labels.setCurrentLabel(start);
        assertSame(local, holder.resolveSafe("value"));
        labels.setCurrentLabel(outside);
        assertNull(holder.resolveSafe("value"));
        assertTrue(holder.isLocalLiving(local, end));
    }

    @Test
    void rejectsNegativeAndDuplicateIndexes()
    {
        LabelsHolder labels = new LabelsHolder();
        LocalVariablesHolder holder = newHolder(labels);

        holder.register(1, TypeDescriptor.INTEGER, "first");

        assertThrows(UnknownLocalVariableException.class, () -> holder.register(-1, TypeDescriptor.INTEGER, "negative"));
        assertThrows(UnknownLocalVariableException.class, () -> holder.register(1, TypeDescriptor.INTEGER, "duplicate"));
    }

    @Test
    void nullNameUsesGeneratedLocalName()
    {
        LabelsHolder labels = new LabelsHolder();
        LocalVariablesHolder holder = newHolder(labels);

        LocalVariableInfo local = holder.register(12, TypeDescriptor.LONG, null,
                                                  labels.getCurrentLabel(), labels.getGlobalEnd());

        assertEquals("local00012", local.name());
        assertEquals(12, local.index());
        assertEquals(TypeDescriptor.LONG, local.type());
    }

    @Test
    void parametersAreSortedAndSkippedWhenFinalisingLocalVariableTable()
    {
        LabelsHolder labels = new LabelsHolder();
        LocalVariablesHolder holder = newHolder(labels);
        LabelInfo start = labels.getGlobalStart();
        LabelInfo end = labels.getGlobalEnd();
        MethodNode method = new MethodNode();
        method.name = "locals";
        method.desc = "()V";
        method.localVariables = new ArrayList<>();

        holder.registerParameter("arg1", TypeDescriptor.INTEGER, 1);
        holder.registerParameter("arg0", TypeDescriptor.LONG, 0);
        holder.register(3, TypeDescriptor.OBJECT, "objectValue", start, end);

        holder.evaluateLocals(method);

        LocalVariableInfo[] parameters = holder.getParameters();
        assertEquals("arg0", parameters[0].name());
        assertEquals("arg1", parameters[1].name());
        assertEquals(1, method.localVariables.size());
        assertEquals("objectValue", method.localVariables.getFirst().name);
        assertEquals(TypeDescriptor.OBJECT.toString(), method.localVariables.getFirst().desc);
    }

    @Test
    void availableLocalsAreFilteredByScope()
    {
        LocalVariablesHolder holder = newHolder(new LabelsHolder());
        LabelInfo first = label("FIRST", 1);
        LabelInfo second = label("SECOND", 2);
        LabelInfo third = label("THIRD", 3);
        LocalVariableInfo early = holder.register(0, TypeDescriptor.INTEGER, "early", first, second);
        LocalVariableInfo longLived = holder.register(1, TypeDescriptor.INTEGER, "longLived", first, third);

        assertArrayEquals(new LocalVariableInfo[]{early, longLived}, holder.getAvailableLocalsAt(second));
        assertArrayEquals(new LocalVariableInfo[]{longLived}, holder.getAvailableLocalsAt(third));
    }

    @Test
    void importLocalVariableUsesKnownLabelsOrGlobalFallback()
    {
        LabelsHolder labels = new LabelsHolder();
        LocalVariablesHolder holder = newHolder(labels);
        LabelNode knownStart = new LabelNode(new Label());
        LabelInfo knownStartInfo = labels.importASMLabel(knownStart, 5);
        LabelNode unknownEnd = new LabelNode(new Label());

        holder.importLocalVariable(new LocalVariableNode("imported", "I", null, knownStart, unknownEnd, 7));

        LocalVariableInfo imported = holder.resolveSafe(7);
        assertNotNull(imported);
        assertEquals("imported", imported.name());
        assertEquals(TypeDescriptor.INTEGER, imported.type());
        assertSame(knownStartInfo, imported.start());
        assertSame(labels.getGlobalEnd(), imported.end());
    }

    private static LocalVariablesHolder newHolder(LabelsHolder labels)
    {
        return new LocalVariablesHolder(
                new FileEvaluatingReporter(new RecordingCompileReporter(), Path.of("sample.jal")),
                labels
        );
    }

    private static LabelInfo label(String name, int instructionIndex)
    {
        return new LabelInfo(name, new Label(), instructionIndex);
    }

    private static final class RecordingCompileReporter implements CompileReporter
    {
        private final List<String> infoMessages = new ArrayList<>();

        @Override
        public void postWarning(@NotNull String message, Path sourcePath)
        {
        }

        @Override
        public void postInfo(@NotNull String message, Path sourcePath)
        {
            this.infoMessages.add(message);
        }

        @Override
        public void postError(@NotNull String message, Path sourcePath)
        {
        }

        @Override
        public void postError(@NotNull String message, @NotNull CompileErrorException cause, Path sourcePath)
        {
        }

        @Override
        public void postWarning(@NotNull String message, Path sourcePath, long line, long column, long length)
        {
        }

        @Override
        public void postWarning(@NotNull String message, @NotNull Path sourcePath, @NotNull ParserRuleContext ctxt)
        {
        }
    }
}
