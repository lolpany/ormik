package lol.lolpany.ormik.beans;

import lol.lolpany.ormik.persistence.AbstractPreparedStatementInnerJdbcCallback;
import lol.lolpany.ormik.persistence.FlushableJdbcCallback;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static lol.lolpany.ormik.reinsertableBeans.ReinsertableBeanUtils.*;
import static lol.lolpany.ormik.persistence.JdbcUtils.NUMBERS_PARAM;
import static lol.lolpany.ormik.persistence.JdbcUtils.setArray;

public final class DeleteByIdBeanJdbcCallback<T> extends FlushableJdbcCallback {
    private final String tableName;
    private final String primaryKeyColumnName;
    private final Field primaryKeyField;
    private final int batchSize;
    private final List<T> beans;

    public DeleteByIdBeanJdbcCallback(Class<T> beanClass, String tableName,
                                      String primaryKeyColumnName, int batchSize) throws SQLException {
        this.tableName = tableName;
        this.primaryKeyColumnName = primaryKeyColumnName;
        try {
            this.primaryKeyField = beanClass.getDeclaredField(dbFieldNameToBeanFieldName(primaryKeyColumnName));
            if (this.primaryKeyField != null) {
                this.primaryKeyField.setAccessible(true);
            }
        } catch (NoSuchFieldException e) {
            throw new SQLException();
        }
        this.batchSize = batchSize;
        this.beans = new ArrayList<>();
    }

    public DeleteByIdBeanJdbcCallback(Class<T> beanClass, int batchSize) throws SQLException {
        this(beanClass, fetchTableNameFromClass(beanClass), fetchPrimaryKeyNameFromClass(beanClass),
                batchSize);
    }

    @Override
    protected AbstractPreparedStatementInnerJdbcCallback[] getInnerJdbcCallbacks() {
        return new AbstractPreparedStatementInnerJdbcCallback[]{
        };
    }

    @Override
    protected PreparedStatement createInnermostPreparedStatement(Connection c) throws SQLException {
        StringBuilder query = new StringBuilder().append("DELETE FROM ").append(tableName)
                .append(" WHERE ").append(primaryKeyColumnName).append(" IN (").append(NUMBERS_PARAM).append(")");
        return c.prepareStatement(query.toString());
    }

    private void deleteBatch(List<T> beans) throws SQLException {
        long[] ids = new long[beans.size()];
        int i = 0;
        for (T bean : beans) {
            try {
                ids[i++] = (Long) primaryKeyField.get(bean);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new SQLException();
            }
        }
        setArray(1, ids, ps);
        ps.executeUpdate();
    }

    private void flushOnThreshold(int threshold) throws SQLException {
        if (beans.size() >= threshold) {
            deleteBatch(beans);
            beans.clear();
        }
    }

    public void delete(T bean) throws SQLException {
        beans.add(bean);
        flushOnThreshold(batchSize);
    }

    protected void flushInner() throws SQLException {
        flushOnThreshold(0);
    }

}
