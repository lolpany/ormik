package lol.lolpany.ormik.codeGeneration;

import com.squareup.javapoet.*;
import com.squareup.javapoet.MethodSpec.Builder;
import lol.lolpany.ormik.jdbc.IPreparedStatementJdbcCallback;
import lol.lolpany.ormik.persistence.AbstractPreparedStatementInnerJdbcCallback;
import lol.lolpany.ormik.reinsertableBeans.EnumUtils;
import lol.lolpany.ormik.reinsertableBeans.IdEnum;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.lang.model.element.Modifier;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

import static com.google.common.base.CaseFormat.*;
import static lol.lolpany.ormik.codeGeneration.SelectType.MULTIPLE;
import static lol.lolpany.ormik.codeGeneration.SelectType.ONE;
import static lol.lolpany.ormik.jdbc.JdbcUtils.execute;
import static lol.lolpany.ormik.regression.EnvironmentUtils.getScheme;
import static org.apache.commons.lang3.ClassUtils.isPrimitiveOrWrapper;

/**
 * Generates jdbc callbacks for selection/insertion/update of beans by Bean.class.
 */
@SuppressWarnings("unused")
public class JdbcCallbackGenerationUtils {

    private static final String JDBC_CONNECTION = "jdbc:oracle:thin:@10.0.0.99:1521:test";
    private static final String DB_PASSWORD = "SYS";

    private static final String DB_FIELD_NAME_PREFIX = "";
    private static final String CREATE_INNER_PREPARED_STATEMENT = "createInnerPreparedStatement";
    private static final String EXECUTE = "execute";
    private static final String CONNECTION = "c";
    static final String JDBC_CALLBACK = "JdbcCallback";

    static <T> String identifyTableName(int envNumber, Class<T> bean) throws SQLException {
        for (String tableName : readTableNames(envNumber)) {
            if (bean.getSimpleName().contains(UPPER_UNDERSCORE.to(UPPER_CAMEL, tableName))) {
                return tableName;
            }
        }
        return null;
    }

    private static List<String> readTableNames(int envNumber) throws SQLException {
        return execute(DriverManager.getConnection(JDBC_CONNECTION, getScheme(envNumber), DB_PASSWORD),
                new IPreparedStatementJdbcCallback<List<String>>() {
                    @Override
                    public PreparedStatement createPreparedStatement(Connection c) throws SQLException {
                        return c.prepareStatement("SELECT table_name FROM user_tables");
                    }

                    @Override
                    public List<String> doInPreparedStatement(PreparedStatement ps) throws SQLException {
                        List<String> result = new ArrayList<>();
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                result.add(rs.getString(1));
                            }
                        }
                        return result;
                    }
                });
    }

    /**
     * @param <T>            - returnedType class type parameter
     * @param tableName      - select from what table
     * @param packageName    - package of generated JdbcCallback
     * @param beanName       - for JdbcCallback name
     * @param returnedType   - returnedType from which to generate JdbcCallback
     * @param tableFieldName - for returnedType.isPrimitive() or IdEnum - field name from which to select
     * @param whereColumns   - list of where columns
     * @param selectType     - select type: check if exists, single row, multiple rows     @return - string containing JdbcCallback - ready to be pasted
     */
    static <T> String generateSelect(String tableName, String packageName, String beanName,
                                     Class<T> returnedType,
                                     String tableFieldName, LinkedHashMap<String, Class<?>> tableColumns,
                                     List<String> whereColumns, String whereAddition, SelectType selectType) {

        TypeSpec selectJdbcCallback = TypeSpec.classBuilder(beanToJdbcCallbackName(beanName, "Select"))
                .addModifiers(Modifier.FINAL)
                .superclass(AbstractPreparedStatementInnerJdbcCallback.class)
                .addMethod(generateCreateInnerPreparedStatement(generateSelectQuery(tableName, returnedType,
                        tableFieldName, whereColumns, whereAddition, selectType)))
                .addMethod(generateExecuteSelect(beanName, returnedType, tableColumns, whereColumns, selectType))
                .build();

        JavaFile javaFile = JavaFile.builder(packageName, selectJdbcCallback)
                .build();

        return javaFile.toString();
    }

    static <T> String generateSelect(String tableName, String packageName, Class<T> bean, List<String> whereColumns,
                                     String whereAddition, SelectType selectType) {

        LinkedHashMap<String, Class<?>> tableColumns = new LinkedHashMap<>();
        for (Field field : bean.getDeclaredFields()) {
            tableColumns.put(field.getName(), field.getType());
        }

        return generateSelect(tableName, packageName, bean.getSimpleName(), bean, null, tableColumns, whereColumns,
                whereAddition, selectType);
    }


    private static <T> String beanToJdbcCallbackName(String beanName, String sqlOperation) {
        return beanName + sqlOperation + JDBC_CALLBACK;
    }

    private static <T> MethodSpec generateCreateInnerPreparedStatement(String query) {

        return MethodSpec.methodBuilder(CREATE_INNER_PREPARED_STATEMENT)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(PreparedStatement.class)
                .addParameter(Connection.class, CONNECTION)
                .addException(SQLException.class)
                .addStatement("return " + CONNECTION + ".prepareStatement($N)", query)
                .build();
    }

    private static <T> String generateSelectQuery(String tableName, Class<T> bean, String fieldName,
                                                  List<String> whereColumns, String whereAddition,
                                                  SelectType selectType) {
        StringBuilder query = new StringBuilder("\" SELECT " + " \"\n + \"");

        int i = 1;
        switch (selectType) {
            case EXISTS:
                query.append(" 1 \"\n + ");
                break;

            case ONE:
            case MULTIPLE:
                boolean isIdEnum = false;
                try {
                    bean.asSubclass(IdEnum.class);
                    isIdEnum = true;
                } catch (ClassCastException e) {
                    //
                }
                if (isPrimitiveOrWrapper(bean) || bean == String.class || isIdEnum) {
                    query.append(" ").append(fieldName).append(" \"\t\t\t// ").append(i++).append("\n + ");
                } else {
                    // fields

                    for (Field field : bean.getDeclaredFields()) {
                        String dbFieldName = beanToDbFieldName(field.getName());
                        if (!whereColumns.contains(dbFieldName)) {
                            query.append(" ").append(dbFieldName).append(" \"\t\t\t// ").append(i++).append("\n + \",");
                        }
                    }

                    query.setLength(query.length() - 2);
                }
                break;
        }

        query.append("\" FROM " + tableName).append("\"\n + ");

        query.append("\"\tWHERE \"\n + ");
        query.append("\"\t\t").append(whereColumns.get(0)).append(" = ? \" ").append(whereColumns.size() > 1 ? "+" : "")
                .append(" \t\t\t// ").append(1).append(" \n");
        if (whereColumns.size() > 1) {
            for (int j = 1; j < whereColumns.size() - 1; j++) {
                query.append("\" AND ").append(whereColumns.get(j)).append(" = ? \" + \t\t\t// ").append(j + 1)
                        .append("  \n");
            }
            query.append("\" AND ").append(whereColumns.get(whereColumns.size() - 1)).append(" = ? \"  \t\t\t// ")
                    .append(whereColumns.size()).append("  \n");
        }
        query.append("+ \"" + whereAddition + "\"\n");

        return query.toString();
    }

    private static <T> MethodSpec generateExecuteSelect(String beanName, Class<T> returnType,
                                                        LinkedHashMap<String, Class<?>> tableColumns,
                                                        List<String> whereColumns,
                                                        SelectType selectType) {
        Builder result = MethodSpec.methodBuilder(EXECUTE)
                .returns(selectType == MULTIPLE ? List.class : selectType == ONE ? returnType : boolean.class)
                .addException(SQLException.class);

        for (Map.Entry<String, Class<?>> field : tableColumns.entrySet()) {
            if (whereColumns != null && whereColumns.contains(beanToDbFieldName(field.getKey()))) {
                result.addParameter(field.getValue(), field.getKey());
            }
        }

        int i = 1;
        for (Map.Entry<String, Class<?>> field : tableColumns.entrySet()) {
            if (whereColumns != null && whereColumns.contains(beanToDbFieldName(field.getKey()))) {
                result.addStatement(generatePreparedStatementSetter(null, field.getKey(), field.getValue()), i++);
            }
        }

        if (selectType == SelectType.MULTIPLE) {
            result.addStatement("List<$T> result = new $T<>()", returnType, ArrayList.class);
        }
        CodeBlock.Builder codeBlockBuilder =
                CodeBlock.builder().beginControlFlow("try ($T rs = ps.executeQuery())", ResultSet.class);

        switch (selectType) {
            case EXISTS:
                codeBlockBuilder.addStatement("return rs.next()");
                codeBlockBuilder.endControlFlow();
                break;
            case ONE:
                codeBlockBuilder.beginControlFlow("if (rs.next())");
                if (isPrimitiveOrWrapper(returnType) || returnType == String.class) {
                    codeBlockBuilder.addStatement("return " + generateResultSetGetter(returnType, 1).getLeft());
                } else {
                    boolean isIdEnum = false;
                    try {
                        returnType.asSubclass(IdEnum.class);
                        isIdEnum = true;
                    } catch (ClassCastException e) {
                        // ignore
                    }
                    if (isIdEnum) {
                        codeBlockBuilder.addStatement(
                                "return " + generateResultSetGetter(returnType, 1).getLeft(), EnumUtils.class,
                                returnType);
                    } else {
                        Pair<String, List<Class<?>>> constructor = generateSelectConstructorCall(beanName, tableColumns, whereColumns);
                        codeBlockBuilder
                                .addStatement("return " + constructor.getLeft(), constructor.getRight().toArray());
                    }
                }
                codeBlockBuilder.endControlFlow()
                        .endControlFlow();
                break;
            case MULTIPLE:
                codeBlockBuilder.beginControlFlow("while (rs.next())");
                Pair<String, List<Class<?>>> constructor = generateSelectConstructorCall(beanName, tableColumns, whereColumns);
                codeBlockBuilder
                        .addStatement("result.add(" + constructor.getLeft() + ")", constructor.getRight().toArray());
                codeBlockBuilder.endControlFlow()
                        .endControlFlow();
                break;
        }

        if (selectType == ONE || selectType == MULTIPLE) {
            codeBlockBuilder.addStatement("return " + (selectType == SelectType.MULTIPLE ? "result" : "null"));
        }
        result.addCode(codeBlockBuilder.build());

        return result.build();
    }

    static <T> Pair<String, List<Class<?>>> generateSelectConstructorCall(String beanName,
                                                                          LinkedHashMap<String, Class<?>> tableColumns,
                                                                          List<String> whereColumns) {
        StringBuilder template = new StringBuilder("new " + beanName + "(");
        List<Class<?>> types = new ArrayList<>();
        int i = 1;
        for (Map.Entry<String, Class<?>> field : tableColumns.entrySet()) {
            if (!whereColumns.contains(beanToDbFieldName(field.getKey()))) {
                Pair<String, Class<?>> getter = generateResultSetGetter(field.getValue(), i++);
                template.append(getter.getLeft() + ", ");
                if (getter.getRight() != null) {
                    types.add(EnumUtils.class);
                    types.add(getter.getRight());
                }
            } else {
                template.append(beanToDbFieldName(field.getKey() + ", "));
            }
        }
        template.setLength(template.length() - 2);
        template.append(")");
        return new ImmutablePair<>(template.toString(), types);
    }

    private static <T> Pair<String, Class<?>> generateResultSetGetter(Class<T> fieldType, int i) {
        if (JDBC_TYPE_BY_CLASS.containsKey(fieldType.getSimpleName())) {
            return new ImmutablePair<>(
                    "rs.get" + JDBC_TYPE_BY_CLASS.get(fieldType.getSimpleName()) + "(" + i + ")"
                            + (fieldType == boolean.class || fieldType == Boolean.class ? " == 1 " : ""),
                    null);
        } else {
            try {
                fieldType.asSubclass(IdEnum.class);
                return new ImmutablePair<>("$T.getById($T.class, rs.getInt" + "(" + i + "))", fieldType);
            } catch (Exception e) {
                // continue
            }
        }
        return null;
    }

    static <T> String generateInsert(String tableName, String packageName, Class<T> bean, String sequenceColumnName,
                                     String sequenceName) {

        TypeSpec updateJdbcCallback = TypeSpec.classBuilder(beanToJdbcCallbackName(tableName, "Insert"))
                .addModifiers(Modifier.FINAL)
                .superclass(AbstractPreparedStatementInnerJdbcCallback.class)
                .addMethod(generateCreateInnerPreparedStatement(
                        generateInsertQuery(tableName, bean, sequenceColumnName,
                                sequenceName)))
                .addMethod(generateExecuteUpdate(bean, null))
                .build();

        JavaFile javaFile = JavaFile.builder(packageName, updateJdbcCallback)
                .build();

        return javaFile.toString();
    }

    private static <T> String generateInsertQuery(String tableName, Class<T> bean, String sequenceColumnName,
                                                  String sequenceName) {
        StringBuilder query = new StringBuilder("\" INSERT INTO " + tableName + " ( \"\n + \"");

        if (sequenceColumnName != null) {
            query.append(sequenceColumnName + "\" \n + \",");
        }

        // fields
        int i = 1;
        for (Field field : bean.getDeclaredFields()) {
            if (!beanToDbFieldName(field.getName()).equals(sequenceColumnName)) {
                query.append(" ").append(beanToDbFieldName(field.getName())).append("\"\t\t\t// <").append(i++)
                        .append("\n + \",");
            }
        }

        query.setLength(query.length() - 2);

        query.append("\") VALUES (");

        if (sequenceColumnName != null) {
            query.append(sequenceName + ".nextval").append(",");
        }

        for (Field field : bean.getDeclaredFields()) {
            if (!beanToDbFieldName(field.getName()).equals(sequenceColumnName)) {
                query.append("?,");
            }
        }

        query.setLength(query.length() - 1);

        query.append(")\"");

        return query.toString();
    }

    static <T> String generateUpdate(String tableName, String packageName, Class<T> bean, List<String> whereColumns) {

        TypeSpec updateJdbcCallback = TypeSpec.classBuilder(beanToJdbcCallbackName(
                        LOWER_UNDERSCORE.to(UPPER_CAMEL, tableName), "Update"))
                .addModifiers(Modifier.FINAL)
                .superclass(AbstractPreparedStatementInnerJdbcCallback.class)
                .addMethod(
                        generateCreateInnerPreparedStatement(generateUpdateQuery(tableName, bean, whereColumns)))
                .addMethod(generateExecuteUpdate(bean, whereColumns))
                .build();

        JavaFile javaFile = JavaFile.builder(packageName, updateJdbcCallback)
                .build();

        return javaFile.toString();
    }

    private static <T> String generateUpdateQuery(String tableName, Class<T> bean, List<String> whereColumns) {
        StringBuilder query = new StringBuilder("\" UPDATE " + tableName + " SET \"\n + \"");

        // fields
        int i = 1;
        for (Field field : bean.getDeclaredFields()) {
            String dbFieldName = beanToDbFieldName(field.getName());
            if (!whereColumns.contains(dbFieldName)) {
                query.append(" ").append(dbFieldName).append(" = ? \"\t\t\t// <").append(i++).append("\n + \",");
            }
        }

        query.setLength(query.length() - 2);

        // where clause
        query.append("\"\tWHERE \"\n + ");
        query.append("\"\t\t").append(whereColumns.get(0)).append(" = ? \" ").append(whereColumns.size() > 1 ? "+" : "")
                .append(" \t\t\t// ").append(i++).append(" \n");

        if (whereColumns.size() > 1) {
            for (int j = 1; j < whereColumns.size() - 1; j++) {
                query.append("\" AND ").append(whereColumns.get(j)).append(" = ? \" + \t\t\t// ").append(i++)
                        .append("  \n");
            }
            query.append("\" AND ").append(whereColumns.get(whereColumns.size() - 1)).append(" = ? \"  \t\t\t// ")
                    .append(i).append("  \n");
        }
        return query.toString();
    }

    private static <T> MethodSpec generateExecuteUpdate(Class<T> bean, List<String> whereColumns) {
        Builder result = MethodSpec.methodBuilder(EXECUTE)
                .returns(TypeName.VOID)
                .addException(SQLException.class)
                .addParameter(bean, UPPER_CAMEL.to(LOWER_CAMEL, bean.getSimpleName()));
        int i = 1;
        for (Field field : bean.getDeclaredFields()) {
            if (whereColumns == null || !whereColumns.contains(beanToDbFieldName(field.getName()))) {
                result.addStatement(generatePreparedStatementSetter(bean, field.getName(), field.getType()), i++);
            }
        }
        for (Field field : bean.getDeclaredFields()) {
            if (whereColumns != null && whereColumns.contains(beanToDbFieldName(field.getName()))) {
                result.addStatement(generatePreparedStatementSetter(bean, field.getName(), field.getType()), i++);
            }
        }
        result.addStatement("ps.executeUpdate()");
        return result.build();
    }

    private static <T> String generatePreparedStatementSetter(Class<T> bean, String fieldName, Class<?> fieldType) {
        String beanPart = "";
        if (bean != null) {
            beanPart = UPPER_CAMEL.to(LOWER_CAMEL, bean.getSimpleName()) + ".";
        }
        if (JDBC_TYPE_BY_CLASS.containsKey(fieldType.getSimpleName())) {
            return "ps.set" + JDBC_TYPE_BY_CLASS.get(fieldType.getSimpleName()) + "($L, " + beanPart + fieldName
                    + (fieldType == boolean.class || fieldType == Boolean.class ? " ? 1 : 2" : "") + ")";
        } else {
            try {
                fieldType.asSubclass(IdEnum.class);
                return "ps.setInt" + "($L, " + beanPart + fieldName + ".getId())";
            } catch (Exception e) {
                // continue
            }
        }
        return "";
    }

    private static final Map<String, String> JDBC_TYPE_BY_CLASS = new HashMap<String, String>() {{
        put("long", "Long");
        put("int", "Int");
        put("Integer", "Int");
        put("boolean", "Int");
        put("Boolean", "Int");
        put("Long", "Long");
        put("Date", "Date");
        put("BigDecimal", "BigDecimal");
        put("String", "String");
    }};

    private static String beanToDbFieldName(String beanFieldName) {
        String result;
        if (beanFieldName.startsWith(DB_FIELD_NAME_PREFIX)) {
            return LOWER_CAMEL.to(LOWER_UNDERSCORE, beanFieldName);
        } else {
            return DB_FIELD_NAME_PREFIX + LOWER_CAMEL.to(LOWER_UNDERSCORE, beanFieldName);
        }
    }

    static String dbToBeanFieldName(String dbFieldName) {
        String result = "";
        if (dbFieldName.startsWith(DB_FIELD_NAME_PREFIX)) {
            result = dbFieldName.substring(2);
        }
        return result;
    }

}
