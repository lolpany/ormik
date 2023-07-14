package lol.lolpany.ormik.beans;

import org.apache.commons.lang3.tuple.Pair;
import org.intellij.lang.annotations.Language;
import lol.lolpany.ormik.persistence.AbstractPreparedStatementInnerJdbcCallback;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static lol.lolpany.ormik.beans.BeanCache.getBeanCache;
import static lol.lolpany.ormik.beans.BeanUtils.createBean;
import static lol.lolpany.ormik.reinsertableBeans.ReinsertableBeanUtils.*;

public class SelectJdbcCallback<I> extends AbstractPreparedStatementInnerJdbcCallback {
    protected final String query;
    protected final String generatedQuery;
    protected final Class<? extends I> clas;
    protected final SortedMap<String, Pair<Integer, Field>> columnToIndexAndField;
    private final int parameterCount;

    public SelectJdbcCallback(@Language("sql") String query, Class<I> one) {
        if (one == null) {
            throw new IllegalArgumentException();
        }
        BeanCache beanCache = getBeanCache();
        this.query = query;
        this.generatedQuery = beanCache.select(query, one);
        this.clas = one.isInterface() ? beanCache.findClassForInterface(one) : one;
        this.columnToIndexAndField = beanCache.queryToBeanSelectCache.get(generatedQuery).getRight();
        this.parameterCount = identifyParameterCount(query);
    }

    @Override
    protected PreparedStatement createInnerPreparedStatement(Connection c) throws SQLException {
        return c.prepareStatement(generatedQuery);
    }

    public I select(Object... keyValues) throws SQLException {
        setParametersValues(ps, parameterCount, keyValues);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return createBean(rs, clas, columnToIndexAndField.values(), 0);
            }
            return null;
        }
    }

    public List<I> selectList() throws SQLException {
        List<I> result = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(createBean(rs, clas, columnToIndexAndField.values(), 0));
            }
        }
        return result;
    }

    public List<I> selectList(Object parameter) throws SQLException {
        List<I> result = new ArrayList<>();
        setParameter(ps, parameter, 1);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(createBean(rs, clas, columnToIndexAndField.values(), 0));
            }
        }
        return result;
    }

    public List<I> selectList(Object... parameters) throws SQLException {
        List<I> result = new ArrayList<>();
        setParametersValues(ps, parameterCount, parameters);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(createBean(rs, clas, columnToIndexAndField.values(), 0));
            }
        }
        return result;
    }

    public Map<Long, I> selectMap(String mapByColumnName, Object... keyValues) throws SQLException {
        int columnIndex = getBeanCache().queryToBeanSelectCache.get(generatedQuery).getRight().get(mapByColumnName).getLeft();
        setParametersValues(ps, parameterCount, keyValues);
        Map<Long, I> result = new HashMap<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getLong(columnIndex), createBean(rs, clas, columnToIndexAndField.values(), 0));
            }
        }
        return result;
    }

    public Map<Long, List<I>> selectGroupedBy(String mapByColumnName, Object... keyValues) throws SQLException {
        int columnIndex = getBeanCache().queryToBeanSelectCache.get(generatedQuery).getRight().get(mapByColumnName).getLeft();
        setParametersValues(ps, parameterCount, keyValues);
        Map<Long, List<I>> result = new HashMap<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.computeIfAbsent(rs.getLong(columnIndex), k -> new ArrayList<>())
                        .add(createBean(rs, clas, columnToIndexAndField.values(), 0));
            }
        }
        return result;
    }
}
