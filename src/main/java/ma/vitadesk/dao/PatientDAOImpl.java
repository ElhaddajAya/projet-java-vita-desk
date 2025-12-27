package ma.vitadesk.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import ma.vitadesk.model.Patient;
import ma.vitadesk.util.DatabaseConnection;

/**
 * Implémentation du DAO pour les patients
 * Gère toutes les opérations SQL sur la table patient
 */
public class PatientDAOImpl implements IPatientDAO {

    // Format de date utilisé dans l'application (JavaFX)
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Récupère tous les patients de la BDD
     */
    @Override
    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        
        String sql = "SELECT * FROM patient ORDER BY nom, prenom";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            
            while (rs.next()) {
                patients.add(construirePatient(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur chargement patients : " + e.getMessage());
            e.printStackTrace();
        }
        
        return patients;
    }

    /**
     * Cherche un patient par son numéro de sécu
     */
    @Override
    public Patient getPatientByNumSocial(String numSocial) {
        String sql = "SELECT * FROM patient WHERE numSecuSociale = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, numSocial);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return construirePatient(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur recherche patient : " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Ajoute un nouveau patient dans la BDD
     */
    @Override
    public boolean ajouterPatient(Patient patient) {
        String sql = "INSERT INTO patient (numSecuSociale, nom, prenom, dateNaissance, " +
                     "telephone, cin, sexe, adresse) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            remplirPreparedStatement(pst, patient);
            
            int lignesAffectees = pst.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur ajout patient : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Modifie les infos d'un patient existant
     */
    @Override
    public boolean modifierPatient(Patient patient) {
        String sql = "UPDATE patient SET nom = ?, prenom = ?, dateNaissance = ?, " +
                     "telephone = ?, cin = ?, sexe = ?, adresse = ? " +
                     "WHERE numSecuSociale = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            // Remplir les paramètres 1 à 7
            pst.setString(1, patient.getNom());
            pst.setString(2, patient.getPrenom());
            
            // Convertir String → SQL Date
            if (patient.getDateNaissance() != null && !patient.getDateNaissance().isEmpty()) {
                LocalDate date = LocalDate.parse(patient.getDateNaissance(), DATE_FORMATTER);
                pst.setDate(3, Date.valueOf(date));
            } else {
                pst.setDate(3, null);
            }
            
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
     */
    @Override
    public boolean supprimerPatient(String numSocial) {
        String sql = "DELETE FROM patient WHERE numSecuSociale = ?";
        
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
    
    // ========== MÉTHODES PRIVÉES UTILITAIRES ==========
    
    /**
     * Construit un objet Patient depuis le ResultSet
     * Évite la duplication de code
     */
    private Patient construirePatient(ResultSet rs) throws SQLException {
        // Convertir la date SQL en String format dd/MM/yyyy
        String dateNaissance = "";
        if (rs.getDate("dateNaissance") != null) {
            dateNaissance = rs.getDate("dateNaissance").toLocalDate().format(DATE_FORMATTER);
        }
        
        return new Patient(
            rs.getInt("idPatient"),
            rs.getString("numSecuSociale"),
            rs.getString("nom"),
            rs.getString("prenom"),
            dateNaissance,
            rs.getString("telephone"),
            rs.getString("cin"),
            rs.getString("sexe"),
            rs.getString("adresse")
        );
    }
    
    /**
     * Remplit un PreparedStatement avec les données d'un patient
     * Utilisé pour INSERT
     */
    private void remplirPreparedStatement(PreparedStatement pst, Patient patient) throws SQLException {
        pst.setString(1, patient.getNumSocial());
        pst.setString(2, patient.getNom());
        pst.setString(3, patient.getPrenom());
        
        // Convertir String dd/MM/yyyy → SQL Date
        if (patient.getDateNaissance() != null && !patient.getDateNaissance().isEmpty()) {
            LocalDate date = LocalDate.parse(patient.getDateNaissance(), DATE_FORMATTER);
            pst.setDate(4, Date.valueOf(date));
        } else {
            pst.setDate(4, null);
        }
        
        pst.setString(5, patient.getTelephone());
        pst.setString(6, patient.getCin());
        pst.setString(7, patient.getSexe());
        pst.setString(8, patient.getAdresse());
    }
    
    /**
     * Récupère le nombre de nouveaux patients du mois en cours
     * NOTE: Cette méthode suppose qu'il y a une colonne dateInscription
     * Si elle n'existe pas, compter simplement tous les patients
     */
    @Override
    public int getNouveauxPatientsCeMois() {
        // Retourner le nombre total de patients comme approximation
        return getAllPatients().size();
    }
}