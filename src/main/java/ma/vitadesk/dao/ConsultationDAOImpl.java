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

import ma.vitadesk.model.Consultation;
import ma.vitadesk.util.DatabaseConnection;

/**
 * Implémentation DAO pour les consultations
 * Version corrigée avec dateConsultation (DATE SQL) au lieu de date (String)
 */
public class ConsultationDAOImpl implements IConsultationDAO {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Récupère toutes les consultations d'un patient
     */
    @Override
    public List<Consultation> getConsultationsByPatient(String numSocial) {
        List<Consultation> consultations = new ArrayList<>();
        
        String sql = "SELECT c.idConsultation, c.dateConsultation, c.diagnostic, c.traitement, " +
                     "c.observations, c.prixConsultation, m.nom, m.prenom " +
                     "FROM consultation c " +
                     "JOIN medecin m ON c.idMedecin = m.idMedecin " +
                     "JOIN patient p ON c.idPatient = p.idPatient " +
                     "WHERE p.numSecuSociale = ? " +
                     "ORDER BY c.dateConsultation DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, numSocial);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                String nomMedecin = "Dr. " + rs.getString("prenom") + " " + rs.getString("nom");
                
                // Convertir SQL Date → String dd/MM/yyyy
                String dateStr = "";
                if (rs.getDate("dateConsultation") != null) {
                    dateStr = rs.getDate("dateConsultation").toLocalDate().format(DATE_FORMATTER);
                }
                
                Consultation consultation = new Consultation(
                    rs.getInt("idConsultation"),
                    dateStr,
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
     * Récupère les consultations d'un médecin
     */
    @Override
    public List<Consultation> getConsultationsByMedecin(int idMedecin) {
        List<Consultation> consultations = new ArrayList<>();
        
        String sql = "SELECT c.idConsultation, c.dateConsultation, c.diagnostic, c.traitement, " +
                     "c.observations, c.prixConsultation, m.nom, m.prenom " +
                     "FROM consultation c " +
                     "JOIN medecin m ON c.idMedecin = m.idMedecin " +
                     "WHERE c.idMedecin = ? " +
                     "ORDER BY c.dateConsultation DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, idMedecin);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                String nomMedecin = "Dr. " + rs.getString("prenom") + " " + rs.getString("nom");
                
                String dateStr = "";
                if (rs.getDate("dateConsultation") != null) {
                    dateStr = rs.getDate("dateConsultation").toLocalDate().format(DATE_FORMATTER);
                }
                
                Consultation consultation = new Consultation(
                    rs.getInt("idConsultation"),
                    dateStr,
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
     * Ajoute une nouvelle consultation
     * Convertit String dd/MM/yyyy → SQL Date
     */
    @Override
    public boolean ajouterConsultation(Consultation consultation, String numSocial, int idMedecin) {
        String sql = "INSERT INTO consultation (dateConsultation, idPatient, idMedecin, " +
                     "diagnostic, traitement, observations, prixConsultation) " +
                     "VALUES (?, (SELECT idPatient FROM patient WHERE numSecuSociale = ?), " +
                     "?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            // Convertir String → SQL Date
            LocalDate date = LocalDate.parse(consultation.getDate(), DATE_FORMATTER);
            pst.setDate(1, Date.valueOf(date));
            
            pst.setString(2, numSocial);
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
            pst.setInt(5, consultation.getIdConsultation());
            
            int lignesAffectees = pst.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur modification consultation : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Supprime une consultation
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
     * Compte les consultations d'un médecin
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
     * Statistiques par jour de la semaine
     */
    public int[] getConsultationsParJourSemaine(int idMedecin) {
        int[] consultationsParJour = new int[7];
        
        String sql = "SELECT DAYOFWEEK(dateConsultation) AS jour, COUNT(*) AS nb " +
                     "FROM consultation " +
                     "WHERE idMedecin = ? " +
                     "GROUP BY jour";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, idMedecin);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                int jourMySQL = rs.getInt("jour");
                int nb = rs.getInt("nb");
                
                // Conversion : MySQL (1=Dim, 2=Lun) → Index (0=Lun, 6=Dim)
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
     * Total des revenus
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
    
    /**
     * Récupère le nombre de consultations du mois en cours
     */
    @Override
    public int getConsultationsCeMois() {
        String sql = "SELECT COUNT(*) AS total " +
                     "FROM consultation " +
                     "WHERE MONTH(dateConsultation) = MONTH(CURDATE()) " +
                     "AND YEAR(dateConsultation) = YEAR(CURDATE())";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("total");
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur comptage consultations du mois : " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Récupère les numéros de sécu des patients ayant consulté avec un médecin
     * Retourne une liste UNIQUE (DISTINCT)
     */
    @Override
    public List<String> getNumSecuPatientsConsultes(int idMedecin) {
        List<String> numSecus = new ArrayList<>();
        
        String sql = "SELECT DISTINCT p.numSecuSociale " +
                     "FROM consultation c " +
                     "JOIN patient p ON c.idPatient = p.idPatient " +
                     "WHERE c.idMedecin = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, idMedecin);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                numSecus.add(rs.getString("numSecuSociale"));
            }
            
            System.out.println("✅ " + numSecus.size() + " patient(s) unique(s) ayant consulté");
            
        } catch (SQLException e) {
            System.err.println("Erreur récupération patients consultés : " + e.getMessage());
            e.printStackTrace();
        }
        
        return numSecus;
    }
}