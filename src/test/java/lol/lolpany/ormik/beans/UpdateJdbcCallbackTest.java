package lol.lolpany.ormik.beans;

import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static lol.lolpany.ormik.reinsertableBeans.ReinsertableBeanUtils.ONE_BATCH_SIZE;
import static lol.lolpany.ormik.persistence.JdbcUtils.NUMBERS_PARAM;

public class UpdateJdbcCallbackTest {

    private static final String KPP = "TOSI-BOSI";
    private static final long[] BUYS = new long[]{1007630};

//    @Test
//    public void updates() throws SQLException {
//
//        Connection c = getDataSource().getConnection();
//
//        List<PayDoc> payDocs = InsertJdbcCallbackTest.inserts(c);
//
//        try (UpdateJdbcCallback<PayDoc> update = new UpdateJdbcCallback<>(PayDoc.class, ONE_BATCH_SIZE)) {
//            update.createPreparedStatement(c);
//            for (PayDoc payDoc : payDocs) {
//                payDoc.kpp = KPP;
//                update.add(payDoc);
//            }
//        }
//
//        try (SelectJdbcCallback<PayDoc> payDocSelect = new SelectJdbcCallback<>(
//                " select * from pay_doc " + InsertJdbcCallbackTest.WHERE_CONDITION, PayDoc.class)) {
//            payDocSelect.createPreparedStatement(c);
//            List<PayDoc> updatedPayDocs = payDocSelect.selectList(InsertJdbcCallbackTest.ACT_SUM, InsertJdbcCallbackTest.ACT_BANKFEE, InsertJdbcCallbackTest.NOT_ACT_SUM, InsertJdbcCallbackTest.NOT_ACT_BANKFEE);
//            for (PayDoc payDoc : updatedPayDocs) {
//                assertEquals(KPP, payDoc.kpp);
//            }
//        }
//    }
//
//    @Test
//    public void updatesInterface() throws SQLException {
//
//        Connection c = getDataSource().getConnection();
//
//        try (SelectJdbcCallback<IPaySchedule> payScheduleSelect = new SelectJdbcCallback<>(
//                " select * from pay_schedule where b_buy " +
//                        "in (" + NUMBERS_PARAM + ") ", IPaySchedule.class)) {
//            payScheduleSelect.createPreparedStatement(c);
//            List<IPaySchedule> paySchedules = payScheduleSelect.selectList(BUYS);
//
//            try (UpdateJdbcCallback<IPaySchedule> update = new UpdateJdbcCallback<>(IPaySchedule.class,
//                    ONE_BATCH_SIZE)) {
//                update.createPreparedStatement(c);
//                for (IPaySchedule paySchedule : paySchedules) {
//                    paySchedule.setDueterm((int) (paySchedule.getUniqid() % 1000000001));
//                    update.add(paySchedule);
//                }
//            }
//
//            try (SelectJdbcCallback<PaySchedule> payScheduleFullSelect = new SelectJdbcCallback<>(
//                    " select * from pay_schedule where b_buy " +
//                            "in (" + NUMBERS_PARAM + ") ", PaySchedule.class)) {
//                payScheduleFullSelect.createPreparedStatement(c);
//                List<PaySchedule> updatedPaySchedules =
//                        payScheduleFullSelect.selectList(BUYS);
//                for (PaySchedule pays : updatedPaySchedules) {
//                    assertEquals((int) (pays.uniqid % 1000000001), pays.dueterm);
//                }
//            }
//        }
//    }

}
