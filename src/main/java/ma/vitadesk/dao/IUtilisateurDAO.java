package ma.vitadesk.dao;

import ma.vitadesk.model.Utilisateur;

/**
 * Interface qui définit les opérations CRUD pour les utilisateurs
 * C'est un contrat que toute classe DAO doit respecter
 */
public interface IUtilisateurDAO {
    
    /**
     * Récupère un utilisateur depuis la BDD par son login
     * @param login le nom d'utilisateur
     * @return l'objet Utilisateur ou null si non trouvé
     */
    Utilisateur getUtilisateurByLogin(String login);
    
    /**
     * Vérifie si le mot de passe correspond à celui stocké en BDD
     * @param login le nom d'utilisateur
     * @param motDePasse le mot de passe à vérifier
     * @return true si le mot de passe est correct, false sinon
     */
    boolean verifierMotDePasse(String login, String motDePasse);
}