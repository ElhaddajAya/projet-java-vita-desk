package ma.vitadesk.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/vitadesk?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            System.err.println("Erreur MySQL : " + e.getMessage());
            return null;
        }
    }

    public static void testConnection() {
        if (getConnection() != null) {
            System.out.println("Connexion MySQL avec Maven OK !");
        }
    }
}