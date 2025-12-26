package ma.vitadesk.dao;

import java.util.List;
import ma.vitadesk.model.Consultation;

/**
 * Interface DAO pour les consultations
 * Gère l'accès aux consultations dans la BDD
 */
public interface IConsultationDAO {
    
    /**
     * Récupère toutes les consultations d'un patient
     * @param numSocial numéro de sécu du patient
     * @return liste des consultations du patient
     */
    List<Consultation> getConsultationsByPatient(String numSocial);
    
    /**
     * Récupère les consultations d'un médecin
     * @param idMedecin l'ID du médecin
     * @return liste des consultations du médecin
     */
    List<Consultation> getConsultationsByMedecin(int idMedecin);
    
    /**
     * Ajoute une nouvelle consultation
     * @param consultation la consultation à ajouter
     * @param numSocial le numéro de sécu du patient
     * @param idMedecin l'ID du médecin
     * @return true si ajout réussi
     */
    boolean ajouterConsultation(Consultation consultation, String numSocial, int idMedecin);
    
    /**
     * Modifie une consultation existante
     * @param consultation la consultation modifiée
     * @return true si modif réussie
     */
    boolean modifierConsultation(Consultation consultation);
}