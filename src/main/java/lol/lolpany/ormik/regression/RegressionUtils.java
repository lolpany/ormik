package lol.lolpany.ormik.regression;

import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class RegressionUtils {

    public static Map<String, List<String>> generateScheme() {
        Reflections reflections = new Reflections("lol.lolpany", new FieldAnnotationsScanner());
        Set<Field> annotated = reflections.getFieldsAnnotatedWith(ManyToOne.class);
        Map<String, List<String>> result = new HashMap<>();
        for (Field field : reflections.getFieldsAnnotatedWith(JoinColumn.class)) {
            if (field.getDeclaringClass().getAnnotation(Table.class) != null &&
                    !field.getAnnotation(JoinColumn.class).table().equals("")) {
                result.computeIfAbsent(field.getDeclaringClass().getAnnotation(Table.class).name(),
                        key -> new ArrayList<>());
                result.get(field.getDeclaringClass().getAnnotation(Table.class).name())
                        .add(field.getAnnotation(JoinColumn.class).table());
            }
        }
        return result;
    }

}
