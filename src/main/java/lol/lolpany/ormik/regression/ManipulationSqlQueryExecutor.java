package lol.lolpany.ormik.regression;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import static lol.lolpany.ormik.regression.EnvironmentUtils.getScheme;


/**
 * @deprecated - use common db access methods ({@link lol.lolpany.ormik.jdbc.IPreparedStatementJdbcCallback} and alike)
 */
@Deprecated
public class ManipulationSqlQueryExecutor extends SqlQueryExecutor<Void> {

    public ManipulationSqlQueryExecutor(Connection connection, String query, Map<String, Object> queryParameters)
            throws SQLException {
        super(connection, query, queryParameters);
    }

    public ManipulationSqlQueryExecutor(int envNumber, String jdbcConnection, String password, String query,
                                        Map<String, Object> queryParameters) throws SQLException {
        super(DriverManager.getConnection(jdbcConnection, getScheme(envNumber), password), query, queryParameters);
    }

    @Override
    public Void runOn(Integer envNumber) throws Exception {
        preparedStatement.executeUpdate();
        return null;
    }
}
