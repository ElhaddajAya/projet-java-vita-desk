package ma.vitadesk.util;

import ma.vitadesk.model.Utilisateur;

/**
 * Classe qui gère la session de l'utilisateur connecté
 * Pattern Singleton : une seule instance dans toute l'application
 * Permet de stocker et récupérer l'utilisateur connecté partout dans l'app
 */
public class SessionManager {
    
    // Instance unique de SessionManager (Singleton pattern)
    private static SessionManager instance;
    
    // L'utilisateur actuellement connecté
    private Utilisateur utilisateurConnecte;
    
    // Constructeur privé pour empêcher la création d'instances depuis l'extérieur
    private SessionManager() {
    }
    
    /**
     * Méthode pour obtenir l'instance unique de SessionManager
     * Si elle n'existe pas encore, on la crée
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Définir l'utilisateur connecté (appelé après login réussi)
     */
    public void setUtilisateurConnecte(Utilisateur utilisateur) {
        this.utilisateurConnecte = utilisateur;
    }
    
    /**
     * Récupérer l'utilisateur connecté
     */
    public Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }
    
    /**
     * Vérifier si un utilisateur est connecté
     */
    public boolean estConnecte() {
        return utilisateurConnecte != null;
    }
    
    /**
     * Déconnecter l'utilisateur (nettoyer la session)
     */
    public void deconnecter() {
        utilisateurConnecte = null;
    }
    
    /**
     * Obtenir le rôle de l'utilisateur connecté
     */
    public Utilisateur.Role getRole() {
        if (utilisateurConnecte != null) {
            return utilisateurConnecte.getRole();
        }
        return null;
    }
}