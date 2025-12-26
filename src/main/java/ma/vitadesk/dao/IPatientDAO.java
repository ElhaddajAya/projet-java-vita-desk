package ma.vitadesk.dao;

import java.util.List;
import ma.vitadesk.model.Patient;

/**
 * Interface DAO pour les patients
 * Définit le contrat pour toutes les opérations sur les patients
 * Pattern DAO = séparer l'accès aux données de la logique métier
 */
public interface IPatientDAO {
    
    /**
     * Récupère tous les patients depuis la BDD
     * @return liste de tous les patients
     */
    List<Patient> getAllPatients();
    
    /**
     * Récupère un patient par son numéro de sécurité sociale
     * @param numSocial numéro de sécu du patient
     * @return le patient ou null si non trouvé
     */
    Patient getPatientByNumSocial(String numSocial);
    
    /**
     * Ajoute un nouveau patient dans la BDD
     * @param patient le patient à ajouter
     * @return true si succès, false sinon
     */
    boolean ajouterPatient(Patient patient);
    
    /**
     * Modifie les infos d'un patient existant
     * @param patient le patient avec les nouvelles infos
     * @return true si succès, false sinon
     */
    boolean modifierPatient(Patient patient);
    
    /**
     * Supprime un patient de la BDD
     * @param numSocial numéro de sécu du patient à supprimer
     * @return true si succès, false sinon
     */
    boolean supprimerPatient(String numSocial);
}