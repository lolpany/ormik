package lol.lolpany.ormik.beans;

import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static lol.lolpany.ormik.beans.BeanUtils.fetchAllFieldsByInterface;
import static lol.lolpany.ormik.beans.BeanUtils.fetchModifiedFields;
import static lol.lolpany.ormik.reinsertableBeans.QueryCache.buildSelectQuery;
import static lol.lolpany.ormik.reinsertableBeans.QueryCache.buildUpdateQuery;
import static lol.lolpany.ormik.reinsertableBeans.ReinsertableBeanUtils.fetchPrimaryKeyNameFromClass;
import static lol.lolpany.ormik.reinsertableBeans.ReinsertableBeanUtils.fetchTableNameFromClass;

/**
 * Категорически запрещено использовать методы @java.sql.ResultSet.getXXX(String)@: идентифицировать колонки необходимо
 * только по номеру.
 * В Oracle-овом JDBC-драйвере эти методы выполняют линейный поиск в списке строк при каждом вызове
 * (см. oracle.jdbc.driver.OracleStatement#getColumnIndex(java.lang.String)), причем используется регистронезависимое
 * сравнение строк(!), т.е. если, например, запрос выбрал 1000 строк из таблицы acc_m, в которой 136 столбцов, то мы
 * выполним линейный поиск в массиве из 136 строк 136 000 раз, т.е. примерно 136 / 2 * 136 000 = 9 248 000
 * регистронезависимых сравнений строк: это, как правило, будет работать дольше, чем выполнялся сам запрос.
 */
final class BeanService {
    /**
     * 100 services * 10 beans average
     */
    private static final int NUMBER_OF_BEANS = 100 * 10;

    final Map<Class<?>, Map<QueryType, Map<String, SortedMap<String, Pair<Integer, Field>>>>>
            classToColumnNameToDbColumnIndexAndBeanField;
    // todo https://docs.oracle.com/cd/B28359_01/server.111/b28274/optimops.htm#i49183
    private final Map<Class<?>, Map<String, Map<String, String>>> selectCache;
    private final Map<Class<?>, String> updateCache;

    private BeanService() {
        this.classToColumnNameToDbColumnIndexAndBeanField = new ConcurrentHashMap<>(NUMBER_OF_BEANS);
        this.selectCache = new ConcurrentHashMap<>(NUMBER_OF_BEANS);
        this.updateCache = new ConcurrentHashMap<>(NUMBER_OF_BEANS);
    }

    private static final class Holder {
        private static final BeanService INSTANCE;

        static {
            INSTANCE = new BeanService();
        }
    }

    static BeanService getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * @return - Map<column name, Pair<column index, bean field>>
     */
    private SortedMap<String, Pair<Integer, Field>> fetchFieldsMap(Class<?> beanInterface, String tableName,
                                                                   QueryType queryType,
                                                                   SortedMap<String, Pair<Integer, Field>> fields) {
        SortedMap<String, Pair<Integer, Field>> result;
        Map<QueryType, Map<String, SortedMap<String, Pair<Integer, Field>>>> tablesToFields =
                classToColumnNameToDbColumnIndexAndBeanField.get(beanInterface);
        if (tablesToFields == null || tablesToFields.get(queryType) == null ||
                tablesToFields.get(queryType).get(tableName) == null) {
            result = fields;
            classToColumnNameToDbColumnIndexAndBeanField
                    .computeIfAbsent(beanInterface, key -> new ConcurrentHashMap<>());
            classToColumnNameToDbColumnIndexAndBeanField
                    .get(beanInterface).computeIfAbsent(queryType, key -> new ConcurrentHashMap<>());
            classToColumnNameToDbColumnIndexAndBeanField.get(beanInterface).get(queryType).put(tableName, fields);
        } else {
            result = tablesToFields.get(queryType).get(tableName);
        }
        return result;
    }

    /**
     * @return - Map<column name, Pair<column index, bean field>>
     */
    Map<String, Pair<Integer, Field>> fetchFieldsMap(Class<?> beanInterface, QueryType queryType, String tableName) {
        return ofNullable(classToColumnNameToDbColumnIndexAndBeanField.get(beanInterface)).orElse(emptyMap())
                .get(queryType).get(tableName);
    }

    public String select(Class<?> beanInterface, Class<?> beanClass, String tableName, String whereCondition) {
        String where = ofNullable(whereCondition).orElse("");
        String query = ofNullable(ofNullable(selectCache.get(beanInterface)).orElse(emptyMap()).get(tableName))
                .orElse(emptyMap()).get(where);
        if (query == null) {
            query = buildSelectQuery(tableName,
                    new ArrayList<>(fetchFieldsMap(beanInterface, tableName, QueryType.SELECT,
                            fetchAllFieldsByInterface(beanInterface, beanClass)).keySet()), where);
            selectCache.computeIfAbsent(beanInterface, key -> new ConcurrentHashMap<>());
            selectCache.get(beanInterface).computeIfAbsent(tableName, key -> new ConcurrentHashMap<>());
            selectCache.get(beanInterface).get(tableName).put(where, query);
        }
        return query;
    }

    public String update(Class<?> beanInterface, Class<?> beanClass) {
        String query = updateCache.get(beanInterface);
        if (query == null) {
            Map<String, Pair<Integer, Field>> dbColumnIndexesAndFieldsByColumnName = fetchFieldsMap(beanInterface,
                    fetchTableNameFromClass(beanClass), QueryType.UPDATE,
                    fetchModifiedFields(beanInterface, beanClass));
            String primaryKey = fetchPrimaryKeyNameFromClass(beanClass);
            List<String> columns = new ArrayList<>(dbColumnIndexesAndFieldsByColumnName.keySet());
            columns.remove(primaryKey);
            query = buildUpdateQuery(fetchTableNameFromClass(beanClass), columns,
                    primaryKey != null ? primaryKey + " = ? " : null);
            updateCache.put(beanInterface, query);
        }
        return query;
    }
}
