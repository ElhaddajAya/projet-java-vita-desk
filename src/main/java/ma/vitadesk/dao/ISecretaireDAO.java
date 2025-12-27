package ma.vitadesk.dao;

import ma.vitadesk.model.Secretaire;

/**
 * Interface DAO pour les secrétaires
 * Définit toutes les opérations qu'on peut faire sur les secrétaires
 */
public interface ISecretaireDAO {
    
    /**
     * Récupère une secrétaire par son ID utilisateur
     * @param idUtilisateur l'ID de l'utilisateur lié
     * @return la secrétaire ou null
     */
    Secretaire getSecretaireById(int idUtilisateur);
    
    /**
     * Ajoute une nouvelle secrétaire
     * @param secretaire la secrétaire à ajouter
     * @return true si ajout réussi
     */
    boolean ajouterSecretaire(Secretaire secretaire);
    
    /**
     * Modifie les infos d'une secrétaire
     * @param secretaire la secrétaire modifiée
     * @return true si modif réussie
     */
    boolean modifierSecretaire(Secretaire secretaire);
    
    /**
     * Supprime une secrétaire
     * @param idUtilisateur l'ID de la secrétaire à supprimer
     * @return true si suppression réussie
     */
    boolean supprimerSecretaire(int idUtilisateur);
}