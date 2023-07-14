package lol.lolpany.ormik.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ResultSetUtils {

    public static Double getDouble(int columnIndex, ResultSet rs) throws SQLException {
        final double v = rs.getDouble(columnIndex);
        if (v == 0.0 && rs.wasNull()) {
            return null;
        }
        return v;
    }
}
