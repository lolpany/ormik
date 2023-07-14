package lol.lolpany.ormik.codeGeneration;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.metamodel.SimpleNameMetaModel;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.typesystem.ReferenceTypeImpl;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.CaseFormat.*;
import static java.util.Arrays.asList;
import static lol.lolpany.ormik.codeGeneration.CodeGenerationUtils.*;
import static lol.lolpany.ormik.codeGeneration.CodeGenerationUtils.SqlOperation.*;
import static lol.lolpany.ormik.codeGeneration.CodeGenerationUtils.WHERE_COLUMNS_SEPARATOR;
import static lol.lolpany.ormik.codeGeneration.JdbcCallbackGenerationUtils.*;

/**
 * Gnerates and inserts jdbcCallback in place of lol.lolpany.ormik.codeGeneration.JdbcCallbackGenerationUtils.callback(...) call.
 */
@SuppressWarnings("unused")
public class JdbcCallbackApplicationUtils {

    static final CombinedTypeSolver TYPE_SOLVER = new CombinedTypeSolver(
            new JavaParserTypeSolver(new File("G:\\Projects\\ormik\\src\\main\\java\\")),
            new ReflectionTypeSolver());

    static void applyJdbcCallbacks(String path, int envNumber) throws IOException, SQLException {
        List<Pair<String, String>> jdbcCallbacks = new ArrayList<>();
        File javaClassFile = new File(path);
        CompilationUnit compilationUnit = JavaParser.parse(FileUtils.readFileToString(javaClassFile,
                StandardCharsets.UTF_8));
        Optional<ClassOrInterfaceDeclaration> classForInsertionOfCallbacks = compilationUnit.getClassByName(javaClassFile.getName()
                .substring(0, javaClassFile.getName().lastIndexOf(".")));
        for (MethodDeclaration method : classForInsertionOfCallbacks.get().getMethods()) {
            for (Statement statement : method.getBody()
                    .get().getStatements()) {
                List<Node> firstNodeChildNodes = statement.getChildNodes().get(0).getChildNodes();
                if (!firstNodeChildNodes.isEmpty()) {
                    if ((firstNodeChildNodes.size() == 4
                            && firstNodeChildNodes.get(0).getMetaModel() instanceof SimpleNameMetaModel
                            && "callback".equals(firstNodeChildNodes.get(0).toString()))
                            || (firstNodeChildNodes.size() == 5
                            && firstNodeChildNodes.get(0).getMetaModel() instanceof SimpleNameMetaModel
                            && "insert".equals(firstNodeChildNodes.get(0).toString()))) {
                        try {
                            Pair<ExpressionStmt, Pair<String, String>> jdbcCallback = null;
                            if ("insert".equals(firstNodeChildNodes.get(0).toString())) {
                                jdbcCallback = handleInsert(firstNodeChildNodes,
                                        compilationUnit.getPackageDeclaration().get().getName().toString(), envNumber,
                                        removeQuotes(firstNodeChildNodes.get(4).toString()));
                            } else {
                                SqlOperation sqlOperation = SqlOperation.valueOf(firstNodeChildNodes.get(1).toString());
                                switch (sqlOperation) {
                                    case SELECT:
                                        jdbcCallback = handleSelect(firstNodeChildNodes,
                                                compilationUnit.getPackageDeclaration().get().getName().toString(), envNumber);
                                        break;
                                    case UPDATE:
                                        jdbcCallback = handleUpdate(firstNodeChildNodes,
                                                compilationUnit.getPackageDeclaration().get().getName().toString(), envNumber);
                                        break;
                                }
                            }
                            jdbcCallbacks.add(jdbcCallback.getRight());
                            statement.replace(jdbcCallback.getLeft());
                        } catch (ClassNotFoundException e) {

                        }
                    }
                }
            }
        }


        for (Pair<String, String> jdbcCallback : jdbcCallbacks) {
            FileUtils.writeStringToFile(new File(javaClassFile.getParentFile() + File.separator + jdbcCallback.getLeft() +
                            ".java"),
                    jdbcCallback.getRight(), StandardCharsets.UTF_8);
        }

        for (Pair<String, String> jdbcCallback : jdbcCallbacks) {
            classForInsertionOfCallbacks.get().addPrivateField(jdbcCallback.getLeft(), UPPER_CAMEL.to(LOWER_CAMEL, jdbcCallback.getLeft()));
        }

        MethodDeclaration getInnerJdbcCallbacksMethod = classForInsertionOfCallbacks.get()
                .getMethodsByName("getInnerJdbcCallbacks").get(0);
        for (Statement statement : getInnerJdbcCallbacksMethod.getBody().get().getStatements()) {
            if (statement instanceof ReturnStmt) {
                ArrayInitializerExpr callbacksInitializationsExpression =
                        ((ArrayInitializerExpr) ((ReturnStmt) statement).getExpression().get().getChildNodes().get(2));
                NodeList callbacksInitializations = new NodeList<>(callbacksInitializationsExpression.getValues());
                for (Pair<String, String> jdbcCallback : jdbcCallbacks) {
                    callbacksInitializations.add(new AssignExpr(new NameExpr(UPPER_CAMEL.to(LOWER_CAMEL, jdbcCallback.getLeft())),
                            new ObjectCreationExpr(null, new ClassOrInterfaceType(null, jdbcCallback.getLeft()),
                                    new NodeList<>()),
                            AssignExpr.Operator.ASSIGN));
                }
                callbacksInitializationsExpression.setValues(callbacksInitializations);
                break;
            }
        }


        FileUtils.writeStringToFile(javaClassFile, compilationUnit.toString(), StandardCharsets.UTF_8);
    }

    private static Pair<ExpressionStmt, Pair<String, String>> handleSelect(List<Node> firstNodeChildNodes,
                                                                           String packageName, int envNumber)
            throws ClassNotFoundException, IOException, SQLException {
        ResolvedType typeOfTheNode = JavaParserFacade.get(
                TYPE_SOLVER).getType(firstNodeChildNodes.get(2));
        Class<?> beanClass = Class.forName(((ReferenceTypeImpl) typeOfTheNode).getTypeDeclaration()
                .getQualifiedName());
        String whereColumnsAsString = firstNodeChildNodes.get(3).toString().substring(1,
                firstNodeChildNodes.get(3).toString().length() - 1);
        List<String> whereColumns = asList(whereColumnsAsString.split(WHERE_COLUMNS_SEPARATOR));
        String tableName = identifyTableName(envNumber, beanClass);
        Pair<String, String> jdbcCallback =
                new ImmutablePair<>(UPPER_UNDERSCORE.to(UPPER_CAMEL, tableName) + UPPER_UNDERSCORE.to(UPPER_CAMEL, SELECT.name())
                        + JDBC_CALLBACK,
                        generateSelect(tableName, packageName, beanClass, whereColumns, "", SelectType.ONE));

        NodeList<Expression> callbackCallArguments = new NodeList<>();
        for (String whereColumn : whereColumns) {
            callbackCallArguments.add(new NameExpr(dbToBeanFieldName(whereColumn)));
        }
        return new ImmutablePair<>(new ExpressionStmt(new MethodCallExpr(
                new NameExpr(UPPER_CAMEL.to(LOWER_CAMEL, jdbcCallback.getLeft())), "execute",
                callbackCallArguments)), jdbcCallback);
    }

    private static Pair<ExpressionStmt, Pair<String, String>> handleUpdate(List<Node> firstNodeChildNodes,
                                                                           String packageName, int envNumber)
            throws ClassNotFoundException, SQLException {
        ResolvedType typeOfTheNode = JavaParserFacade.get(TYPE_SOLVER)
                .getType(firstNodeChildNodes.get(2));
        Class<?> beanClass = Class.forName(((ReferenceTypeImpl) typeOfTheNode).getTypeDeclaration()
                .getQualifiedName());
        String whereColumnsAsString = removeQuotes(firstNodeChildNodes.get(3).toString());
        List<String> whereColumns = asList(whereColumnsAsString.split(WHERE_COLUMNS_SEPARATOR));
        String tableName = identifyTableName(envNumber, beanClass);
        Pair<String, String> jdbcCallback =
                new ImmutablePair<>(UPPER_UNDERSCORE.to(UPPER_CAMEL, tableName) + UPPER_UNDERSCORE.to(UPPER_CAMEL, UPDATE.name())
                        + JDBC_CALLBACK,
                        generateUpdate(tableName, packageName, beanClass, whereColumns));

        NodeList<Expression> nodeListArguments = new NodeList<>();
        for (String whereColumn : whereColumns) {
            nodeListArguments.add(new NameExpr(firstNodeChildNodes.get(2).toString()));
        }
        return new ImmutablePair<>(new ExpressionStmt(new MethodCallExpr(
                new NameExpr(UPPER_CAMEL.to(LOWER_CAMEL, jdbcCallback.getLeft())), "execute",
                nodeListArguments)), jdbcCallback);
    }

    private static Pair<ExpressionStmt, Pair<String, String>> handleInsert(List<Node> firstNodeChildNodes,
                                                                           String packageName,
                                                                           int envNumber, String tableName)
            throws ClassNotFoundException, SQLException {
        ResolvedType typeOfTheNode = JavaParserFacade.get(
                TYPE_SOLVER).getType(firstNodeChildNodes.get(1));
        Class<?> beanClass = Class.forName(((ReferenceTypeImpl) typeOfTheNode).getTypeDeclaration()
                .getQualifiedName());
        String table = tableName == null ? identifyTableName(envNumber, beanClass) : tableName;
        Pair<String, String> jdbcCallback =
                new ImmutablePair<>(UPPER_UNDERSCORE.to(UPPER_CAMEL, table) + UPPER_UNDERSCORE.to(UPPER_CAMEL, INSERT.name())
                        + JDBC_CALLBACK,
                        generateInsert(tableName, packageName, beanClass,
                                removeQuotes(firstNodeChildNodes.get(2).toString()),
                                removeQuotes(firstNodeChildNodes.get(3).toString())));

        NodeList<Expression> nodeListArguments = new NodeList<>();
        nodeListArguments.add(new NameExpr(firstNodeChildNodes.get(1).toString()));
        return new ImmutablePair<>(new ExpressionStmt(new MethodCallExpr(
                new NameExpr(UPPER_CAMEL.to(LOWER_CAMEL, jdbcCallback.getLeft())), "execute",
                nodeListArguments)), jdbcCallback);
    }

    private static String removeQuotes(String input) {
        return input.substring(0, input.length() - 1).substring(1);
    }

}
