package lol.lolpany.ormik.beans;

import lol.lolpany.ormik.reinsertableBeans.SequenceJdbcCallback;
import lol.lolpany.ormik.persistence.AbstractCompositePreparedStatementInnerJdbcCallback;
import lol.lolpany.ormik.persistence.AbstractPreparedStatementInnerJdbcCallback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class SequencePrefetchJdbcCallback extends AbstractCompositePreparedStatementInnerJdbcCallback {

    private final String primaryKeySequenceName;
    private final int batchSize;
    private int sequenceValuesIndex;
    private List<Long> sequenceValues;

    private SequenceJdbcCallback sequenceJdbcCallback;

    public SequencePrefetchJdbcCallback(String primaryKeySequenceName, int batchSize) {
        this.primaryKeySequenceName = primaryKeySequenceName;
        this.batchSize = batchSize;
        this.sequenceValuesIndex = 0;
    }

    @Override
    protected PreparedStatement createInnermostPreparedStatement(Connection c) throws SQLException {
        return null;
    }

    @Override
    protected AbstractPreparedStatementInnerJdbcCallback[] getInnerJdbcCallbacks() throws SQLException {
        return new AbstractPreparedStatementInnerJdbcCallback[]{
                sequenceJdbcCallback = new SequenceJdbcCallback(primaryKeySequenceName)
        };
    }

    public long fetchNext() throws SQLException {
        if (sequenceValuesIndex == batchSize || sequenceValues == null) {
            sequenceValuesIndex = 0;
            sequenceValues = sequenceJdbcCallback.doInPreparedStatement(batchSize);
        }
        return sequenceValues.get(sequenceValuesIndex++);
    }

}
