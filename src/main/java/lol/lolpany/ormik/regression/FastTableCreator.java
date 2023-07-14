package lol.lolpany.ormik.regression;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static lol.lolpany.ormik.regression.EnvironmentUtils.getScheme;
import static org.apache.commons.lang3.StringUtils.*;

public class FastTableCreator extends EnvironmentBoundAction<Void> {

    private final Connection connection;
    private final Connection envForRefreshmentConnection;
    private final TablesToRecreateGenerator tablesToRecreateGenerator;
    private final String table;
    private final int envForRefreshment;
    private final boolean withIndexes;

    public FastTableCreator(Connection connection, Connection envForRefreshmentConnection,
                            TablesToRecreateGenerator tablesToRecreateGenerator, String table,
                            int envForRefreshment, boolean withIndexes) {
        super(null);
        this.connection = connection;
        this.envForRefreshmentConnection = envForRefreshmentConnection;
        this.tablesToRecreateGenerator = tablesToRecreateGenerator;
        this.table = table;
        this.envForRefreshment = envForRefreshment;
        this.withIndexes = withIndexes;
    }

    public FastTableCreator(int envNumber, String jdbcUrl, String password,
                            TablesToRecreateGenerator tablesToRecreateGenerator, String table, int envForRefreshment)
            throws SQLException {
        this(DriverManager.getConnection(jdbcUrl, getScheme(envNumber), password),
                DriverManager.getConnection(jdbcUrl, getScheme(envForRefreshment), password),
                tablesToRecreateGenerator, table,
                envForRefreshment, false);
    }

    @Override
    protected Void runOn(Integer envNumber) throws Exception {

        try {
            new NoParametersSqlQueryExecutor<Void>(connection,
                    "create table " + table + " as select * from " + getScheme(envForRefreshment) + "." + table) {
                @Override
                protected Void runOn(Integer envNumber) throws Exception {
                    statement.executeUpdate(query);
                    return null;
                }
            }.call();
        } catch (SQLSyntaxErrorException e) {
            if (e.getErrorCode() != 955) {
                throw e;
            }
        }


        // primary key recreation
        Pair<String, String> primaryKeyInfo =
                new SqlQueryExecutor<Pair<String, String>>(connection,
                        "SELECT cons.constraint_name, cols.column_name\n" +
                                "FROM all_constraints cons, all_cons_columns cols\n" +
                                "WHERE cons.constraint_type = 'P'\n" +
                                "      AND cons.constraint_name = cols.constraint_name\n" +
                                "      AND cons.owner = cols.owner\n" +
                                "      AND cons.owner = '" + getScheme(envForRefreshment) + "'\n" +
                                "      AND cons.status = 'ENABLED'\n" +
                                "      and cols.table_name = '" + table.toUpperCase() + "'" +
                                "   order by cols.position", null) {
                    @Override
                    protected Pair<String, String> runOn(Integer envNumber) throws Exception {
                        String keyName = "";
                        String columns = "";
                        try (ResultSet rs = preparedStatement.executeQuery()) {
                            while (rs.next()) {
                                keyName = rs.getString(1);
                                columns += rs.getString(2) + ",";
                            }
                            if (!isBlank(keyName)) {
                                return new ImmutablePair<>(keyName, columns.substring(0, columns.length() - 1));
                            } else {
                                return null;
                            }
                        }
                    }
                }.call();
        if (primaryKeyInfo != null) {
            new NoParametersSqlQueryExecutor<Void>(connection,
                    "ALTER TABLE " + table + " ADD CONSTRAINT " + primaryKeyInfo.getLeft() + " PRIMARY KEY ("
                            + primaryKeyInfo.getRight() + ")") {
                @Override
                protected Void runOn(Integer envNumber) throws Exception {
                    try {
                        statement.executeUpdate(query);
                    } catch (SQLException e) {
                        if (e.getErrorCode() == 2260) {
                            //
                        }
                    }
                    return null;
                }
            }.call();
        }

        if (withIndexes) {
            // indexes recreation
            Map<String, List<String>> indexes =
                    new SqlQueryExecutor<Map<String, List<String>>>(connection,
                            "SELECT\n" +
                                    "  i.index_name,\n" +
                                    "  column_name,\n" +
                                    "  column_expression\n" +
                                    "FROM      all_indexes i\n" +
                                    "            LEFT JOIN all_ind_columns c\n" +
                                    "              ON   i.index_name      = c.index_name\n" +
                                    "                     AND  i.owner           = c.index_owner\n" +
                                    "            LEFT JOIN all_ind_expressions f\n" +
                                    "              ON   c.index_owner     = f.index_owner\n" +
                                    "                     AND  c.index_name      = f.index_name\n" +
                                    "                     AND  c.table_owner     = f.table_owner\n" +
                                    "                     AND  c.table_name      = f.table_name\n" +
                                    "                     AND  c.column_position = f.column_position\n" +
                                    "WHERE i.table_owner = '" + getScheme(envForRefreshment) + "'" +
                                    "    AND  i.table_name = '" + table.toUpperCase() + "'\n" +
                                    "ORDER BY i.table_owner, i.table_name, i.index_name, c.column_position", null) {
                        @Override
                        protected Map<String, List<String>> runOn(Integer envNumber) throws Exception {
                            Map<String, List<String>> result = new HashMap<>();
                            try (ResultSet rs = preparedStatement.executeQuery()) {
                                while (rs.next()) {
                                    result.computeIfAbsent(rs.getString(1), key -> new ArrayList<>());
                                    result.get(rs.getString(1))
                                            .add(!isEmpty(rs.getString(3)) ? rs.getString(3) : rs.getString(2));
                                }
                            }
                            return result;
                        }
                    }.call();
            for (Map.Entry<String, List<String>> index : indexes.entrySet()) {
                new NoParametersSqlQueryExecutor<Void>(connection,
                        "create index " + index.getKey() + " on " + table + " (" + join(index.getValue(), ",") + ")") {
                    @Override
                    protected Void runOn(Integer envNumber) throws Exception {
                        try {
                            statement.executeUpdate(query);
                        } catch (SQLException e) {
                            if (e.getErrorCode() == 955) {
                                // ignore
                            }
                        }
                        return null;
                    }
                }.call();
            }
        }


        // triggers recreation
        List<Pair<String, String>> triggersInfo =
                new SqlQueryExecutor<List<Pair<String, String>>>(envForRefreshmentConnection,
                        "SELECT description, trigger_body FROM USER_triggers WHERE table_name = '" +
                                table.toUpperCase() + "'", null) {
                    @Override
                    protected List<Pair<String, String>> runOn(Integer envNumber) throws Exception {
                        List<Pair<String, String>> result = new ArrayList<>();
                        try (ResultSet rs = preparedStatement.executeQuery()) {
                            while (rs.next()) {
                                result.add(new ImmutablePair<>(rs.getString(1), rs.getString(2)));
                            }
                        }
                        return result;
                    }
                }.call();
        for (Pair<String, String> triggerInfo : triggersInfo) {
            new NoParametersSqlQueryExecutor<Void>(connection,
                    "create trigger " + triggerInfo.getLeft() + " " + triggerInfo.getRight()) {
                @Override
                protected Void runOn(Integer envNumber) throws Exception {
                    try {
                        statement.executeUpdate(query);
                    } catch (SQLException e) {
                        int a = 5;
                    }
                    return null;
                }
            }.call();
        }
        tablesToRecreateGenerator.complete(table);

        return null;
    }
}
