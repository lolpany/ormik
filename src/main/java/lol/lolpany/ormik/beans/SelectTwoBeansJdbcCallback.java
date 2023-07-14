package lol.lolpany.ormik.beans;

import org.apache.commons.lang3.tuple.Triple;
import org.intellij.lang.annotations.Language;
import reactor.util.function.Tuple2;
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

public class SelectTwoBeansJdbcCallback<I1, I2> extends AbstractPreparedStatementInnerJdbcCallback {
    private final String query;
    private final String generatedQuery;
    private final Class<I1> one;
    private final Class<I2> two;
    private final int parameterCount;

    public SelectTwoBeansJdbcCallback(@Language("sql") String query, Class<I1> one, Class<I2> two) {
        if (one == null || !one.isInterface() || two == null || !two.isInterface()) {
            throw new IllegalArgumentException();
        }
        this.query = query;
        this.generatedQuery = getBeanCache().select(query, one, two);
        this.one = one;
        this.two = two;
        this.parameterCount = identifyParameterCount(query);
    }

    @Override
    protected PreparedStatement createInnerPreparedStatement(Connection c) throws SQLException {
        return c.prepareStatement(generatedQuery);
    }

    public Tuple2<Map<Long, I1>, Map<Long, I2>> select(Object... keyValues) throws SQLException {
        setParametersValues(ps, parameterCount, keyValues);
        Map<Long, I1> codeToOne = new HashMap<>();
        Map<Long, I2> codeToTwo = new HashMap<>();
        try (ResultSet rs = ps.executeQuery()) {
            Tuple2<Triple<Class<?>, Integer, List<Field>>, Triple<Class<?>, Integer, List<Field>>> oneTwo =
                    getBeanCache().queryToTwoBeansSelectCache.get(generatedQuery);
            while (rs.next()) {
                processTable(codeToOne, rs, oneTwo.getT1(), 0);
                processTable(codeToTwo, rs, oneTwo.getT2(), oneTwo.getT1().getRight().size());
            }
        }
        return Tuples.of(codeToOne, codeToTwo);
    }
}
