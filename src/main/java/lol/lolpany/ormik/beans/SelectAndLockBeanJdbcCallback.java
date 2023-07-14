package lol.lolpany.ormik.beans;

import org.intellij.lang.annotations.Language;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static lol.lolpany.ormik.persistence.JdbcUtils.getLockAcquisitionException;

public final class SelectAndLockBeanJdbcCallback<I> extends SelectJdbcCallback<I> {

    private static final String FOR_UPDATE_NOWAIT = " for update nowait ";

    public SelectAndLockBeanJdbcCallback(@Language("sql") String query, Class<I> beanInterface) {
        super(query, beanInterface);
    }

    @Override
    protected PreparedStatement createInnerPreparedStatement(Connection c) throws SQLException {
        return c.prepareStatement(generatedQuery + FOR_UPDATE_NOWAIT);
    }

    /**
     * @return - null if no lock aquired, ParentTableRowLock which contain null if no object in db,
     * ParentTableRowLock which contain object if its present in db
     */
    public ParentTableRowLock<I> selectAndLock(Object... keyValues) throws SQLException {
        ParentTableRowLock<I> result;
        try {
            I bean = super.select(keyValues);
            LockingResult lockingResult = bean != null ? LockingResult.OK : LockingResult.ABSENT;
            result = new ParentTableRowLock<>(lockingResult, bean);
        } catch (SQLException e) {
            if (getLockAcquisitionException(e) != null) {
                result = new ParentTableRowLock<>(LockingResult.ALREADY_LOCKED, null);
            } else {
                throw e;
            }
        }
        return result;
    }
}
