package lol.lolpany.ormik.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface IPreparedStatementJdbcCallback<T> extends IPreparedStatementCreator {

    /**
     * Method is copy of <tt>org.springframework.jdbc.core.PreparedStatementCallback#doInPreparedStatement(java.sql.PreparedStatement)</tt>
     *
     * @param ps active JDBC PreparedStatement
     * @return a result object, or {@code null} if none
     * @throws SQLException thrown by any database-related errors
     */
    T doInPreparedStatement(PreparedStatement ps) throws SQLException;

}
