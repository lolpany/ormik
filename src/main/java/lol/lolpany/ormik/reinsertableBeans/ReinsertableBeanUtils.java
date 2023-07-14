package lol.lolpany.ormik.reinsertableBeans;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.Date;
import java.util.*;
import java.util.regex.Pattern;

import static com.google.common.base.CaseFormat.*;
import static java.math.BigDecimal.ZERO;
import static lol.lolpany.ormik.reinsertableBeans.EnumUtils.getById;
import static org.apache.commons.collections4.MapUtils.isEmpty;
import static org.apache.commons.lang3.ArrayUtils.toPrimitive;
import static org.apache.commons.lang3.StringUtils.countMatches;
import static lol.lolpany.ormik.reinsertableBeans.DateUtils.toLocalDate;
import static lol.lolpany.ormik.reinsertableBeans.DateUtils.toSqlDate;
import static lol.lolpany.ormik.persistence.JdbcUtils.setArray;

public final class ReinsertableBeanUtils {

    public static final int ONE_BATCH_SIZE = 1;
    public static final String COLUMN_PREFIX_SEPARATOR = "";
    public static final int COLUMN_PREFIX_SEPARATOR_LENGTH = COLUMN_PREFIX_SEPARATOR.length();
    private static final String COLUMN_PREFIX_LETTER = "";
    public static final String COLUMN_PREFIX = COLUMN_PREFIX_LETTER + COLUMN_PREFIX_SEPARATOR;
    private static final String SEQUENCE_PREFIX = "seq_";
    private static final String GETTER_PREFIX = "get";
    private static final int GETTER_PREFIX_LENGTH = GETTER_PREFIX.length();
    private static final String SETTER_PREFIX = "set";
    private static final int SETTER_PREFIX_LENGTH = SETTER_PREFIX.length();
    private static final int AVERAGE_NUMBER_OF_FIELDS = 50;
    public static final String SELECT = "select";
    private static final String WHERE = "where";
    private static final Pattern WHERE_PATTERN = Pattern.compile(WHERE);

    private ReinsertableBeanUtils() {
    }

    /**
     * Used, for example, when need to get acc_f from accfhist and leave fields that not present
     * in accfhist as they were.
     */
    public static void copyBeanToBean(IBean from, IBean to) throws IllegalAccessException, NoSuchFieldException {
        for (Field field : from.getClass().getDeclaredFields()) {
            if (field.get(from) != null) {
                if (!isEmpty(to.getOtherFields()) && to.getOtherFields().keySet().contains(field.getName())) {
                    to.getOtherFields().put(field.getName(), field.get(from));
                } else {
                    to.getClass().getField(field.getName()).set(to, field.get(from));
                }
            }
        }

        for (Map.Entry<String, Object> otherField : from.getOtherFields().entrySet()) {
            if (otherField.getValue() != null) {
                if (!isEmpty(to.getOtherFields()) && to.getOtherFields().keySet().contains(otherField.getKey())) {
                    to.getOtherFields().put(otherField.getKey(), otherField.getValue());
                } else {
                    to.getClass().getField(otherField.getKey()).set(to, otherField.getValue());
                }
            }
        }
    }

    public static <T extends IInsertableBean> void setField(PreparedStatement ps, T bean, int i,
                                                            TableColumnMetaData tableColumn, Field field)
            throws SQLException {
        if (field != null) {
            try {
                setPreperadStetmentParameter(bean, ps, i, field, tableColumn);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        } else if (bean.getOtherFields() != null &&
                bean.getOtherFields().get(tableColumn.name) != null) {
            ps.setObject(i, bean.getOtherFields().get(tableColumn.name));
        } else if (tableColumn.type == Types.NUMERIC) {
            ps.setObject(i, ZERO);
        } else {
            ps.setObject(i, null);
        }
    }

    static <T> void setPreperadStetmentParameter(T bean, PreparedStatement ps, int index, Field field,
                                                 TableColumnMetaData columnMetaData)
            throws IllegalAccessException, SQLException {
        if (field.getType().isEnum()) {
            IdEnum idEnum = ((IdEnum) field.get(bean));
            if (idEnum != null) {
                ps.setInt(index, ((IdEnum) field.get(bean)).getId());
            } else {
                ps.setInt(index, 0);
            }
        } else {
            Object fieldValue = field.get(bean);
            if (fieldValue != null) {
                if (fieldValue.getClass() == Boolean.class) {
                    ps.setInt(index, ((Boolean) fieldValue) ? 1 : 2);
                } else if (fieldValue.getClass() == LocalDate.class) {
                    ps.setDate(index, toSqlDate((LocalDate) fieldValue));
                } else {
                    ps.setObject(index, fieldValue);
                }
            } else if (columnMetaData.type == Types.NUMERIC && field.getAnnotationsByType(Nullable.class).length == 0) {
                ps.setObject(index, 0);
            } else {
                ps.setObject(index, null);
            }
        }
    }

    public static String beanFieldNameToDbFieldName(String beanFieldName) {
        String result;
        if (!beanFieldName.startsWith(COLUMN_PREFIX_SEPARATOR)) {
            result = COLUMN_PREFIX + LOWER_CAMEL.to(LOWER_UNDERSCORE, beanFieldName);
        } else {
            result = COLUMN_PREFIX_LETTER + LOWER_CAMEL.to(LOWER_UNDERSCORE, beanFieldName);
        }
        return result;
    }

    public static String dbFieldNameToBeanFieldName(String dbFieldName) {
        String result;
        if (dbFieldName.startsWith(COLUMN_PREFIX)) {
            result = dbFieldName.substring(COLUMN_PREFIX.length());
        } else {
            result = dbFieldName;
        }
        return LOWER_UNDERSCORE.to(LOWER_CAMEL, result);
    }

    private static <T> Class<? super T> identifyPersistableParent(Class<T> beanClass) {
        Class<? super T> parent = beanClass;
        while (parent != null && parent.getAnnotation(Table.class) == null &&
                parent.getSuperclass() != ReinsertableBean.class) {
            parent = parent.getSuperclass();
        }
        return parent;
    }

    public static <T> String fetchTableNameFromClass(Class<T> beanClass) {
        Class<? super T> parent = identifyPersistableParent(beanClass);
        Table tableAnnotation = parent.getAnnotation(Table.class);
        if (tableAnnotation != null) {
            return tableAnnotation.name();
        } else {
            return UPPER_CAMEL.to(LOWER_UNDERSCORE, beanClass.getSimpleName());
        }
    }

    public static <T> String fetchPrimaryKeyNameFromClass(Class<T> beanClass) {
        String result = null;
        Class<? super T> parent = identifyPersistableParent(beanClass);
        if (parent != null) {
            for (Field field : parent.getDeclaredFields()) {
                if (field.getAnnotation(Id.class) != null) {
                    result = beanFieldNameToDbFieldName(field.getName());
                }
            }
        }
        return result;
    }

    public static <T> String fetchPrimaryKeySequenceNameFromClass(Class<T> beanClass) {
        Class<? super T> parent = identifyPersistableParent(beanClass);
        SequenceGenerator sequenceAnnotation = parent.getAnnotation(SequenceGenerator.class);
        if (sequenceAnnotation != null) {
            return sequenceAnnotation.sequenceName();
        } else {
            return SEQUENCE_PREFIX + fetchTableNameFromClass(beanClass);
        }
    }

    public static int identifyParameterCount(String whereAddition) {
        return whereAddition != null ? countMatches(whereAddition, '?') : 0;
    }

    static int identifyParameterCount(List<String> keyColumns, String whereAddition) {
        int keyColumnsSize = keyColumns != null ? keyColumns.size() : 0;
        return keyColumnsSize + (whereAddition != null ? countMatches(whereAddition, '?') : 0);
    }

    public static void setParametersValues(PreparedStatement ps, int parameterCount, Object[] keyValues)
            throws SQLException {
        if (keyValues != null) {
            // if array passed as one parameter
            if (parameterCount == 1 && keyValues.length == 1) {
                setParameter(ps, keyValues[0], 1);
            } else if (parameterCount == 1 && keyValues.length > 1) {
                setParameter(ps, keyValues, 1);
            } else if (parameterCount > 1) {
                for (int i = 1; i <= keyValues.length; i++) {
                    setParameter(ps, keyValues[i - 1], i);
                }
            }
        }
    }

    public static void setParameter(PreparedStatement ps, Object value, int parameterIndex) throws SQLException {
        if (value != null) {
            Class<?> keyClass = value.getClass();
            if (Collection.class.isAssignableFrom(keyClass)) {
                // no way to identify type of colletion elements, use array
                throw new SQLException();
            } else if (keyClass == int.class || keyClass == Integer.class) {
                ps.setInt(parameterIndex, (Integer) value);
            } else if (keyClass == long.class || keyClass == Long.class) {
                ps.setLong(parameterIndex, (Long) value);
            } else if (keyClass == BigDecimal.class) {
                ps.setBigDecimal(parameterIndex, (BigDecimal) value);
            } else if (keyClass == String.class) {
                ps.setString(parameterIndex, (String) value);
            } else if (keyClass == java.sql.Date.class) {
                ps.setDate(parameterIndex, (java.sql.Date) value);
            } else if (keyClass == java.sql.Timestamp.class) {
                ps.setTimestamp(parameterIndex, (java.sql.Timestamp) value);
            } else if (keyClass == LocalDate.class) {
                ps.setDate(parameterIndex, toSqlDate((LocalDate) value));
            } else if (int[].class.isAssignableFrom(keyClass)) {
                setArray(parameterIndex, (int[]) value, ps);
            } else if (Integer[].class.isAssignableFrom(keyClass)) {
                setArray(parameterIndex, toPrimitive((Integer[]) value), ps);
            } else if (long[].class.isAssignableFrom(keyClass)) {
                setArray(parameterIndex, (long[]) value, ps);
            } else if (Long[].class.isAssignableFrom(keyClass)) {
                setArray(parameterIndex, toPrimitive((Long[]) value), ps);
            } else if (String[].class.isAssignableFrom(keyClass)) {
                setArray(parameterIndex, (String[]) value, ps);
            } else if (IdEnum.class.isAssignableFrom(keyClass)) {
                ps.setInt(parameterIndex, ((IdEnum) value).getId());
            }
        } else {
            ps.setObject(parameterIndex, null);
        }
    }

    static Map<String, Pair<Integer, Field>> mapColumnNamesToBeanFields(Class<?> beanClass) {

        Map<String, Pair<Integer, Field>> res = new LinkedHashMap<>();

        Class<?> clazz = beanClass;
        int i = 1;
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getAnnotation(Transient.class) == null) {
                    field.setAccessible(true);
                    res.put(beanFieldNameToDbFieldName(field.getName()), new ImmutablePair<>(i++, field));
                }
            }

            Class<?> superclass = clazz.getSuperclass();
            clazz = superclass != Object.class ? superclass : null;
        }

        return res;
    }

    public static <T> void setField(T bean, Field field, ResultSet rs, int columnIndex)
            throws SQLException, IllegalAccessException {
        field.setAccessible(true);
        Class<?> fieldType = field.getType();

        if (fieldType.isEnum()) {
            field.set(bean, getById(fieldType.asSubclass(IdEnum.class), rs.getInt(columnIndex)));
        } else {
            if (fieldType == int.class) {
                field.set(bean, rs.getInt(columnIndex));
            } else if (fieldType == Integer.class) {
                int intVal = rs.getInt(columnIndex);
                field.set(bean, rs.wasNull() ? null : intVal);
            } else if (fieldType == boolean.class) {
                field.set(bean, rs.getInt(columnIndex) == 1);
            } else if (fieldType == Boolean.class) {
                int val = rs.getInt(columnIndex);
                field.set(bean, rs.wasNull() ? null : val == 1);
            } else if (fieldType == long.class) {
                field.set(bean, rs.getLong(columnIndex));
            } else if (fieldType == Long.class) {
                long longVal = rs.getLong(columnIndex);
                field.set(bean, rs.wasNull() ? null : longVal);
            } else if (fieldType == double.class || fieldType == Double.class) {
                double doubleVal = rs.getDouble(columnIndex);
                field.set(bean, rs.wasNull() ? null : doubleVal);
            } else if (fieldType == Date.class || fieldType == java.sql.Date.class) {
                field.set(bean, rs.getDate(columnIndex));
            } else if (fieldType == LocalDate.class) {
                field.set(bean, toLocalDate(rs.getDate(columnIndex)));
            } else if (fieldType == Timestamp.class) {
                field.set(bean, rs.getTimestamp(columnIndex));
            } else if (fieldType == String.class) {
                field.set(bean, rs.getString(columnIndex));
            } else if (fieldType == byte[].class || fieldType == Blob.class) {
                field.set(bean, rs.getBytes(columnIndex));
            } else {
                field.set(bean, rs.getObject(columnIndex));
            }
        }
    }

    public static String extractWhereClause(String query) {
        String result;
        String normalizedQuery = query;
        if (!StringUtils.isEmpty(normalizedQuery)) {
            normalizedQuery = normalizedQuery.toLowerCase().trim();
        }
        if (StringUtils.isEmpty(normalizedQuery) ||
                (normalizedQuery.startsWith(SELECT) && !normalizedQuery.contains(WHERE))) {
            result = null;
        } else if (!normalizedQuery.contains(WHERE)) {
            result = normalizedQuery;
        } else {
            result = WHERE_PATTERN.split(query.toLowerCase(), 2)[1];
        }
        return result;
    }
}
