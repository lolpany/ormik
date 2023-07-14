package lol.lolpany.ormik.beans;

import org.junit.Test;
import lol.lolpany.ormik.jdbc.IPreparedStatementJdbcCallback;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static lol.lolpany.ormik.reinsertableBeans.ReinsertableBeanUtils.ONE_BATCH_SIZE;
import static lol.lolpany.ormik.persistence.JdbcUtils.execute;

public class InsertJdbcCallbackTest {
    static final BigDecimal ACT_SUM = new BigDecimal("888888.88");
    static final BigDecimal ACT_BANKFEE = new BigDecimal("7777.77");
    static final BigDecimal NOT_ACT_SUM = new BigDecimal("555555.55");
    static final BigDecimal NOT_ACT_BANKFEE = new BigDecimal("4444.44");

    static final String WHERE_CONDITION =
            " where (b_sum = ? and b_bankfee = ?) or (b_sum = ? and b_bankfee = ?) ";

//    @Test
//    public void insertsTest() throws SQLException {
//        Connection c = getDataSource().getConnection();
//
//        List<PayDoc> inserts = inserts(c);
//
//        try (SelectJdbcCallback<PayDoc> payDocSelect = new SelectJdbcCallback<>(
//                " select * from pay_doc " + WHERE_CONDITION,
//                PayDoc.class)) {
//            payDocSelect.createPreparedStatement(c);
//            List<PayDoc> payDocs = payDocSelect.selectList(ACT_SUM, ACT_BANKFEE, NOT_ACT_SUM, NOT_ACT_BANKFEE);
//            assertEquals(inserts.size(), payDocs.size());
//        }
//
//    }
//
//    static List<PayDoc> inserts(Connection c) throws SQLException {
//        List<PayDoc> result = new ArrayList<>();
//
//        execute(c, new IPreparedStatementJdbcCallback<Void>() {
//            @Override
//            public PreparedStatement createPreparedStatement(Connection c) throws SQLException {
//                return c.prepareStatement(" delete from pay_doc " + WHERE_CONDITION);
//            }
//
//            @Override
//            public Void doInPreparedStatement(PreparedStatement ps) throws SQLException {
//                ps.setBigDecimal(1, ACT_SUM);
//                ps.setBigDecimal(2, ACT_BANKFEE);
//                ps.setBigDecimal(3, NOT_ACT_SUM);
//                ps.setBigDecimal(4, NOT_ACT_BANKFEE);
//                ps.executeUpdate();
//                return null;
//            }
//        });
//
//        try (InsertJdbcCallback<PayDoc> payDocInsert = new InsertJdbcCallback<>(PayDoc.class, ONE_BATCH_SIZE)) {
//            payDocInsert.createPreparedStatement(c);
//            PayDoc actPayDoc = new PayDoc();
//            actPayDoc.act = 1;
//            actPayDoc.sum = ACT_SUM;
//            actPayDoc.bankfee = ACT_BANKFEE;
//            payDocInsert.add(actPayDoc);
//            result.add(actPayDoc);
//            PayDoc notActPayDoc = new PayDoc();
//            notActPayDoc.act = 2;
//            notActPayDoc.sum = NOT_ACT_SUM;
//            notActPayDoc.bankfee = NOT_ACT_BANKFEE;
//            payDocInsert.add(notActPayDoc);
//            result.add(notActPayDoc);
//        }
//        return result;
//    }

}
