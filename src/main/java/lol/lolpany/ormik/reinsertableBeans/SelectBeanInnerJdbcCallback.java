package lol.lolpany.ormik.reinsertableBeans;

import org.apache.commons.lang3.tuple.Pair;
import org.intellij.lang.annotations.Language;
import lol.lolpany.ormik.persistence.AbstractPreparedStatementInnerJdbcCallback;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static lol.lolpany.ormik.reinsertableBeans.EnumUtils.getById;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static lol.lolpany.ormik.reinsertableBeans.ReinsertableBeanUtils.*;

/**
 * Категорически запрещено использовать методы @java.sql.ResultSet.getXXX(String)@: идентифицировать колонки необходимо
 * только по номеру.
 * В Oracle-овом JDBC-драйвере эти методы выполняют линейный поиск в списке строк при каждом вызове
 * (см. oracle.jdbc.driver.OracleStatement#getColumnIndex(java.lang.String)), причем используется регистронезависимое
 * сравнение строк(!), т.е. если, например, запрос выбрал 1000 строк из таблицы acc_m, в которой 136 столбцов, то мы
 * выполним линейный поиск в массиве из 136 строк 136 000 раз, т.е. примерно 136 / 2 * 136 000 = 9 248 000
 * регистронезависимых сравнений строк: это, как правило, будет работать дольше, чем выполнялся сам запрос.
 */

public final class SelectBeanInnerJdbcCallback<T extends ISelectableBean>
        extends AbstractPreparedStatementInnerJdbcCallback {

    private final ReinsertableBeanService reinsertableBeanService;
    private List<Pair<TableColumnMetaData, Field>> columnsAndFilelds;
    private final String tableName;
    private final List<String> keyColumns;
    private final String whereAddition;
    private final Class<T> beanClass;
    private int parameterCount;

    public SelectBeanInnerJdbcCallback(ReinsertableBeanService reinsertableBeanService, Class<T> beanClass,
                                       String tableName, List<String> keyColumns, String fullQuery) {
        if (beanClass == null || isEmpty(tableName)) {
            throw new IllegalArgumentException();
        }

        this.tableName = tableName;
        this.keyColumns = keyColumns;
        this.whereAddition = extractWhereClause(fullQuery);
        this.beanClass = beanClass;
        this.reinsertableBeanService = reinsertableBeanService;
    }

    public SelectBeanInnerJdbcCallback(Class<T> beanClass, String tableName, List<String> keyColumns,
                                       @Language("sql") String fullQuery) {
        this(ReinsertableBeanService.getInstance(), beanClass, tableName, keyColumns, fullQuery);
    }

    /**
     * @deprecated - use {@link SelectBeanInnerJdbcCallback#SelectBeanInnerJdbcCallback(Class, String)}
     */
    @Deprecated
    public SelectBeanInnerJdbcCallback(Class<T> beanClass, List<String> keyColumns,
                                       @Language("sql") String fullQuery) {
        this(beanClass, fetchTableNameFromClass(beanClass), keyColumns, fullQuery);
    }

    public SelectBeanInnerJdbcCallback(Class<T> beanClass, @Language("sql") String fullQuery) {
        this(beanClass, fetchTableNameFromClass(beanClass), null, fullQuery);
    }

    public SelectBeanInnerJdbcCallback(Class<T> beanClass, String tableName, List<String> keyColumns) {
        this(beanClass, tableName, keyColumns, null);
    }

    @Override
    protected PreparedStatement createInnerPreparedStatement(Connection c) throws SQLException {

        this.columnsAndFilelds = reinsertableBeanService.buildColumnsAndFields(c, beanClass, tableName);

        StringBuilder query = new StringBuilder().append("SELECT\n");

        for (Pair<TableColumnMetaData, Field> pair : reinsertableBeanService.buildColumnsAndFields(c, beanClass,
                tableName)) {
            query.append(pair.getLeft().name).append(",");
        }
        query.setLength(query.length() - 1);

        query.append("\nFROM ").append(tableName);

        if (!isEmpty(keyColumns) || !isEmpty(whereAddition)) {
            query.append("\nWHERE\n");
        }

        if (!isEmpty(keyColumns)) {
            for (String keyField : keyColumns) {
                query.append(keyField).append(" = ? AND\n");
            }
            query.setLength(query.length() - 5);
        }

        if (whereAddition != null) {
            query.append(whereAddition);
        }

        parameterCount = identifyParameterCount(keyColumns, whereAddition);

        return c.prepareStatement(query.toString());
    }

    public T select(Object keyValue) throws SQLException {

        setParameter(ps, keyValue, 1);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return createBean(rs);
            }
            return null;
        }
    }

    public T select(Object... keyValues) throws SQLException {

        setParametersValues(ps, parameterCount, keyValues);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return createBean(rs);
            }
            return null;
        }
    }

    public List<T> selectList(Object keyValue) throws SQLException {

        setParameter(ps, keyValue, 1);

        List<T> result = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(createBean(rs));
            }
        }
        return result;
    }

    public List<T> selectList(Object... keyValues) throws SQLException {

        setParametersValues(ps, parameterCount, keyValues);

        List<T> result = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(createBean(rs));
            }
        }
        return result;
    }

    Map<Long, T> selectMap(String mapByColumnName, Object keyValue) throws SQLException {

        int columnIndex = reinsertableBeanService.indexOfColumn(ps.getConnection(), tableName, mapByColumnName);

        setParameter(ps, keyValue, 1);

        Map<Long, T> result = new HashMap<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getLong(columnIndex), createBean(rs));
            }
        }
        return result;
    }

    public Map<Long, T> selectMap(String mapByColumnName) throws SQLException {

        int columnIndex = reinsertableBeanService.indexOfColumn(ps.getConnection(), tableName, mapByColumnName);

        Map<Long, T> result = new HashMap<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getLong(columnIndex), createBean(rs));
            }
        }
        return result;
    }

    public Map<Long, T> selectMap(String mapByColumnName, Object... keyValues) throws SQLException {

        int columnIndex = reinsertableBeanService.indexOfColumn(ps.getConnection(), tableName, mapByColumnName);

        setParametersValues(ps, parameterCount, keyValues);

        Map<Long, T> result = new HashMap<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getLong(columnIndex), createBean(rs));
            }
        }
        return result;
    }

    public Map<Long, List<T>> selectGroupedBy(String groubByColumnName, Object keyValue) throws SQLException {

        int columnIndex = reinsertableBeanService.indexOfColumn(ps.getConnection(), tableName, groubByColumnName);

        setParameter(ps, keyValue, 1);

        Map<Long, List<T>> result = new HashMap<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                List<T> list = result.computeIfAbsent(rs.getLong(columnIndex), k -> new ArrayList<>());
                list.add(createBean(rs));
            }
        }
        return result;
    }

    public Map<Long, List<T>> selectGroupedBy(String groubByColumnName, Object... keyValues) throws SQLException {

        int columnIndex = reinsertableBeanService.indexOfColumn(ps.getConnection(), tableName, groubByColumnName);

        setParametersValues(ps, parameterCount, keyValues);

        Map<Long, List<T>> result = new HashMap<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                List<T> list = result.computeIfAbsent(rs.getLong(columnIndex), k -> new ArrayList<>());
                list.add(createBean(rs));
            }
        }
        return result;
    }

    private T createBean(ResultSet rs) throws SQLException {
        try {
            Constructor<T> constructor = beanClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            T result = constructor.newInstance();
            Map<String, Object> otherFields = new HashMap<>();
            //rs.getObject types are
            //    number = BigDecimal
            //    varchar2 = String
            //    clob = oracle.sql.CLOB
            //    blob = oracle.sql.BLOB
            //    Date = java.sql.Timestamp
            //    Timestamp = oracle.sql.TIMESTAMP
            for (int i = 0; i < columnsAndFilelds.size(); i++) {
                int columnIndex = i + 1;

                Field field = columnsAndFilelds.get(i).getRight();
                if (field != null) {
                    setField(result, field, rs, columnIndex);
                } else {
                    otherFields.put(columnsAndFilelds.get(i).getLeft().name, rs.getObject(columnIndex));
                }
            }

            if (!otherFields.isEmpty()) {
                result.setOtherFields(otherFields);
            }

            return result;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static <T> void setField(T bean, Field field, ResultSet rs, int columnIndex)
            throws SQLException, IllegalAccessException {

        Class<?> fieldType = field.getType();

        if (fieldType.isEnum()) {
            field.set(bean, getById(fieldType.asSubclass(IdEnum.class), rs.getInt(columnIndex)));
        } else {
            if (fieldType == int.class) {
                field.set(bean, rs.getInt(columnIndex));
            } else if (fieldType == Integer.class) {
                int intVal = rs.getInt(columnIndex);
                field.set(bean, rs.wasNull() ? null : intVal);
            } else if (fieldType == boolean.class) {
                field.set(bean, rs.getInt(columnIndex) == 1);
            } else if (fieldType == Boolean.class) {
                int val = rs.getInt(columnIndex);
                field.set(bean, rs.wasNull() ? null : val == 1);
            } else if (fieldType == long.class) {
                field.set(bean, rs.getLong(columnIndex));
            } else if (fieldType == Long.class) {
                long longVal = rs.getLong(columnIndex);
                field.set(bean, rs.wasNull() ? null : longVal);
            } else if (fieldType == double.class || fieldType == Double.class) {
                double doubleVal = rs.getDouble(columnIndex);
                field.set(bean, rs.wasNull() ? null : doubleVal);
            } else if (fieldType == java.util.Date.class || fieldType == java.sql.Date.class) {
                field.set(bean, rs.getDate(columnIndex));
            } else if (fieldType == java.sql.Timestamp.class) {
                field.set(bean, rs.getTimestamp(columnIndex));
            } else if (fieldType == byte[].class) {
                field.set(bean, rs.getBytes(columnIndex));
            } else if (fieldType == String.class) {
                field.set(bean, rs.getString(columnIndex));
            } else {
                field.set(bean, rs.getObject(columnIndex));
            }
        }
    }
}
