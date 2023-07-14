package lol.lolpany.ormik.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

public interface IConnectionJdbcCallback<T> {

    T doInConnection(Connection c) throws SQLException;

}
