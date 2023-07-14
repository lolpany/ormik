package lol.lolpany.ormik.beans;

import org.intellij.lang.annotations.Language;
import lol.lolpany.ormik.persistence.AbstractPreparedStatementInnerJdbcCallback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static lol.lolpany.ormik.reinsertableBeans.ReinsertableBeanUtils.setParametersValues;

public class DeleteBeanJdbcCallback extends AbstractPreparedStatementInnerJdbcCallback {
    private final String fullDeleteQuery;

    public DeleteBeanJdbcCallback(@Language("sql") String fullDeleteQuery) {
        this.fullDeleteQuery = fullDeleteQuery;
    }

    @Override
    protected PreparedStatement createInnerPreparedStatement(Connection c) throws SQLException {
        return c.prepareStatement(fullDeleteQuery);
    }

    public int delete(Object... keyValues) throws SQLException {
        setParametersValues(ps, keyValues.length, keyValues);
        return ps.executeUpdate();
    }
}
