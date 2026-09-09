package br.com.totvs.conversacional.conexoes;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionFactory {

    private static final Properties props = new Properties();

    static {
        try (InputStream input = ConnectionFactory.class
                .getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (input == null) {
                throw new RuntimeException(
                        "Arquivo db.properties nao encontrado em src/main/resources/\n" +
                                "Copie o db.properties.example, renomeie para db.properties " +
                                "e preencha com suas credenciais."
                );
            }
            props.load(input);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar db.properties: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            return DriverManager.getConnection(
                    props.getProperty("db.url"),
                    props.getProperty("db.usuario"),
                    props.getProperty("db.senha")
            );
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver Oracle nao encontrado: " + e.getMessage());
        }
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Erro ao fechar conexao: " + e.getMessage());
            }
        }
    }
}