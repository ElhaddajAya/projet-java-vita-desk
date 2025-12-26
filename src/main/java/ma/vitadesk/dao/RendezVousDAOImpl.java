package ma.vitadesk.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ma.vitadesk.model.Medecin;
import ma.vitadesk.model.Patient;
import ma.vitadesk.model.RendezVous;
import ma.vitadesk.util.DatabaseConnection;

/**
 * Implémentation DAO pour les rendez-vous
 * Gère toutes les opérations SQL sur la table rendez_vous
 * Plus complexe car il faut charger aussi le patient et le médecin
 */
public class RendezVousDAOImpl implements IRendezVousDAO {

    // On a besoin des autres DAO pour charger les patients et médecins
    private IPatientDAO patientDAO = new PatientDAOImpl();
    private IMedecinDAO medecinDAO = new MedecinDAOImpl();

    /**
     * Récupère tous les RDV de la BDD
     * Fait des JOIN pour récupérer les infos complètes du patient et médecin
     */
    @Override
    public List<RendezVous> getAllRendezVous() {
        List<RendezVous> rdvList = new ArrayList<>();
        
        // JOIN pour récupérer toutes les infos en une seule requête
        String sql = "SELECT r.*, " +
                     "p.numSecuSocial, p.nom AS pNom, p.prenom AS pPrenom, p.dateNaissance, " +
                     "p.telephone AS pTel, p.cin, p.sexe, p.adresse, " +
                     "m.nom AS mNom, m.prenom AS mPrenom, m.specialite, " +
                     "m.telephone AS mTel, m.email " +
                     "FROM rendez_vous r " +
                     "JOIN patient p ON r.idPatient = p.idPatient " +
                     "JOIN medecin m ON r.idMedecin = m.idMedecin " +
                     "ORDER BY r.date, r.heure";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            
            while (rs.next()) {
                // Créer le patient à partir du ResultSet
                Patient patient = new Patient(
                    rs.getString("numSecuSocial"),
                    rs.getString("pNom"),
                    rs.getString("pPrenom"),
                    rs.getString("dateNaissance"),
                    rs.getString("pTel"),
                    rs.getString("cin"),
                    rs.getString("sexe"),
                    rs.getString("adresse")
                );
                
                // Créer le médecin
                Medecin medecin = new Medecin(
                    rs.getString("mNom"),
                    rs.getString("mPrenom"),
                    rs.getString("specialite"),
                    rs.getString("mTel"),
                    rs.getString("email")
                );
                
                // Créer le RDV avec le patient et le médecin
                RendezVous rdv = new RendezVous(
                    rs.getDate("date").toLocalDate(),      // SQL Date → LocalDate
                    rs.getTime("heure").toLocalTime(),     // SQL Time → LocalTime
                    patient,
                    medecin,
                    rs.getString("motif"),
                    RendezVous.Statut.valueOf(rs.getString("statut"))  // String → Enum
                );
                
                rdvList.add(rdv);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur chargement RDV : " + e.getMessage());
            e.printStackTrace();
        }
        
        return rdvList;
    }

    /**
     * Récupère les RDV d'un médecin spécifique
     * Utilise un filtre WHERE sur idMedecin
     */
    @Override
    public List<RendezVous> getRendezVousByMedecin(int idMedecin) {
        List<RendezVous> rdvList = new ArrayList<>();
        
        String sql = "SELECT r.*, " +
                     "p.numSecuSocial, p.nom AS pNom, p.prenom AS pPrenom, p.dateNaissance, " +
                     "p.telephone AS pTel, p.cin, p.sexe, p.adresse, " +
                     "m.nom AS mNom, m.prenom AS mPrenom, m.specialite, " +
                     "m.telephone AS mTel, m.email " +
                     "FROM rendez_vous r " +
                     "JOIN patient p ON r.idPatient = p.idPatient " +
                     "JOIN medecin m ON r.idMedecin = m.idMedecin " +
                     "WHERE r.idMedecin = ? " +
                     "ORDER BY r.date, r.heure";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, idMedecin);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                Patient patient = new Patient(
                    rs.getString("numSecuSocial"),
                    rs.getString("pNom"),
                    rs.getString("pPrenom"),
                    rs.getString("dateNaissance"),
                    rs.getString("pTel"),
                    rs.getString("cin"),
                    rs.getString("sexe"),
                    rs.getString("adresse")
                );
                
                Medecin medecin = new Medecin(
                    rs.getString("mNom"),
                    rs.getString("mPrenom"),
                    rs.getString("specialite"),
                    rs.getString("mTel"),
                    rs.getString("email")
                );
                
                RendezVous rdv = new RendezVous(
                    rs.getDate("date").toLocalDate(),
                    rs.getTime("heure").toLocalTime(),
                    patient,
                    medecin,
                    rs.getString("motif"),
                    RendezVous.Statut.valueOf(rs.getString("statut"))
                );
                
                rdvList.add(rdv);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur chargement RDV médecin : " + e.getMessage());
            e.printStackTrace();
        }
        
        return rdvList;
    }

    /**
     * Récupère les RDV d'une date spécifique
     * Utile pour afficher le planning d'un jour
     */
    @Override
    public List<RendezVous> getRendezVousByDate(LocalDate date) {
        List<RendezVous> rdvList = new ArrayList<>();
        
        String sql = "SELECT r.*, " +
                     "p.numSocial, p.nom AS pNom, p.prenom AS pPrenom, p.dateNaissance, " +
                     "p.telephone AS pTel, p.cin, p.sexe, p.adresse, " +
                     "m.nom AS mNom, m.prenom AS mPrenom, m.specialite, " +
                     "m.telephone AS mTel, m.email " +
                     "FROM rendez_vous r " +
                     "JOIN patient p ON r.idPatient = p.idPatient " +
                     "JOIN medecin m ON r.idMedecin = m.idMedecin " +
                     "WHERE r.date = ? " +
                     "ORDER BY r.heure";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            // LocalDate → SQL Date
            pst.setDate(1, Date.valueOf(date));
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                Patient patient = new Patient(
                    rs.getString("numSocial"),
                    rs.getString("pNom"),
                    rs.getString("pPrenom"),
                    rs.getString("dateNaissance"),
                    rs.getString("pTel"),
                    rs.getString("cin"),
                    rs.getString("sexe"),
                    rs.getString("adresse")
                );
                
                Medecin medecin = new Medecin(
                    rs.getString("mNom"),
                    rs.getString("mPrenom"),
                    rs.getString("specialite"),
                    rs.getString("mTel"),
                    rs.getString("email")
                );
                
                RendezVous rdv = new RendezVous(
                    rs.getDate("date").toLocalDate(),
                    rs.getTime("heure").toLocalTime(),
                    patient,
                    medecin,
                    rs.getString("motif"),
                    RendezVous.Statut.valueOf(rs.getString("statut"))
                );
                
                rdvList.add(rdv);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur chargement RDV date : " + e.getMessage());
            e.printStackTrace();
        }
        
        return rdvList;
    }

    /**
     * Ajoute un nouveau RDV dans la BDD
     * PROBLÈME : Il faut l'idPatient et l'idMedecin, mais on a que les objets
     * SOLUTION : Chercher les ID dans la BDD à partir du nom/prénom
     */
    @Override
    public boolean ajouterRendezVous(RendezVous rdv) {
        String sql = "INSERT INTO rendez_vous (date, heure, idPatient, idMedecin, motif, statut) " +
                     "VALUES (?, ?, " +
                     "(SELECT idPatient FROM patient WHERE numSocial = ?), " +
                     "(SELECT idMedecin FROM medecin WHERE nom = ? AND prenom = ?), " +
                     "?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            // Conversion LocalDate/LocalTime → SQL Date/Time
            pst.setDate(1, Date.valueOf(rdv.getDate()));
            pst.setTime(2, Time.valueOf(rdv.getHeure()));
            pst.setString(3, rdv.getPatient().getNumSocial());  // Sous-requête pour idPatient
            pst.setString(4, rdv.getDocteur().getNom());         // Sous-requête pour idMedecin
            pst.setString(5, rdv.getDocteur().getPrenom());
            pst.setString(6, rdv.getMotif());
            pst.setString(7, rdv.getStatut().name());  // Enum → String
            
            int lignesAffectees = pst.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur ajout RDV : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Modifie un RDV existant (surtout pour changer le statut)
     * PROBLÈME : On n'a pas l'idRDV dans la classe RendezVous
     * SOLUTION TEMPORAIRE : Identifier par date + heure + patient + médecin
     */
    @Override
    public boolean modifierRendezVous(RendezVous rdv) {
        String sql = "UPDATE rendez_vous SET statut = ? " +
                     "WHERE date = ? AND heure = ? " +
                     "AND idPatient = (SELECT idPatient FROM patient WHERE numSocial = ?) " +
                     "AND idMedecin = (SELECT idMedecin FROM medecin WHERE nom = ? AND prenom = ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, rdv.getStatut().name());
            pst.setDate(2, Date.valueOf(rdv.getDate()));
            pst.setTime(3, Time.valueOf(rdv.getHeure()));
            pst.setString(4, rdv.getPatient().getNumSocial());
            pst.setString(5, rdv.getDocteur().getNom());
            pst.setString(6, rdv.getDocteur().getPrenom());
            
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
     * Méthode auxiliaire pour supprimer un RDV par ses caractéristiques
     * Utilisée quand on n'a pas l'idRDV
     */
    public boolean supprimerRendezVous(RendezVous rdv) {
        String sql = "DELETE FROM rendez_vous " +
                     "WHERE date = ? AND heure = ? " +
                     "AND idPatient = (SELECT idPatient FROM patient WHERE numSocial = ?) " +
                     "AND idMedecin = (SELECT idMedecin FROM medecin WHERE nom = ? AND prenom = ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setDate(1, Date.valueOf(rdv.getDate()));
            pst.setTime(2, Time.valueOf(rdv.getHeure()));
            pst.setString(3, rdv.getPatient().getNumSocial());
            pst.setString(4, rdv.getDocteur().getNom());
            pst.setString(5, rdv.getDocteur().getPrenom());
            
            int lignesAffectees = pst.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur suppression RDV : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}