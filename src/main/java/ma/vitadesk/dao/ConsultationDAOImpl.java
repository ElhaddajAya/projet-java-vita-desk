package ma.vitadesk.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ma.vitadesk.model.Consultation;
import ma.vitadesk.util.DatabaseConnection;

/**
 * Implémentation DAO pour les consultations
 * Version améliorée avec utilisation de idConsultation
 */
public class ConsultationDAOImpl implements IConsultationDAO {

    /**
     * Récupère toutes les consultations d'un patient
     * Charge l'ID depuis la BDD pour pouvoir modifier plus tard
     */
    @Override
    public List<Consultation> getConsultationsByPatient(String numSocial) {
        List<Consultation> consultations = new ArrayList<>();
        
        String sql = "SELECT c.idConsultation, c.date, c.diagnostic, c.traitement, " +
                     "c.observations, c.prixConsultation, m.nom, m.prenom " +
                     "FROM consultation c " +
                     "JOIN medecin m ON c.idMedecin = m.idMedecin " +
                     "JOIN patient p ON c.idPatient = p.idPatient " +
                     "WHERE p.numSocial = ? " +
                     "ORDER BY STR_TO_DATE(c.date, '%d/%m/%Y') DESC";  // Les plus récentes en premier
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, numSocial);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                // Construire le nom complet du médecin
                String nomMedecin = "Dr. " + rs.getString("prenom") + " " + rs.getString("nom");
                
                // Utiliser le constructeur avec ID
                Consultation consultation = new Consultation(
                    rs.getInt("idConsultation"),  // ID depuis BDD
                    rs.getString("date"),
                    nomMedecin,
                    rs.getString("diagnostic"),
                    rs.getString("traitement"),
                    rs.getString("observations"),
                    rs.getDouble("prixConsultation")
                );
                
                consultations.add(consultation);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur chargement consultations patient : " + e.getMessage());
            e.printStackTrace();
        }
        
        return consultations;
    }

    /**
     * Récupère les consultations d'un médecin spécifique
     * Utile pour les statistiques du médecin
     */
    @Override
    public List<Consultation> getConsultationsByMedecin(int idMedecin) {
        List<Consultation> consultations = new ArrayList<>();
        
        String sql = "SELECT c.idConsultation, c.date, c.diagnostic, c.traitement, " +
                     "c.observations, c.prixConsultation, m.nom, m.prenom, " +
                     "p.nom AS pNom, p.prenom AS pPrenom " +
                     "FROM consultation c " +
                     "JOIN medecin m ON c.idMedecin = m.idMedecin " +
                     "JOIN patient p ON c.idPatient = p.idPatient " +
                     "WHERE c.idMedecin = ? " +
                     "ORDER BY STR_TO_DATE(c.date, '%d/%m/%Y') DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, idMedecin);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                String nomMedecin = "Dr. " + rs.getString("prenom") + " " + rs.getString("nom");
                
                Consultation consultation = new Consultation(
                    rs.getInt("idConsultation"),
                    rs.getString("date"),
                    nomMedecin,
                    rs.getString("diagnostic"),
                    rs.getString("traitement"),
                    rs.getString("observations"),
                    rs.getDouble("prixConsultation")
                );
                
                consultations.add(consultation);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur chargement consultations médecin : " + e.getMessage());
            e.printStackTrace();
        }
        
        return consultations;
    }

    /**
     * Ajoute une nouvelle consultation dans la BDD
     * Lie la consultation au patient et au médecin via leurs ID
     */
    @Override
    public boolean ajouterConsultation(Consultation consultation, String numSocial, int idMedecin) {
        String sql = "INSERT INTO consultation (date, idPatient, idMedecin, diagnostic, " +
                     "traitement, observations, prixConsultation) " +
                     "VALUES (?, " +
                     "(SELECT idPatient FROM patient WHERE numSocial = ?), " +
                     "?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, consultation.getDate());
            pst.setString(2, numSocial);  // Sous-requête pour trouver idPatient
            pst.setInt(3, idMedecin);
            pst.setString(4, consultation.getDiagnostic());
            pst.setString(5, consultation.getTraitement());
            pst.setString(6, consultation.getObservations());
            pst.setDouble(7, consultation.getPrixConsultation());
            
            int lignesAffectees = pst.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur ajout consultation : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Modifie une consultation existante
     * Maintenant utilise l'idConsultation pour identifier précisément la consultation
     */
    @Override
    public boolean modifierConsultation(Consultation consultation) {
        String sql = "UPDATE consultation SET diagnostic = ?, traitement = ?, " +
                     "observations = ?, prixConsultation = ? " +
                     "WHERE idConsultation = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, consultation.getDiagnostic());
            pst.setString(2, consultation.getTraitement());
            pst.setString(3, consultation.getObservations());
            pst.setDouble(4, consultation.getPrixConsultation());
            pst.setInt(5, consultation.getIdConsultation());  // Identification précise par ID
            
            int lignesAffectees = pst.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur modification consultation : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Supprime une consultation par son ID
     * Méthode bonus (pas dans l'interface mais utile)
     */
    public boolean supprimerConsultation(int idConsultation) {
        String sql = "DELETE FROM consultation WHERE idConsultation = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, idConsultation);
            
            int lignesAffectees = pst.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur suppression consultation : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Compte le nombre de consultations d'un médecin (pour statistiques)
     * Méthode bonus pour le graphique du dashboard médecin
     */
    public int compterConsultationsMedecin(int idMedecin) {
        String sql = "SELECT COUNT(*) AS total FROM consultation WHERE idMedecin = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, idMedecin);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("total");
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur comptage consultations : " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Compte les consultations par jour de la semaine (pour graphique)
     * Retourne un tableau avec le nombre de consultations pour chaque jour
     * Index 0 = Lundi, 1 = Mardi, ..., 6 = Dimanche
     */
    public int[] getConsultationsParJourSemaine(int idMedecin) {
        int[] consultationsParJour = new int[7];  // 7 jours de la semaine
        
        // DAYOFWEEK retourne 1=Dimanche, 2=Lundi, ..., 7=Samedi en MySQL
        // On ajuste pour avoir 0=Lundi, 6=Dimanche
        String sql = "SELECT DAYOFWEEK(STR_TO_DATE(date, '%d/%m/%Y')) AS jour, COUNT(*) AS nb " +
                     "FROM consultation " +
                     "WHERE idMedecin = ? " +
                     "GROUP BY jour";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, idMedecin);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                int jourMySQL = rs.getInt("jour");  // 1-7 (Dimanche-Samedi)
                int nb = rs.getInt("nb");
                
                // Conversion : MySQL (1=Dim, 2=Lun) → Notre index (0=Lun, 6=Dim)
                int index = (jourMySQL == 1) ? 6 : jourMySQL - 2;
                consultationsParJour[index] = nb;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur stats consultations : " + e.getMessage());
            e.printStackTrace();
        }
        
        return consultationsParJour;
    }
    
    /**
     * Récupère le total des revenus des consultations d'un médecin
     * Utile pour les statistiques financières
     */
    public double getTotalRevenusMedecin(int idMedecin) {
        String sql = "SELECT SUM(prixConsultation) AS total FROM consultation WHERE idMedecin = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, idMedecin);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur calcul revenus : " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0.0;
    }
}