package lol.lolpany.ormik.regression;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static lol.lolpany.ormik.regression.EnvironmentUtils.getScheme;


public abstract class NoParametersSqlQueryExecutor<V> extends EnvironmentBoundAction<V> {


    protected Connection connection;
    protected Statement statement;
    protected String query;

    public NoParametersSqlQueryExecutor(Connection connection, String query) throws SQLException {
        super(null);
        statement = connection.createStatement();
        this.query = query;
    }

    public NoParametersSqlQueryExecutor(int envNumber, String jdbcConnection, String password, String query)
            throws SQLException {
        this(DriverManager.getConnection(jdbcConnection, getScheme(envNumber), password), query);
    }

    @Override
    protected abstract V runOn(Integer envNumber) throws Exception;
}
