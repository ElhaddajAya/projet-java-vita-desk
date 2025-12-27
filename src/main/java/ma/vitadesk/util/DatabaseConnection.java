package ma.vitadesk.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe utilitaire pour gérer la connexion à la base de données MySQL
 * Pattern Singleton implicite : chaque appel crée une nouvelle connexion
 * mais avec les mêmes paramètres centralisés
 */
public class DatabaseConnection {
    
    // Paramètres de connexion MySQL
    private static final String URL = "jdbc:mysql://localhost:3306/vitadesk?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    /**
     * Retourne une nouvelle connexion à la BDD
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Charger le driver MySQL (optionnel avec JDBC 4.0+)
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
            
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver MySQL introuvable : " + e.getMessage());
            throw new SQLException("Driver MySQL non trouvé", e);
        } catch (SQLException e) {
            System.err.println("❌ Erreur connexion MySQL : " + e.getMessage());
            throw e;
        }
    }

    /**
     * Méthode pour tester la connexion au démarrage de l'application
     * Affiche un message de succès ou d'erreur
     */
    public static void testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Connexion MySQL réussie !");
            }
        } catch (SQLException e) {
            System.err.println("Test de connexion échoué : " + e.getMessage());
        }
    }
}