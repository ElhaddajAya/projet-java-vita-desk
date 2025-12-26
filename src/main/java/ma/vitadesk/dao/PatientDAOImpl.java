package ma.vitadesk.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ma.vitadesk.model.Patient;
import ma.vitadesk.util.DatabaseConnection;

/**
 * Implémentation du DAO pour les patients
 * Gère toutes les opérations SQL sur la table patient
 * Pattern DAO = séparer l'accès BDD de la logique métier
 */
public class PatientDAOImpl implements IPatientDAO {

    /**
     * Récupère tous les patients de la BDD
     * Utilise try-with-resources pour fermer auto les connexions (bonne pratique)
     */
    @Override
    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        
        // Requête SQL pour récupérer tous les patients
        String sql = "SELECT * FROM patient ORDER BY nom, prenom";
        
        // try-with-resources → ferme auto la connexion même si erreur
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            
            // Pour chaque ligne du résultat, créer un objet Patient
            while (rs.next()) {
                Patient patient = new Patient(
                    rs.getString("numSecuSocial"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("dateNaissance"),
                    rs.getString("telephone"),
                    rs.getString("cin"),
                    rs.getString("sexe"),
                    rs.getString("adresse")
                );
                
                patients.add(patient);
            }
            
        } catch (SQLException e) {
            // En cas d'erreur, afficher dans console pour debug
            System.err.println("Erreur lors du chargement des patients : " + e.getMessage());
            e.printStackTrace();
        }
        
        return patients; // Retourne la liste (vide si erreur ou aucun patient)
    }

    /**
     * Cherche un patient par son numéro de sécu
     * Utilise PreparedStatement pour éviter les injections SQL (sécurité)
     */
    @Override
    public Patient getPatientByNumSocial(String numSocial) {
        String sql = "SELECT * FROM patient WHERE numSecuSocial = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            // Le ? est remplacé par le paramètre de manière sécurisée
            pst.setString(1, numSocial);
            ResultSet rs = pst.executeQuery();
            
            // Si on trouve un résultat, créer et retourner le Patient
            if (rs.next()) {
                return new Patient(
                    rs.getString("numSecuSocial"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("dateNaissance"),
                    rs.getString("telephone"),
                    rs.getString("cin"),
                    rs.getString("sexe"),
                    rs.getString("adresse")
                );
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur recherche patient : " + e.getMessage());
            e.printStackTrace();
        }
        
        return null; // Patient non trouvé ou erreur
    }

    /**
     * Ajoute un nouveau patient dans la BDD
     * @return true si l'ajout a réussi, false sinon
     */
    @Override
    public boolean ajouterPatient(Patient patient) {
        // Requête INSERT avec tous les champs
        String sql = "INSERT INTO patient (numSocial, nom, prenom, dateNaissance, " +
                     "telephone, cin, sexe, adresse) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            // Remplir tous les ? avec les valeurs du patient
            pst.setString(1, patient.getNumSocial());
            pst.setString(2, patient.getNom());
            pst.setString(3, patient.getPrenom());
            pst.setString(4, patient.getDateNaissance());
            pst.setString(5, patient.getTelephone());
            pst.setString(6, patient.getCin());
            pst.setString(7, patient.getSexe());
            pst.setString(8, patient.getAdresse());
            
            // Exécuter la requête et vérifier qu'une ligne a été ajoutée
            int lignesAffectees = pst.executeUpdate();
            return lignesAffectees > 0; // true si au moins 1 ligne ajoutée
            
        } catch (SQLException e) {
            System.err.println("Erreur ajout patient : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Modifie les infos d'un patient existant
     * On identifie le patient par son numSocial (qui ne change pas)
     */
    @Override
    public boolean modifierPatient(Patient patient) {
        String sql = "UPDATE patient SET nom = ?, prenom = ?, dateNaissance = ?, " +
                     "telephone = ?, cin = ?, sexe = ?, adresse = ? " +
                     "WHERE numSecuSocial = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            // Remplir les paramètres
            pst.setString(1, patient.getNom());
            pst.setString(2, patient.getPrenom());
            pst.setString(3, patient.getDateNaissance());
            pst.setString(4, patient.getTelephone());
            pst.setString(5, patient.getCin());
            pst.setString(6, patient.getSexe());
            pst.setString(7, patient.getAdresse());
            pst.setString(8, patient.getNumSocial()); // WHERE
            
            int lignesAffectees = pst.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur modification patient : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Supprime un patient de la BDD
     * ATTENTION : supprime aussi ses consultations et RDV (CASCADE)
     */
    @Override
    public boolean supprimerPatient(String numSocial) {
        String sql = "DELETE FROM patient WHERE numSecuSocial = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, numSocial);
            
            int lignesAffectees = pst.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur suppression patient : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}