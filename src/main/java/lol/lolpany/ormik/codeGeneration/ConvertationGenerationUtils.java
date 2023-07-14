package lol.lolpany.ormik.codeGeneration;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.VoidType;

import java.util.EnumSet;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;

public class ConvertationGenerationUtils {

    private static final String FROM_PARAMETER_NAME = "from";
    private static final String TO_PARAMETER_NAME = "to";

    static <F, T> MethodDeclaration generateTo(String fromQualifiedName, ClassOrInterfaceDeclaration from, String toPackageName,
                                               String toQualifiedName, ClassOrInterfaceDeclaration to) {
        MethodDeclaration result = new MethodDeclaration(new NodeList<>(Modifier.staticModifier()),
                new ClassOrInterfaceType(null, toQualifiedName),
                "to" + generateMethodPrefix(toPackageName, to));
        result.addParameter(new Parameter(new ClassOrInterfaceType(null, fromQualifiedName),
                FROM_PARAMETER_NAME));

        NodeList<Statement> statements = new NodeList<>();
        NodeList<Expression> constructorArguments = new NodeList<>();
        for (FieldDeclaration field : to.getFields()) {
            constructorArguments.add(new FieldAccessExpr(new NameExpr(FROM_PARAMETER_NAME),
                    field.getVariables().get(0).getNameAsString()));
        }
        ReturnStmt returnStatement = new ReturnStmt(new ObjectCreationExpr(null,
                new ClassOrInterfaceType(null, toQualifiedName), constructorArguments));
        statements.add(returnStatement);
        result.setBody(new BlockStmt().setStatements(statements));
        return result;
    }

    static <F, T> MethodDeclaration generateApply(String fromPackageName, ClassOrInterfaceDeclaration from,
                                                  ClassOrInterfaceDeclaration to) throws ClassNotFoundException {
        MethodDeclaration result = new MethodDeclaration(new NodeList<>(Modifier.staticModifier()),
                new VoidType(),
                "apply" + generateMethodPrefix(fromPackageName, from));
        result.addParameter(new Parameter(new ClassOrInterfaceType(null, fromPackageName + "." + from.getNameAsString()),
                FROM_PARAMETER_NAME));
        result.addParameter(new Parameter(new ClassOrInterfaceType(null, to.getNameAsString()), TO_PARAMETER_NAME));

        NodeList<Statement> statements = new NodeList<>();
        for (FieldDeclaration field : from.getFields()) {
            Statement statement = new ExpressionStmt(new AssignExpr(new FieldAccessExpr(new NameExpr(TO_PARAMETER_NAME),
                    field.getVariables().get(0).getNameAsString()),
                    new FieldAccessExpr(new NameExpr(FROM_PARAMETER_NAME), field.getVariables().get(0).getNameAsString()),
                    AssignExpr.Operator.ASSIGN));
            if (!(field.getCommonType() instanceof PrimitiveType)) {
                statements.add(new IfStmt(new BinaryExpr(new FieldAccessExpr(new NameExpr(FROM_PARAMETER_NAME),
                        field.getVariables().get(0).getNameAsString()), new NullLiteralExpr(), BinaryExpr.Operator.NOT_EQUALS),
                        statement, null));
            } else {
                statements.add(statement);
            }
        }
        result.setBody(new BlockStmt().setStatements(statements));
        return result;
    }

    private static <T> String generateMethodPrefix(String packageName, ClassOrInterfaceDeclaration beanClass) {
        String[] packageNameParts = packageName.split("\\.");
        return LOWER_CAMEL.to(UPPER_CAMEL, packageNameParts[packageNameParts.length - 1] + beanClass.getNameAsString());
    }

}
