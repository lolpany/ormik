package lol.lolpany.ormik.jdbc;

import java.sql.CallableStatement;
import java.sql.SQLException;

public interface ICallableStatementJdbcCallback<T> extends ICallableStatementCreator {

    /**
     * Method is copy of <tt>org.springframework.jdbc.core.CallableStatementCallback#doInCallableStatement(java.sql.PreparedStatement)</tt>
     *
     * @param cs active JDBC CallableStatement
     * @return a result object, or {@code null} if none
     * @throws java.sql.SQLException thrown by any database-related errors
     */
    T doInCallableStatement(CallableStatement cs) throws SQLException;

}
