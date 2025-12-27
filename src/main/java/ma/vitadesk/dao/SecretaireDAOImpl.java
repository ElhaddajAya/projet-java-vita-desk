package ma.vitadesk.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import ma.vitadesk.model.Secretaire;
import ma.vitadesk.util.DatabaseConnection;

/**
 * Implémentation DAO pour les secrétaires
 * Gère les opérations SQL sur la table secretaire
 */
public class SecretaireDAOImpl implements ISecretaireDAO {

    /**
     * Récupère une secrétaire par son ID utilisateur
     * Utilisé lors de la connexion
     */
    @Override
    public Secretaire getSecretaireById(int idUtilisateur) {
        String sql = "SELECT * FROM secretaire WHERE idUtilisateur = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, idUtilisateur);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return new Secretaire(
                    rs.getInt("idSecretaire"),
                    rs.getInt("idUtilisateur"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("telephone"),
                    rs.getString("email")
                );
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur recherche secrétaire : " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Ajoute une nouvelle secrétaire
     */
    @Override
    public boolean ajouterSecretaire(Secretaire secretaire) {
        String sql = "INSERT INTO secretaire (nom, prenom, telephone, email) " +
                     "VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, secretaire.getNom());
            pst.setString(2, secretaire.getPrenom());
            pst.setString(3, secretaire.getTelephone());
            pst.setString(4, secretaire.getEmail());
            
            int lignesAffectees = pst.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur ajout secrétaire : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Modifie les infos d'une secrétaire
     */
    @Override
    public boolean modifierSecretaire(Secretaire secretaire) {
        String sql = "UPDATE secretaire SET nom = ?, prenom = ?, " +
                     "telephone = ?, email = ? WHERE idSecretaire = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, secretaire.getNom());
            pst.setString(2, secretaire.getPrenom());
            pst.setString(3, secretaire.getTelephone());
            pst.setString(4, secretaire.getEmail());
            pst.setInt(5, secretaire.getIdSecretaire());
            
            int lignesAffectees = pst.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur modification secrétaire : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Supprime une secrétaire
     */
    @Override
    public boolean supprimerSecretaire(int idUtilisateur) {
        String sql = "DELETE FROM secretaire WHERE idUtilisateur = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, idUtilisateur);
            
            int lignesAffectees = pst.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur suppression secrétaire : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}