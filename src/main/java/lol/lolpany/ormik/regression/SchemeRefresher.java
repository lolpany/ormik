package lol.lolpany.ormik.regression;

import lol.lolpany.ormik.regression.TableComparator.Config;
import lol.lolpany.ormik.jdbc.IPreparedStatementJdbcCallback;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

import static java.lang.Thread.sleep;
import static lol.lolpany.ormik.regression.EnvironmentUtils.getScheme;
import static lol.lolpany.ormik.regression.RegressionUtils.generateScheme;
import static lol.lolpany.ormik.jdbc.JdbcUtils.execute;

public class SchemeRefresher extends EnvironmentBoundAction<Void> {

    private final static String COMMON_PACKAGE = "COMMON";

    private final static Set<String> EXCLUDED_SEQUENCES = new HashSet<String>() {{
        add("SEQ_LOG_RECORDS");
        add("SEQ_XML_JOUR");
        add("SEQ_ERR_WORK");
    }};

    private Connection connection;
    private int envForRefreshment;
    private Connection envForRefreshmentConnection;
    private Config config;
    private boolean isWithIndexes;
    private boolean isWithReadonlyTables;

    public SchemeRefresher(int envNumber, Connection connection, int envForRefreshment,
                           Connection envForRefreshmentConnection,
                           Config config, boolean isWithIndexes, boolean isWithReadonlyTables) throws SQLException {
        super(envNumber);
        this.connection = connection;
        this.envForRefreshment = envForRefreshment;
        this.envForRefreshmentConnection = envForRefreshmentConnection;
        this.config = config;
        this.isWithIndexes = isWithIndexes;
        this.isWithReadonlyTables = isWithReadonlyTables;
    }

    public SchemeRefresher(int envNumber, String jdbcConnection, String password, int envForRefreshment,
                           Config config, boolean isWithIndexes) throws SQLException {
        this(envNumber, DriverManager.getConnection(jdbcConnection, getScheme(envNumber), password), envForRefreshment,
                DriverManager.getConnection(jdbcConnection, getScheme(envForRefreshment), password),
                config, isWithIndexes, false);
    }

    public SchemeRefresher(int envNumber, String jdbcConnection, String password, int envForRefreshment,
                           Config config, boolean isWithIndexes, boolean isWithReadonlyTables) throws SQLException {
        this(envNumber, DriverManager.getConnection(jdbcConnection, getScheme(envNumber), password), envForRefreshment,
                DriverManager.getConnection(jdbcConnection, getScheme(envForRefreshment), password),
                config, isWithIndexes, isWithReadonlyTables);
    }

    @Override
    public Void runOn(Integer envNumber) throws Exception {


        if (config == null) {
            config = execute(connection,
                    new IPreparedStatementJdbcCallback<Config>() {
                        @Override
                        public Config doInPreparedStatement(PreparedStatement ps)
                                throws SQLException {
                            Config result = new Config();
                            try (ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                    result.tablesConfigs.put(rs.getString(1), new Config.TableConfig());
                                }
                            }
                            return result;
                        }

                        @Override
                        public PreparedStatement createPreparedStatement(Connection c) throws SQLException {
                            return c.prepareStatement("SELECT table_name FROM user_tables");
                        }
                    });
        }

        for (String sequence : new SqlQueryExecutor<List<String>>(connection,
                "select sequence_name from USER_SEQUENCES", null) {
            @Override
            protected List<String> runOn(Integer envNumber) throws Exception {
                List<String> result = new ArrayList<>();
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String sequenceName = resultSet.getString(1);
                        if (!EXCLUDED_SEQUENCES.contains(sequenceName.toUpperCase())) {
                            result.add(sequenceName);
                        }
                    }
                }
                return result;
            }
        }.call()) {
            new SequenceRecreator(envNumber, sequence.toUpperCase(), envForRefreshment).call();
        }

        int threadsNumber = Runtime.getRuntime().availableProcessors() / 4;
        ExecutorService executorService = new ThreadPoolExecutor(threadsNumber, threadsNumber, 0L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(700));


        TablesToRecreateGenerator tablesToRecreateGenerator =
                new TablesToRecreateGenerator(generateScheme(), config.tablesConfigs.keySet());

        List<Future<Void>> tableClearFutures = new ArrayList<>();
        String table = tablesToRecreateGenerator.next();
        while (table != null) {
            if (!table.equals("")) {
                if (config.tablesConfigs.get(table) == null) {
                    tableClearFutures.addAll(executorService.invokeAll(Collections
                            .singletonList(new TableTruncater(connection, tablesToRecreateGenerator, table))));
                } else if (config.tablesConfigs.get(table).readWrite == null
                        || (config.tablesConfigs.get(table).readWrite == ReadWriteType.READ_WRITE &&
                        !config.tablesConfigs.get(table).isModifiedInTask)
                        || (isWithReadonlyTables &&
                        config.tablesConfigs.get(table).readWrite == ReadWriteType.READ_ONLY)) {
                    tableClearFutures.addAll(executorService.invokeAll(
                            Collections.singletonList(new TableDropper(connection, tablesToRecreateGenerator, table))));
                } else if (config.tablesConfigs.get(table).readWrite == ReadWriteType.WRITE_ONLY
                        || (config.tablesConfigs.get(table).readWrite == ReadWriteType.READ_WRITE &&
                        config.tablesConfigs.get(table).isModifiedInTask)) {
                    tableClearFutures.addAll(executorService.invokeAll(Collections
                            .singletonList(new TableTruncater(connection, tablesToRecreateGenerator, table))));
                } else {
                    tablesToRecreateGenerator.complete(table);
                }
            } else {
                sleep(3000);
            }
            table = tablesToRecreateGenerator.next();
        }
        for (Future<Void> tableRecreator : tableClearFutures) {
            tableRecreator.get();
        }

        createTables(config, executorService, isWithIndexes);

        for (String packageToRecompile : new SqlQueryExecutor<List<String>>(connection,
                "select OBJECT_NAME from SYS.ALL_OBJECTS where UPPER(OBJECT_TYPE) = 'PACKAGE' and owner = '"
                        + getScheme(envNumber) + "'", null) {
            @Override
            protected List<String> runOn(Integer envNumber) throws Exception {
                List<String> result = new ArrayList<>();
                // common package must be first, cause other packages depend on it
                result.add(COMMON_PACKAGE);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String packageName = resultSet.getString(1);
                        if (!COMMON_PACKAGE.equalsIgnoreCase(packageName)) {
                            result.add(packageName);
                        }
                    }
                }
                return result;
            }
        }.call()) {
            execute(connection, new IPreparedStatementJdbcCallback<Void>() {
                @Override
                public PreparedStatement createPreparedStatement(Connection c) throws SQLException {
                    return c.prepareStatement("ALTER PACKAGE " + packageToRecompile + " COMPILE PACKAGE");
                }

                @Override
                public Void doInPreparedStatement(PreparedStatement ps) throws SQLException {
                    ps.executeUpdate();
                    return null;
                }
            });
        }

        return null;
    }

    private void createTables(Config config, ExecutorService executorService, boolean isWithIndexes)
            throws InterruptedException, ExecutionException {
        TablesToRecreateGenerator tablesToRecreateGenerator =
                new TablesToRecreateGenerator(generateScheme(), config.tablesConfigs.keySet());

        List<Future<Void>> tableCreateFutures = new ArrayList<>();
        String table = tablesToRecreateGenerator.next();
        while (table != null) {
            if (!table.equals("")) {
                if (config.tablesConfigs.get(table) != null &&
                        config.tablesConfigs.get(table).readWrite == ReadWriteType.READ_WRITE &&
                        config.tablesConfigs.get(table).isModifiedInTask) {
                    tableCreateFutures.addAll(executorService.invokeAll(
                            Collections.singletonList(
                                    new CorrectTableCreator(connection, tablesToRecreateGenerator, table,
                                            envForRefreshmentConnection, envForRefreshment, ""))));
                } else if (config.tablesConfigs.get(table) != null && (config.tablesConfigs.get(table).readWrite == null
                        || config.tablesConfigs.get(table).readWrite == ReadWriteType.READ_WRITE
                        || (isWithReadonlyTables &&
                        config.tablesConfigs.get(table).readWrite == ReadWriteType.READ_ONLY))) {
                    tableCreateFutures.addAll(executorService.invokeAll(
                            Collections.singletonList(
                                    new FastTableCreator(connection, envForRefreshmentConnection,
                                            tablesToRecreateGenerator, table,
                                            envForRefreshment, isWithIndexes))));
                } else {
                    tablesToRecreateGenerator.complete(table);
                }
            } else {
                sleep(3000);
            }
            table = tablesToRecreateGenerator.next();
        }
        for (Future<Void> tableRecreator : tableCreateFutures) {
            tableRecreator.get();
        }
    }
}
