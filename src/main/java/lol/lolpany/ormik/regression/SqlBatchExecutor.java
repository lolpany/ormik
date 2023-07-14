package lol.lolpany.ormik.regression;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static lol.lolpany.ormik.regression.EnvironmentUtils.getScheme;


public class SqlBatchExecutor extends EnvironmentBoundAction<Void> {
    String jdbcUrl;
    String password;
    List<String> sqlQueries;

    public SqlBatchExecutor(int envNumber, String jdbcUrl, String password, List<String> sqlQueries) {
        super(envNumber);
        this.jdbcUrl = jdbcUrl;
        this.password = password;
        this.sqlQueries = sqlQueries;
    }

    @Override
    protected Void runOn(Integer envNumber) throws Exception {
        try (Connection testedConnection = DriverManager.getConnection(jdbcUrl, getScheme(envNumber), password)) {
            Statement statement = testedConnection.createStatement();
            for (String sqlQuery : sqlQueries) {
                statement.addBatch(sqlQuery);
            }
            statement.executeBatch();
            testedConnection.commit();
        }
        return null;
    }
}
