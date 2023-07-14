package lol.lolpany.ormik.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface IPreparedStatementCreator {

    /**
     * Method is copy of <tt>org.springframework.jdbc.core.PreparedStatementCreator#createPreparedStatement(java.sql.Connection)</tt>
     *
     * @param c Connection to use to create statement
     * @return a prepared statement
     * @throws SQLException thrown by any database-related errors
     */
    PreparedStatement createPreparedStatement(Connection c) throws SQLException;


}
