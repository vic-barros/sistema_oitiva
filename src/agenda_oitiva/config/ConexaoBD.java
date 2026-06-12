package agenda_oitiva.config;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConexaoBD {

    private static final String URL = "jdbc:postgresql://localhost:5432/sistema-oitiva-bd";
    private static final String USUARIO = "postgres";
    private static final String SENHA = "admin";

    public static Connection conectar() {
        try {
            return DriverManager.getConnection(URL, USUARIO, SENHA);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao conectar ao banco: " + e.getMessage());
        }
    }
}