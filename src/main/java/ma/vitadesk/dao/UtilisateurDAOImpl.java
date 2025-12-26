package ma.vitadesk.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import at.favre.lib.crypto.bcrypt.BCrypt;
import ma.vitadesk.model.Utilisateur;
import ma.vitadesk.util.DatabaseConnection;

/**
 * Classe qui implémente les opérations de base de données pour les utilisateurs
 * Pattern DAO (Data Access Object) pour séparer la logique métier de l'accès aux données
 */
public class UtilisateurDAOImpl implements IUtilisateurDAO {

    /**
     * Récupère un utilisateur complet depuis la BDD (avec toutes ses infos)
     * On fait un JOIN entre utilisateur et medecin/secretaire selon le role
     */
    @Override
    public Utilisateur getUtilisateurByLogin(String login) {
        // Requête SQL pour récupérer l'utilisateur avec ses infos complètes
        String sql = "SELECT u.idUtilisateur, u.login, u.role, " +
                     "m.nom, m.prenom, m.specialite, m.telephone, m.email " +
                     "FROM utilisateur u " +
                     "LEFT JOIN medecin m ON u.idUtilisateur = m.idUtilisateur " +
                     "LEFT JOIN secretaire s ON u.idUtilisateur = s.idUtilisateur " +
                     "WHERE u.login = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            // On remplace le ? par le login fourni (protection contre SQL injection)
            pst.setString(1, login);
            ResultSet rs = pst.executeQuery();
            
            // Si on trouve un résultat
            if (rs.next()) {
                Utilisateur user = new Utilisateur();
                
                // On remplit l'objet Utilisateur avec les données de la BDD
                user.setId(rs.getInt("idUtilisateur"));
                user.setLogin(rs.getString("login"));
                user.setRole(Utilisateur.Role.valueOf(rs.getString("role")));
                
                // Les autres champs peuvent être null si c'est une secrétaire sans fiche
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setSpecialite(rs.getString("specialite")); // null pour secrétaire
                user.setTelephone(rs.getString("telephone"));
                user.setEmail(rs.getString("email"));
                
                return user;
            }
            
        } catch (SQLException e) {
            // En cas d'erreur, on l'affiche dans la console pour debug
            System.err.println("Erreur lors de la récupération de l'utilisateur : " + e.getMessage());
            e.printStackTrace();
        }
        
        // Si aucun utilisateur trouvé ou erreur, on retourne null
        return null;
    }

    /**
     * Vérifie si le mot de passe fourni correspond à celui en BDD
     * On utilise BCrypt pour vérifier le hash
     */
    @Override
    public boolean verifierMotDePasse(String login, String motDePasse) {
        String sql = "SELECT motDePasse FROM utilisateur WHERE login = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, login);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                String hashedPassword = rs.getString("motDePasse");
                // BCrypt compare le mot de passe en clair avec le hash stocké
                return BCrypt.verifyer().verify(motDePasse.toCharArray(), hashedPassword).verified;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification du mot de passe : " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
}