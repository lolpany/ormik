package lol.lolpany.ormik.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 * Jdbc callback to use inside of other jdbc callbacks, which itself has inner jdbc callbacks.
 * Use {@link TransactionalJdbcCallback} with {@link FlushableJdbcCallback}'s instead.
 */
public abstract class AbstractCompositePreparedStatementInnerJdbcCallback
        extends AbstractPreparedStatementInnerJdbcCallback {

    protected AbstractPreparedStatementInnerJdbcCallback[] innerJdbcCallbacks;

    protected PreparedStatement createInnerPreparedStatement(Connection c) throws SQLException {

        innerJdbcCallbacks = getInnerJdbcCallbacks();

        if (innerJdbcCallbacks != null) {
            for (AbstractPreparedStatementInnerJdbcCallback callback : innerJdbcCallbacks) {
                callback.createPreparedStatement(c);
            }
        }

        return createInnermostPreparedStatement(c);
    }

    @Override
    public void close() {
        super.close();
        if (innerJdbcCallbacks != null) {
            for (AbstractPreparedStatementInnerJdbcCallback callback : innerJdbcCallbacks) {
                callback.close();
            }
        }
    }

    protected abstract AbstractPreparedStatementInnerJdbcCallback[] getInnerJdbcCallbacks() throws SQLException;

    protected abstract PreparedStatement createInnermostPreparedStatement(Connection c) throws SQLException;
}
