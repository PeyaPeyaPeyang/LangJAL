package tokyo.peya.langjal.compiler.member;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import tokyo.peya.langjal.compiler.CompileReporter;
import tokyo.peya.langjal.compiler.FileEvaluatingReporter;
import tokyo.peya.langjal.compiler.exceptions.CompileErrorException;
import tokyo.peya.langjal.compiler.jvm.ClassReferenceType;
import tokyo.peya.langjal.compiler.jvm.TypeDescriptor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TryCatchDirectivesHolder")
class TryCatchDirectivesHolderTest {
    private static TryCatchDirectivesHolder newHolder(RecordingCompileReporter reporter) {
        return new TryCatchDirectivesHolder(
                new FileEvaluatingReporter(reporter, Path.of("sample.jal")),
                new LabelsHolder()
        );
    }

    private static MethodNode newMethod(String name) {
        MethodNode method = new MethodNode();
        method.name = name;
        method.desc = "()V";
        method.tryCatchBlocks = new ArrayList<>();
        return method;
    }

    private static LabelInfo label(String name, int instructionIndex) {
        return new LabelInfo(name, new Label(), instructionIndex);
    }

    private static void assertTryCatchBlock(TryCatchBlockNode block,
                                            LabelInfo expectedStart,
                                            LabelInfo expectedEnd,
                                            LabelInfo expectedHandler,
                                            String expectedType) {
        assertSame(expectedStart.node(), block.start);
        assertSame(expectedEnd.node(), block.end);
        if (expectedHandler == null)
            assertNull(block.handler);
        else
            assertSame(expectedHandler.node(), block.handler);
        assertEquals(expectedType, block.type);
    }

    private static String internalName(TypeDescriptor typeDescriptor) {
        assertFalse(typeDescriptor.isArray(), "Exception type must not be array: " + typeDescriptor);
        assertInstanceOf(ClassReferenceType.class, typeDescriptor.getBaseType(), "Exception type must be class: " + typeDescriptor);
        return ((ClassReferenceType) typeDescriptor.getBaseType()).getInternalName();
    }

    @Test
    void finalisingWithoutDirectivesLeavesMethodUntouched() {
        RecordingCompileReporter reporter = new RecordingCompileReporter();
        TryCatchDirectivesHolder holder = newHolder(reporter);
        MethodNode method = newMethod("emptyMethod");

        holder.finaliseTryCatchDirectives(method);

        assertTrue(method.tryCatchBlocks.isEmpty());
        assertTrue(reporter.infoMessages().isEmpty());
    }

    @Test
    void finalisingCatchDirectiveAddsSingleTryCatchBlock() {
        RecordingCompileReporter reporter = new RecordingCompileReporter();
        TryCatchDirectivesHolder holder = newHolder(reporter);
        MethodNode method = newMethod("catchMethod");

        LabelInfo start = label("TRY_START", 10);
        LabelInfo end = label("TRY_END", 20);
        LabelInfo catchLabel = label("CATCH", 30);
        TypeDescriptor exceptionType = TypeDescriptor.className("Ljava/lang/IllegalStateException;");

        holder.addTryCatchDirective(start, end, catchLabel, exceptionType, null);
        holder.finaliseTryCatchDirectives(method);

        assertEquals(1, method.tryCatchBlocks.size());
        assertTryCatchBlock(method.tryCatchBlocks.get(0), start, end, catchLabel, internalName(exceptionType));
        assertEquals(1, reporter.infoMessages().size());
        assertEquals(
                "Finalising try-catch directives for method catchMethod()V",
                reporter.infoMessages().get(0).message()
        );
        assertEquals(Path.of("sample.jal"), reporter.infoMessages().get(0).sourcePath());
    }

    @Test
    void finalisingFinallyOnlyDirectiveAddsFinallyBlockAfterPrimaryRange() {
        RecordingCompileReporter reporter = new RecordingCompileReporter();
        TryCatchDirectivesHolder holder = newHolder(reporter);
        MethodNode method = newMethod("finallyMethod");

        LabelInfo start = label("TRY_START", 5);
        LabelInfo end = label("TRY_END", 15);
        LabelInfo finallyLabel = label("FINALLY", 25);

        holder.addTryCatchDirective(start, end, null, null, finallyLabel);
        holder.finaliseTryCatchDirectives(method);

        assertEquals(2, method.tryCatchBlocks.size());
        assertTryCatchBlock(method.tryCatchBlocks.get(0), start, end, null, null);
        assertTryCatchBlock(method.tryCatchBlocks.get(1), start, end, finallyLabel, null);
        assertEquals(1, reporter.infoMessages().size());
        assertTrue(reporter.infoMessages().get(0).message().contains("finallyMethod"));
    }

    @Test
    void finalisingMultipleDirectivesPreservesDeclarationOrder() {
        RecordingCompileReporter reporter = new RecordingCompileReporter();
        TryCatchDirectivesHolder holder = newHolder(reporter);
        MethodNode method = newMethod("multiMethod");

        LabelInfo firstStart = label("FIRST_START", 1);
        LabelInfo firstEnd = label("FIRST_END", 2);
        LabelInfo firstCatch = label("FIRST_CATCH", 3);
        LabelInfo secondStart = label("SECOND_START", 4);
        LabelInfo secondEnd = label("SECOND_END", 5);
        LabelInfo secondCatch = label("SECOND_CATCH", 6);
        LabelInfo secondFinally = label("SECOND_FINALLY", 7);

        TypeDescriptor firstExceptionType = TypeDescriptor.className("Ljava/lang/IllegalStateException;");
        holder.addTryCatchDirective(firstStart, firstEnd, firstCatch, firstExceptionType, null);
        holder.addTryCatchDirective(
                secondStart,
                secondEnd,
                secondCatch,
                TypeDescriptor.className("Ljava/lang/RuntimeException;"),
                secondFinally
        );
        holder.finaliseTryCatchDirectives(method);

        assertEquals(3, method.tryCatchBlocks.size());
        assertTryCatchBlock(
                method.tryCatchBlocks.get(0),
                firstStart,
                firstEnd,
                firstCatch,
                internalName(firstExceptionType)
        );
        assertTryCatchBlock(
                method.tryCatchBlocks.get(1),
                secondStart,
                secondEnd,
                secondCatch,
                internalName(TypeDescriptor.className("Ljava/lang/RuntimeException;"))
        );
        assertTryCatchBlock(method.tryCatchBlocks.get(2), secondStart, secondEnd, secondFinally, null);
        assertEquals(1, reporter.infoMessages().size());
    }

    private static final class RecordingCompileReporter implements CompileReporter {
        private final List<InfoMessage> infoMessages = new ArrayList<>();

        List<InfoMessage> infoMessages() {
            return this.infoMessages;
        }

        @Override
        public void postWarning(@NotNull String message, Path sourcePath) {
        }

        @Override
        public void postInfo(@NotNull String message, Path sourcePath) {
            this.infoMessages.add(new InfoMessage(message, sourcePath));
        }

        @Override
        public void postError(@NotNull String message, Path sourcePath) {
        }

        @Override
        public void postError(@NotNull String message, @NotNull CompileErrorException cause, Path sourcePath) {
        }

        @Override
        public void postWarning(@NotNull String message, Path sourcePath, long line, long column, long length) {
        }

        @Override
        public void postWarning(@NotNull String message, @NotNull Path sourcePath,
                                org.antlr.v4.runtime.@NotNull ParserRuleContext ctxt) {
        }
    }

    private record InfoMessage(String message, Path sourcePath) {
    }
}
