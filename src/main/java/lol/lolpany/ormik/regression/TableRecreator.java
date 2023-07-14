package lol.lolpany.ormik.regression;

import java.sql.ResultSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TableRecreator extends EnvironmentBoundAction<Void> {

    private final String table;
    private final int envForRefreshment;

    public TableRecreator(int envNumber, String table, int envForRefreshment) {
        super(envNumber);
        this.table = table;
        this.envForRefreshment = envForRefreshment;
    }

    @Override
    protected Void runOn(Integer envNumber) throws Exception {
        String tableDdl = new SqlQueryExecutor<String>(envNumber, "jdbc:oracle:thin:@10.0.0.137:1521:test", "SYS",
                "SELECT dbms_metadata.get_ddl( 'TABLE', '" + table.toUpperCase() + "' ) FROM DUAL",
                null) {
            @Override
            protected String runOn(Integer envNumber) throws Exception {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString(1);
                    }
                }
                return null;
            }
        }.call();

        new NoParametersSqlQueryExecutor<Void>(envNumber, "jdbc:oracle:thin:@10.0.0.137:1521:test", "SYS",
                "drop table " + table) {
            @Override
            protected Void runOn(Integer envNumber) throws Exception {
                statement.executeUpdate(query);
                return null;
            }
        }.call();

        new NoParametersSqlQueryExecutor<Void>(envNumber, "jdbc:oracle:thin:@10.0.0.137:1521:test", "SYS",
                addSelectClause(tableDdl)) {
            @Override
            protected Void runOn(Integer envNumber) throws Exception {
                statement.executeUpdate(query);
                return null;
            }
        }.call();
        return null;
    }

    private String addSelectClause(String tableDdl) {
        Matcher matcher = Pattern.compile("(?ms)\\s*CREATE TABLE .+?\\..+\\s+\\((.+^\\s+)\\).*").matcher(tableDdl);
        if (matcher.matches()) {
            StringBuilder columns = new StringBuilder(matcher.group(1).replaceAll("(\"\\w+\").+", "$1,"));
            columns.setLength(columns.length() - 2);
            return tableDdl.replaceAll("(?ms)\\s*CREATE TABLE (.+?)\\.(.+)\\s+\\((.+^\\s+)\\)(.*)", "CREATE TABLE $1.$2 ("
                    + columns.toString() + ") $4");
        }
        return null;
    }
}
