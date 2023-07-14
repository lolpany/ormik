package lol.lolpany.ormik.jdbc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

public interface ICallableStatementCreator {

    /**
     * Method is copy of <tt>org.springframework.jdbc.core.CallableStatementCreator#createCallableStatement(java.sql.Connection)</tt>
     *
     * @param c Connection to use to create statement
     * @return a callable statement
     * @throws java.sql.SQLException thrown by any database-related errors
     */
    CallableStatement createCallableStatement(Connection c) throws SQLException;


}
