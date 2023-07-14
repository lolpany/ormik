package lol.lolpany.ormik.reinsertableBeans;

import org.apache.commons.lang3.mutable.MutableInt;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface IPreparedStatementParemetersSetter {
    void setParemeters(PreparedStatement ps, MutableInt index) throws SQLException;
}
