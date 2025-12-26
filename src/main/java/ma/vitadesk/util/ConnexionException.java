package ma.vitadesk.util;

/**
 * Exception personnalisée pour gérer les erreurs de connexion
 * Cela permet de distinguer les erreurs de connexion des autres erreurs
 */
public class ConnexionException extends Exception {
    
    /**
     * Constructeur avec message d'erreur
     * @param message le message explicatif de l'erreur
     */
    public ConnexionException(String message) {
        super(message);
    }
    
    /**
     * Constructeur avec message et cause (exception d'origine)
     * Utile pour garder la trace de l'erreur originale
     */
    public ConnexionException(String message, Throwable cause) {
        super(message, cause);
    }
}