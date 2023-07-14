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

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static lol.lolpany.ormik.reinsertableBeans.ReinsertableBeanUtils.*;

public final class InsertBeanJdbcCallback<T extends IInsertableBean> extends FlushableJdbcCallback {
    private final ReinsertableBeanService reinsertableBeanService;
    private List<Pair<TableColumnMetaData, Field>> columnsAndFilelds;
    private final String tableName;
    private final Class<T> beanClass;
    private final String primaryKeyColumnName;
    private final String primaryKeySequenceName;
    private final int batchSize;
    public final List<T> beans;
    private SequenceJdbcCallback seqJdbcCallback;
    private Field primaryKeyField;

    public InsertBeanJdbcCallback(ReinsertableBeanService reinsertableBeanService, Class<T> beanClass, String tableName,
                                  String primaryKeyColumnName, String primaryKeySequenceName, int batchSize) {
        this.beanClass = beanClass;
        this.tableName = tableName;
        this.primaryKeyColumnName = primaryKeyColumnName;
        this.primaryKeySequenceName = primaryKeySequenceName;
        this.reinsertableBeanService = reinsertableBeanService;
        this.batchSize = batchSize;
        this.beans = new ArrayList<>();
    }

    public InsertBeanJdbcCallback(Class<T> beanClass, String tableName, String primaryKeyColumnName,
                                  String primaryKeySequenceName, int batchSize) {
        this(ReinsertableBeanService.getInstance(), beanClass, tableName, primaryKeyColumnName, primaryKeySequenceName,
                batchSize);
    }

    public InsertBeanJdbcCallback(Class<T> beanClass, int batchSize) {
        this(beanClass, fetchTableNameFromClass(beanClass), fetchPrimaryKeyNameFromClass(beanClass),
                fetchPrimaryKeySequenceNameFromClass(beanClass), batchSize);
    }

    public InsertBeanJdbcCallback(Class<T> beanClass) {
        this(beanClass, fetchTableNameFromClass(beanClass), fetchPrimaryKeyNameFromClass(beanClass),
                fetchPrimaryKeySequenceNameFromClass(beanClass), ONE_BATCH_SIZE);
    }

    public InsertBeanJdbcCallback(Class<T> beanClass, String tableName, String primaryKeyColumnName,
                                  String primaryKeySequenceName) {
        this(beanClass, tableName, primaryKeyColumnName, primaryKeySequenceName, ONE_BATCH_SIZE);
    }

    @Override
    protected AbstractPreparedStatementInnerJdbcCallback[] getInnerJdbcCallbacks() {
        List<AbstractPreparedStatementInnerJdbcCallback> result = new ArrayList<>();
        if (primaryKeyColumnName != null && isNotBlank(primaryKeySequenceName)) {
            this.seqJdbcCallback = new SequenceJdbcCallback(primaryKeySequenceName);
            result.add(seqJdbcCallback);
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
     * @deprecated - use {@link InsertBeanJdbcCallback#add(IInsertableBean)}
     */
    @Deprecated
    public Long insert(T bean) throws SQLException {
        Long id = null;
        if (seqJdbcCallback != null) {
            id = seqJdbcCallback.doInPreparedStatement(1).get(0);
            try {
                primaryKeyField.set(bean, id);
            } catch (IllegalAccessException e) {
                throw new SQLException();
            }
        }
        setParameters(bean);
        ps.executeUpdate();
        return id;
    }

    /**
     * Can't fetch generated keys for batch insert in Oracle.
     *
     * @see <a href="https://stackoverflow.com/questions/15684297/how-to-get-generated-keys-from-jdbc-batch-insert-in-oracle">stackoverflow</a>
     */
    private void insertBatch(Collection<T> beans) throws SQLException {
        List<Long> ids = null;
        if (seqJdbcCallback != null) {
            ids = seqJdbcCallback.doInPreparedStatement(beans.size());
        }
        int i = 0;
        for (T bean : beans) {
            if (primaryKeyField != null) {
                try {
                    Object primaryKeyValue = primaryKeyField.get(bean);
                    if (primaryKeyValue == null ||
                            (primaryKeyValue.getClass() == Long.class && (Long) primaryKeyValue == 0)) {
                        primaryKeyField.set(bean, ids.get(i++));
                    }
                } catch (IllegalAccessException e) {
                    throw new SQLException();
                }
            }
            setParameters(bean);
            ps.addBatch();
        }
        ps.executeBatch();
    }

    public void add(T bean) throws SQLException {
        beans.add(bean);
        if (beans.size() >= batchSize) {
            insertBatch(beans);
            beans.clear();
        }
    }

    public void addAll(Collection<T> beanList) throws SQLException {
        for (T bean : beanList) {
            add(bean);
        }
    }

    protected void flushInner() throws SQLException {
        if (beans.size() > 0) {
            insertBatch(beans);
            beans.clear();
        }
    }

}
