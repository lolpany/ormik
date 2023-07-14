package lol.lolpany.ormik.reinsertableBeans;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.binarySearch;
import static lol.lolpany.ormik.reinsertableBeans.ReinsertableBeanUtils.beanFieldNameToDbFieldName;

public final class ReinsertableBeanService {

    private static final class Holder {
        private static final ReinsertableBeanService INSTANCE;

        static {
            INSTANCE = new ReinsertableBeanService();
        }
    }

    public static ReinsertableBeanService getInstance() {
        return Holder.INSTANCE;
    }

    private final Map<String, List<TableColumnMetaData>> tableColumnsByTable = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Integer>> columnIndexesByNameByTable = new ConcurrentHashMap<>();
    private final Map<Class<?>, Map<String, Field>> beanFieldsByColumnsByBean =
            new ConcurrentHashMap<>();

    /**
     * @return - list of pairs of all table fields with bean field (or null if no such field),
     * indexed by column index as returned from 'SELECT * FROM " + tableName + " WHERE rownum = 1' query
     */
    public List<Pair<TableColumnMetaData, Field>> buildColumnsAndFields(Connection c,
                                                                        Class<?> beanClass,
                                                                        String tableName) {

        List<TableColumnMetaData> sortedTableColumns = takeSortedTableColumns(c, tableName);

        Map<String, Field> beanFieldsByColumns = beanFieldsByColumnsByBean.computeIfAbsent(beanClass,
                t -> buildBeanFiledsByColumns(beanClass));

        List<Pair<TableColumnMetaData, Field>> res = new ArrayList<>();
        for (TableColumnMetaData tableColumn : sortedTableColumns) {
            res.add(new ImmutablePair<>(tableColumn, beanFieldsByColumns.get(tableColumn.name)));
        }
        return res;
    }

    public Map<String, Field> takeBeanFieldsByColumnNameMap(Class<? extends IInsertableBean> beanClass) {
        return beanFieldsByColumnsByBean.computeIfAbsent(beanClass,
                t -> buildBeanFiledsByColumns(beanClass));
    }

    int indexOfColumn(Connection c, String tableName, String columnName) {

        String tn = tableName.toLowerCase();
        String cn = columnName.toLowerCase();

        //дабы не уподобляться авторам ораклового драйвера, кэшируем результаты поиска

        Map<String, Integer> columnIndexesByName = columnIndexesByNameByTable.computeIfAbsent(tn,
                t -> new ConcurrentHashMap<>());

        return columnIndexesByName.computeIfAbsent(cn, t -> {
            List<TableColumnMetaData> sortedTableColumns = takeSortedTableColumns(c, tn);

            int i = binarySearch(sortedTableColumns, new TableColumnMetaData(cn, 0, 0, 0, 0),
                    Comparator.comparing(o -> o.name));
            if (i < 0) {
                throw new IllegalArgumentException("Column " + cn + " was not found");
            }
            return ++i;
        });
    }

    public List<TableColumnMetaData> takeSortedTableColumns(Connection c, String tableName) {

        String tn = tableName.toLowerCase();

        return tableColumnsByTable.computeIfAbsent(tn, t -> {
            try {
                return buildSortedTableColumnsList(c, tn);
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }

    private List<TableColumnMetaData> buildSortedTableColumnsList(Connection c, String tableName) throws SQLException {

        List<TableColumnMetaData> res = new ArrayList<>();

        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM " + tableName + " WHERE rownum = 1")) {
            ps.setMaxRows(1);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                ResultSetMetaData rsmd = rs.getMetaData();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    res.add(new TableColumnMetaData(rsmd.getColumnName(i).toLowerCase(), rsmd.getColumnType(i),
                            rsmd.getPrecision(i), rsmd.getScale(i), rsmd.getColumnDisplaySize(i)));
                }

                res.sort(Comparator.comparing(o -> o.name));
            }
        }

        return res;
    }

    private Map<String, Field> buildBeanFiledsByColumns(Class<?> beanClass) {

        Map<String, Field> res = new HashMap<>();

        Class<?> clazz = beanClass;
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getAnnotation(Transient.class) == null) {
                    field.setAccessible(true);
                    res.put(beanFieldNameToDbFieldName(field.getName()), field);
                }
            }

            Class<?> superclass = clazz.getSuperclass();
            clazz = superclass != Object.class ? superclass : null;
        }

        return res;
    }
}
