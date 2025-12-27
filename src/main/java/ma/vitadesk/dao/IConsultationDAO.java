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
     * Récupère les numéros de sécu des patients ayant consulté avec un médecin
     * Utilisé pour afficher l'historique des patients dans le dashboard médecin
     * @param idMedecin l'ID du médecin
     * @return liste des numéros de sécu uniques
     */
    List<String> getNumSecuPatientsConsultes(int idMedecin);
    
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
    
    /**
     * Récupère le nombre de consultations par jour pour la semaine en cours
     * Utilisé pour le graphique du dashboard médecin
     * @param idMedecin l'ID du médecin
     * @return tableau de 7 entiers (Lundi à Dimanche)
     */
    int[] getConsultationsParJourSemaine(int idMedecin);
    
    /**
     * Récupère le nombre de consultations du mois en cours
     * @return nombre de consultations ce mois
     */
    int getConsultationsCeMois();
    
    /**
     * Compte le nombre total de consultations d'un médecin
     * Utilisé pour les statistiques du dashboard
     * @param idMedecin l'ID du médecin
     * @return nombre total de consultations
     */
    int compterConsultationsMedecin(int idMedecin);
    
    /**
     * Calcule le total des revenus d'un médecin
     * Somme de tous les prix de consultations
     * @param idMedecin l'ID du médecin
     * @return total des revenus en MAD
     */
    double getTotalRevenusMedecin(int idMedecin);
}