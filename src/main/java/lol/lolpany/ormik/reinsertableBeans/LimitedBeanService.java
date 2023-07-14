package lol.lolpany.ormik.reinsertableBeans;

import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static lol.lolpany.ormik.reinsertableBeans.QueryCache.buildSelectQuery;
import static lol.lolpany.ormik.reinsertableBeans.QueryCache.buildUpdateQuery;
import static lol.lolpany.ormik.reinsertableBeans.ReinsertableBeanUtils.*;

public final class LimitedBeanService {
    /**
     * 100 services * 10 beans average
     */
    private static final int NUMBER_OF_BEANS = 100 * 10;

    final Map<Class<?>, Map<String, Pair<Integer, Field>>> classToColumnNameToDbColumnIndexAndBeanField;
    // todo https://docs.oracle.com/cd/B28359_01/server.111/b28274/optimops.htm#i49183
    private final Map<Class<?>, String> selectCache;
    private final Map<Class<?>, String> updateCache;

    private LimitedBeanService() {
        this.classToColumnNameToDbColumnIndexAndBeanField = new ConcurrentHashMap<>(NUMBER_OF_BEANS);
        this.selectCache = new ConcurrentHashMap<>(NUMBER_OF_BEANS);
        this.updateCache = new ConcurrentHashMap<>(NUMBER_OF_BEANS);
    }

    private static final class Holder {
        private static final LimitedBeanService INSTANCE;

        static {
            INSTANCE = new LimitedBeanService();
        }
    }

    public static LimitedBeanService getInstance() {
        return Holder.INSTANCE;
    }

    Map<String, Pair<Integer, Field>> getFieldsMap(Class<?> beanClass) {
        Map<String, Pair<Integer, Field>> result =
                classToColumnNameToDbColumnIndexAndBeanField.get(beanClass);
        if (result == null) {
            result = mapColumnNamesToBeanFields(beanClass);
            classToColumnNameToDbColumnIndexAndBeanField.put(beanClass, result);
        }
        return result;
    }

    public String select(Class<?> beanClass, String whereCondition) {
        String query = selectCache.get(beanClass);
        if (query == null) {
            query = buildSelectQuery(fetchTableNameFromClass(beanClass),
                    new ArrayList<>(getFieldsMap(beanClass).keySet()), whereCondition);
            selectCache.put(beanClass, query);
        }
        return query;
    }

    public String update(Class<?> beanClass) {
        String query = updateCache.get(beanClass);
        if (query == null) {
            Map<String, Pair<Integer, Field>> dbColumnIndexesAndFieldsByColumnName = getFieldsMap(beanClass);
            String primaryKey = fetchPrimaryKeyNameFromClass(beanClass);
            List<String> columns = new ArrayList<>(dbColumnIndexesAndFieldsByColumnName.keySet());
            columns.remove(primaryKey);
            query = buildUpdateQuery(fetchTableNameFromClass(beanClass), columns, primaryKey + " = ? ");
            updateCache.put(beanClass, query);
        }
        return query;
    }


}
