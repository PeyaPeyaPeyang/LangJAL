package tokyo.peya.langjal.compiler;

import lombok.Getter;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import tokyo.peya.langjal.compiler.exceptions.CompileErrorException;
import tokyo.peya.langjal.compiler.jvm.EOpcodes;
import tokyo.peya.langjal.compiler.member.JALMethodCompiler;
import tokyo.peya.langjal.compiler.utils.EvaluatorCommons;
import tokyo.peya.langjal.compiler.utils.RuntimeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JALClassCompiler
{
    private final FileEvaluatingReporter reporter;
    private final String fileName;
    @MagicConstant(valuesFromClass = CompileSettings.class)
    private final int compileFlags;

    @Getter
    private final ClassNode compiledClass;
    private final List<JALMethodCompiler> methodCompilers;

    public JALClassCompiler(@NotNull FileEvaluatingReporter reporter, @Nullable String fileName,
                            @MagicConstant(valuesFromClass = CompileSettings.class) int compileFlags)
    {
        this.reporter = reporter;
        this.fileName = fileName;
        this.compileFlags = compileFlags;

        this.compiledClass = new ClassNode();
        this.methodCompilers = new ArrayList<>();
    }

    public ClassNode compileClassAST(@NotNull JALParser.ClassDefinitionContext clazz) throws CompileErrorException
    {
        this.visitClassInformation(this.compiledClass, clazz);
        this.visitClassBody(this.compiledClass, clazz.classBody());

        return this.compiledClass;
    }

    public List<JALMethodCompiler> getMethodCompilers()
    {
        return Collections.unmodifiableList(this.methodCompilers);
    }

    private void visitClassBody(@NotNull ClassNode classNode, @Nullable JALParser.ClassBodyContext body)
    {
        if (body == null)
            return;

        List<JALParser.ClassBodyItemContext> items = body.classBodyItem();
        for (JALParser.ClassBodyItemContext item : items)
        {
            if (item.methodDefinition() != null)
            {
                JALMethodCompiler evaluator = new JALMethodCompiler(this.reporter, classNode, this.compileFlags);
                evaluator.evaluateMethod(item.methodDefinition());
                this.methodCompilers.add(evaluator);
            }
            if (item.fieldDefinition() != null)
                visitField(classNode, item.fieldDefinition());
        }
    }

    private static void visitField(@NotNull ClassNode classNode,
                                   @NotNull JALParser.FieldDefinitionContext fieldDefinition)
    {
        JALParser.AccModFieldContext accessModifier = fieldDefinition.accModField();
        String fieldName = fieldDefinition.fieldName().getText();
        String fieldType = fieldDefinition.typeDescriptor().getText();
        Object scalarValue = null;

        if (fieldDefinition.jvmInsArgScalarType() != null)
            scalarValue = EvaluatorCommons.evaluateScalar(fieldDefinition.jvmInsArgScalarType());

        int modifier = visitFieldAccessModifier(accessModifier);
        FieldNode fieldNode = new FieldNode(
                modifier,
                fieldName,
                fieldType,
                null, // signature
                scalarValue // value
        );
        classNode.fields.add(fieldNode);
    }

    private void visitClassInformation(@NotNull ClassNode classNode,
                                       @NotNull JALParser.ClassDefinitionContext definitionContext)
            throws CompileErrorException
    {
        int major = -1;
        int minor = -1;
        int modifier = visitClassAccessModifier(definitionContext.accModClass());
        String className = definitionContext.className().getText();
        String superClassName = null;
        List<String> interfaceName = Collections.emptyList();

        JALParser.ClassMetaContext meta = definitionContext.classMeta();
        if (meta != null)
        {
            List<JALParser.ClassMetaItemContext> metaItems = meta.classMetaItem();
            for (JALParser.ClassMetaItemContext item : metaItems)
            {
                if (item.classPropMajor() != null)
                    major = EvaluatorCommons.asInteger(item.classPropMajor().NUMBER());
                else if (item.classPropMinor() != null)
                    minor = EvaluatorCommons.asInteger(item.classPropMinor().NUMBER());
                else if (item.classPropSuperClass() != null)
                    superClassName = item.classPropSuperClass().className().getText();
                else if (item.classPropInterfaces() != null)
                    interfaceName = item.classPropInterfaces().className()
                                        .stream()
                                        .map(JALParser.ClassNameContext::getText)
                                        .filter(name -> name != null && !name.isEmpty())
                                        .toList();
            }
        }

        // フォールバック
        if (major <= 0 || minor < 0)
        {
            // デフォルトのバージョンを使用
            major = RuntimeUtils.getClassFileMajorVersion();
            minor = 0;
        }

        if (superClassName == null || superClassName.isEmpty())
            superClassName = "java/lang/Object"; // デフォルトのスーパークラス

        int version = minor << 16 | major;
        classNode.visit(
                version,
                modifier,
                className,
                null, // signature
                superClassName,  // Analysis プロセスで， invokespecial のターゲットを特定するために必要
                interfaceName.toArray(new String[0])
        );

        if (this.fileName != null)
            classNode.visitSource(this.fileName, null); // ソースファイル名を設定
    }

    private static int visitClassAccessModifier(@NotNull JALParser.AccModClassContext accessModifier)
    {
        JALParser.AccessLevelContext accessLevel = accessModifier.accessLevel();
        List<JALParser.AccAttrClassContext> attributes = accessModifier.accAttrClass();

        int modifier = EvaluatorCommons.asAccessLevel(accessLevel);
        for (JALParser.AccAttrClassContext attr : attributes)
        {
            if (attr.KWD_ACC_ATTR_FINAL() != null)
                modifier |= EOpcodes.ACC_FINAL;
            else if (attr.KWD_ACC_ATTR_SUPER() != null)
                modifier |= EOpcodes.ACC_SUPER;
            else if (attr.KWD_INTERFACE() != null)
                modifier |= EOpcodes.ACC_INTERFACE;
            else if (attr.KWD_ACC_ATTR_ABSTRACT() != null)
                modifier |= EOpcodes.ACC_ABSTRACT;
            else if (attr.KWD_ACC_ATTR_SYNTHETIC() != null)
                modifier |= EOpcodes.ACC_SYNTHETIC;
            else if (attr.KWD_ACC_ATTR_ANNOTATION() != null)
                modifier |= EOpcodes.ACC_ANNOTATION;
            else if (attr.KWD_ACC_ATTR_ENUM() != null)
                modifier |= EOpcodes.ACC_ENUM;
        }

        return modifier;
    }

    private static int visitFieldAccessModifier(@NotNull JALParser.AccModFieldContext accessModifier)
    {
        JALParser.AccessLevelContext accessLevel = accessModifier.accessLevel();
        List<JALParser.AccAttrFieldContext> attributes = accessModifier.accAttrField();

        int modifier = EvaluatorCommons.asAccessLevel(accessLevel);
        for (JALParser.AccAttrFieldContext attr : attributes)
        {
            if (attr.KWD_ACC_ATTR_FINAL() != null)
                modifier |= EOpcodes.ACC_FINAL;
            else if (attr.KWD_ACC_ATTR_STATIC() != null)
                modifier |= EOpcodes.ACC_STATIC;
            else if (attr.KWD_ACC_ATTR_VOLATILE() != null)
                modifier |= EOpcodes.ACC_VOLATILE;
            else if (attr.KWD_ACC_ATTR_TRANSIENT() != null)
                modifier |= EOpcodes.ACC_TRANSIENT;
            else if (attr.KWD_ACC_ATTR_SYNTHETIC() != null)
                modifier |= EOpcodes.ACC_SYNTHETIC;
            else if (attr.KWD_ACC_ATTR_ENUM() != null)
                modifier |= EOpcodes.ACC_ENUM;
        }

        return modifier;
    }
}
