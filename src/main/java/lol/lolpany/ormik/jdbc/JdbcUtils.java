package lol.lolpany.ormik.jdbc;

import javax.sql.DataSource;
import java.sql.*;

public final class JdbcUtils {

    public static <T> T execute(Connection c, IPreparedStatementJdbcCallback<T> jdbcCallback) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = jdbcCallback.createPreparedStatement(c);
            return jdbcCallback.doInPreparedStatement(ps);
        } finally {
            closeQuietly(ps);
            if (jdbcCallback instanceof AutoCloseable) {
                close((AutoCloseable) jdbcCallback);
            }
        }
    }

    public static <T> T execute(Connection c, ICallableStatementJdbcCallback<T> jdbcCallback) throws SQLException {
        CallableStatement ps = null;
        try {
            ps = jdbcCallback.createCallableStatement(c);
            return jdbcCallback.doInCallableStatement(ps);
        } finally {
            closeQuietly(ps);
            if (jdbcCallback instanceof AutoCloseable) {
                close((AutoCloseable) jdbcCallback);
            }
        }
    }


    private static void close(AutoCloseable jdbcCallback) throws SQLException {
        try {
            jdbcCallback.close();
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }


    public static void closeQuietly(ResultSet r) {
        try {
            if (r != null && !r.isClosed()) {
                r.close();
            }
        } catch (SQLException ignored) {
        }
    }


    public static void closeQuietly(Statement s) {
        try {
            if (s != null && !s.isClosed()) {
                s.close();
            }
        } catch (SQLException ignored) {
        }
    }


    public static <T> T execute(Connection c, IConnectionJdbcCallback<T> callback) throws SQLException {
        final boolean autoCommit = beginTransaction(c);
        boolean success = false;
        try {
            T result = callback.doInConnection(c);
            success = true;
            return result;
        } finally {
            endTransaction(c, success, autoCommit);
        }
    }


    public static <T> T execute(DataSource dataSource, IConnectionJdbcCallback<T> callback) throws SQLException {
        try (Connection c = dataSource.getConnection()) {
            return execute(c, callback);
        }
    }


    public static boolean beginTransaction(Connection c) throws SQLException {
        boolean autoCommit = c.getAutoCommit();
        if (autoCommit) {
            c.setAutoCommit(false);
        }
        return autoCommit;
    }


    public static void endTransaction(Connection c, boolean success, boolean previousAutoCommit) throws SQLException {
        try {
            if (success) {
                c.commit();
            } else {
                c.rollback();
            }
        } finally {
            if (previousAutoCommit) {
                c.setAutoCommit(true);
            }
        }
    }


    public static <T> T execute(DataSource dataSource, final IPreparedStatementJdbcCallback<T> callback) throws SQLException {
        return execute(dataSource, c -> execute(c, callback));
    }

    private JdbcUtils() {
    }
}
