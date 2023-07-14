package lol.lolpany.ormik.regression;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import lol.lolpany.ormik.jdbc.IPreparedStatementJdbcCallback;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static lol.lolpany.ormik.jdbc.JdbcUtils.execute;
import static lol.lolpany.ormik.regression.EnvironmentUtils.getScheme;

public class TableDropper extends EnvironmentBoundAction<Void> {

    private final Connection connection;
    private final TablesToRecreateGenerator tablesToRecreateGenerator;
    private final String table;

    TableDropper(Connection connection, TablesToRecreateGenerator tablesToRecreateGenerator, String table) {
        super(null);
        this.connection = connection;
        this.tablesToRecreateGenerator = tablesToRecreateGenerator;
        this.table = table;
    }

    TableDropper(int envNumber, String jdbcUrl, String password, TablesToRecreateGenerator tablesToRecreateGenerator,
                 String table) throws SQLException {
        this(DriverManager.getConnection(jdbcUrl, getScheme(envNumber), password), tablesToRecreateGenerator, table);
    }

    @Override
    protected Void runOn(Integer envNumber) throws Exception {

        List<Pair<String, String>> referencingConstraints =
                execute(connection, new IPreparedStatementJdbcCallback<List<Pair<String, String>>>() {
                    @Override
                    public PreparedStatement createPreparedStatement(Connection c) throws SQLException {
                        return c.prepareStatement("SELECT t.table_name, t.constraint_name\n" +
                                "FROM   user_constraints t\n" +
                                "         JOIN   user_constraints r ON t.r_constraint_name = r.constraint_name\n" +
                                "WHERE  t.constraint_type = 'R'\n" +
                                "    AND r.table_name = ?");
                    }

                    @Override
                    public List<Pair<String, String>> doInPreparedStatement(PreparedStatement ps) throws SQLException {
                        ps.setString(1, table.toUpperCase());
                        List<Pair<String, String>> result = new ArrayList<>();
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                result.add(new ImmutablePair<>(rs.getString(1), rs.getString(2)));
                            }
                        }
                        return result;
                    }
                });

        for (Pair<String, String> referencingConstraint : referencingConstraints) {
            execute(connection, new IPreparedStatementJdbcCallback<Void>() {
                @Override
                public PreparedStatement createPreparedStatement(Connection c) throws SQLException {
                    return c.prepareStatement(" alter table " + referencingConstraint.getLeft() + " drop constraint " + referencingConstraint.getRight());
                }

                @Override
                public Void doInPreparedStatement(PreparedStatement ps) throws SQLException {
                    ps.executeUpdate();
                    return null;
                }
            });
        }

        new NoParametersSqlQueryExecutor<Void>(connection,
                "drop table " + table) {
            @Override
            protected Void runOn(Integer envNumber) throws Exception {
                try {
                    statement.executeUpdate(query);
                } catch (SQLException e) {
                    if (e.getErrorCode() == 942){
                        //ignore
                    }
                }
                return null;
            }
        }.call();
        tablesToRecreateGenerator.complete(table);
        return null;
    }
}
