package lol.lolpany.ormik.reinsertableBeans;

import org.apache.commons.lang3.tuple.Pair;
import lol.lolpany.ormik.persistence.AbstractPreparedStatementInnerJdbcCallback;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static lol.lolpany.ormik.reinsertableBeans.ReinsertableBeanUtils.setField;

public final class InsertBeanInnerJdbcCallback<T extends IInsertableBean>
        extends AbstractPreparedStatementInnerJdbcCallback {

    private static final int DEFAULT_BATCH_SIZE = 1;

    private final ReinsertableBeanService reinsertableBeanService;
    private List<Pair<TableColumnMetaData, Field>> columnsAndFilelds;
    private final String tableName;
    private final Class<T> beanClass;
    private final String primaryKeyColumnName;
    private final String primaryKeySequenceName;
    private final int batchSize;
    private final List<T> beans;

    public InsertBeanInnerJdbcCallback(Class<T> beanClass, String tableName, String primaryKeyColumnName,
                                       String primaryKeySequenceName, int batchSize) {
        this.beanClass = beanClass;
        this.tableName = tableName;
        this.primaryKeyColumnName = primaryKeyColumnName;
        this.primaryKeySequenceName = primaryKeySequenceName;
        this.reinsertableBeanService = ReinsertableBeanService.getInstance();
        this.batchSize = batchSize;
        this.beans = new ArrayList<>();
    }

    public InsertBeanInnerJdbcCallback(Class<T> beanClass, String tableName, String primaryKeyColumnName,
                                       String primaryKeySequenceName) {
        this(beanClass, tableName, primaryKeyColumnName, primaryKeySequenceName, DEFAULT_BATCH_SIZE);
    }

    public InsertBeanInnerJdbcCallback(Class<T> beanClass, String tableName) {
        this(beanClass, tableName, null, null);
    }

    @Override
    protected PreparedStatement createInnerPreparedStatement(Connection c) throws SQLException {

        this.columnsAndFilelds = reinsertableBeanService.buildColumnsAndFields(c, beanClass, tableName);

        StringBuilder query = new StringBuilder().append("INSERT INTO ").append(tableName).append(" (\n");

        for (Pair<TableColumnMetaData, Field> pair : columnsAndFilelds) {
            query.append(pair.getLeft().name).append(",\n");
        }
        query.setLength(query.length() - 2);

        query.append("\n) VALUES (\n");

        for (Pair<TableColumnMetaData, Field> columnsAndFileld : columnsAndFilelds) {
            if (primaryKeyColumnName == null || !primaryKeyColumnName.equals(columnsAndFileld.getLeft().name)) {
                query.append("?,\n");
            } else {
                query.append(primaryKeySequenceName).append(".nextval,\n");
            }
        }
        query.setLength(query.length() - 2);

        query.append(")");

        PreparedStatement ps;
        if (primaryKeyColumnName != null) {
            ps = c.prepareStatement(query.toString(), new String[]{primaryKeyColumnName});
        } else {
            ps = c.prepareStatement(query.toString());
        }
        return ps;
    }

    private void setParameters(T bean) throws SQLException {
        int i = 1;
        for (Pair<TableColumnMetaData, Field> pair : columnsAndFilelds) {
            if (primaryKeyColumnName == null || !primaryKeyColumnName.equals(pair.getLeft().name)) {
                setField(ps, bean, i, pair.getLeft(), pair.getRight());
                i++;
            }
        }
    }

    public Long insert(T bean) throws SQLException {
        setParameters(bean);
        ps.executeUpdate();
        if (primaryKeyColumnName != null) {
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new SQLException();
                }
            }
        }
        return null;
    }

    /**
     * Can't get generated keys.
     *
     * @see - https://stackoverflow.com/questions/15684297/how-to-get-generated-keys-from-jdbc-batch-insert-in-oracle
     */
    public void insertBatch(Collection<T> beans) throws SQLException {
        for (T bean : beans) {
            setParameters(bean);
            ps.addBatch();
        }
        ps.executeBatch();
    }

    public void add(T syncResult) throws SQLException {
        beans.add(syncResult);
        if (beans.size() >= batchSize) {
            insertBatch(beans);
            beans.clear();
        }
    }

    /**
     * Bad - must not forget.
     */
    public void flush() throws SQLException {
        if (beans.size() > 0) {
            insertBatch(beans);
            beans.clear();
        }
    }

}
