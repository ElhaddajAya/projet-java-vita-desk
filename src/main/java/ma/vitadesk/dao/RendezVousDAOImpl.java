package ma.vitadesk.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import ma.vitadesk.model.Medecin;
import ma.vitadesk.model.Patient;
import ma.vitadesk.model.RendezVous;
import ma.vitadesk.util.DatabaseConnection;

/**
 * Implémentation DAO pour les rendez-vous
 * Version améliorée avec récupération de l'idRDV
 */
public class RendezVousDAOImpl implements IRendezVousDAO {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Récupère tous les RDV avec JOIN sur patient et medecin
     * IMPORTANT: Maintenant on récupère aussi l'idRDV
     */
    @Override
    public List<RendezVous> getAllRendezVous() {
        List<RendezVous> rdvList = new ArrayList<>();
        
        String sql = "SELECT r.idRDV, r.dateRDV, r.heureRDV, r.motif, r.statut, " +
                     "p.idPatient, p.numSecuSociale, p.nom AS pNom, p.prenom AS pPrenom, " +
                     "p.dateNaissance, p.telephone AS pTel, p.cin, p.sexe, p.adresse, " +
                     "m.idMedecin, m.idUtilisateur, m.nom AS mNom, m.prenom AS mPrenom, " +
                     "m.specialite, m.telephone AS mTel, m.email " +
                     "FROM rendez_vous r " +
                     "JOIN patient p ON r.idPatient = p.idPatient " +
                     "JOIN medecin m ON r.idMedecin = m.idMedecin " +
                     "ORDER BY r.dateRDV, r.heureRDV";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            
            while (rs.next()) {
                rdvList.add(creerRendezVousDepuisResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur chargement RDV : " + e.getMessage());
            e.printStackTrace();
        }
        
        return rdvList;
    }

    /**
     * Récupère les RDV d'un médecin spécifique
     */
    @Override
    public List<RendezVous> getRendezVousByMedecin(int idMedecin) {
        List<RendezVous> rdvList = new ArrayList<>();
        
        String sql = "SELECT r.idRDV, r.dateRDV, r.heureRDV, r.motif, r.statut, " +
                     "p.idPatient, p.numSecuSociale, p.nom AS pNom, p.prenom AS pPrenom, " +
                     "p.dateNaissance, p.telephone AS pTel, p.cin, p.sexe, p.adresse, " +
                     "m.idMedecin, m.idUtilisateur, m.nom AS mNom, m.prenom AS mPrenom, " +
                     "m.specialite, m.telephone AS mTel, m.email " +
                     "FROM rendez_vous r " +
                     "JOIN patient p ON r.idPatient = p.idPatient " +
                     "JOIN medecin m ON r.idMedecin = m.idMedecin " +
                     "WHERE r.idMedecin = ? " +
                     "ORDER BY r.dateRDV, r.heureRDV";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, idMedecin);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                rdvList.add(creerRendezVousDepuisResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur chargement RDV médecin : " + e.getMessage());
            e.printStackTrace();
        }
        
        return rdvList;
    }

    /**
     * Récupère les RDV d'une date spécifique
     */
    @Override
    public List<RendezVous> getRendezVousByDate(LocalDate date) {
        List<RendezVous> rdvList = new ArrayList<>();
        
        String sql = "SELECT r.idRDV, r.dateRDV, r.heureRDV, r.motif, r.statut, " +
                     "p.idPatient, p.numSecuSociale, p.nom AS pNom, p.prenom AS pPrenom, " +
                     "p.dateNaissance, p.telephone AS pTel, p.cin, p.sexe, p.adresse, " +
                     "m.idMedecin, m.idUtilisateur, m.nom AS mNom, m.prenom AS mPrenom, " +
                     "m.specialite, m.telephone AS mTel, m.email " +
                     "FROM rendez_vous r " +
                     "JOIN patient p ON r.idPatient = p.idPatient " +
                     "JOIN medecin m ON r.idMedecin = m.idMedecin " +
                     "WHERE r.dateRDV = ? " +
                     "ORDER BY r.heureRDV";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setDate(1, Date.valueOf(date));
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                rdvList.add(creerRendezVousDepuisResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur chargement RDV date : " + e.getMessage());
            e.printStackTrace();
        }
        
        return rdvList;
    }

    /**
     * Ajoute un nouveau RDV
     */
    @Override
    public boolean ajouterRendezVous(RendezVous rdv) {
        String sql = "INSERT INTO rendez_vous (dateRDV, heureRDV, idPatient, idMedecin, motif, statut) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setDate(1, Date.valueOf(rdv.getDate()));
            pst.setTime(2, Time.valueOf(rdv.getHeure()));
            pst.setInt(3, rdv.getPatient().getIdPatient());
            pst.setInt(4, rdv.getMedecin().getIdMedecin());
            pst.setString(5, rdv.getMotif());
            pst.setString(6, rdv.getStatut().name());
            
            int lignesAffectees = pst.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur ajout RDV : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Modifie un RDV (principalement le statut)
     * Maintenant on utilise l'idRDV pour identifier précisément
     */
    @Override
    public boolean modifierRendezVous(RendezVous rdv) {
        String sql = "UPDATE rendez_vous SET statut = ?, motif = ? WHERE idRDV = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, rdv.getStatut().name());
            pst.setString(2, rdv.getMotif());
            pst.setInt(3, rdv.getIdRDV());
            
            int lignesAffectees = pst.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur modification RDV : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Supprime un RDV par son ID
     */
    @Override
    public boolean supprimerRendezVous(int idRDV) {
        String sql = "DELETE FROM rendez_vous WHERE idRDV = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, idRDV);
            
            int lignesAffectees = pst.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur suppression RDV : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Supprime un RDV par l'objet complet
     * Utilise l'idRDV si disponible, sinon les caractéristiques
     */
    @Override
    public boolean supprimerRendezVous(RendezVous rdv) {
        // Si on a l'ID, on l'utilise directement
        if (rdv.getIdRDV() > 0) {
            return supprimerRendezVous(rdv.getIdRDV());
        }
        
        // Sinon on utilise les caractéristiques (méthode ancienne)
        String sql = "DELETE FROM rendez_vous " +
                     "WHERE dateRDV = ? AND heureRDV = ? AND idPatient = ? AND idMedecin = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setDate(1, Date.valueOf(rdv.getDate()));
            pst.setTime(2, Time.valueOf(rdv.getHeure()));
            pst.setInt(3, rdv.getPatient().getIdPatient());
            pst.setInt(4, rdv.getMedecin().getIdMedecin());
            
            int lignesAffectees = pst.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur suppression RDV : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Méthode helper pour créer un RendezVous depuis un ResultSet
     * Évite la duplication de code (principe DRY)
     */
    private RendezVous creerRendezVousDepuisResultSet(ResultSet rs) throws SQLException {
        // Construire le patient
        String dateNaissance = "";
        if (rs.getDate("dateNaissance") != null) {
            dateNaissance = rs.getDate("dateNaissance").toLocalDate().format(DATE_FORMATTER);
        }
        
        Patient patient = new Patient(
            rs.getInt("idPatient"),
            rs.getString("numSecuSociale"),
            rs.getString("pNom"),
            rs.getString("pPrenom"),
            dateNaissance,
            rs.getString("pTel"),
            rs.getString("cin"),
            rs.getString("sexe"),
            rs.getString("adresse")
        );
        
        // Construire le médecin
        Medecin medecin = new Medecin(
            rs.getInt("idMedecin"),
            rs.getInt("idUtilisateur"),
            rs.getString("mNom"),
            rs.getString("mPrenom"),
            rs.getString("specialite"),
            rs.getString("mTel"),
            rs.getString("email")
        );
        
        // Construire le RDV avec l'ID
        return new RendezVous(
            rs.getInt("idRDV"),  // MAINTENANT ON RÉCUPÈRE L'ID !
            rs.getDate("dateRDV").toLocalDate(),
            rs.getTime("heureRDV").toLocalTime(),
            patient,
            medecin,
            rs.getString("motif"),
            RendezVous.Statut.valueOf(rs.getString("statut"))
        );
    }
}