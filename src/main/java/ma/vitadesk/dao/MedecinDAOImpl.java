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
     * Récupère tous les médecins de la BDD
     * JOIN pas nécessaire ici car on a déjà tout dans la table medecin
     */
    @Override
    public List<Medecin> getAllMedecins() {
        List<Medecin> medecins = new ArrayList<>();
        
        String sql = "SELECT * FROM medecin ORDER BY nom, prenom";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            
            // Pour chaque médecin trouvé, créer un objet Medecin
            while (rs.next()) {
                Medecin medecin = new Medecin(
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
     * Utile quand on se connecte (on connaît l'idUtilisateur)
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
     * ATTENTION : Il faut d'abord créer un utilisateur, puis lier le médecin
     * Pour l'instant, on ajoute juste le médecin sans créer l'utilisateur
     * (à améliorer plus tard avec une transaction)
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
     * On identifie par l'idUtilisateur
     */
    @Override
    public boolean modifierMedecin(Medecin medecin) {
        // Note : pour modifier, il faudrait avoir l'idUtilisateur dans la classe Medecin
        // Pour l'instant, on cherche par nom/prenom (pas idéal mais temporaire)
        String sql = "UPDATE medecin SET specialite = ?, telephone = ?, email = ? " +
                     "WHERE nom = ? AND prenom = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, medecin.getSpecialite());
            pst.setString(2, medecin.getTelephone());
            pst.setString(3, medecin.getEmail());
            pst.setString(4, medecin.getNom());
            pst.setString(5, medecin.getPrenom());
            
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
     * ATTENTION : supprime aussi ses RDV et consultations (CASCADE)
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