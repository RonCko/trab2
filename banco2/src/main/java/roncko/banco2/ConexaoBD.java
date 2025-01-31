
package roncko.banco2;

/**
 *
 * @author Bastos
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoBD {
    private static final String URL = "jdbc:postgresql://localhost:5432/trabalhobd2";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    public static Connection conectar() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexão estabelecida com sucesso!");
        } catch (SQLException e) {
            System.out.println("Erro ao conectar ao banco de dados: " + e.getMessage());
        }
        return conn;
    }

}