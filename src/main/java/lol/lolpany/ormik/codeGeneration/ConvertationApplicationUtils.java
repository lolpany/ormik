package lol.lolpany.ormik.codeGeneration;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static lol.lolpany.ormik.codeGeneration.ConvertationGenerationUtils.generateApply;
import static lol.lolpany.ormik.codeGeneration.ConvertationGenerationUtils.generateTo;

public class ConvertationApplicationUtils {

    private static final String SOURCE_ROOT = "G:\\Projects\\ormik\\src\\main\\java\\";
    private static final String JAVA_EXTENSION = ".java";
    private static final String CONVERTATION_UTILS = "ConvertationUtils";
    private static final String CONVERT_METHOD = "lol.lolpany.ormik.codeGeneration.CodeGenerationUtils.convert";
    private static final String CONVERT_METHOD_SHORT = "convert";
    private static final String APPLY_METHOD = "lol.lolpany.ormik.codeGeneration.CodeGenerationUtils.apply";
    private static final String APPLY_METHOD_SHORT = "apply";
    private static final Map<String, Class<?>> PRIMITIVE_TYPES_TO_WRAPPER = new HashMap<String, Class<?>>() {{
        put("int", Integer.class);
        put("long", Long.class);
        put("float", Float.class);
        put("double", Double.class);
        put("boolean", Boolean.class);
    }};

    static void applyConverts(String javaClassPath) throws IOException {
        File convertationUtilsFile = new File(new File(javaClassPath).getParentFile().getPath() + File.separator
                + CONVERTATION_UTILS + JAVA_EXTENSION);
        if (!convertationUtilsFile.exists()) {
            CompilationUnit compilationUnit = JavaParser.parse(FileUtils.readFileToString(new File(javaClassPath),
                    UTF_8));
            writeStringToFile(convertationUtilsFile, "package " + compilationUnit.getPackageDeclaration().get().getName()
                    + ";\n final class ConvertationUtils {}", UTF_8);
        }
        CompilationUnit convertationUtilsCompilationUnit = JavaParser.parse(FileUtils.readFileToString(convertationUtilsFile,
                UTF_8));
        applyVisitor(javaClassPath, convertationUtilsFile, convertationUtilsCompilationUnit,
                new ConvertMethodCallVisitor(convertationUtilsCompilationUnit));
    }

    static void applyApplies(String javaClassPath) throws IOException {
        File convertationUtilsFile = new File(new File(javaClassPath).getParentFile().getPath() + File.separator
                + CONVERTATION_UTILS + JAVA_EXTENSION);
        if (!convertationUtilsFile.exists()) {
            CompilationUnit compilationUnit = JavaParser.parse(FileUtils.readFileToString(new File(javaClassPath),
                    UTF_8));
            writeStringToFile(convertationUtilsFile, "package " + compilationUnit.getPackageDeclaration().get().getName()
                    + ";\n final class ConvertationUtils {}", UTF_8);
        }
        CompilationUnit convertationUtilsCompilationUnit = JavaParser.parse(FileUtils.readFileToString(convertationUtilsFile,
                UTF_8));
        applyVisitor(javaClassPath, convertationUtilsFile, convertationUtilsCompilationUnit,
                new ApplyMethodCallVisitor(convertationUtilsCompilationUnit));
    }


    static void applyVisitor(String javaClassPath, File convertationUtilsFile, CompilationUnit convertationUtilsCompilationUnit,
                             VoidVisitorAdapter<Void> visitorAdapter) throws IOException {
        File javaFile = new File(javaClassPath);
        CompilationUnit compilationUnit = JavaParser.parse(FileUtils.readFileToString(javaFile,
                UTF_8));
        Optional<ClassOrInterfaceDeclaration> classForInsertionOfCallbacks = compilationUnit.getClassByName(javaFile.getName()
                .substring(0, javaFile.getName().lastIndexOf(".")));

        visitorAdapter.visit(compilationUnit, null);
        writeStringToFile(javaFile, compilationUnit.toString(), UTF_8);
        writeStringToFile(convertationUtilsFile, convertationUtilsCompilationUnit.toString(), UTF_8);
    }

    static private boolean isMethodPresent(CompilationUnit compilationUnit, MethodDeclaration toMethod) {
        for (MethodDeclaration methodDeclaration : compilationUnit.getClassByName(CONVERTATION_UTILS).get().getMethods()) {
            if (toMethod.getSignature().equals(methodDeclaration.getSignature())) {
                return true;
            }
        }
        return false;
    }

    private static class ConvertMethodCallVisitor extends VoidVisitorAdapter<Void> {

        private final CompilationUnit compilationUnit;

        private ConvertMethodCallVisitor(CompilationUnit compilationUnit) {
            this.compilationUnit = compilationUnit;
        }

        @Override
        public void visit(MethodCallExpr node, Void arg) {
            if (node.getChildNodes().size() == 2 && node.getChildNodes().get(0).toString().equals(CONVERT_METHOD_SHORT)) {
                try {
                    node.tryAddImportToParentCompilationUnit(CodeGenerationUtils.class);
                    String fromQualifiedName = JavaParserFacade.get(JdbcCallbackApplicationUtils.TYPE_SOLVER)
                            .getType(node.getChildNodes().get(1)).asReferenceType().getQualifiedName();
                    File fromFile = new File(classQualifiedNameToPath(fromQualifiedName));
                    ClassOrInterfaceDeclaration fromClass = JavaParser.parse(fromFile)
                            .getClassByName(classQualifiedNameToName(fromQualifiedName)).get();
                    String toQualifiedName = JavaParserFacade.get(JdbcCallbackApplicationUtils.TYPE_SOLVER)
                            .getType(node.getParentNode().get()).asReferenceType().getQualifiedName();
                    File toFile = new File(classQualifiedNameToPath(toQualifiedName));
                    ClassOrInterfaceDeclaration toClass = JavaParser.parse(toFile)
                            .getClassByName(classQualifiedNameToName(toQualifiedName)).get();
                    UpdateFields classes =
                            updateFieldInClass(toFile, toQualifiedName, fromFile, fromQualifiedName);
                    MethodDeclaration toMethod = generateTo(
                            fromQualifiedName, classes.toClass, classes.toPackageName, toQualifiedName, classes.fromClass);
                    if (!isMethodPresent(compilationUnit, toMethod)) {
                        MethodDeclaration convertationUtilsToMethod = compilationUnit.getClassByName(CONVERTATION_UTILS)
                                .get().addMethod(toMethod.getNameAsString(), Modifier.Keyword.STATIC);
                        convertationUtilsToMethod.setType(toMethod.getType());
                        convertationUtilsToMethod.setParameters(toMethod.getParameters());
                        convertationUtilsToMethod.setBody(toMethod.getBody().get());
                    }
                    NodeList<Expression> methodCallParameters = new NodeList<>();
                    methodCallParameters.add(new NameExpr(node.getArgument(0).toString()));
                    MethodCallExpr convertCall = new MethodCallExpr(new NameExpr(CONVERTATION_UTILS), toMethod.getName());
                    convertCall.setArguments(methodCallParameters);
                    node.replace(convertCall);
                } catch (ClassNotFoundException ignored) {
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            super.visit(node, arg);
        }
    }

    private static class ApplyMethodCallVisitor extends VoidVisitorAdapter<Void> {

        private final CompilationUnit compilationUnit;

        private ApplyMethodCallVisitor(CompilationUnit compilationUnit) {
            this.compilationUnit = compilationUnit;
        }

        @Override
        public void visit(MethodCallExpr node, Void arg) {
            if (node.getChildNodes().size() == 3 && node.getChildNodes().get(0).toString().equals(APPLY_METHOD_SHORT)) {
                try {
                    node.tryAddImportToParentCompilationUnit(CodeGenerationUtils.class);
                    String fromQualifiedName = JavaParserFacade.get(JdbcCallbackApplicationUtils.TYPE_SOLVER)
                            .getType(node.getChildNodes().get(1)).asReferenceType().getQualifiedName();
                    String toQualifiedName = JavaParserFacade.get(JdbcCallbackApplicationUtils.TYPE_SOLVER)
                            .getType(node.getChildNodes().get(2)).asReferenceType().getQualifiedName();
                    File fromFile = new File(classQualifiedNameToPath(fromQualifiedName));
                    File toFile = new File(classQualifiedNameToPath(toQualifiedName));
                    UpdateFields classes =
                            updateFieldInClass(fromFile, fromQualifiedName, toFile, toQualifiedName);
                    MethodDeclaration applyMethod = generateApply(classes.fromPackageName, classes.fromClass, classes.toClass);
                    if (!isMethodPresent(compilationUnit, applyMethod)) {
                        MethodDeclaration convertationUtilsToMethod = compilationUnit.getClassByName(CONVERTATION_UTILS)
                                .get().addMethod(applyMethod.getNameAsString(), Modifier.Keyword.STATIC);
                        convertationUtilsToMethod.setType(applyMethod.getType());
                        convertationUtilsToMethod.setParameters(applyMethod.getParameters());
                        convertationUtilsToMethod.setBody(applyMethod.getBody().get());
                    }
                    NodeList<Expression> methodCallParameters = new NodeList<>();
                    methodCallParameters.add(new NameExpr(node.getArgument(0).toString()));
                    methodCallParameters.add(new NameExpr(node.getArgument(1).toString()));
                    MethodCallExpr convertCall = new MethodCallExpr(new NameExpr(CONVERTATION_UTILS), applyMethod.getName());
                    convertCall.setArguments(methodCallParameters);
                    node.replace(convertCall);
                } catch (ClassNotFoundException ignored) {
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            super.visit(node, arg);
        }
    }

    private static class UpdateFields {
        String fromPackageName;
        ClassOrInterfaceDeclaration fromClass;
        String toPackageName;
        ClassOrInterfaceDeclaration toClass;

        public UpdateFields(String fromPackageName, ClassOrInterfaceDeclaration fromClass, String toPackageName, ClassOrInterfaceDeclaration toClass) {
            this.fromPackageName = fromPackageName;
            this.fromClass = fromClass;
            this.toPackageName = toPackageName;
            this.toClass = toClass;
        }
    }

    private static UpdateFields updateFieldInClass(
            File fromFile, String fromQualifiedName, File toFile, String toQualifiedName)
            throws IOException, ClassNotFoundException {

        CompilationUnit fromClassCompilationUnit = JavaParser.parse(fromFile);
        CompilationUnit outClassCompilationUnit = JavaParser.parse(toFile);

        for (ImportDeclaration importDeclaration: fromClassCompilationUnit.getImports()) {
            outClassCompilationUnit.addImport(importDeclaration);
        }

        ClassOrInterfaceDeclaration fromClass = fromClassCompilationUnit.getClassByName(classQualifiedNameToName(fromQualifiedName)).get();

        ClassOrInterfaceDeclaration toClass = outClassCompilationUnit.getClassByName(classQualifiedNameToName(toQualifiedName)).get();

        List<String> toFields = new ArrayList<>();
        for (com.github.javaparser.ast.body.FieldDeclaration field : toClass.getFields()) {
            toFields.add(field.getVariables().get(0).toString());
        }
        for (com.github.javaparser.ast.body.FieldDeclaration field : fromClass.getFields()) {
            if (!toFields.contains(field.getVariables().get(0).toString())) {
//                if (field.getCommonType() instanceof PrimitiveType) {
//                    toClass.addField(PRIMITIVE_TYPES_TO_WRAPPER.get(field.getCommonType().asString()),
//                            field.getVariables().get(0).toString());
//                } else {
                    toClass.addField(field.getCommonType(), field.getVariables().get(0).toString());
//                }
            }
        }
        writeStringToFile(toFile, outClassCompilationUnit.toString(), UTF_8);
        return new UpdateFields(fromQualifiedName.substring(0, fromQualifiedName.lastIndexOf(".")), fromClass,
                toQualifiedName.substring(0, toQualifiedName.lastIndexOf(".")),toClass);
    }

    private static String classQualifiedNameToPath(String classQualifiedName) {
        return SOURCE_ROOT + classQualifiedName.replaceAll("\\.", "/") + JAVA_EXTENSION;
    }

    private static String classQualifiedNameToName(String classQualifiedName) {
        return classQualifiedName.substring(classQualifiedName.lastIndexOf(".") + 1, classQualifiedName.length());
    }

}
