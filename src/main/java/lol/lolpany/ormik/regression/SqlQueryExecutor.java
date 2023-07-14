package lol.lolpany.ormik.regression;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static lol.lolpany.ormik.regression.EnvironmentUtils.getScheme;


public abstract class SqlQueryExecutor<V> extends EnvironmentBoundAction<V> {

    private static final String PARAMETER_PATTERN_STRING = "\\$\\{(.*?)\\}";
    private static final Pattern PARAMETER_PATTERN = Pattern.compile(PARAMETER_PATTERN_STRING);

    protected Connection connection;
    protected PreparedStatement preparedStatement;

    public SqlQueryExecutor(Connection connection, String query,
                            Map<String, Object> queryParameters) throws SQLException {
        super(null);
        this.connection = connection;
        Map<String, List<Integer>> parametersIndexes = namedParametersToPostions(query);
        String queryWithPositionalPrameters = query.replaceAll(PARAMETER_PATTERN_STRING, "?");
        preparedStatement = connection.prepareStatement(queryWithPositionalPrameters);
        for (Map.Entry<String, List<Integer>> parameterIndexes : parametersIndexes.entrySet()) {
            for (Integer parameterIndex : parameterIndexes.getValue()) {
                preparedStatement.setObject(parameterIndex, queryParameters.get(parameterIndexes.getKey()));
            }
        }
    }

    public SqlQueryExecutor(int envNumber, String jdbcConnection, String password, String query,
                            Map<String, Object> queryParameters) throws SQLException {
        this(DriverManager.getConnection(jdbcConnection, getScheme(envNumber), password), query, queryParameters);
    }

    private static Map<String, List<Integer>> namedParametersToPostions(String query) {
        Map<String, List<Integer>> result = new HashMap<>();
        Matcher matcher = PARAMETER_PATTERN.matcher(query);
        int index = 1;
        while (matcher.find()) {
            result.putIfAbsent(matcher.group(1), new ArrayList<>());
            result.get(matcher.group(1)).add(index);
            index++;
        }
        return result;
    }

    @Override
    protected abstract V runOn(Integer envNumber) throws Exception;
}
