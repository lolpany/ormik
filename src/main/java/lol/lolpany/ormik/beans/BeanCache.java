package lol.lolpany.ormik.beans;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import reactor.util.function.*;

import javax.persistence.Table;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static java.util.stream.Collectors.toList;
import static lol.lolpany.ormik.reinsertableBeans.ReinsertableBeanUtils.beanFieldNameToDbFieldName;

class BeanCache {

    private static final class Holder {
        private static final BeanCache INSTANCE;

        static {
            INSTANCE = new BeanCache();
        }
    }

    static BeanCache getBeanCache() {
        return Holder.INSTANCE;
    }

    /**
     * 100 services * 3 beans average
     */
    private static final int NUMBER_OF_JOINED_BEANS = 100 * 10;
    private static final Pattern COLUMNS_PATTERN =
            Pattern.compile("^\\s*select\\s*(\\*).*", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    private static final Reflections reflections = new Reflections("lol.lolpany", new SubTypesScanner());
    final Map<String, Pair<Class<?>, SortedMap<String, Pair<Integer, Field>>>> queryToBeanSelectCache;
    final Map<String, Tuple2<Triple<Class<?>, Integer, List<Field>>, Triple<Class<?>, Integer, List<Field>>>>
            queryToTwoBeansSelectCache;
    final Map<String, Tuple3<Triple<Class<?>, Integer, List<Field>>, Triple<Class<?>, Integer, List<Field>>,
            Triple<Class<?>, Integer, List<Field>>>> queryToThreeBeansSelectCache;
    final Map<String, Tuple4<Triple<Class<?>, Integer, List<Field>>, Triple<Class<?>, Integer, List<Field>>,
            Triple<Class<?>, Integer, List<Field>>, Triple<Class<?>, Integer, List<Field>>>>
            queryToFourBeansSelectCache;
    final Map<String, Tuple5<Triple<Class<?>, Integer, List<Field>>, Triple<Class<?>, Integer, List<Field>>,
            Triple<Class<?>, Integer, List<Field>>, Triple<Class<?>, Integer, List<Field>>, Triple<Class<?>, Integer, List<Field>>>>
            queryToFiveBeansSelectCache;
    // todo https://docs.oracle.com/cd/B28359_01/server.111/b28274/optimops.htm#i49183
    final Map<String, Map<Class<?>, String>> selectCache;
    final Map<Class<?>, Class<?>> interfaceToClass;

    private BeanCache() {
        this.queryToBeanSelectCache = new ConcurrentHashMap<>(NUMBER_OF_JOINED_BEANS);
        this.queryToTwoBeansSelectCache = new ConcurrentHashMap<>(NUMBER_OF_JOINED_BEANS);
        this.queryToThreeBeansSelectCache = new ConcurrentHashMap<>(NUMBER_OF_JOINED_BEANS);
        this.queryToFourBeansSelectCache = new ConcurrentHashMap<>(NUMBER_OF_JOINED_BEANS);
        this.queryToFiveBeansSelectCache = new ConcurrentHashMap<>(NUMBER_OF_JOINED_BEANS);
        this.selectCache = new ConcurrentHashMap<>(NUMBER_OF_JOINED_BEANS);
        this.interfaceToClass = new ConcurrentHashMap<>(NUMBER_OF_JOINED_BEANS);
    }

    public <I1, I2> String select(String query, Class<I1> one) {
        String result = getQueryFromCache(query, one);
        if (result == null) {
            Pair<String, Pair<Class<?>, SortedMap<String, Pair<Integer, Field>>>> selectAndBeanMappings =
                    generateSelect(query, one);
            result = selectAndBeanMappings.getLeft();
            queryToBeanSelectCache.put(result, selectAndBeanMappings.getRight());
            selectCache.computeIfAbsent(query, key -> new ConcurrentHashMap<>()).put(one, result);
        }
        return result;
    }

    public <I1, I2> String select(String query, Class<I1> one, Class<I2> two) {
        String result = getQueryFromCache(query, one);
        if (result == null) {
            Pair<String, Tuple2<Triple<Class<?>, Integer, List<Field>>, Triple<Class<?>, Integer, List<Field>>>>
                    selectAndBeanMappings = generateSelect(query, one, two);
            result = selectAndBeanMappings.getLeft();
            queryToTwoBeansSelectCache.put(result, selectAndBeanMappings.getRight());
            selectCache.computeIfAbsent(query, key -> new ConcurrentHashMap<>()).put(one, result);
        }
        return result;
    }

    public <I1, I2, I3> String select(String query, Class<I1> one, Class<I2> two, Class<I3> three) {
        String result = getQueryFromCache(query, one);
        if (result == null) {
            Pair<String, Tuple3<Triple<Class<?>, Integer, List<Field>>, Triple<Class<?>, Integer, List<Field>>,
                    Triple<Class<?>, Integer, List<Field>>>> selectAndBeanMappings =
                    generateSelect(query, one, two, three);
            result = selectAndBeanMappings.getLeft();
            queryToThreeBeansSelectCache.put(result, selectAndBeanMappings.getRight());
            selectCache.computeIfAbsent(query, key -> new ConcurrentHashMap<>()).put(one, result);
        }
        return result;
    }

    public <I1, I2, I3, I4> String select(String query, Class<I1> one, Class<I2> two, Class<I3> three, Class<I4> four) {
        String result = getQueryFromCache(query, one);
        if (result == null) {
            Pair<String, Tuple4<Triple<Class<?>, Integer, List<Field>>, Triple<Class<?>, Integer, List<Field>>,
                    Triple<Class<?>, Integer, List<Field>>, Triple<Class<?>, Integer, List<Field>>>>
                    selectAndBeanMappings =
                    generateSelect(query, one, two, three, four);
            result = selectAndBeanMappings.getLeft();
            queryToFourBeansSelectCache.put(result, selectAndBeanMappings.getRight());
            selectCache.computeIfAbsent(query, key -> new ConcurrentHashMap<>()).put(one, result);
        }
        return result;
    }

    public <I1, I2, I3, I4, I5> String select(String query, Class<I1> one, Class<I2> two, Class<I3> three,
                                              Class<I4> four, Class<I5> five) {
        String result = getQueryFromCache(query, one);
        if (result == null) {
            Pair<String, Tuple5<Triple<Class<?>, Integer, List<Field>>, Triple<Class<?>, Integer, List<Field>>,
                    Triple<Class<?>, Integer, List<Field>>, Triple<Class<?>, Integer, List<Field>>,
                    Triple<Class<?>, Integer, List<Field>>>> selectAndBeanMappings =
                    generateSelect(query, one, two, three, four, five);
            result = selectAndBeanMappings.getLeft();
            queryToFiveBeansSelectCache.put(result, selectAndBeanMappings.getRight());
            selectCache.computeIfAbsent(query, key -> new ConcurrentHashMap<>()).put(one, result);
        }
        return result;
    }

    private <I1> String getQueryFromCache(String query, Class<I1> one) {
        return selectCache.computeIfAbsent(query, key -> new HashMap<>()).get(one);
    }

    private <I1, I2> Pair<String, Pair<Class<?>, SortedMap<String, Pair<Integer, Field>>>> generateSelect(String query,
                                                                                                          Class<I1> one) {
        Class<?> oneClass = one.isInterface() ? findClassForInterface(one) : one;
        SortedMap<String, Pair<Integer, Field>> columnNameToIndexAndField = BeanUtils.fetchAllFields(one, oneClass);
        List<Pair<Class<?>, List<Field>>> result = new ArrayList<>();
        result.add(new ImmutablePair<>(oneClass,
                columnNameToIndexAndField.values().stream().map(Pair::getRight).collect(toList())));
        return new ImmutablePair<>(replaceColumns(query, result),
                new ImmutablePair<>(oneClass, columnNameToIndexAndField));
    }

    private <I1, I2> Pair<String, Tuple2<Triple<Class<?>, Integer, List<Field>>, Triple<Class<?>, Integer, List<Field>>>>
    generateSelect(String query, Class<I1> one, Class<I2> two) {

        Class<?> oneClass = findClassForInterface(one);
        Pair<Integer, List<Field>> oneFields = BeanUtils.fetchPrimaryKeyIndexAndFields(one, oneClass);
        Class<?> twoClass = findClassForInterface(two);
        Pair<Integer, List<Field>> twoFields = BeanUtils.fetchPrimaryKeyIndexAndFields(two, twoClass);
        List<Pair<Class<?>, List<Field>>> result = new ArrayList<>();
        result.add(new ImmutablePair<>(oneClass, oneFields.getRight()));
        result.add(new ImmutablePair<>(twoClass, twoFields.getRight()));
        return new ImmutablePair<>(replaceColumns(query, result),
                Tuples.of(new ImmutableTriple<>(oneClass, oneFields.getLeft(), oneFields.getRight()),
                        new ImmutableTriple<>(twoClass, twoFields.getLeft(), twoFields.getRight())));
    }

    private <I1, I2, I3> Pair<String, Tuple3<Triple<Class<?>, Integer, List<Field>>, Triple<Class<?>, Integer, List<Field>>,
            Triple<Class<?>, Integer, List<Field>>>> generateSelect(String query, Class<I1> one, Class<I2> two,
                                                                    Class<I3> three) {
        Class<?> oneClass = findClassForInterface(one);
        Pair<Integer, List<Field>> oneFields = BeanUtils.fetchPrimaryKeyIndexAndFields(one, oneClass);
        Class<?> twoClass = findClassForInterface(two);
        Pair<Integer, List<Field>> twoFields = BeanUtils.fetchPrimaryKeyIndexAndFields(two, twoClass);
        Class<?> threeClass = findClassForInterface(three);
        Pair<Integer, List<Field>> threeFields = BeanUtils.fetchPrimaryKeyIndexAndFields(three, threeClass);
        List<Pair<Class<?>, List<Field>>> result = new ArrayList<>();
        result.add(new ImmutablePair<>(oneClass, oneFields.getRight()));
        result.add(new ImmutablePair<>(twoClass, twoFields.getRight()));
        result.add(new ImmutablePair<>(threeClass, threeFields.getRight()));
        return new ImmutablePair<>(replaceColumns(query, result),
                Tuples.of(new ImmutableTriple<>(oneClass, oneFields.getLeft(), oneFields.getRight()),
                        new ImmutableTriple<>(twoClass, twoFields.getLeft(), twoFields.getRight()),
                        new ImmutableTriple<>(threeClass, threeFields.getLeft(), threeFields.getRight())));
    }

    private <I1, I2, I3, I4> Pair<String, Tuple4<Triple<Class<?>, Integer, List<Field>>, Triple<Class<?>, Integer, List<Field>>,
            Triple<Class<?>, Integer, List<Field>>, Triple<Class<?>, Integer, List<Field>>>> generateSelect(
            String query, Class<I1> one, Class<I2> two, Class<I3> three, Class<I4> four) {
        Class<?> oneClass = findClassForInterface(one);
        Pair<Integer, List<Field>> oneFields = BeanUtils.fetchPrimaryKeyIndexAndFields(one, oneClass);
        Class<?> twoClass = findClassForInterface(two);
        Pair<Integer, List<Field>> twoFields = BeanUtils.fetchPrimaryKeyIndexAndFields(two, twoClass);
        Class<?> threeClass = findClassForInterface(three);
        Pair<Integer, List<Field>> threeFields = BeanUtils.fetchPrimaryKeyIndexAndFields(three, threeClass);
        Class<?> fourClass = findClassForInterface(four);
        Pair<Integer, List<Field>> fourFields = BeanUtils.fetchPrimaryKeyIndexAndFields(four, fourClass);
        List<Pair<Class<?>, List<Field>>> result = new ArrayList<>();
        result.add(new ImmutablePair<>(oneClass, oneFields.getRight()));
        result.add(new ImmutablePair<>(twoClass, twoFields.getRight()));
        result.add(new ImmutablePair<>(threeClass, threeFields.getRight()));
        result.add(new ImmutablePair<>(fourClass, fourFields.getRight()));
        return new ImmutablePair<>(replaceColumns(query, result),
                Tuples.of(new ImmutableTriple<>(oneClass, oneFields.getLeft(), oneFields.getRight()),
                        new ImmutableTriple<>(twoClass, twoFields.getLeft(), twoFields.getRight()),
                        new ImmutableTriple<>(threeClass, threeFields.getLeft(), threeFields.getRight()),
                        new ImmutableTriple<>(fourClass, fourFields.getLeft(), fourFields.getRight())));
    }

    private <I1, I2, I3, I4, I5> Pair<String, Tuple5<Triple<Class<?>, Integer, List<Field>>,
            Triple<Class<?>, Integer, List<Field>>, Triple<Class<?>, Integer, List<Field>>,
            Triple<Class<?>, Integer, List<Field>>, Triple<Class<?>, Integer, List<Field>>>> generateSelect(
            String query, Class<I1> one, Class<I2> two, Class<I3> three, Class<I4> four, Class<I5> five) {
        Class<?> oneClass = findClassForInterface(one);
        Pair<Integer, List<Field>> oneFields = BeanUtils.fetchPrimaryKeyIndexAndFields(one, oneClass);
        Class<?> twoClass = findClassForInterface(two);
        Pair<Integer, List<Field>> twoFields = BeanUtils.fetchPrimaryKeyIndexAndFields(two, twoClass);
        Class<?> threeClass = findClassForInterface(three);
        Pair<Integer, List<Field>> threeFields = BeanUtils.fetchPrimaryKeyIndexAndFields(three, threeClass);
        Class<?> fourClass = findClassForInterface(four);
        Pair<Integer, List<Field>> fourFields = BeanUtils.fetchPrimaryKeyIndexAndFields(four, fourClass);
        Class<?> fiveClass = findClassForInterface(five);
        Pair<Integer, List<Field>> fiveFields = BeanUtils.fetchPrimaryKeyIndexAndFields(five, fiveClass);
        List<Pair<Class<?>, List<Field>>> result = new ArrayList<>();
        result.add(new ImmutablePair<>(oneClass, oneFields.getRight()));
        result.add(new ImmutablePair<>(twoClass, twoFields.getRight()));
        result.add(new ImmutablePair<>(threeClass, threeFields.getRight()));
        result.add(new ImmutablePair<>(fourClass, fourFields.getRight()));
        result.add(new ImmutablePair<>(fiveClass, fiveFields.getRight()));
        return new ImmutablePair<>(replaceColumns(query, result),
                Tuples.of(new ImmutableTriple<>(oneClass, oneFields.getLeft(), oneFields.getRight()),
                        new ImmutableTriple<>(twoClass, twoFields.getLeft(), twoFields.getRight()),
                        new ImmutableTriple<>(threeClass, threeFields.getLeft(), threeFields.getRight()),
                        new ImmutableTriple<>(fourClass, fourFields.getLeft(), fourFields.getRight()),
                        new ImmutableTriple<>(fiveClass, fiveFields.getLeft(), fiveFields.getRight())));
    }

    <I, T extends I> Class<T> findClassForInterface(Class<I> one) {
        Class<T> result = (Class<T>) interfaceToClass.get(one);
        if (result == null) {
            for (Class<? extends I> subtype : reflections.getSubTypesOf(one)) {
                Table tableAnnotation = subtype.getAnnotation(Table.class);
                if (!subtype.isInterface() && tableAnnotation != null && one.getSimpleName()
                        .endsWith(LOWER_UNDERSCORE.to(UPPER_CAMEL, tableAnnotation.name()))) {
                    return (Class<T>) subtype;
                }
            }
        }
        return null;
    }

    private String replaceColumns(String query, List<Pair<Class<?>, List<Field>>> classAndFieldsList) {
        Matcher matcher = COLUMNS_PATTERN.matcher(query);
        matcher.find();
        StringBuilder result = new StringBuilder(query.substring(0, matcher.start(1)));
        for (Pair<Class<?>, List<Field>> classAndFields : classAndFieldsList) {
            result.append(generateColumns(classAndFields.getLeft(), classAndFields.getRight()));
        }
        result.setLength(result.length() - 1);
        return result.append(query.substring(matcher.end(1))).toString();
    }

    private String generateColumns(Class<?> clas, List<Field> fields) {
        StringBuilder result = new StringBuilder();
        String oneTablePrefix = UPPER_CAMEL.to(LOWER_UNDERSCORE, clas.getAnnotation(Table.class).name()) + ".";
        for (Field field : fields) {
            result.append(oneTablePrefix).append(beanFieldNameToDbFieldName(field.getName())).append(",");
        }

        return result.toString();
    }

}
