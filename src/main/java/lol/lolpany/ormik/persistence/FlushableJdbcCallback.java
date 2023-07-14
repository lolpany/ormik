package lol.lolpany.ormik.persistence;

import java.sql.SQLException;

/**
 * Jdbc callback to use when there are need to insert/update large number of rows.
 */
public abstract class FlushableJdbcCallback extends AbstractCompositePreparedStatementInnerJdbcCallback {

    protected void flush() throws SQLException {
        flushInner();
        if (innerJdbcCallbacks != null) {
            for (AbstractPreparedStatementInnerJdbcCallback jdbcCallback : innerJdbcCallbacks) {
                if (jdbcCallback instanceof FlushableJdbcCallback) {
                    ((FlushableJdbcCallback) jdbcCallback).flushInner();
                }
            }
        }
    }

    protected abstract void flushInner() throws SQLException;
}
