package lol.lolpany.ormik.beans;

import org.apache.commons.lang3.tuple.Triple;
import org.intellij.lang.annotations.Language;
import reactor.util.function.Tuple5;
import reactor.util.function.Tuples;
import lol.lolpany.ormik.persistence.AbstractPreparedStatementInnerJdbcCallback;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static lol.lolpany.ormik.beans.BeanCache.getBeanCache;
import static lol.lolpany.ormik.beans.BeanUtils.processTable;
import static lol.lolpany.ormik.reinsertableBeans.ReinsertableBeanUtils.identifyParameterCount;
import static lol.lolpany.ormik.reinsertableBeans.ReinsertableBeanUtils.setParametersValues;

public class SelectFiveBeansJdbcCallback<I1, I2, I3, I4, I5> extends AbstractPreparedStatementInnerJdbcCallback {
    private final String query;
    private final String generatedQuery;
    private final Class<I1> one;
    private final Class<I2> two;
    private final Class<I3> three;
    private final Class<I4> four;
    private final Class<I5> five;
    private final int parameterCount;

    public SelectFiveBeansJdbcCallback(@Language("sql") String query, Class<I1> one, Class<I2> two,
                                       Class<I3> three, Class<I4> four, Class<I5> five) {
        if (one == null || !one.isInterface() || two == null || !two.isInterface() || three == null
                || !three.isInterface() || four == null || !four.isInterface() || five == null || !five.isInterface()) {
            throw new IllegalArgumentException();
        }
        this.query = query;
        this.generatedQuery = getBeanCache().select(query, one, two, three, four, five);
        this.one = one;
        this.two = two;
        this.three = three;
        this.four = four;
        this.five = five;
        this.parameterCount = identifyParameterCount(query);
    }

    @Override
    protected PreparedStatement createInnerPreparedStatement(Connection c) throws SQLException {
        return c.prepareStatement(generatedQuery);
    }

    public Tuple5<Map<Long, I1>, Map<Long, I2>, Map<Long, I3>, Map<Long, I4>, Map<Long, I5>> select(Object... keyValues)
            throws SQLException {
        setParametersValues(ps, parameterCount, keyValues);
        Map<Long, I1> codeToOne = new HashMap<>();
        Map<Long, I2> codeToTwo = new HashMap<>();
        Map<Long, I3> codeToThree = new HashMap<>();
        Map<Long, I4> codeToFour = new HashMap<>();
        Map<Long, I5> codeToFive = new HashMap<>();
        try (ResultSet rs = ps.executeQuery()) {
            Tuple5<Triple<Class<?>, Integer, List<Field>>, Triple<Class<?>, Integer, List<Field>>,
                    Triple<Class<?>, Integer, List<Field>>, Triple<Class<?>, Integer, List<Field>>,
                    Triple<Class<?>, Integer, List<Field>>> oneTwoThreeFourFive =
                    getBeanCache().queryToFiveBeansSelectCache.get(generatedQuery);
            while (rs.next()) {
                processTable(codeToOne, rs, oneTwoThreeFourFive.getT1(), 0);
                processTable(codeToTwo, rs, oneTwoThreeFourFive.getT2(), oneTwoThreeFourFive.getT1().getRight().size());
                processTable(codeToThree, rs, oneTwoThreeFourFive.getT3(), oneTwoThreeFourFive.getT1().getRight().size()
                        + oneTwoThreeFourFive.getT2().getRight().size());
                processTable(codeToFour, rs, oneTwoThreeFourFive.getT4(), oneTwoThreeFourFive.getT1().getRight().size()
                        + oneTwoThreeFourFive.getT2().getRight().size() +
                        oneTwoThreeFourFive.getT3().getRight().size());
                processTable(codeToFive, rs, oneTwoThreeFourFive.getT5(), oneTwoThreeFourFive.getT1().getRight().size()
                        + oneTwoThreeFourFive.getT2().getRight().size() + oneTwoThreeFourFive.getT3().getRight().size()
                        + oneTwoThreeFourFive.getT4().getRight().size());
            }
        }
        return Tuples.of(codeToOne, codeToTwo, codeToThree, codeToFour, codeToFive);
    }
}
