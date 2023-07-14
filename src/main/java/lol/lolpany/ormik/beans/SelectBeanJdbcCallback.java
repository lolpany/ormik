package lol.lolpany.ormik.beans;

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

import static lol.lolpany.ormik.beans.BeanService.getInstance;
import static lol.lolpany.ormik.reinsertableBeans.ReinsertableBeanUtils.*;

/**
 * @deprecated - use {@link SelectJdbcCallback}
 */
@Deprecated
public class SelectBeanJdbcCallback<I, B extends I> extends AbstractPreparedStatementInnerJdbcCallback {
    protected final Class<I> beanInterface;
    protected final Class<B> beanClass;
    protected final String tableName;
    protected final String whereCondition;
    private final int parameterCount;

    /**
     * @param beanInterface - to generate query
     * @param beanClass     - to instantinate
     * @param fullQuery     - for where-clause
     */
    public SelectBeanJdbcCallback(Class<I> beanInterface, Class<B> beanClass, @Language("sql") String fullQuery,
                                  String tableName) {
        if (beanInterface == null || !beanInterface.isInterface() || (beanClass == null && tableName == null) ||
                (beanClass != null && beanClass.isInterface())) {
            throw new IllegalArgumentException();
        }
        this.beanClass = beanClass;
        this.beanInterface = beanInterface;
        this.tableName = tableName != null ? tableName : fetchTableNameFromClass(beanClass);
        this.whereCondition = extractWhereClause(fullQuery);
        this.parameterCount = identifyParameterCount(fullQuery);
    }

    public SelectBeanJdbcCallback(Class<I> beanInterface, Class<B> beanClass, @Language("sql") String fullQuery) {
        this(beanInterface, beanClass, fullQuery, null);
    }

    @Override
    protected PreparedStatement createInnerPreparedStatement(Connection c) throws SQLException {
        return c.prepareStatement(getInstance().select(beanInterface, beanClass, tableName, whereCondition));
    }

    public I select(Object... keyValues) throws SQLException {
        setParametersValues(ps, parameterCount, keyValues);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return createBeanFromResultSet(rs);
            }
            return null;
        }
    }

    public List<I> selectList() throws SQLException {
        List<I> result = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(createBeanFromResultSet(rs));
            }
        }
        return result;
    }

    public List<I> selectList(Object parameter) throws SQLException {
        List<I> result = new ArrayList<>();
        setParameter(ps, parameter, 1);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(createBeanFromResultSet(rs));
            }
        }
        return result;
    }

    public List<I> selectList(Object... parameters) throws SQLException {
        List<I> result = new ArrayList<>();
        setParametersValues(ps, parameterCount, parameters);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(createBeanFromResultSet(rs));
            }
        }
        return result;
    }

    public Map<Long, I> selectMap(String mapByColumnName) throws SQLException {
        int columnIndex =
                getInstance().classToColumnNameToDbColumnIndexAndBeanField.get(beanInterface).get(QueryType.SELECT)
                        .get(tableName).get(mapByColumnName).getLeft();
        Map<Long, I> result = new HashMap<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getLong(columnIndex), createBeanFromResultSet(rs));
            }
        }
        return result;
    }

    public Map<Long, I> selectMap(String mapByColumnName, Object keyValue) throws SQLException {
        setParameter(ps, keyValue, 1);
        return selectMap(mapByColumnName);
    }

    public Map<Long, List<I>> selectGroupedBy(String groubByColumnName, Object keyValue) throws SQLException {
        int columnIndex = getInstance().classToColumnNameToDbColumnIndexAndBeanField.get(beanInterface)
                .get(QueryType.SELECT).get(tableName).get(groubByColumnName).getLeft();
        setParameter(ps, keyValue, 1);
        Map<Long, List<I>> result = new HashMap<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                List<I> list = result.computeIfAbsent(rs.getLong(columnIndex), k -> new ArrayList<>());
                list.add(createBeanFromResultSet(rs));
            }
        }
        return result;
    }

    public Map<Long, List<I>> selectGroupedBy(String groubByColumnName, Object... keyValues) throws SQLException {
        int columnIndex = getInstance().classToColumnNameToDbColumnIndexAndBeanField.get(beanInterface)
                .get(QueryType.SELECT).get(tableName).get(groubByColumnName).getLeft();
        setParametersValues(ps, parameterCount, keyValues);
        Map<Long, List<I>> result = new HashMap<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                List<I> list = result.computeIfAbsent(rs.getLong(columnIndex), k -> new ArrayList<>());
                list.add(createBeanFromResultSet(rs));
            }
        }
        return result;
    }

    private B createBeanFromResultSet(ResultSet rs) throws SQLException {
        try {
            Constructor<B> constructor = beanClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            B result = constructor.newInstance();
            for (Pair<Integer, Field> dbColumnIndexAndSetter : getInstance()
                    .fetchFieldsMap(beanInterface, QueryType.SELECT, tableName)
                    .values()) {
                setField(result, dbColumnIndexAndSetter.getRight(), rs, dbColumnIndexAndSetter.getLeft());
            }
            return result;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
