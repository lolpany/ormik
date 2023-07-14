package lol.lolpany.ormik.regression;

import lol.lolpany.ormik.jdbc.IPreparedStatementJdbcCallback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static lol.lolpany.ormik.regression.EnvironmentUtils.getScheme;
import static lol.lolpany.ormik.jdbc.JdbcUtils.execute;

/**
 * Recreates as 'truncate -> insert into select from'. Slow, but saves all aspects of table (primary keys, indexes,
 * ...) and doesn't break packages and triggers.
 */
public class CorrectTableCreator extends EnvironmentBoundAction<Void> {

    private final Connection connection;
    private final String table;
    private final TablesToRecreateGenerator tablesToRecreateGenerator;
    private final Connection refreshConnection;
    private final int envForRefreshment;
    private final String whereClause;

    public CorrectTableCreator(Connection connection, TablesToRecreateGenerator tablesToRecreateGenerator, String table,
                               Connection refreshConnection, int envForRefreshment, String whereClause) {
        super(null);
        this.connection = connection;
        this.table = table;
        this.tablesToRecreateGenerator = tablesToRecreateGenerator;
        this.refreshConnection = refreshConnection;
        this.envForRefreshment = envForRefreshment;
        this.whereClause = whereClause;
    }

    @Override
    protected Void runOn(Integer envNumber) throws Exception {

        String columns = execute(refreshConnection, new IPreparedStatementJdbcCallback<String>() {
            @Override
            public PreparedStatement createPreparedStatement(Connection c) throws SQLException {
                return c.prepareStatement("SELECT column_name " +
                        "FROM user_tab_cols " +
                        "WHERE table_name = ?"
                );
            }

            @Override
            public String doInPreparedStatement(PreparedStatement ps) throws SQLException {
                ps.setString(1, table.toUpperCase());
                StringBuilder result = new StringBuilder();
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.append(rs.getString(1)).append(",");
                    }
                }
                result.setLength(result.length() - 1);
                return result.toString();
            }
        });

        new NoParametersSqlQueryExecutor<Void>(connection,
                "insert into " + table + "(" +columns + ") select " +
                        columns
                        + "  from " + getScheme(envForRefreshment) + "." + table + " " +
                        whereClause) {
            @Override
            protected Void runOn(Integer envNumber) throws Exception {
                statement.executeUpdate(query);
                return null;
            }
        }.call();
        tablesToRecreateGenerator.complete(table);
        return null;
    }
}
