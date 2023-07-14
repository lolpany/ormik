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

import static lol.lolpany.ormik.reinsertableBeans.ReinsertableBeanUtils.*;

/**
 * @deprecated - use {@link lol.lolpany.ormik.beans.SelectBeanJdbcCallback}
 * Категорически запрещено использовать методы @java.sql.ResultSet.getXXX(String)@: идентифицировать колонки необходимо
 * только по номеру.
 * В Oracle-овом JDBC-драйвере эти методы выполняют линейный поиск в списке строк при каждом вызове
 * (см. oracle.jdbc.driver.OracleStatement#getColumnIndex(java.lang.String)), причем используется регистронезависимое
 * сравнение строк(!), т.е. если, например, запрос выбрал 1000 строк из таблицы acc_m, в которой 136 столбцов, то мы
 * выполним линейный поиск в массиве из 136 строк 136 000 раз, т.е. примерно 136 / 2 * 136 000 = 9 248 000
 * регистронезависимых сравнений строк: это, как правило, будет работать дольше, чем выполнялся сам запрос.
 */
@Deprecated
public final class SelectLimitedBeanJdbcCallback<T extends ILimitedBean>
        extends AbstractPreparedStatementInnerJdbcCallback {
    private final Class<T> beanClass;
    private final String whereCondition;
    private final int parameterCount;

    public SelectLimitedBeanJdbcCallback(Class<T> beanClass, @Language("sql") String fullQuery) {
        if (beanClass == null || beanClass.isInterface()) {
            throw new IllegalArgumentException();
        }
        this.beanClass = beanClass;
        this.whereCondition = extractWhereClause(fullQuery);
        this.parameterCount = identifyParameterCount(fullQuery);
    }

    @Override
    protected PreparedStatement createInnerPreparedStatement(Connection c) throws SQLException {
        return c.prepareStatement(LimitedBeanService.getInstance().select(beanClass, whereCondition));
    }

    public T select(Object... keyValues) throws SQLException {
        setParametersValues(ps, parameterCount, keyValues);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return createBeanFromResultSet(rs);
            }
            return null;
        }
    }

    public List<T> selectList() throws SQLException {
        List<T> result = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(createBeanFromResultSet(rs));
            }
        }
        return result;
    }

    public List<T> selectList(Object keyValue) throws SQLException {
        List<T> result = new ArrayList<>();
        setParameter(ps, keyValue, 1);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(createBeanFromResultSet(rs));
            }
        }
        return result;
    }

    public Map<Long, T> selectMap(String mapByColumnName, Object keyValue) throws SQLException {
        int columnIndex = LimitedBeanService.getInstance().classToColumnNameToDbColumnIndexAndBeanField.get(beanClass)
                .get(mapByColumnName).getLeft();
        setParameter(ps, keyValue, 1);
        Map<Long, T> result = new HashMap<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getLong(columnIndex), createBeanFromResultSet(rs));
            }
        }
        return result;
    }

    public Map<Long, List<T>> selectGroupedBy(String groubByColumnName, Object... keyValues) throws SQLException {
        int columnIndex = LimitedBeanService.getInstance().classToColumnNameToDbColumnIndexAndBeanField.get(beanClass)
                .get(groubByColumnName).getLeft();
        setParametersValues(ps, parameterCount, keyValues);
        Map<Long, List<T>> result = new HashMap<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                List<T> list = result.computeIfAbsent(rs.getLong(columnIndex), k -> new ArrayList<>());
                list.add(createBeanFromResultSet(rs));
            }
        }
        return result;
    }

    private T createBeanFromResultSet(ResultSet rs) throws SQLException {
        try {
            Constructor<T> constructor = beanClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            T result = constructor.newInstance();
            for (Pair<Integer, Field> dbColumnIndexAndSetter : LimitedBeanService.getInstance().getFieldsMap(beanClass)
                    .values()) {
                setField(result, dbColumnIndexAndSetter.getRight(), rs, dbColumnIndexAndSetter.getLeft());
            }
            return result;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
