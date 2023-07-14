package lol.lolpany.ormik.reinsertableBeans;

import org.apache.commons.lang3.tuple.Pair;
import lol.lolpany.ormik.persistence.AbstractPreparedStatementInnerJdbcCallback;
import lol.lolpany.ormik.persistence.FlushableJdbcCallback;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static lol.lolpany.ormik.reinsertableBeans.ReinsertableBeanUtils.*;

/**
 * @deprecated - use {@link lol.lolpany.ormik.beans.UpdateBeanJdbcCallback}
 */
@Deprecated
public final class UpdateBeanInnerJdbcCallback<T extends IInsertableBean> extends FlushableJdbcCallback {

    private final ReinsertableBeanService reinsertableBeanService;
    private List<Pair<TableColumnMetaData, Field>> columnsAndFilelds;
    private final String tableName;
    private final Class<T> beanClass;
    private final String primaryKeyColumnName;
    private final int batchSize;
    private final List<T> beans;

    public UpdateBeanInnerJdbcCallback(Class<T> beanClass, String tableName, String primaryKeyColumnName,
                                       int batchSize) {
        this.beanClass = beanClass;
        this.tableName = tableName;
        this.primaryKeyColumnName = primaryKeyColumnName;
        this.reinsertableBeanService = ReinsertableBeanService.getInstance();
        this.batchSize = batchSize;
        this.beans = new ArrayList<>();
    }

    public UpdateBeanInnerJdbcCallback(Class<T> beanClass, int batchSize) {
        this(beanClass, fetchTableNameFromClass(beanClass), fetchPrimaryKeyNameFromClass(beanClass), batchSize);
    }

    public UpdateBeanInnerJdbcCallback(Class<T> beanClass, String tableName, String primaryKeyColumnName) {
        this(beanClass, tableName, primaryKeyColumnName, ONE_BATCH_SIZE);
    }

    @Override
    protected AbstractPreparedStatementInnerJdbcCallback[] getInnerJdbcCallbacks() throws SQLException {
        return new AbstractPreparedStatementInnerJdbcCallback[0];
    }

    @Override
    protected PreparedStatement createInnermostPreparedStatement(Connection c) throws SQLException {

        this.columnsAndFilelds = reinsertableBeanService.buildColumnsAndFields(c, beanClass, tableName);

        StringBuilder query = new StringBuilder().append("UPDATE ").append(tableName).append(" SET \n");

        for (Pair<TableColumnMetaData, Field> columnsAndFields : columnsAndFilelds) {
            if (!primaryKeyColumnName.equals(columnsAndFields.getLeft().name)) {
                query.append(columnsAndFields.getLeft().name).append(" = ?,\n");
            }
        }
        query.setLength(query.length() - 2);

        query.append(" WHERE ").append(primaryKeyColumnName).append(" = ? ");

        return c.prepareStatement(query.toString());
    }

    private void setParameters(T bean) throws SQLException {
        Pair<TableColumnMetaData, Field> primaryKeyField = null;
        int i = 1;
        for (Pair<TableColumnMetaData, Field> pair : columnsAndFilelds) {
            if (!primaryKeyColumnName.equals(pair.getLeft().name)) {
                Field field = pair.getRight();
                setField(ps, bean, i, pair.getLeft(), field);
                i++;
            } else {
                primaryKeyField = pair;
            }
        }
        Field field = primaryKeyField.getRight();
        setField(ps, bean, i, primaryKeyField.getLeft(), field);
    }


    public void update(T bean) throws SQLException {
        setParameters(bean);
        ps.executeUpdate();
    }

    private void updateBatch(Collection<T> beans) throws SQLException {
        for (T bean : beans) {
            setParameters(bean);
            ps.addBatch();
        }
        ps.executeBatch();
    }

    public void add(T syncResult) throws SQLException {
        beans.add(syncResult);
        if (beans.size() >= batchSize) {
            updateBatch(beans);
            beans.clear();
        }
    }

    protected void flushInner() throws SQLException {
        if (beans.size() > 0) {
            updateBatch(beans);
            beans.clear();
        }
    }
}
