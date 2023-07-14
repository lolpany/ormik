package lol.lolpany.ormik.regression;

import java.sql.ResultSet;
import java.sql.SQLSyntaxErrorException;

public class SequenceRecreator extends EnvironmentBoundAction<Void> {

    private final String sequenceName;
    private final int envForRefreshment;

    public SequenceRecreator(int envNumber, String sequenceName, int envForRefreshment) {
        super(envNumber);
        this.sequenceName = sequenceName;
        this.envForRefreshment = envForRefreshment;
    }

    @Override
    protected Void runOn(Integer envNumber) throws Exception {
        Long number = new SqlQueryExecutor<Long>(envForRefreshment, "jdbc:oracle:thin:@10.0.0.137:1521:test", "SYS",
                "select last_number from user_sequences where sequence_name = '"+sequenceName+"'",
                null) {
            @Override
            protected Long runOn(Integer envNumber) throws Exception {
                try(ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getLong(1);
                    }
                }
                return null;
            }
        }.call() + 1;

        try {
            new NoParametersSqlQueryExecutor<Void>(envNumber, "jdbc:oracle:thin:@10.0.0.137:1521:test", "SYS",
                    "BEGIN\n" +
                            "    EXECUTE IMMEDIATE 'drop sequence " + sequenceName + "';\n" +
                            "END;") {
                @Override
                protected Void runOn(Integer envNumber) throws Exception {
                    statement.executeUpdate(query);
                    return null;
                }
            }.call();
        } catch (SQLSyntaxErrorException e) {
            if (e.getErrorCode() != 2289) {
                throw e;
            }
        }

        new NoParametersSqlQueryExecutor<Void>(envNumber, "jdbc:oracle:thin:@10.0.0.137:1521:test", "SYS",
                "BEGIN\n" +
                        "    EXECUTE IMMEDIATE 'create SEQUENCE " +sequenceName + " start with "+number+"';\n" +
                        "END;") {
            @Override
            protected Void runOn(Integer envNumber) throws Exception {
                statement.executeUpdate(query);
                return null;
            }
        }.call();
        return null;
    }
}
