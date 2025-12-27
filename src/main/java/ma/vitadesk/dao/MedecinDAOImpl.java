package ma.vitadesk.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ma.vitadesk.model.Medecin;
import ma.vitadesk.util.DatabaseConnection;

/**
 * Implémentation DAO pour les médecins
 * Gère les opérations SQL sur la table medecin
 */
public class MedecinDAOImpl implements IMedecinDAO {

    /**
     * Récupère tous les médecins de la BDD avec leurs ID
     */
    @Override
    public List<Medecin> getAllMedecins() {
        List<Medecin> medecins = new ArrayList<>();
        
        String sql = "SELECT * FROM medecin ORDER BY nom, prenom";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            
            while (rs.next()) {
                // Utiliser le constructeur complet avec les ID
                Medecin medecin = new Medecin(
                    rs.getInt("idMedecin"),
                    rs.getInt("idUtilisateur"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("specialite"),
                    rs.getString("telephone"),
                    rs.getString("email")
                );
                
                medecins.add(medecin);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur chargement médecins : " + e.getMessage());
            e.printStackTrace();
        }
        
        return medecins;
    }

    /**
     * Récupère un médecin par son ID utilisateur
     * Utilisé lors de la connexion
     */
    @Override
    public Medecin getMedecinById(int idUtilisateur) {
        String sql = "SELECT * FROM medecin WHERE idUtilisateur = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, idUtilisateur);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return new Medecin(
                    rs.getInt("idMedecin"),
                    rs.getInt("idUtilisateur"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("specialite"),
                    rs.getString("telephone"),
                    rs.getString("email")
                );
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur recherche médecin : " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Ajoute un nouveau médecin
     * Note: Pour l'instant sans créer l'utilisateur associé
     */
    @Override
    public boolean ajouterMedecin(Medecin medecin) {
        String sql = "INSERT INTO medecin (nom, prenom, specialite, telephone, email) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, medecin.getNom());
            pst.setString(2, medecin.getPrenom());
            pst.setString(3, medecin.getSpecialite());
            pst.setString(4, medecin.getTelephone());
            pst.setString(5, medecin.getEmail());
            
            int lignesAffectees = pst.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur ajout médecin : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Modifie les infos d'un médecin
     * Utilise idMedecin pour identifier précisément
     */
    @Override
    public boolean modifierMedecin(Medecin medecin) {
        String sql = "UPDATE medecin SET nom = ?, prenom = ?, specialite = ?, " +
                     "telephone = ?, email = ? WHERE idMedecin = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, medecin.getNom());
            pst.setString(2, medecin.getPrenom());
            pst.setString(3, medecin.getSpecialite());
            pst.setString(4, medecin.getTelephone());
            pst.setString(5, medecin.getEmail());
            pst.setInt(6, medecin.getIdMedecin());
            
            int lignesAffectees = pst.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur modification médecin : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Supprime un médecin
     */
    @Override
    public boolean supprimerMedecin(int idUtilisateur) {
        String sql = "DELETE FROM medecin WHERE idUtilisateur = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, idUtilisateur);
            
            int lignesAffectees = pst.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur suppression médecin : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}