package lol.lolpany.ormik.reinsertableBeans;

import org.apache.commons.lang3.mutable.MutableInt;
import lol.lolpany.ormik.persistence.AbstractPreparedStatementInnerJdbcCallback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CountJdbcCallback extends AbstractPreparedStatementInnerJdbcCallback {

    private final String query;
    private final IPreparedStatementParemetersSetter preparedStatementParemetersSetter;

    public CountJdbcCallback(String query, IPreparedStatementParemetersSetter preparedStatementParemetersSetter) {
        this.query = "select count(*) from (" + query + ")";
        this.preparedStatementParemetersSetter = preparedStatementParemetersSetter;
    }

    @Override
    protected PreparedStatement createInnerPreparedStatement(Connection c) throws SQLException {
        return c.prepareStatement(query);
    }

    public long doInPreparedStatement() throws SQLException {
        preparedStatementParemetersSetter.setParemeters(ps, new MutableInt(1));
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            } else {
                throw new SQLException();
            }
        }
    }
}
