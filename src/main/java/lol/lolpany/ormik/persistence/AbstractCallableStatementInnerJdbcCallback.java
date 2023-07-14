package lol.lolpany.ormik.persistence;

import lol.lolpany.ormik.jdbc.ICallableStatementCreator;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import static lol.lolpany.ormik.persistence.JdbcUtils.closeQuietly;


/**
 * Jdbc callback to use inside of other jdbc callbacks
 */
public abstract class AbstractCallableStatementInnerJdbcCallback implements ICallableStatementCreator, AutoCloseable {

    protected CallableStatement cs;

    @Override
    public final CallableStatement createCallableStatement(Connection c) throws SQLException {
        if (cs == null) {
            cs = createInnerCallableStatement(c);
        }
        return cs;
    }

    protected abstract CallableStatement createInnerCallableStatement(Connection c) throws SQLException;

    @Override
    public void close() {
        closeQuietly(cs);
    }

}
