package lol.lolpany.ormik.reinsertableBeans;

import lol.lolpany.ormik.persistence.AbstractPreparedStatementInnerJdbcCallback;
import lol.lolpany.ormik.persistence.FlushableJdbcCallback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static lol.lolpany.ormik.reinsertableBeans.ReinsertableBeanUtils.*;

public final class DeleteJdbcCallback extends FlushableJdbcCallback {
    private final String tableName;
    private final List<String> whereColumns;
    private final int batchSize;
    private final List<Object[]> parameters;
    private int parameterCount;

    public DeleteJdbcCallback(String tableName, List<String> whereColumns, int batchSize) {
        this.tableName = tableName;
        this.whereColumns = whereColumns;
        this.batchSize = batchSize;
        this.parameters = new ArrayList<>();
    }

    @Override
    protected AbstractPreparedStatementInnerJdbcCallback[] getInnerJdbcCallbacks() {
        return new AbstractPreparedStatementInnerJdbcCallback[]{
        };
    }

    @Override
    protected PreparedStatement createInnermostPreparedStatement(Connection c) throws SQLException {
        StringBuilder query = new StringBuilder().append("DELETE FROM ").append(tableName)
                .append(" WHERE ");
        int i = 0;
        for (String whereColumn : whereColumns) {
            query.append(i > 0 ? " AND " : "").append(whereColumn).append(" = ? ");
            i++;
        }
        parameterCount = identifyParameterCount(whereColumns, null);
        return c.prepareStatement(query.toString());
    }

    private void deleteBatch(List<Object[]> parameters) throws SQLException {
        for (Object[] params : parameters) {
            if (params.length > 1) {
                setParametersValues(ps, parameterCount, params);
            } else {
                setParameter(ps, params[0], 1);
            }
            ps.addBatch();
        }
        ps.executeBatch();
    }

    private void flushOnThreshold(int threshold) throws SQLException {
        if (parameters.size() > threshold) {
            deleteBatch(parameters);
            parameters.clear();
        }
    }

    public void delete(Object... keyValues) throws SQLException {
        parameters.add(keyValues);
        flushOnThreshold(batchSize);
    }

    protected void flushInner() throws SQLException {
        flushOnThreshold(0);
    }

}
