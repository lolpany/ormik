package lol.lolpany.ormik.beans;

import org.apache.commons.lang3.tuple.Triple;
import org.intellij.lang.annotations.Language;
import reactor.util.function.Tuple3;
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

public class SelectThreeBeansJdbcCallback<I1, I2, I3> extends AbstractPreparedStatementInnerJdbcCallback {
    private final String query;
    private final String generatedQuery;
    private final Class<I1> one;
    private final Class<I2> two;
    private final Class<I3> three;
    private final int parameterCount;

    public SelectThreeBeansJdbcCallback(@Language("sql") String query, Class<I1> one, Class<I2> two,
                                        Class<I3> three) {
        if (one == null || !one.isInterface() || two == null || !two.isInterface() || three == null
                || !three.isInterface()) {
            throw new IllegalArgumentException();
        }
        this.query = query;
        this.generatedQuery = getBeanCache().select(query, one, two, three);
        this.one = one;
        this.two = two;
        this.three = three;
        this.parameterCount = identifyParameterCount(query);
    }

    @Override
    protected PreparedStatement createInnerPreparedStatement(Connection c) throws SQLException {
        return c.prepareStatement(generatedQuery);
    }

    public Tuple3<Map<Long, I1>, Map<Long, I2>, Map<Long, I3>> select(Object... keyValues) throws SQLException {
        setParametersValues(ps, parameterCount, keyValues);
        Map<Long, I1> codeToOne = new HashMap<>();
        Map<Long, I2> codeToTwo = new HashMap<>();
        Map<Long, I3> codeToThree = new HashMap<>();
        try (ResultSet rs = ps.executeQuery()) {
            Tuple3<Triple<Class<?>, Integer, List<Field>>, Triple<Class<?>, Integer, List<Field>>,
                    Triple<Class<?>, Integer, List<Field>>> oneTwoThree =
                    getBeanCache().queryToThreeBeansSelectCache.get(generatedQuery);
            while (rs.next()) {
                processTable(codeToOne, rs, oneTwoThree.getT1(), 0);
                processTable(codeToTwo, rs, oneTwoThree.getT2(), oneTwoThree.getT1().getRight().size());
                processTable(codeToThree, rs, oneTwoThree.getT3(), oneTwoThree.getT1().getRight().size()
                        + oneTwoThree.getT2().getRight().size());
            }
        }
        return Tuples.of(codeToOne, codeToTwo, codeToThree);
    }
}
