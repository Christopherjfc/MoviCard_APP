package cat.iticbcn.clientiot;

import java.sql.*;

public class ConnectDB {
    public static Connection getConnection(String urlConn, String name, String pass) throws SQLException {

        Connection connection = null;

        try {
            // Establecer la conexión
            connection = DriverManager.getConnection(urlConn, name, pass);
            return connection;
        } catch (SQLException e) {
            // Si ocurre un error, se lanza la excepción
            throw new SQLException("Error al establecer la conexión a la base de datos", e);
        }
    }
   
}
