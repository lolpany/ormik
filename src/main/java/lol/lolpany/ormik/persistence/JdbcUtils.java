package lol.lolpany.ormik.persistence;

import org.apache.commons.io.input.CharSequenceReader;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.HibernateException;
import lol.lolpany.ormik.jdbc.ICallableStatementJdbcCallback;
import lol.lolpany.ormik.jdbc.IPreparedStatementJdbcCallback;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Calendar;

import static java.sql.Types.*;
import static java.util.Calendar.getInstance;
import static org.apache.commons.lang3.StringUtils.replaceAll;


public abstract class JdbcUtils {

    public static final int NOT_VALUE = 9500001;

    public static final String NUMBERS_PARAM = "SELECT column_value FROM TABLE(CAST(? AS NUMBER_ARRAY))";
    public static final String DATES_PARAM = "SELECT column_value FROM TABLE(CAST(? AS DATE_ARRAY))";
    public static final String STRINGS_PARAM = "SELECT column_value FROM TABLE(CAST(? AS VARCHAR2_ARRAY))";

    public static final Date MIN_DATE = new Date(-2209003200000L);//01.01.1900
    public static final Date MAX_DATE = new Date(4133880000000L);//31.12.2100

    public static String buildCardinalityNumbersParam(int cardinality) {

        String crdnlt;
        if (cardinality < 3000) {
            crdnlt = "3000";
        } else if (cardinality < 6000) {
            crdnlt = "6000";
        } else {
            crdnlt = "9000";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT /*+ CARDINALITY(t_").append(crdnlt).append(" ").append(crdnlt)
                .append(") */ column_value FROM TABLE(CAST(? AS NUMBER_ARRAY)) t_").append(crdnlt);
        return sb.toString();
    }

    /**
     * <p>
     * Passes Oracle's array to SQL Query.
     * </p>
     * The type <tt>number_array</tt> must be created before
     * <pre>CREATE OR REPLACE TYPE number_array IS TABLE OF NUMBER(18,0)</pre>
     */
    public static void setArray(int index, long[] value, PreparedStatement st) throws SQLException {
        setArray(index, "NUMBER_ARRAY", value, st);
    }

    /**
     * <p>
     * Passes Oracle's array to SQL Query.
     * </p>
     * The type <tt>number_array</tt> must be created before
     * <pre>CREATE OR REPLACE TYPE number_array IS TABLE OF NUMBER(18,0)</pre>
     */
    public static void setArray(int index, int[] value, PreparedStatement st) throws SQLException {
        setArray(index, "NUMBER_ARRAY", value, st);
    }


    /**
     * The type <tt>date_array</tt> must be created before
     * <pre>CREATE OR REPLACE TYPE "DATE_ARRAY" IS TABLE OF DATE</pre>
     */
    public static void setArray(int index, Date[] value, PreparedStatement st) throws SQLException {
        setArray(index, "DATE_ARRAY", value, st);
    }

    /**
     * The type <tt>bigdecimal_array</tt> must be created before
     * <pre>CREATE OR REPLACE TYPE "BIGDECIMAL_ARRAY" IS TABLE OF NUMBER(18,4)</pre>
     */
    public static void setArray(int index, BigDecimal[] value, PreparedStatement st) throws SQLException {
        setArray(index, "BIGDECIMAL_ARRAY", value, st);
    }


    /**
     * The type <tt>varchar2_array</tt> must be created before
     * <pre>CREATE OR REPLACE TYPE "VARCHAR2_ARRAY" IS TABLE OF VARCHAR2(32767)</pre>
     */
    public static void setArray(int index, String[] value, PreparedStatement st) throws SQLException {
        setArray(index, "VARCHAR2_ARRAY", value, st);
    }

    public static void setArray(int index, String type, Object value, PreparedStatement st) throws SQLException {
//        OracleConnection c = st.getConnection().unwrap(OracleConnection.class);
//        st.setArray(index, c.createOracleArray(type, value));
    }

    public static void setInt(int parameterIndex, Integer value, PreparedStatement st) throws SQLException {
        if (value != null) {
            st.setInt(parameterIndex, value);
        } else {
            st.setNull(parameterIndex, NUMERIC);
        }
    }

    public static void setLong(int parameterIndex, Long value, PreparedStatement st) throws SQLException {
        if (value != null) {
            st.setLong(parameterIndex, value);
        } else {
            st.setNull(parameterIndex, NUMERIC);
        }
    }

    public static void setDouble(PreparedStatement ps, int parameterIndex, Double value) throws SQLException {
        if (value != null) {
            ps.setDouble(parameterIndex, value);
        } else {
            ps.setNull(parameterIndex, NUMERIC);
        }
    }

    public static void setBigDecimal(int parameterIndex, BigDecimal value, PreparedStatement ps) throws SQLException {
        if (value != null) {
            ps.setBigDecimal(parameterIndex, value);
        } else {
            ps.setNull(parameterIndex, NUMERIC);
        }
    }

    public static void setTimestamp(PreparedStatement ps, int parameterIndex, Timestamp value) throws SQLException {
        if (value != null) {
            ps.setTimestamp(parameterIndex, value);
        } else {
            ps.setNull(parameterIndex, TIMESTAMP);
        }
    }

    public static void setClob(PreparedStatement ps, int parameterIndex, CharSequence value) throws SQLException {
        if (value != null) {
            ps.setClob(parameterIndex, new CharSequenceReader(value));
        } else {
            ps.setNull(parameterIndex, CLOB);
        }
    }

    public static void setBlob(PreparedStatement ps, int parameterIndex, Blob value) throws SQLException {
        if (value != null) {
            ps.setBlob(parameterIndex, value);
        } else {
            ps.setNull(parameterIndex, BLOB);
        }
    }

    /**
     * For given <tt>source</tt>:
     * <ol>
     * <li>Escapes the exist '%' characters</li>
     * <li>Wraps with '%' wildcards</li>
     * </ol>
     *
     * @param source any text
     * @return value for parameter in SQL LIKE condition
     */
    public static String wildcardUpperLike(String source) {
        StringBuilder b = new StringBuilder(source.length());
        b.append('%');
        return replaceAll(endWildcardUpperLike(source, b), "Ё", "Е");
    }

    /**
     * For given <tt>source</tt>:
     * <ol>
     * <li>Escapes the exist '%' characters</li>
     * <li>Appends '%' wildcard at end</li>
     * </ol>
     *
     * @param source any text
     * @return value for parameter in SQL LIKE condition
     */
    public static String endWildcardUpperLike(String source, StringBuilder b) {
        final int length = source.length();
        for (int i = 0; i < length; i++) {
            char c = source.charAt(i);
            if (c == '/' || c == '%') {
                b.append('/');
                b.append(c);
            } else {
                b.append(Character.toUpperCase(c));
            }
        }
        b.append('%');
        return b.toString();
    }

    public static void closeQuietly(ResultSet r) {
        try {
            lol.lolpany.ormik.jdbc.JdbcUtils.closeQuietly(r);
        } catch (HibernateException ignored) {
        }
    }

    public static void closeQuietly(Statement s) {
        try {
            lol.lolpany.ormik.jdbc.JdbcUtils.closeQuietly(s);
        } catch (HibernateException ignored) {
        }
    }

    public static <T> T execute(Connection c, IPreparedStatementJdbcCallback<T> jdbcCallback) throws SQLException {
//        return lol.lolpany.ormik.jdbc.JdbcUtils.execute(unwrapConnection(c), jdbcCallback);
        return null;
    }

    public static <T> T execute(Connection c, ICallableStatementJdbcCallback<T> jdbcCallback) throws SQLException {
//        return lol.lolpany.ormik.jdbc.JdbcUtils.execute(unwrapConnection(c), jdbcCallback);
        return null;
    }


    public static SQLException getLockAcquisitionException(Throwable ex) {
        return findSpecifiedException(ex, 54); //ORA-00054: resource busy and acquire with NOWAIT specified or timeout expired
    }

    public static SQLException getUniqueConstraintException(Throwable ex) {
        return findSpecifiedException(ex, 1); //ORA-00001: unique constraint violated
    }

    private static SQLException findSpecifiedException(Throwable ex, int errorCode) {
        while (ex != null) {
            if (ex instanceof SQLException) {
                if (((SQLException) ex).getErrorCode() == errorCode) {
                    return (SQLException) ex;
                }
            }
            ex = ex.getCause();
        }
        return null;
    }

    public static String queryUniqueString(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String result = rs.getString(1);
                if (rs.next()) {
                    throw new IllegalStateException("Multiple rows for expected unique value");
                }
                return result;
            }
        }
        throw new IllegalStateException("No unique value");
    }

    public static Date truncate(java.util.Date src) {
        if (src == null) {
            return null;
        }
        final Calendar c = getInstance();
        c.setTime(src);
        return new Date(DateUtils.truncate(c, Calendar.DATE).getTimeInMillis());
    }

    public static java.util.Date truncate1(java.util.Date src) {
        if (src == null) {
            return null;
        }
        final Calendar c = getInstance();
        c.setTime(src);
        return new java.util.Date(DateUtils.truncate(c, Calendar.DATE).getTimeInMillis());
    }


    public static void closeQuietly(Connection c) {
        try {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        } catch (SQLException ignored) {
        }
    }
}
