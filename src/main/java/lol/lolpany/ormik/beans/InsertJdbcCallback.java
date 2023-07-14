package lol.lolpany.ormik.beans;

import org.apache.commons.lang3.tuple.Pair;
import lol.lolpany.ormik.reinsertableBeans.IInsertableBean;
import lol.lolpany.ormik.reinsertableBeans.ReinsertableBeanService;
import lol.lolpany.ormik.reinsertableBeans.TableColumnMetaData;
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

public final class InsertJdbcCallback<T extends IInsertableBean> extends FlushableJdbcCallback {
    private final ReinsertableBeanService reinsertableBeanService;
    private List<Pair<TableColumnMetaData, Field>> columnsAndFilelds;
    private final String tableName;
    private final Class<T> beanClass;
    private final String primaryKeyColumnName;
    private final String primaryKeySequenceName;
    private final int batchSize;
    public final List<T> beans;
    private SequencePrefetchJdbcCallback sequencePrefetch;
    private Field primaryKeyField;

    public InsertJdbcCallback(ReinsertableBeanService reinsertableBeanService, Class<T> beanClass, String tableName,
                              String primaryKeyColumnName, String primaryKeySequenceName, int batchSize) {
        this.beanClass = beanClass;
        this.tableName = tableName;
        this.primaryKeyColumnName = primaryKeyColumnName;
        this.primaryKeySequenceName = primaryKeySequenceName;
        this.reinsertableBeanService = reinsertableBeanService;
        this.batchSize = batchSize;
        this.beans = new ArrayList<>();
    }

    public InsertJdbcCallback(Class<T> beanClass, String tableName, String primaryKeyColumnName,
                              String primaryKeySequenceName, int batchSize) {
        this(ReinsertableBeanService.getInstance(), beanClass, tableName, primaryKeyColumnName, primaryKeySequenceName,
                batchSize);
    }

    public InsertJdbcCallback(Class<T> beanClass, int batchSize) {
        this(beanClass, fetchTableNameFromClass(beanClass), fetchPrimaryKeyNameFromClass(beanClass),
                fetchPrimaryKeySequenceNameFromClass(beanClass), batchSize);
    }

    public InsertJdbcCallback(Class<T> beanClass) {
        this(beanClass, fetchTableNameFromClass(beanClass), fetchPrimaryKeyNameFromClass(beanClass),
                fetchPrimaryKeySequenceNameFromClass(beanClass), ONE_BATCH_SIZE);
    }

    public InsertJdbcCallback(Class<T> beanClass, String tableName, String primaryKeyColumnName,
                              String primaryKeySequenceName) {
        this(beanClass, tableName, primaryKeyColumnName, primaryKeySequenceName, ONE_BATCH_SIZE);
    }

    @Override
    protected AbstractPreparedStatementInnerJdbcCallback[] getInnerJdbcCallbacks() {
        List<AbstractPreparedStatementInnerJdbcCallback> result = new ArrayList<>();
        if (primaryKeyColumnName != null) {
            this.sequencePrefetch = new SequencePrefetchJdbcCallback(primaryKeySequenceName, batchSize);
            result.add(sequencePrefetch);
        }
        return result.toArray(new AbstractPreparedStatementInnerJdbcCallback[0]);
    }

    @Override
    protected PreparedStatement createInnermostPreparedStatement(Connection c) throws SQLException {

        this.columnsAndFilelds = reinsertableBeanService.buildColumnsAndFields(c, beanClass, tableName);

        this.primaryKeyField =
                reinsertableBeanService.takeBeanFieldsByColumnNameMap(beanClass).get(primaryKeyColumnName);

        StringBuilder query = new StringBuilder().append("INSERT INTO ").append(tableName).append(" (\n");

        for (Pair<TableColumnMetaData, Field> pair : columnsAndFilelds) {
            query.append(pair.getLeft().name).append(",\n");
        }
        query.setLength(query.length() - 2);

        query.append("\n) VALUES (\n");

        for (Pair<TableColumnMetaData, Field> columnsAndFileld : columnsAndFilelds) {
            query.append("?,\n");
        }
        query.setLength(query.length() - 2);

        query.append(")");
        return c.prepareStatement(query.toString());
    }

    private void setParameters(T bean) throws SQLException {
        int i = 1;
        for (Pair<TableColumnMetaData, Field> pair : columnsAndFilelds) {
            setField(ps, bean, i, pair.getLeft(), pair.getRight());
            i++;
        }
    }

    /**
     * Can't fetch generated keys for batch insert in Oracle.
     *
     * @see <a href="https://stackoverflow.com/questions/15684297/how-to-get-generated-keys-from-jdbc-batch-insert-in-oracle">stackoverflow</a>
     */
    private void insertBatch(Collection<T> beans) throws SQLException {
        for (T bean : beans) {
            setParameters(bean);
            ps.addBatch();
        }
        ps.executeBatch();
    }

    public void add(T bean) throws SQLException {
        if (primaryKeyField != null) {
            try {
                Object primaryKeyValue = primaryKeyField.get(bean);
                if (primaryKeyValue == null ||
                        ((primaryKeyValue.getClass() == Long.class || primaryKeyValue.getClass() == long.class) &&
                                (Long) primaryKeyValue == 0)) {
                    primaryKeyField.set(bean, sequencePrefetch.fetchNext());
                }
            } catch (IllegalAccessException e) {
                throw new SQLException();
            }
        }
        beans.add(bean);
        if (beans.size() >= batchSize) {
            insertBatch(beans);
            beans.clear();
        }
    }

    protected void flushInner() throws SQLException {
        if (beans.size() > 0) {
            insertBatch(beans);
            beans.clear();
        }
    }

}
