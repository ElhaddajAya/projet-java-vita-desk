package ma.vitadesk.dao;

import java.util.List;
import ma.vitadesk.model.Medecin;

/**
 * Interface DAO pour les médecins
 * Définit toutes les opérations qu'on peut faire sur les médecins
 */
public interface IMedecinDAO {
    
    /**
     * Récupère tous les médecins de la BDD
     * @return liste complète des médecins
     */
    List<Medecin> getAllMedecins();
    
    /**
     * Récupère un médecin par son ID utilisateur
     * @param idUtilisateur l'ID de l'utilisateur lié
     * @return le médecin ou null
     */
    Medecin getMedecinById(int idUtilisateur);
    
    /**
     * Ajoute un nouveau médecin
     * @param medecin le médecin à ajouter
     * @return true si ajout réussi
     */
    boolean ajouterMedecin(Medecin medecin);
    
    /**
     * Modifie les infos d'un médecin
     * @param medecin le médecin modifié
     * @return true si modif réussie
     */
    boolean modifierMedecin(Medecin medecin);
    
    /**
     * Supprime un médecin
     * @param idUtilisateur l'ID du médecin à supprimer
     * @return true si suppression réussie
     */
    boolean supprimerMedecin(int idUtilisateur);
}