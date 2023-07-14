package lol.lolpany.ormik.regression;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import lol.lolpany.ormik.regression.TableComparator.Config.TableConfig;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static lol.lolpany.ormik.regression.EnvironmentUtils.getScheme;
import static org.apache.commons.lang3.StringUtils.*;
import static lol.lolpany.ormik.regression.ReadWriteType.READ_WRITE;
import static lol.lolpany.ormik.regression.ReadWriteType.WRITE_ONLY;

// todo tepmlating for where and maybe order by
// todo case insensitive config values
// todo signal that no writes in table (no coverage) or, better, integrate code coverage
public class TableComparator {

    private static final String TABLE_MARK_COLUMN_NAME = "table_mark";
    private static final int CONTROL_TABLE_MARK = 1;
    private static final int TESTED_TABLE_MARK = 2;

    private String jdbcUrl;
    private String password;
    private String controlScheme;
    private String testedScheme;
    private String configFile;

    public TableComparator(String jdbcUrl, String password, int controlEnv, int testedEnv, String configFile) {
        this.jdbcUrl = jdbcUrl;
        this.password = password;
        this.controlScheme = getScheme(controlEnv);
        this.testedScheme = getScheme(testedEnv);
        this.configFile = configFile;
    }

    public String runComparison() throws SQLException, IOException, InterruptedException, ExecutionException {
        StringBuilder result = new StringBuilder();


        try (Connection con = DriverManager.getConnection(jdbcUrl, controlScheme, password)) {

            Config config;
            if (configFile != null) {
                config = new Gson().fromJson(FileUtils.readFileToString(new File(configFile), StandardCharsets.UTF_8),
                        Config.class);
            } else {
                config = generateDefaultConfigForAllTables(con);
            }

            //
            List<String> tables = new ArrayList<>();
            tables.addAll(config.tablesConfigs.keySet());

            result.append(controlScheme).append(" - ").append(testedScheme).append("\n");
            int threads = Runtime.getRuntime().availableProcessors();
            ExecutorService executorService = new ThreadPoolExecutor(threads, threads, 0L, TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(700, false));
            List<TableComparatorCallable> tableComparators = new ArrayList<>();
            for (Map.Entry<String, TableConfig> tableConfig : config.tablesConfigs.entrySet()) {
                if (tableConfig.getValue().readWrite == null || tableConfig.getValue().readWrite == READ_WRITE
                        || tableConfig.getValue().readWrite == WRITE_ONLY) {
                    tableComparators.add(new TableComparatorCallable(jdbcUrl, password, controlScheme, testedScheme,
                            tableConfig.getKey(),
                            tableConfig.getValue()));
                }
            }
            List<Future<String>> diffs = executorService.invokeAll(tableComparators);
            for (Future<String> diff : diffs) {
//                System.out.println(diff.get());
                result.append(
                        diff.get() + "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n" +
                                "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
            }
        }
        return result.toString();
//        return "";
    }

    private Config generateDefaultConfigForAllTables(Connection con) throws SQLException {
        Config result = new Config();
        result.tablesConfigs = new HashMap<>();

        DatabaseMetaData meta = con.getMetaData();
        try (ResultSet rs = meta.getTables(null, controlScheme, null, new String[]{"TABLE"})) {
            while (rs.next()) {
                String tableName = rs.getString(3);
                TableConfig tableConfig =
                        new TableConfig(new HashSet<>(), new HashSet<>(), "", new ArrayList<>(), READ_WRITE);
                result.tablesConfigs.put(tableName, tableConfig);
            }
        }
        return result;
    }

    public static boolean isMinusApplicable(Connection connection, String scheme, String table,
                                            TableConfig tableConfig) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData()
                .getColumns(null, scheme.toUpperCase(), table.toUpperCase(), null)) {
            while (resultSet.next()) {
                if (isColumnIncluded(tableConfig, resultSet.getString(4)) && resultSet.getInt(5) == Types.CLOB) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isColumnIncluded(TableConfig tableConfig, String column) {
        return !TABLE_MARK_COLUMN_NAME.equalsIgnoreCase(column) && (!tableConfig.exludedColumns.contains(column)
                && (tableConfig.includedColumns.isEmpty()
                || tableConfig.includedColumns.contains(column)));
    }

    public static String columnsForSelect(Connection connection, String scheme, String table,
                                          TableConfig tableConfig) throws SQLException {
        List<String> columns = new ArrayList<>();
        try (ResultSet resultSet = connection.getMetaData()
                .getColumns(null, scheme.toUpperCase(), table.toUpperCase(), null)) {
            while (resultSet.next()) {
                if (isColumnIncluded(tableConfig, resultSet.getString(4))) {
                    columns.add(resultSet.getString(4));
                }
            }
        }
        return join(columns, ',');
    }


    public static class Config {
        public Map<String, TableConfig> tablesConfigs;

        public Config() {
            tablesConfigs = new HashMap<>();
        }

        public static class TableConfig {
            public Set<String> includedColumns;
            public Set<String> exludedColumns;
            public String whereClause;
            public List<String> columnsForJoin;
            public ReadWriteType readWrite;
            public boolean isModifiedInTask;

            public TableConfig() {
                this.includedColumns = new HashSet<>();
                this.exludedColumns = new HashSet<>();
                this.whereClause = "";
                this.columnsForJoin = new ArrayList<>();
                this.readWrite = READ_WRITE;
                this.isModifiedInTask = false;
            }

            public TableConfig(Set<String> exludedColumns, Set<String> includedColumns, String whereClause,
                               List<String> columnsForJoin, ReadWriteType readWrite) {
                this.includedColumns = includedColumns;
                this.exludedColumns = exludedColumns;
                this.whereClause = whereClause;
                this.columnsForJoin = columnsForJoin;
                this.readWrite = readWrite;
            }
        }
    }

    private static class TableComparatorCallable implements Callable<String> {
        String jdbcUrl;
        String password;
        String controlScheme;
        String testedScheme;
        String table;
        TableConfig tableConfig;

        public TableComparatorCallable(String jdbcUrl, String password, String controlScheme, String testedScheme,
                                       String table, TableConfig tableConfig) {
            this.jdbcUrl = jdbcUrl;
            this.password = password;
            this.controlScheme = controlScheme;
            this.testedScheme = testedScheme;
            this.table = table;
            this.tableConfig = tableConfig;
        }

        @Override
        public String call() throws Exception {
            StringBuilder result = new StringBuilder();
            try (Connection con = DriverManager.getConnection(jdbcUrl, controlScheme, password)) {
                result.append(table).append("\n");
                List<String> columnsForOrderBy;
                if (tableConfig.columnsForJoin.isEmpty()) {
                    columnsForOrderBy = new ArrayList<>();
                    try (ResultSet rs = con.getMetaData().getPrimaryKeys(null, controlScheme, table.toUpperCase())) {
                        while (rs.next()) {
                            columnsForOrderBy.add(rs.getString(4));
                        }
                    }
                } else {
                    columnsForOrderBy = tableConfig.columnsForJoin;
                }

                boolean isMinus = isMinusApplicable(con, controlScheme, table,
                        tableConfig);
//                boolean isMinus = false;

                String query = isMinus ?
                        getMinusQuery(con, controlScheme, table, tableConfig, columnsForOrderBy)
                        : getNoMinusQuery(table, columnsForOrderBy);

                try (Statement statement = con.createStatement()) {
                    System.out.println(table);
                    System.out.println(query + "\n\n\n\n\n");
                    try (ResultSet rs = statement.executeQuery(query)) {
                        result.append(isMinus ? diffFromMinusResultSet(tableConfig, columnsForOrderBy, rs) :
                                diffFromNoMinusResultSet(tableConfig, columnsForOrderBy, rs));
                    }
                }
            }
            return result.toString();
        }

        private String getMinusQuery(Connection connection, String schema, String table, TableConfig tableConfig,
                                     List<String> columnsForOrderBy) throws SQLException {
            String columnsForSelect = columnsForSelect(connection, schema, table, tableConfig);
            return "select * from (\n" +
                    "   SELECT testedMinusControl.*, " + TESTED_TABLE_MARK + " AS " + TABLE_MARK_COLUMN_NAME + " from" +
                    "    (select " + columnsForSelect + " from " + testedScheme + "." + table + "\n" +
                    (!isBlank(tableConfig.whereClause) ?
                            " where " + testedScheme + "." + table+ "." + trim(tableConfig.whereClause) : EMPTY) +
                    "     minus\n" +
                    "     select " + columnsForSelect + " from " + controlScheme + "." + table +
                    (!isBlank(tableConfig.whereClause) ?
                            " where " + controlScheme + "." + table + "."+ trim(tableConfig.whereClause) : EMPTY) +
                    ") testedMinusControl \n" +
                    "    union all \n" +
                    "    SELECT controlMinusTested.*, " + CONTROL_TABLE_MARK + " AS " + TABLE_MARK_COLUMN_NAME +
                    " from " +
                    "    (select " + columnsForSelect + " from " + controlScheme + "." + table + "\n" +
                    (!isBlank(tableConfig.whereClause) ?
                            " where " + controlScheme + "." + table+ "." + trim(tableConfig.whereClause) : EMPTY) +
                    "     minus\n" +
                    "     select " + columnsForSelect + " from " + testedScheme + "." + table +
                    (!isBlank(tableConfig.whereClause) ?
                            " where " + testedScheme + "." + table+ "." + trim(tableConfig.whereClause) : EMPTY) +
                    ")  controlMinusTested )" +
                    " order by " + join(columnsForOrderBy, ",") + ", " + TABLE_MARK_COLUMN_NAME;
        }

        private String getNoMinusQuery(String table, List<String> columnsForOrderBy) {
            String onClause = columnsForOrderBy.stream().map((column) -> "control." + column + " = "
                    + "tested." + column).collect(Collectors.joining(" AND "));

            return "SELECT * FROM " + controlScheme + "." + table + " control FULL OUTER JOIN " + testedScheme +
                    "." + table + " tested ON " + onClause;
        }

        private StringBuilder diffFromMinusResultSet(TableConfig tableConfig, List<String> columnsForOrderBy,
                                                     ResultSet rs)
                throws SQLException {
            StringBuilder result = new StringBuilder();
            if (rs.next()) {
                List<String> columnNames = getColumnNames(tableConfig, rs);
                while (!rs.isAfterLast()) {
                    StringBuilder header = new StringBuilder();
                    for (String primaryKeyColumn : columnsForOrderBy) {
                        header.append("\t" + primaryKeyColumn + "=" + rs.getObject(primaryKeyColumn) + ", ");
                    }
                    String rowsDiff = compareRows(getRowsToCompareFromMinus(tableConfig, columnNames, rs));
                    if (!isBlank(rowsDiff)) {
                        result.append(header);
                        result.setLength(result.length() - 2);
                        result.append("\n");
                        result.append(rowsDiff);
                    }
                }
            }
            return result;
        }

        private List<String> getColumnNames(TableConfig tableConfig, ResultSet rs) throws SQLException {
            List<String> result = new ArrayList<>();
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                String columnName = rs.getMetaData().getColumnName(i);
                result.add(columnName);
            }
            return result;
        }

        private StringBuilder diffFromNoMinusResultSet(TableConfig tableConfig, List<String> columnsForOrderBy,
                                                       ResultSet rs)
                throws SQLException {
            StringBuilder result = new StringBuilder();
            while (rs.next()) {
                String rowsDiff = compareRows(getRowsToCompareFromNoMinus(tableConfig, rs));
                if (!isBlank(rowsDiff)) {
                    for (String primaryKeyColumn : columnsForOrderBy) {
                        result.append("\t" + primaryKeyColumn + "=" + rs.getObject(primaryKeyColumn) + ", ");
                    }
                    result.setLength(result.length() - 2);
                    result.append("\n");
                    result.append(rowsDiff);
                }
            }
            return result;
        }

        private List<Triple<String, Object, Object>> getRowsToCompareFromMinus(TableConfig tableConfig,
                                                                               List<String> columnNames, ResultSet rs)
                throws SQLException {
            int columnCount = rs.getMetaData().getColumnCount();
            List<Triple<String, Object, Object>> result = new ArrayList<>();
            Object[] controlRow = null;
            Object[] testedRow = null;
            if (rs.getInt(TABLE_MARK_COLUMN_NAME) == CONTROL_TABLE_MARK) {
                controlRow = getRowValues(tableConfig, rs);
                rs.next();
            }
            if (!rs.isAfterLast() && rs.getInt(TABLE_MARK_COLUMN_NAME) == TESTED_TABLE_MARK) {
                testedRow = getRowValues(tableConfig, rs);
                rs.next();
            }
            if (controlRow == null) {
                controlRow = new Object[columnCount];
                Arrays.fill(controlRow, null);
            }
            if (testedRow == null) {
                testedRow = new Object[columnCount];
                Arrays.fill(testedRow, null);
            }
            for (int i = 1; i <= columnCount; i++) {
                if (isColumnIncluded(tableConfig, columnNames.get(i - 1))) {
                    result.add(new ImmutableTriple<>(columnNames.get(i - 1), controlRow[i - 1], testedRow[i - 1]));
                }
            }
            return result;
        }

        private Object[] getRowValues(TableConfig tableConfig, ResultSet rs) throws SQLException {
            int columnCount = rs.getMetaData().getColumnCount();
            Object[] result = new Object[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                if (isColumnIncluded(tableConfig, rs.getMetaData().getColumnName(i))) {
                    if (rs.getMetaData().getColumnType(i) == Types.CLOB) {
                        result[i - 1] = rs.getString(i);
                    } else {
                        result[i - 1] = rs.getObject(i);
                    }
                }
            }
            return result;
        }

        private String compareRows(List<Triple<String, Object, Object>> pairsOfColumnValues) {
            String result = "";
            for (Triple<String, Object, Object> columnValues : pairsOfColumnValues) {
                Object controlColumnValue = columnValues.getMiddle();
                Object testedColumnValue = columnValues.getRight();
                if (!(controlColumnValue == null && testedColumnValue == null)
                        && (controlColumnValue == null || testedColumnValue == null
                        || !controlColumnValue.equals(testedColumnValue))) {
                    result += "\t\t" + columnValues.getLeft() + ":\t" + controlColumnValue
                            + " - " + testedColumnValue + "\n";
                }
            }
            return result;
        }

        private List<Triple<String, Object, Object>> getRowsToCompareFromNoMinus(TableConfig tableConfig, ResultSet rs)
                throws SQLException {
            int halfColumnCount = rs.getMetaData().getColumnCount() / 2;
            List<Triple<String, Object, Object>> result = new ArrayList<>(halfColumnCount);
            for (int i = 1; i <= halfColumnCount; i++) {
                if (isColumnIncluded(tableConfig, rs.getMetaData().getColumnName(i))) {
                    Object controlColumnValue;
                    Object testedColumnValue;
                    if (rs.getMetaData().getColumnType(i) == Types.CLOB) {
                        controlColumnValue = rs.getString(i);
                        testedColumnValue = rs.getString(i);
                    } else {
                        controlColumnValue = rs.getObject(i);
                        testedColumnValue = rs.getObject(halfColumnCount + i);
                    }
                    result.add(new ImmutableTriple<>(rs.getMetaData().getColumnName(i), controlColumnValue,
                            testedColumnValue));
                }
            }
            return result;
        }
    }
}
