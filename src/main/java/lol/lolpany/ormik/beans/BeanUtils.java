package lol.lolpany.ormik.beans;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import javax.persistence.Transient;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Predicate;

import static java.lang.reflect.Modifier.isStatic;
import static java.lang.reflect.Modifier.isTransient;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static lol.lolpany.ormik.reinsertableBeans.ReinsertableBeanUtils.*;

public final class BeanUtils {

    private static final String GETTER_PREFIX = "get";
    private static final String SETTER_PREFIX = "set";
    private static final String UPDATER_PREFIX = "update";
    private static final int GETTER_OR_SETTER_PREFIX_LENGTH = GETTER_PREFIX.length();
    private static final int UPDATER_PREFIX_LENGTH = UPDATER_PREFIX.length();
    private static final int COLUMN_PREFIX_LENGTH = COLUMN_PREFIX.length();

    private BeanUtils() {
    }

    private static SortedSet<Field> fetchFieldsByNames(Class<?> beanClass, Set<String> fieldNames) {

        SortedSet<Field> fields = new TreeSet<>(comparing(o -> beanFieldNameToDbFieldName(o.getName())));
        Class<?> clazz = beanClass;
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (!isTransient(field.getModifiers()) && field.getAnnotation(Transient.class) == null
                        && !isStatic(field.getModifiers())) {
                    String fieldName = field.getName().toLowerCase();
                    if (field.getName().startsWith(COLUMN_PREFIX)) {
                        fieldName = field.getName().substring(COLUMN_PREFIX_LENGTH);
                    } else if (field.getName().startsWith(COLUMN_PREFIX_SEPARATOR)) {
                        fieldName = field.getName().substring(COLUMN_PREFIX_SEPARATOR_LENGTH);
                    }
                    if (fieldNames.contains(fieldName)) {
                        fields.add(field);
                    }
                }
            }
            Class<?> superclass = clazz.getSuperclass();
            clazz = superclass != Object.class ? superclass : null;
        }
        return fields;
    }

    private static SortedMap<String, Pair<Integer, Field>> fetchAllFieldsByClass(Class<?> beanClass) {

        Set<String> fieldNames = new HashSet<>();
        for (Field field : beanClass.getDeclaredFields()) {
            if (!isTransient(field.getModifiers()) && field.getAnnotation(Transient.class) == null
                    && !isStatic(field.getModifiers())) {
                fieldNames.add(field.getName().toLowerCase());
            }
        }
        SortedSet<Field> fields = fetchFieldsByNames(beanClass, fieldNames);
        SortedMap<String, Pair<Integer, Field>> result = new TreeMap<>();
        int fieldIndex = 1;
        for (Field field : fields) {
            result.put(beanFieldNameToDbFieldName(field.getName()), new ImmutablePair<>(fieldIndex++, field));
        }
        return result;
    }

    private static SortedMap<String, Pair<Integer, Field>> fetchFieldsByInterface(
            Class<?> beanInterface, Class<?> beanClass, Predicate<Method> filter) {
        Set<String> fieldNames = new HashSet<>();
        for (Method method : beanInterface.getMethods()) {
            if (method.getAnnotation(Transient.class) == null && filter.test(method)) {
                if (method.getName().startsWith(GETTER_PREFIX) || method.getName().startsWith(SETTER_PREFIX)) {
                    fieldNames.add(method.getName().substring(GETTER_OR_SETTER_PREFIX_LENGTH).toLowerCase());
                } else if (method.getName().startsWith(UPDATER_PREFIX)) {
                    fieldNames.add(method.getName().substring(UPDATER_PREFIX_LENGTH).toLowerCase());
                }
            }
        }
        String primaryKeyColumnName = fetchPrimaryKeyNameFromClass(beanClass);
        if (!isBlank(primaryKeyColumnName)) {
            String primaryKeyName = dbFieldNameToBeanFieldName(primaryKeyColumnName);
            fieldNames.add(primaryKeyName);
        }
        SortedSet<Field> fields = fetchFieldsByNames(beanClass, fieldNames);
        SortedMap<String, Pair<Integer, Field>> result = new TreeMap<>();
        int fieldIndex = 1;
        for (Field field : fields) {
            result.put(beanFieldNameToDbFieldName(field.getName()), new ImmutablePair<>(fieldIndex++, field));
        }
        return result;
    }

    static SortedMap<String, Pair<Integer, Field>> fetchAllFields(Class<?> beanInterface,
                                                                  Class<?> beanClass) {
        SortedMap<String, Pair<Integer, Field>> result;
        if (beanInterface.isInterface()) {
            result = fetchAllFieldsByInterface(beanInterface, beanClass);
        } else {
            result = fetchAllFieldsByClass(beanClass);
        }
        return result;
    }

    static SortedMap<String, Pair<Integer, Field>> fetchAllFieldsByInterface(Class<?> beanInterface,
                                                                             Class<?> beanClass) {
        return fetchFieldsByInterface(beanInterface, beanClass,
                method -> method.getName().startsWith(GETTER_PREFIX)
                        || method.getName().startsWith(SETTER_PREFIX) || method.getName().startsWith(UPDATER_PREFIX));
    }

    static SortedMap<String, Pair<Integer, Field>> fetchModifiedFields(Class<?> beanInterface,
                                                                       Class<?> beanClass) {
        SortedMap<String, Pair<Integer, Field>> result;
        if (beanInterface.isInterface()) {
            result = fetchModifiedFieldsByInterface(beanInterface, beanClass);
        } else {
            result = fetchAllFieldsByClass(beanClass);
        }
        return result;
    }

    static SortedMap<String, Pair<Integer, Field>> fetchModifiedFieldsByInterface(Class<?> beanInterface,
                                                                                  Class<?> beanClass) {
        return fetchFieldsByInterface(beanInterface, beanClass, method -> method.getName().startsWith(SETTER_PREFIX)
                || method.getName().startsWith(UPDATER_PREFIX));
    }

    static Pair<Integer, List<Field>> fetchPrimaryKeyIndexAndFields(Class<?> beanInterface, Class<?> beanClass) {
        SortedMap<String, Pair<Integer, Field>> columnNameToIndexAndField =
                fetchAllFields(beanInterface, beanClass);
        return new ImmutablePair<>(columnNameToIndexAndField.get(fetchPrimaryKeyNameFromClass(beanClass)).getLeft(),
                columnNameToIndexAndField.values().stream().map(Pair::getRight).collect(toList()));
    }

    static Map<String, Pair<Integer, Field>> mapColumnNamesToBeanFields(List<Field> fields) {
        Map<String, Pair<Integer, Field>> result = new LinkedHashMap<>();
        int i = 1;
        for (Field field : fields) {
            if (field.getAnnotation(Transient.class) == null) {
                field.setAccessible(true);
                result.put(beanFieldNameToDbFieldName(field.getName()), new ImmutablePair<>(i++, field));
            }
        }
        return Collections.unmodifiableMap(result);
    }

    static <T> T createBeanFromResultSet(ResultSet rs, Class<?> beanClass, List<Field> fields, int startIndex) throws
            SQLException {
        try {
            Constructor<?> constructor = beanClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            T result = (T) constructor.newInstance();
            int i = startIndex + 1;
            for (Field field : fields) {
                setField(result, field, rs, i++);
            }
            return result;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    static <T> T createBean(ResultSet rs, Class<?> beanClass, Collection<Pair<Integer, Field>> fields,
                            int startIndex) throws
            SQLException {
        try {
            Constructor<?> constructor = beanClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            T result = (T) constructor.newInstance();
            for (Pair<Integer, Field> field : fields) {
                setField(result, field.getRight(), rs, startIndex + field.getLeft());
            }
            return result;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    static <T> void processTable(Map<Long, T> table, ResultSet rs, Triple<Class<?>, Integer, List<Field>> triple,
                                 int startIndex) throws SQLException {
        long oneKey = rs.getLong(startIndex + triple.getMiddle());
        if (!rs.wasNull() && !table.containsKey(oneKey)) {
            table.put(oneKey, createBeanFromResultSet(rs, triple.getLeft(), triple.getRight(), startIndex));
        }
    }

}
