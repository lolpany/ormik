package lol.lolpany.ormik.beans;

import lol.lolpany.ormik.dbAccess.ICoolEmployee;
import lol.lolpany.ormik.jdbc.IPreparedStatementJdbcCallback;
import org.junit.Test;
import org.postgresql.ds.PGSimpleDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static lol.lolpany.ormik.jdbc.JdbcUtils.execute;
import static org.junit.Assert.assertEquals;

public class SelectJdbcCallbackTest {

    @Test
    public void selectByName() throws SQLException {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setServerName("localhost");
        ds.setDatabaseName("postgres");
        ds.setUser("postgres");
        ds.setPassword("postgres");

        Connection c = ds.getConnection();

        Set<Long> employeeCodes = execute(c, new IPreparedStatementJdbcCallback<Set<Long>>() {
            @Override
            public PreparedStatement createPreparedStatement(Connection c) throws SQLException {
                return c.prepareStatement(" select id from employee where id = ? ");
            }

            @Override
            public Set<Long> doInPreparedStatement(PreparedStatement ps) throws SQLException {
                Set<Long> result = new HashSet<>();
                ps.setLong(1, 2);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(rs.getLong(1));
                    }
                }
                return result;
            }
        });

        try (SelectJdbcCallback<ICoolEmployee> coolEmployeeSelect = new SelectJdbcCallback<>(
                " select * from employee where id = ? ", ICoolEmployee.class)) {
            coolEmployeeSelect.createPreparedStatement(c);
            ICoolEmployee coolEmployee = coolEmployeeSelect.select(2);
            assertEquals(2, (Object) coolEmployee.getId());
            List<ICoolEmployee> coolEmployees = coolEmployeeSelect.selectList(2);
            assertEquals(employeeCodes.size(), coolEmployees.size());
            Map<Long, ICoolEmployee> codeToCoolEmployee = coolEmployeeSelect.selectMap("id", 2);
            assertEquals(employeeCodes, codeToCoolEmployee.keySet());
            Map<Long, List<ICoolEmployee>> codeToCoolEmployees = coolEmployeeSelect.selectGroupedBy("id", 2);
            assertEquals(employeeCodes, codeToCoolEmployees.keySet());
        }
    }

//    @Test
//    public void selectRepeatedly() throws SQLException {
//        Connection c = getDataSource().getConnection();
//
//        try (SelectJdbcCallback<IAgentReportBuyersSystem> agentReportBuyersSystemSelect = new SelectJdbcCallback<>(" select * from system ", IAgentReportBuyersSystem.class);
//             SelectJdbcCallback<IAutopaymentsCancelSystem> autopaymentsCanceSystemSelect = new SelectJdbcCallback<>(" select * from system ", IAutopaymentsCancelSystem.class)) {
//            agentReportBuyersSystemSelect.createPreparedStatement(c);
//            autopaymentsCanceSystemSelect.createPreparedStatement(c);
//            agentReportBuyersSystemSelect.select();
//            autopaymentsCanceSystemSelect.select();
//        } catch (SQLException ex) {
//            fail(ex.getMessage());
//        }
//
//        try (SelectJdbcCallback<IAgentReportBuyersSystem> agentReportBuyersSystemSelectOther = new SelectJdbcCallback<>(" select * from system ", IAgentReportBuyersSystem.class);
//             SelectJdbcCallback<IAutopaymentsCancelSystem> autopaymentsCanceSystemSelectOther = new SelectJdbcCallback<>(" select * from system ", IAutopaymentsCancelSystem.class)) {
//            agentReportBuyersSystemSelectOther.createPreparedStatement(c);
//            autopaymentsCanceSystemSelectOther.createPreparedStatement(c);
//            agentReportBuyersSystemSelectOther.select();
//            autopaymentsCanceSystemSelectOther.select();
//        } catch (SQLException ex) {
//            fail(ex.getMessage());
//        }
//    }
//
//    @Ignore
//    @Test
//    public void selectThreeRepeatedly() throws SQLException {
//        Connection c = getDataSource().getConnection();
//
//        try (SelectThreeBeansJdbcCallback<ICancelRejectionsAccM, ICancelRejectionsAccF, ICancelRejectionsAccS> accSelectReject =
//                     new SelectThreeBeansJdbcCallback<>(" SELECT * " +
//                             " FROM acc_m " +
//                             "   LEFT JOIN acc_f ON acc_m.b_group = acc_f.b_group " +
//                             "   LEFT JOIN acc_s ON acc_m.b_group = acc_s.b_group " +
//                             " WHERE acc_m.b_regnum = ? " +
//                             "   ORDER BY acc_m.b_group ", ICancelRejectionsAccM.class, ICancelRejectionsAccF.class,
//                             ICancelRejectionsAccS.class);
//
//             SelectThreeBeansJdbcCallback<ICancelAccM, ICancelAccF, ICancelAccS> accSelectCancel =
//                     new SelectThreeBeansJdbcCallback<>(" SELECT * " +
//                             " FROM acc_m " +
//                             "   LEFT JOIN acc_f ON acc_m.b_group = acc_f.b_group " +
//                             "   LEFT JOIN acc_s ON acc_m.b_group = acc_s.b_group " +
//                             " WHERE acc_m.b_regnum = ? " +
//                             "   ORDER BY acc_m.b_group ", ICancelAccM.class, ICancelAccF.class,
//                             ICancelAccS.class)) {
//            accSelectReject.createPreparedStatement(c);
//            accSelectCancel.createPreparedStatement(c);
//            accSelectReject.select(testedRegnum);
//            accSelectCancel.select(testedRegnum);
//        } catch (SQLException ex) {
//            fail(ex.getMessage());
//        }
//
//        try (SelectThreeBeansJdbcCallback<ICancelRejectionsAccM, ICancelRejectionsAccF, ICancelRejectionsAccS> accSelectRejectOther =
//                     new SelectThreeBeansJdbcCallback<>(" SELECT * " +
//                             " FROM acc_m " +
//                             "   LEFT JOIN acc_f ON acc_m.b_group = acc_f.b_group " +
//                             "   LEFT JOIN acc_s ON acc_m.b_group = acc_s.b_group " +
//                             " WHERE acc_m.b_regnum = ? " +
//                             "   ORDER BY acc_m.b_group ", ICancelRejectionsAccM.class, ICancelRejectionsAccF.class,
//                             ICancelRejectionsAccS.class);
//
//             SelectThreeBeansJdbcCallback<ICancelAccM, ICancelAccF, ICancelAccS> accSelectCancelOther =
//                     new SelectThreeBeansJdbcCallback<>(" SELECT * " +
//                             " FROM acc_m " +
//                             "   LEFT JOIN acc_f ON acc_m.b_group = acc_f.b_group " +
//                             "   LEFT JOIN acc_s ON acc_m.b_group = acc_s.b_group " +
//                             " WHERE acc_m.b_regnum = ? " +
//                             "   ORDER BY acc_m.b_group ", ICancelAccM.class, ICancelAccF.class,
//                             ICancelAccS.class)) {
//            accSelectRejectOther.createPreparedStatement(c);
//            accSelectCancelOther.createPreparedStatement(c);
//            accSelectRejectOther.select(testedRegnum);
//            accSelectCancelOther.select(testedRegnum);
//        } catch (SQLException ex) {
//            fail(ex.getMessage());
//        }
//    }
//
//
//    @Test
//    public void selectHtlSearchTown() throws SQLException {
//        try (SelectJdbcCallback<IHtlSearchVtown> selectIHtlSearchVtown = new SelectJdbcCallback<>(
//                "select * from vtown where b_code = ?", IHtlSearchVtown.class)) {
//            selectIHtlSearchVtown.createPreparedStatement(getDataSource().getConnection());
//            final IHtlSearchVtown vtown = selectIHtlSearchVtown.select(2);
//            assertNotNull(vtown.getNamer());
//            assertEquals(new BigDecimal("37.617815"), vtown.getLong());
//        }
//    }
//
//    @Test
//    public void selectVTownDao() throws SQLException {
//        try (VtownDao vtownDao = new VtownDao()) {
//            vtownDao.createPreparedStatement(getDataSource().getConnection());
//            final IHtlSearchVtown vtown = vtownDao.selectHtlSearchTownWithCache(2);
//            assertNotNull(vtown.getNamer());
//        }
//    }
}
