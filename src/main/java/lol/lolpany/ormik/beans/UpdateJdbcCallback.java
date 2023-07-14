package lol.lolpany.ormik.beans;

import org.apache.commons.lang3.tuple.Pair;
import lol.lolpany.ormik.persistence.AbstractPreparedStatementInnerJdbcCallback;
import lol.lolpany.ormik.persistence.FlushableJdbcCallback;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static lol.lolpany.ormik.beans.BeanCache.getBeanCache;
import static lol.lolpany.ormik.beans.BeanUtils.fetchModifiedFields;
import static lol.lolpany.ormik.reinsertableBeans.ReinsertableBeanUtils.*;

public final class UpdateJdbcCallback<I> extends FlushableJdbcCallback {
    private final Class<I> beanInterface;
    private final Class<? extends I> beanClass;
    private final int batchSize;
    private final List<I> beans;
    private final List<Field> fields;

    public UpdateJdbcCallback(Class<I> beanInterface, int batchSize) throws SQLException {
        if (beanInterface == null) {
            throw new IllegalArgumentException();
        }
        this.beanClass =
                beanInterface.isInterface() ? getBeanCache().findClassForInterface(beanInterface) : beanInterface;
        if (beanClass == null || beanClass.isInterface()) {
            throw new IllegalArgumentException();
        }
        this.fields = fetchUpdateFields(beanInterface, beanClass);
        this.beanInterface = beanInterface;
        this.batchSize = batchSize;
        this.beans = new ArrayList<>();
    }

    private List<Field> fetchUpdateFields(Class<I> beanInterface, Class<? extends I> beanClass) throws SQLException {
        List<Field> result = new ArrayList<>();
        String primaryKey = fetchPrimaryKeyNameFromClass(beanClass);
        for (Pair<Integer, Field> field : fetchModifiedFields(beanInterface, beanClass).values()) {
            String columnName = beanFieldNameToDbFieldName(field.getRight().getName());
            if (!columnName.equalsIgnoreCase(primaryKey)) {
                result.add(field.getRight());
            }
        }
        try {
            if (primaryKey != null) {
                result.add(beanClass.getDeclaredField(dbFieldNameToBeanFieldName(primaryKey)));
            }
        } catch (NoSuchFieldException e) {
            throw new SQLException();
        }
        for (Field field : result) {
            field.setAccessible(true);
        }
        return result;
    }

    @Override
    protected AbstractPreparedStatementInnerJdbcCallback[] getInnerJdbcCallbacks() {
        return new AbstractPreparedStatementInnerJdbcCallback[0];
    }

    @Override
    protected PreparedStatement createInnermostPreparedStatement(Connection c) throws SQLException {
        return c.prepareStatement(BeanService.getInstance().update(beanInterface, beanClass));
    }

    private void updateBatch(Collection<I> beans) throws SQLException {
        for (I bean : beans) {
            setParameters(bean);
            ps.addBatch();
        }
        ps.executeBatch();
    }

    private void setParameters(I bean) throws SQLException {
        try {
            int i = 1;
            for (Field field : fields) {
                setParameter(ps, field.get(bean), i++);
            }
        } catch (IllegalAccessException e) {
            throw new SQLException();
        }
    }

    public void add(I bean) throws SQLException {
        beans.add(bean);
        if (beans.size() >= batchSize) {
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
