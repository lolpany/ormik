package lol.lolpany.ormik.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class AbstractBatchJdbcCallback extends AbstractPreparedStatementWork<Integer> {

    private final String sql;

    public AbstractBatchJdbcCallback(String sql) {
        this.sql = sql;
    }

    @Override
    public final PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
        return connection.prepareStatement(sql);
    }

    @Override
    public final Integer doInPreparedStatement(PreparedStatement ps) throws SQLException {
        fillBatch(ps);

        int[] res = ps.executeBatch();
        if (res != null) {
            return res.length;
        } else {
            return 0;
        }
    }

    protected abstract void fillBatch(PreparedStatement ps) throws SQLException;

}
