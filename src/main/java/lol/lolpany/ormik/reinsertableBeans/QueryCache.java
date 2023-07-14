package lol.lolpany.ormik.reinsertableBeans;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static lol.lolpany.ormik.reinsertableBeans.ReinsertableBeanUtils.fetchTableNameFromClass;

public final class QueryCache {
    private final Map<Class<?>, String> selectCache;

    private QueryCache() {
        this.selectCache = new ConcurrentHashMap<>();
    }

    private static final class Holder {
        private static final QueryCache INSTANCE;

        static {
            INSTANCE = new QueryCache();
        }
    }

    public static QueryCache getInstance() {
        return Holder.INSTANCE;
    }

    public String select(Class<?> beanClass, List<String> columns, String whereCondition) {
        String query = selectCache.get(beanClass);
        if (query == null) {
            query = buildSelectQuery(fetchTableNameFromClass(beanClass), columns, whereCondition);
            selectCache.put(beanClass, query);
        }
        return query;
    }

    public static String buildSelectQuery(String tableName, List<String> columns, String whereCondition) {
        StringBuilder query = new StringBuilder().append("SELECT\n");

        for (String column : columns) {
            query.append(column).append(",");
        }
        query.setLength(query.length() - 1);

        query.append("\nFROM ").append(tableName);

        if (!isEmpty(whereCondition)) {
            query.append("\nWHERE\n").append(whereCondition);
        }

        return query.toString();
    }

    public static String buildUpdateQuery(String tableName, List<String> columns, String whereCondition) {
        StringBuilder query = new StringBuilder().append("UPDATE ").append(tableName).append(" SET \n");

        for (String column : columns) {
            query.append(column).append(" = ?,\n");
        }
        query.setLength(query.length() - 2);

        if (whereCondition != null) {
            query.append(" WHERE ").append(whereCondition);
        }
        return query.toString();
    }
}
