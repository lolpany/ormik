package lol.lolpany.ormik.persistence;

import lol.lolpany.ormik.jdbc.IPreparedStatementCreator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static lol.lolpany.ormik.persistence.JdbcUtils.closeQuietly;


/**
 * Jdbc callback to use inside of other jdbc callbacks
 */
public abstract class AbstractPreparedStatementInnerJdbcCallback implements IPreparedStatementCreator, AutoCloseable {

    protected PreparedStatement ps;

    @Override
    public final PreparedStatement createPreparedStatement(Connection c) throws SQLException {
        if (ps == null) {
            ps = createInnerPreparedStatement(c);
        }
        return ps;
    }

    protected abstract PreparedStatement createInnerPreparedStatement(Connection c) throws SQLException;

    @Override
    public void close() {
        closeQuietly(ps);
    }
}
