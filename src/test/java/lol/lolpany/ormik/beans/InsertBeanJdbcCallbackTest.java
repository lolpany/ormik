package lol.lolpany.ormik.beans;

import org.junit.Test;
import lol.lolpany.ormik.reinsertableBeans.InsertBeanJdbcCallback;
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

public class InsertBeanJdbcCallbackTest {

    private static final BigDecimal ACT_SUM = new BigDecimal("888888.88");
    private static final BigDecimal ACT_BANKFEE = new BigDecimal("7777.77");
    private static final BigDecimal NOT_ACT_SUM = new BigDecimal("555555.55");
    private static final BigDecimal NOT_ACT_BANKFEE = new BigDecimal("4444.44");

    private static final String WHERE_CONDITION =
            " where (b_sum = ? and b_bankfee = ?) or (b_sum = ? and b_bankfee = ?) ";

//    @Test
//    public void inserts() throws SQLException {
//        Connection c = getDataSource().getConnection();
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
//        List<PayDoc> inserts = new ArrayList<>();
//        try (InsertBeanJdbcCallback<PayDoc> payDocInsert = new InsertBeanJdbcCallback<>(PayDoc.class, ONE_BATCH_SIZE)) {
//            payDocInsert.createPreparedStatement(c);
//            PayDoc actPayDoc = new PayDoc();
//            actPayDoc.act = 1;
//            actPayDoc.sum = ACT_SUM;
//            actPayDoc.bankfee = ACT_BANKFEE;
//            payDocInsert.add(actPayDoc);
//            inserts.add(actPayDoc);
//            PayDoc notActPayDoc = new PayDoc();
//            notActPayDoc.act = 2;
//            notActPayDoc.sum = NOT_ACT_SUM;
//            notActPayDoc.bankfee = NOT_ACT_BANKFEE;
//            payDocInsert.add(notActPayDoc);
//            inserts.add(notActPayDoc);
//        }
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

}
