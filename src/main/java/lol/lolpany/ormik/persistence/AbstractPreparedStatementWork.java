package lol.lolpany.ormik.persistence;

import org.hibernate.jdbc.ReturningWork;
import lol.lolpany.ormik.jdbc.IPreparedStatementJdbcCallback;

import java.sql.Connection;
import java.sql.SQLException;


/**
 * Wrapper ensures  a right prepared statement lifecycle
 *
 * @param <T> type of returning object
 */
@Deprecated //use IPreparedStatementJdbcCallback instead
public abstract class AbstractPreparedStatementWork<T> implements IPreparedStatementJdbcCallback<T>, ReturningWork<T> {

    /**
     * The result set closing of statements are tested only for <tt>jdbc.jdbc.driver.T4CConnection}</tt>
     *
     * @param c connection to be used
     * @return a result object, or {@code null} if none
     * @throws SQLException thrown by any database-related errors
     */
    @Override
    public final T execute(Connection c) throws SQLException {
        return JdbcUtils.execute(c, this);
    }
}
