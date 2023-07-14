package lol.lolpany.ormik.regression;//package lol.lolpany.bora.regression;
//
//import org.junit.Assert;
//import org.junit.Ignore;
//import org.junit.Test;
//import org.w3c.dom.Element;
//import org.xml.sax.SAXException;
//import org.xmlunit.diff.*;
//import org.xmlunit.util.Nodes;
//import lol.lolpany.bora.ws.rs.services.test.net.RestVsRestResponseComparator;
//import lol.lolpany.commons.jdbc.JdbcUtils;
//import lol.lolpany.commons.jdbc.OracleConnectionProvider;
//import lol.lolpany.commons.persistence.AbstractPreparedStatementWork;
//import lol.lolpany.security.UserType;
//import lol.lolpany.ws.impl.transfersearch.beans.TransferSearchRequest;
//import lol.lolpany.xml.bo.autogen.LanguageType;
//
//import javax.xml.bind.JAXBException;
//import javax.xml.parsers.ParserConfigurationException;
//import java.io.IOException;
//import java.io.UnsupportedEncodingException;
//import java.net.MalformedURLException;
//import java.nio.charset.StandardCharsets;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.text.DateFormat;
//import java.util.List;
//import java.util.concurrent.ArrayBlockingQueue;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.ThreadPoolExecutor;
//
//import static java.lang.Long.MAX_VALUE;
//import static java.util.Collections.emptyList;
//import static java.util.concurrent.TimeUnit.MILLISECONDS;
//import static lol.lolpany.commons.formatter.FormatUtils.dottedDate;
//import static lol.lolpany.commons.jdbc.JdbcUtils.execute;
//import static lol.lolpany.commons.jdbc.OracleConnectionProvider.getDataSource;
//import static lol.lolpany.security.UserType.INNER;
//
//@Ignore
//public class WebServiceRegressionTest {
//    private static final ThreadLocal<RestVsRestResponseComparator> RESPONSE_COMPARATOR = ThreadLocal.withInitial(() -> {
//        try {
//            return RestVsRestResponseComparator.newInstance(TransferSearchRequest.class,
////                    "http://10.0.0.129:8160/bora",
//                    "http://10.0.0.129:8170/bora",
//                    "http://10.0.0.129:8990/bora");
//        } catch (MalformedURLException | ParserConfigurationException | UnsupportedEncodingException | JAXBException e) {
//            throw new RuntimeException(e);
//        }
//    });
//
//    @Test
//    public void test() throws Exception {
//        System.out.println("STARTED");
//        JdbcUtils.execute(OracleConnectionProvider.getDataSource(), new AbstractPreparedStatementWork<Void>() {
//            @Override
//            public PreparedStatement createPreparedStatement(Connection c) throws SQLException {
//                return c.prepareStatement("SELECT b_req from xml_jour where B_REQUEST = ?");
//            }
//
//            @Override
//            public Void doInPreparedStatement(PreparedStatement ps) throws SQLException {
//                ps.setLong(1, 25);
//                System.out.println(ps.getConnection().getSchema());
//
//                final ArrayBlockingQueue<WebServiceRegressionTest.TestItem> queue = new ArrayBlockingQueue<>(100, true);
//                int threads = 16;
//                final ThreadPoolExecutor executorService = new ThreadPoolExecutor(threads, threads, MAX_VALUE, MILLISECONDS, new ArrayBlockingQueue<>(threads, false));
//                for (int i = 0; i < threads; i++) {
//                    executorService.execute(new WebServiceRegressionTest.MyRunnable(queue));
//                }
//                try {
//                    int i = 0;
////                    java.sql.Date date = new java.sql.Date(addDays(today(), ThreadLocalRandom.current().nextInt(1, 40)).getTime());
////                    ps.setDate(1, date);
////                    ps.setDate(2, date);
////                    ps.setDate(3, date);
////                    ps.setDate(4, date);
//
//                    try (ResultSet rs = ps.executeQuery()) {
//                        while (rs.next()) {
//                            if (i++ % 100 == 0) {
//                                System.out.println(i);
//                            }
//                            if (true) {
//                                try {
//                                    queue.put(new WebServiceRegressionTest.TestItem(
//                                            rs.getString(1)));
//                                } catch (InterruptedException e) {
//                                    throw new RuntimeException(e);
//                                }
//                            }
//                        }
//                    }
//                } finally {
//                    for (int i = 0; i < threads; i++) {
//                        try {
//                            queue.put(MyRunnable.STOP_ITEM);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    executorService.shutdown();
//                }
//                return null;
//            }
//        });
//        //мой код
//    }
//
//    static final class MyRunnable implements Runnable {
//
//        static final WebServiceRegressionTest.TestItem STOP_ITEM = new WebServiceRegressionTest.TestItem(null);
//
//        BlockingQueue<WebServiceRegressionTest.TestItem> queue;
//
//        MyRunnable(BlockingQueue<WebServiceRegressionTest.TestItem> queue) {
//            this.queue = queue;
//        }
//
//        @Override
//        public void run() {
//            Thread t = Thread.currentThread();
//            while (!t.isInterrupted()) {
//                try {
//                    WebServiceRegressionTest.TestItem item = queue.take();
//                    if (STOP_ITEM == item) {
//                        break;
//                    }
//                    item.run();
//                } catch (InterruptedException ignored) {
//
//                }
//            }
//        }
//    }
//
//    public static final class TestItem implements DifferenceEvaluator {
//
//        final String request;
//
//        public TestItem(String request) {
//            this.request = request;
//        }
//
//        @Override
//        public ComparisonResult evaluate(Comparison comparison, ComparisonResult comparisonResult) {
//            Comparison.Detail cd = comparison.getControlDetails();
//            return comparisonResult;
//        }
//
//        void run() {
//            test(request);
//        }
//
//        void test(String request) {
//
//            DateFormat df = FormatUtils.dottedDate();
//
//            testWithUser(request, UserType.INNER, "after", "", "3299");
//        }
//
//        void testWithUser(String r, UserType userType, String userId, String buyerId, String buyerCode) {
//            testWithLanguage(r, LanguageType.RU);
//        }
//
//        void testWithLanguage(String r, LanguageType l) {
//            testWithAlien(r);
//        }
//
//        void testWithAlien(String r) {
////            test(r, false);
//            test(r, false);
//        }
//
//        void test(String r, boolean alien) {
//            List<Difference> differences = null;
//            RestVsRestResponseComparator rc = RESPONSE_COMPARATOR.get();
//
//            try {
//                DefaultNodeMatcher nodeMatcher = getNodeMatcher();
//                differences = rc.compare(r, alien, this, nodeMatcher);
//            } catch (JAXBException | ParserConfigurationException | IOException | SAXException e) {
//                e.printStackTrace();
//                Assert.fail(e.getMessage());
//            }
//            if (!differences.isEmpty()) {
//                try {
//                    System.out.println(rc.getRequestContent().toString(StandardCharsets.UTF_8.name()));
//                } catch (UnsupportedEncodingException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//
//            if (differences.size() > 0) {
//                System.out.println(differences);
//            }
//            Assert.assertEquals(emptyList(), differences);
//        }
//
//    }
//
//    static DefaultNodeMatcher getNodeMatcher() {
//        return new DefaultNodeMatcher(ElementSelectors.byName, ElementSelectors.byNameAndAllAttributes, ElementSelectors.selectorForElementNamed("Position", new ElementSelector() {
//            private boolean bothNullOrEqual(Object o1, Object o2) {
//                return o1 == null ? o2 == null : o1.equals(o2);
//            }
//
//            @Override
//            public boolean canBeCompared(Element controlElement, Element testElement) {
//                boolean result = controlElement != null
//                        && testElement != null
//                        && bothNullOrEqual(Nodes.getQName(controlElement),
//                        Nodes.getQName(testElement));
//                return result;
//            }
//        }));
//    }
//}
