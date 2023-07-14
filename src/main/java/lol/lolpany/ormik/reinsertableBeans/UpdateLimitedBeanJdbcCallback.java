package lol.lolpany.ormik.reinsertableBeans;

import org.apache.commons.lang3.tuple.Pair;
import lol.lolpany.ormik.persistence.AbstractPreparedStatementInnerJdbcCallback;
import lol.lolpany.ormik.persistence.FlushableJdbcCallback;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static lol.lolpany.ormik.reinsertableBeans.LimitedBeanService.getInstance;
import static lol.lolpany.ormik.reinsertableBeans.ReinsertableBeanUtils.*;

/*
 * Категорически запрещено использовать методы @java.sql.ResultSet.getXXX(String)@: идентифицировать колонки необходимо
 * только по номеру.
 * В Oracle-овом JDBC-драйвере эти методы выполняют линейный поиск в списке строк при каждом вызове
 * (см. oracle.jdbc.driver.OracleStatement#getColumnIndex(java.lang.String)), причем используется регистронезависимое
 * сравнение строк(!), т.е. если, например, запрос выбрал 1000 строк из таблицы acc_m, в которой 136 столбцов, то мы
 * выполним линейный поиск в массиве из 136 строк 136 000 раз, т.е. примерно 136 / 2 * 136 000 = 9 248 000
 * регистронезависимых сравнений строк: это, как правило, будет работать дольше, чем выполнялся сам запрос.
 */

public final class UpdateLimitedBeanJdbcCallback<T extends ILimitedBean> extends FlushableJdbcCallback {
    private final Class<T> beanClass;
    private final String primaryKey;
    private final List<String> updatedColumns;
    private final int parameterCount;
    private final int batchSize;
    private final List<T> beans;

    public UpdateLimitedBeanJdbcCallback(Class<T> beanClass, int batchSize) {
        this.batchSize = batchSize;
        if (beanClass == null || beanClass.isInterface()) {
            throw new IllegalArgumentException();
        }
        this.beanClass = beanClass;
        String primaryKey = fetchPrimaryKeyNameFromClass(beanClass);
        List<String> columns = new ArrayList<>(
                LimitedBeanService.getInstance().getFieldsMap(beanClass).keySet());
        columns.remove(primaryKey);
        this.updatedColumns = columns;
        this.primaryKey = fetchPrimaryKeyNameFromClass(beanClass);
        this.parameterCount = 1;
        this.beans = new ArrayList<>();
    }

    @Override
    protected AbstractPreparedStatementInnerJdbcCallback[] getInnerJdbcCallbacks() throws SQLException {
        return new AbstractPreparedStatementInnerJdbcCallback[0];
    }

    @Override
    protected PreparedStatement createInnermostPreparedStatement(Connection c) throws SQLException {
        return c.prepareStatement(getInstance().update(beanClass));
    }

    public T select(Object... keyValues) throws SQLException {

        setParametersValues(ps, parameterCount, keyValues);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return createBeanFromResultSet(rs);
            }
            return null;
        }
    }

    private T createBeanFromResultSet(ResultSet rs) throws SQLException {
        try {
            T result = beanClass.getDeclaredConstructor().newInstance();
            for (Pair<Integer, Field> dbColumnIndexAndSetter : getInstance().getFieldsMap(beanClass).values()) {
                setField(result, dbColumnIndexAndSetter.getRight(), rs, dbColumnIndexAndSetter.getLeft());
            }
            return result;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void updateBatch(Collection<T> beans) throws SQLException {
        for (T bean : beans) {
            setParameters(bean);
            ps.addBatch();
        }
        ps.executeBatch();
    }

    private void setParameters(T bean) throws SQLException {
        int i = 1;
        Map<String, Pair<Integer, Field>> fieldsByColumns = getInstance().getFieldsMap(beanClass);
        try {
            for (String column : updatedColumns) {
                setParameter(bean, ps, i++, fieldsByColumns.get(column).getRight());
            }
            setParameter(bean, ps, i, fieldsByColumns.get(primaryKey).getRight());
        } catch (IllegalAccessException e) {
            throw new SQLException();
        }
    }

    private static <T> void setParameter(T bean, PreparedStatement ps, int index, Field field)
            throws IllegalAccessException, SQLException {
        if (field.getType().isEnum()) {
            IdEnum idEnum = ((IdEnum) field.get(bean));
            if (idEnum != null) {
                ps.setInt(index, ((IdEnum) field.get(bean)).getId());
            } else {
                ps.setInt(index, 0);
            }
        } else {
            Object fieldValue = field.get(bean);
            if (fieldValue != null && fieldValue.getClass() == Boolean.class) {
                ps.setInt(index, ((Boolean) fieldValue) ? 1 : 2);
            } else {
                ps.setObject(index, fieldValue);
            }
        }
    }

    public void add(T syncResult) throws SQLException {
        beans.add(syncResult);
        if (beans.size() > batchSize) {
            updateBatch(beans);
            beans.clear();
        }
    }

    protected void flushInner() throws SQLException {
        if (beans.size() > 0) {
            updateBatch(beans);
            beans.clear();
        }
    }
}
