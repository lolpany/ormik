package lol.lolpany.ormik.beans;

import org.junit.Test;
import lol.lolpany.ormik.jdbc.IPreparedStatementJdbcCallback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static lol.lolpany.ormik.persistence.JdbcUtils.execute;

public class SelectBeanJdbcCallbackTest {
//    @Test
//    public void selects() throws SQLException {
//        Connection c = getDataSource().getConnection();
//
//        Set<Long> vbasecCodes = execute(c, new IPreparedStatementJdbcCallback<Set<Long>>() {
//            @Override
//            public PreparedStatement createPreparedStatement(Connection c) throws SQLException {
//                return c.prepareStatement(" select b_code from vbasec where b_code >= ? ");
//            }
//
//            @Override
//            public Set<Long> doInPreparedStatement(PreparedStatement ps) throws SQLException {
//                Set<Long> result = new HashSet<>();
//                ps.setLong(1, Vbasec.Codes.RUB);
//                try (ResultSet rs = ps.executeQuery()) {
//                    while (rs.next()) {
//                        result.add(rs.getLong(1));
//                    }
//                }
//                return result;
//            }
//        });
//
//        try (SelectBeanJdbcCallback<IVbasec, Vbasec> vbasecSelect = new SelectBeanJdbcCallback<>(
//                IVbasec.class, Vbasec.class, " select * from vbasec where b_code >= ? ")) {
//            vbasecSelect.createPreparedStatement(c);
//            IVbasec vbasec = vbasecSelect.select(Vbasec.Codes.RUB);
//            assertEquals(Vbasec.Codes.RUB, vbasec.getCode());
//            List<IVbasec> vbasecs = vbasecSelect.selectList(Vbasec.Codes.RUB);
//            assertEquals(vbasecCodes.size(), vbasecs.size());
//            Map<Long, IVbasec> codeToVbasec = vbasecSelect.selectMap("b_code", Vbasec.Codes.RUB);
//            assertEquals(vbasecCodes, codeToVbasec.keySet());
//            Map<Long, List<IVbasec>> codeToVbasecs = vbasecSelect.selectGroupedBy("b_code", Vbasec.Codes.RUB);
//            assertEquals(vbasecCodes, codeToVbasecs.keySet());
//        }
//    }
//
//    @Test
//    public void selectByTableName() throws SQLException {
//        Connection c = getDataSource().getConnection();
//        try (SelectBeanJdbcCallback<ICodeAndName, CodeNameElement> vocabularySelect
//                     = new SelectBeanJdbcCallback<>(ICodeAndName.class, CodeNameElement.class,
//                null, "vpay_vid")) {
//            vocabularySelect.createPreparedStatement(c);
//            List<ICodeAndName> vpayVids = vocabularySelect.selectList();
//        }
//    }

}
