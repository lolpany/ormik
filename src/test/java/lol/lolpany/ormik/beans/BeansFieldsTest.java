package lol.lolpany.ormik.beans;

import lol.lolpany.ormik.reinsertableBeans.IdEnum;
import org.junit.Ignore;
import org.junit.Test;
import org.reflections.Reflections;
import lol.lolpany.ormik.reinsertableBeans.ReinsertableBean;

import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Ignore
public class BeansFieldsTest {

    private static final Set<Class<?>> SUPPORTED_FIELD_TYPES = new HashSet<Class<?>>() {{
        add(boolean.class);
        add(Boolean.class);
        add(int.class);
        add(Integer.class);
        add(long.class);
        add(Long.class);
        add(double.class);
        add(Double.class);
        add(String.class);
        add(BigDecimal.class);
        add(java.sql.Date.class);
        add(java.util.Date.class);
        add(java.sql.Timestamp.class);
        add(java.time.LocalDate.class);
        add(byte[].class);
    }};

    @Test
    public void checkFieldTypes() throws Exception {
        for (Class<?> bean : new Reflections("lol.lolpany.ormik").getSubTypesOf(ReinsertableBean.class)) {
            for (Field field : bean.getDeclaredFields()) {
                if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())
                        && field.getAnnotation(Transient.class) == null) {
                    if (!SUPPORTED_FIELD_TYPES.contains(field.getType())
                            && !IdEnum.class.isAssignableFrom(field.getType())) {
                        throw new Exception(
                                "Unsupported ReinsertableBean field type '" + field.getType().getSimpleName() +
                                        "' for field - " +
                                        bean.getName() + "." + field.getName());
                    }
                }
            }
        }
    }
}
