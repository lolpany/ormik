package lol.lolpany.ormik.regression;

import org.intellij.lang.annotations.Language;
import lol.lolpany.ormik.jdbc.IPreparedStatementJdbcCallback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MutateJdbcCallback implements IPreparedStatementJdbcCallback<Void> {

    private final String query;

    public MutateJdbcCallback(@Language("sql") String query) {
        this.query = query;
    }

    @Override
    public PreparedStatement createPreparedStatement(Connection c) throws SQLException {
        return c.prepareStatement(query);
    }

    @Override
    public Void doInPreparedStatement(PreparedStatement ps) throws SQLException {
        ps.executeUpdate();
        return null;
    }
}
