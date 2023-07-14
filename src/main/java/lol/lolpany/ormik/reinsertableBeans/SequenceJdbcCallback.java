package lol.lolpany.ormik.reinsertableBeans;

import lol.lolpany.ormik.persistence.AbstractPreparedStatementInnerJdbcCallback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SequenceJdbcCallback extends AbstractPreparedStatementInnerJdbcCallback {

    private final String sequenceName;

    public SequenceJdbcCallback(String sequenceName) {
        this.sequenceName = sequenceName;
    }

    @Override
    protected PreparedStatement createInnerPreparedStatement(Connection c) throws SQLException {
        return c.prepareStatement("select " + sequenceName + ".nextval " +
                " from dual " +
                "connect by level <= ?"
        );
    }

    public List<Long> doInPreparedStatement(int numberOfIds) throws SQLException {
        List<Long> result = new ArrayList<>();
        ps.setInt(1, numberOfIds);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(rs.getLong(1));
            }
        }
        return result;
    }

    public long fetch() throws SQLException {
        return doInPreparedStatement(1).get(0);
    }
}
