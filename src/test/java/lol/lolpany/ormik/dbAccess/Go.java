package lol.lolpany.ormik.dbAccess;

import org.hibernate.CacheMode;
import org.hibernate.Session;
import org.junit.Test;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import lol.lolpany.ormik.beans.SelectBeanJdbcCallback;
import lol.lolpany.ormik.jdbc.IPreparedStatementJdbcCallback;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.Collections;

import static lol.lolpany.ormik.jdbc.JdbcUtils.execute;

public class Go {

    private static final String JDBC_CONNECTION = "jdbc:oracle:thin:@10.0.0.137:1521:test";
    private static final String JDBC_PASSWORD = "postgres";
    private static final int TESTED_ENV = 16;


    private static final int NUMBER_OF_SELECTS = 100;

//    @Test
//    public void testDifferentAccessPerformance() throws SQLException, IOException {
//        Connection c = DriverManager.getConnection(JDBC_CONNECTION, getScheme(TESTED_ENV), "SYS");
//        warmUpConnection(c);
//
//        System.out.print("IPreparedStatementJdbcCallback one field - ");
//        {
//            IPreparedStatementJdbcCallback<Object> callback = new IPreparedStatementJdbcCallback<Object>() {
//
//                @Override
//                public Object doInPreparedStatement(PreparedStatement ps) throws SQLException {
//                    ps.setLong(1, 5468454);
//                    try (ResultSet rs = ps.executeQuery()) {
//                        if (rs.next()) {
//                            return rs.getLong(1);
//                        }
//                    }
//                    return null;
//                }
//
//                @Override
//                public PreparedStatement createPreparedStatement(Connection c) throws SQLException {
//                    return c.prepareStatement("select b_regnum from ord_m where b_regnum = ?");
//                }
//            };
//            long start = System.currentTimeMillis();
//            for (int i = 0; i < NUMBER_OF_SELECTS; i++) {
//                execute(c, callback);
//            }
//            System.out.println(System.currentTimeMillis() - start);
//        }
//
//        System.out.print("SelectLimitedBeanJdbcCallback one field - ");
//        try (lol.lolpany.bora.commons.services.reinsertableBeans.SelectLimitedBeanJdbcCallback<OrdM> ordMSelect = new lol.lolpany.bora.commons.services.reinsertableBeans.SelectLimitedBeanJdbcCallback<>(OrdM.class,
//                "b_regnum = ?")) {
//            long start = System.currentTimeMillis();
//            ordMSelect.createPreparedStatement(c);
//            for (int i = 0; i < NUMBER_OF_SELECTS; i++) {
//                OrdM ordM = ordMSelect.select(5468454 + i);
//            }
//            System.out.println(System.currentTimeMillis() - start);
//        }
//
//        System.out.print("SelectLimitedBeanJdbcCallback two hundred fields - ");
//        try (lol.lolpany.bora.commons.services.reinsertableBeans.SelectLimitedBeanJdbcCallback<ExtOrdM> ordMSelect = new lol.lolpany.bora.commons.services.reinsertableBeans.SelectLimitedBeanJdbcCallback<>(ExtOrdM.class,
//                "b_regnum = ?")) {
//            ordMSelect.createPreparedStatement(c);
//            long start = System.currentTimeMillis();
//            for (int i = 0; i < NUMBER_OF_SELECTS; i++) {
//                ExtOrdM ordM = ordMSelect.select(5468454 + i);
//            }
//            System.out.println(System.currentTimeMillis() - start);
//        }
//
//        System.out.print("SelectLimitedBeanJdbcCallback Penalty (fourty one field) - ");
//        try (lol.lolpany.bora.commons.services.reinsertableBeans.SelectLimitedBeanJdbcCallback<Penalty> penaltySelect = new lol.lolpany.bora.commons.services.reinsertableBeans.SelectLimitedBeanJdbcCallback<>(Penalty.class,
//                "b_regnum = ?")) {
//            penaltySelect.createPreparedStatement(c);
//            long start = System.currentTimeMillis();
//            for (int i = 0; i < NUMBER_OF_SELECTS; i++) {
//                Penalty ordM = penaltySelect.select(5468454 + i);
//            }
//            System.out.println(System.currentTimeMillis() - start);
//        }
//
//        System.out.print("SelectBeanInnerJdbcCallback Penalty (fourty one field) - ");
//        try (lol.lolpany.bora.commons.services.reinsertableBeans.SelectBeanInnerJdbcCallback<lol.lolpany.bora.commons.services.orderAndRelatedEntities.Penalty> penaltySelect = new lol.lolpany.bora.commons.services.reinsertableBeans.SelectBeanInnerJdbcCallback<>(
//                lol.lolpany.bora.commons.services.orderAndRelatedEntities.Penalty.class,
//                "penalty", Collections.singletonList("b_regnum"))) {
//            penaltySelect.createPreparedStatement(c);
//            long start = System.currentTimeMillis();
//            for (int i = 0; i < NUMBER_OF_SELECTS; i++) {
//                lol.lolpany.bora.commons.services.orderAndRelatedEntities.Penalty ordM = penaltySelect.select(5468454 + i);
//            }
//            System.out.println(System.currentTimeMillis() - start);
//        }
//
//        System.out.print("SelectBeanInnerJdbcCallback - ");
//        try (lol.lolpany.bora.commons.services.reinsertableBeans.SelectBeanInnerJdbcCallback<Order> ordMSelect = new lol.lolpany.bora.commons.services.reinsertableBeans.SelectBeanInnerJdbcCallback<>(Order.class, "ord_m",
//                Collections.singletonList("b_regnum"), null)) {
//            ordMSelect.createPreparedStatement(c);
//            long start = System.currentTimeMillis();
//            for (int i = 0; i < NUMBER_OF_SELECTS; i++) {
//                Order ordM = ordMSelect.select(5468454 + i);
//            }
//            System.out.println(System.currentTimeMillis() - start);
//        }
//
//        final OracleDataSource ds = new OracleDataSource();
//        ds.setURL(JDBC_CONNECTION);
//        ds.setUser(getScheme(5));
//        ds.setPassword(JDBC_PASSWORD);
//
//        LocalSessionFactoryBean localSessionFactoryBean = new LocalSessionFactoryBean();
//        localSessionFactoryBean.setDataSource(ds);
//        localSessionFactoryBean.setAnnotatedClasses(OrdM.class, ExtOrdM.class);
//        localSessionFactoryBean.afterPropertiesSet();
//        Session session = localSessionFactoryBean.getObject().openSession();
//        session.setCacheMode(CacheMode.IGNORE);
//
//        session.get(OrdM.class, 5468454L);
//
//        System.out.print("Hibernate one field - ");
//        long start = System.currentTimeMillis();
//        for (int i = 0; i < NUMBER_OF_SELECTS; i++) {
//            OrdM acc_m = (OrdM) session.get(OrdM.class, 5468454L + i);
//        }
//        System.out.println(System.currentTimeMillis() - start);
//
//
//        System.out.print("Hibernate two hundred fields - ");
//        start = System.currentTimeMillis();
//        for (int i = NUMBER_OF_SELECTS; i < NUMBER_OF_SELECTS + NUMBER_OF_SELECTS; i++) {
//            ExtOrdM acc_m = (ExtOrdM) session.get(ExtOrdM.class, 5468454L + i);
//        }
//        System.out.println(System.currentTimeMillis() - start);
//    }
//
//    @Test
//    public void oracleStatementCache()
//            throws SQLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
//        Connection c = DriverManager.getConnection(JDBC_CONNECTION, getScheme(5), "SYS");
//        ((OracleConnection) c).setImplicitCachingEnabled(true);
//
//        warmUpConnection(c);
//
//        {
//            long start = System.currentTimeMillis();
//            for (int i = 0; i < NUMBER_OF_SELECTS; i++) {
//                IPreparedStatementJdbcCallback<Object> callback = new IPreparedStatementJdbcCallback<Object>() {
//
//                    @Override
//                    public Object doInPreparedStatement(PreparedStatement ps) throws SQLException {
//                        ps.setLong(1, 4229513);
//                        try (ResultSet rs = ps.executeQuery()) {
//                            if (rs.next()) {
//                                return rs.getLong(1);
//                            }
//                        }
//                        return null;
//                    }
//
//                    @Override
//                    public PreparedStatement createPreparedStatement(Connection c) throws SQLException {
//                        return c.prepareStatement("select b_regnum from ord_m where b_regnum = ?");
//                    }
//                };
//                execute(c, callback);
//            }
//            System.out.println(System.currentTimeMillis() - start);
//        }
//        long start = System.currentTimeMillis();
//        for (int i = 0; i < NUMBER_OF_SELECTS; i++) {
//
//            try (lol.lolpany.bora.commons.services.reinsertableBeans.SelectLimitedBeanJdbcCallback<OrdM> ordMSelect = new lol.lolpany.bora.commons.services.reinsertableBeans.SelectLimitedBeanJdbcCallback<>(OrdM.class,
//                    "b_regnum = ?")) {
//
//
//                ordMSelect.createPreparedStatement(c);
//                OrdM ordM = ordMSelect.select(5468460 + i);
//            }
//
//        }
//        System.out.println(System.currentTimeMillis() - start);
//
//        start = System.currentTimeMillis();
//        for (int i = 0; i < NUMBER_OF_SELECTS; i++) {
//
//            try (lol.lolpany.bora.commons.services.reinsertableBeans.SelectLimitedBeanJdbcCallback<ExtOrdM> ordMSelect = new lol.lolpany.bora.commons.services.reinsertableBeans.SelectLimitedBeanJdbcCallback<>(ExtOrdM.class,
//                    "b_regnum = ?")) {
//
//
//                ordMSelect.createPreparedStatement(c);
//                ExtOrdM ordM = ordMSelect.select(5468460 + i);
//            }
//
//        }
//        System.out.println(System.currentTimeMillis() - start);
//        start = System.currentTimeMillis();
//        for (int i = 0; i < NUMBER_OF_SELECTS; i++) {
//
//            try (lol.lolpany.bora.commons.services.reinsertableBeans.SelectBeanInnerJdbcCallback<Order> ordMSelect = new lol.lolpany.bora.commons.services.reinsertableBeans.SelectBeanInnerJdbcCallback<>(Order.class,
//                    Collections.singletonList("b_regnum"), null)) {
//
//
//                ordMSelect.createPreparedStatement(c);
//                Order ordM = ordMSelect.select(5468454 + i);
//            }
//
//        }
//        System.out.println(System.currentTimeMillis() - start);
//
//    }
//
//    private void warmUpConnection(Connection c) throws SQLException {
//        try (lol.lolpany.bora.commons.services.reinsertableBeans.SelectLimitedBeanJdbcCallback<ExtOrdM> ordMSelect = new lol.lolpany.bora.commons.services.reinsertableBeans.SelectLimitedBeanJdbcCallback<>(ExtOrdM.class,
//                "b_regnum = ?")) {
//            ordMSelect.createPreparedStatement(c);
//            long start = System.currentTimeMillis();
//            for (int i = 0; i < NUMBER_OF_SELECTS; i++) {
//                ExtOrdM ordM = ordMSelect.select(5468454 + i);
//            }
//        }
//        try (lol.lolpany.bora.commons.services.reinsertableBeans.SelectBeanInnerJdbcCallback<Order> ordMSelect = new lol.lolpany.bora.commons.services.reinsertableBeans.SelectBeanInnerJdbcCallback<>(Order.class, "ord_m",
//                Collections.singletonList("b_regnum"), null)) {
//            ordMSelect.createPreparedStatement(c);
//            long start = System.currentTimeMillis();
//            for (int i = 0; i < NUMBER_OF_SELECTS; i++) {
//                Order ordM = ordMSelect.select(5468454 + i);
//            }
//        }
//    }
//
//    @Test
//    public void go() throws SQLException {
//        Connection c = DriverManager.getConnection(JDBC_CONNECTION, getScheme(2), "SYS");
//        try (SelectBeanJdbcCallback<IBuyers, Buyers> buyersSelect = new SelectBeanJdbcCallback<>(IBuyers.class,
//                Buyers.class, " select * from buyers where b_buy = ?")) {
//            buyersSelect.createPreparedStatement(c);
//            long start = System.currentTimeMillis();
//            for (int i = 0; i < NUMBER_OF_SELECTS; i++) {
//                IBuyers buyers = buyersSelect.select(1341006 + i);
//                int a = 5;
//            }
//        }
//    }
}
