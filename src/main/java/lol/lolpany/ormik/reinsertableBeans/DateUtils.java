package lol.lolpany.ormik.reinsertableBeans;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;

public class DateUtils {

    public static Date toSqlDate(LocalDate localDate) {
        return new Date(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()).getTime());
    }

    public static LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

}
