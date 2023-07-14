package lol.lolpany.ormik.persistence;


import java.sql.Connection;
import java.sql.SQLException;

import static lol.lolpany.ormik.jdbc.JdbcUtils.beginTransaction;
import static lol.lolpany.ormik.jdbc.JdbcUtils.endTransaction;

/**
 * Jdbc callback to use when there is transaction, which uses {@link AbstractPreparedStatementInnerJdbcCallback},
 * {@link AbstractCompositePreparedStatementInnerJdbcCallback}s, {@link FlushableJdbcCallback}s.
 */
public abstract class TransactionalJdbcCallback<T> extends FlushableJdbcCallback {

    protected final Connection connection;

    protected TransactionalJdbcCallback(Connection connection) {
        this.connection = connection;
    }

    public T transaction() throws Exception {
        T result;
        boolean success = false;
        boolean autoCommit = beginTransaction(connection);
        try {
            result = executeInner();
            success = true;
        } finally {
            if (success) {
                flush();
            }
            endTransaction(connection, autoCommit, success);
        }
        return result;
    }

    @Override
    protected void flushInner() throws SQLException {
        // do nothing
    }

    protected abstract T executeInner() throws Exception;
}
