package ma.vitadesk.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Classe qui gère le verrouillage des sessions avec un Thread
 * Comment ça marche :
 * 1. Quand quelqu'un se connecte → on crée un fichier .lock
 * 2. Si quelqu'un d'autre essaie → on vérifie si le fichier est récent (< 10 secondes)
 * 3. Si le fichier est vieux → on le supprime et on autorise la connexion
 * 4. Un Thread surveille et met à jour le fichier toutes les 2 secondes
 */
public class SessionLockManager {
    
    // Chemin du fichier de verrouillage
    private static final String LOCK_FILE_PATH = System.getProperty("user.home") + "/.vitadesk.lock";
    
    // Thread qui va surveiller le fichier lock
    private static Thread lockMonitorThread;
    
    // Temps maximum avant de considérer qu'une session est abandonnée (10 secondes)
    private static final long MAX_LOCK_AGE = 10000; // 10 secondes en millisecondes
    
    /**
     * Essaie d'acquérir le verrou (lock) pour se connecter
     * @return true si on peut se connecter, false si quelqu'un est déjà connecté
     */
    public static boolean acquireLock() {
        File lockFile = new File(LOCK_FILE_PATH);
        
        // Si le fichier existe déjà
        if (lockFile.exists()) {
            // Vérifier si le fichier est récent (session vraiment active)
            long fileAge = System.currentTimeMillis() - lockFile.lastModified();
            
            if (fileAge > MAX_LOCK_AGE) {
                // Le fichier est vieux → session abandonnée → on le supprime
                System.out.println("⚠️ Ancien fichier lock détecté (session abandonnée), suppression...");
                lockFile.delete();
            } else {
                // Le fichier est récent → quelqu'un est vraiment connecté
                return false;
            }
        }
        
        // Créer le nouveau fichier lock
        try {
            lockFile.createNewFile();
            
            // Écrire la date/heure de connexion
            FileWriter writer = new FileWriter(lockFile);
            writer.write("Session démarrée à : " + java.time.LocalDateTime.now());
            writer.close();
            
            // Supprimer automatiquement à la fermeture
            lockFile.deleteOnExit();
            
            // Démarrer le thread de surveillance
            startLockMonitor();
            
            System.out.println("✅ Session lock acquis");
            return true;
            
        } catch (IOException e) {
            System.err.println("Erreur lors de la création du fichier lock : " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Libère le verrou (supprime le fichier lock)
     * Appelé quand l'utilisateur se déconnecte
     */
    public static void releaseLock() {
        try {
            // Arrêter le thread de surveillance
            if (lockMonitorThread != null && lockMonitorThread.isAlive()) {
                lockMonitorThread.interrupt();
            }
            
            // Supprimer le fichier lock
            Files.deleteIfExists(Paths.get(LOCK_FILE_PATH));
            System.out.println("✅ Session libérée");
            
        } catch (IOException e) {
            System.err.println("Erreur lors de la libération du lock : " + e.getMessage());
        }
    }
    
    /**
     * Démarre un thread qui surveille le fichier lock
     * Le thread met à jour la date de modification toutes les 2 secondes
     * pour prouver que la session est toujours active
     */
    private static void startLockMonitor() {
        lockMonitorThread = new Thread(() -> {
            File lockFile = new File(LOCK_FILE_PATH);
            
            // Boucle infinie qui s'exécute toutes les 2 secondes
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Si le fichier n'existe plus, on le recrée
                    if (!lockFile.exists()) {
                        System.out.println("⚠️ Fichier lock supprimé, recréation...");
                        lockFile.createNewFile();
                    }
                    
                    // Mettre à jour la date de modification du fichier
                    // Ça prouve que la session est toujours active
                    lockFile.setLastModified(System.currentTimeMillis());
                    
                    // Attendre 2 secondes avant la prochaine vérification
                    Thread.sleep(2000);
                    
                } catch (InterruptedException e) {
                    // Le thread a été arrêté → on sort de la boucle
                    System.out.println("🛑 Thread de surveillance arrêté");
                    break;
                } catch (IOException e) {
                    System.err.println("Erreur dans le thread de surveillance : " + e.getMessage());
                }
            }
        });
        
        // Thread daemon = se ferme automatiquement avec l'application
        lockMonitorThread.setDaemon(true);
        lockMonitorThread.start();
        System.out.println("🔄 Thread de surveillance démarré");
    }
    
    /**
     * Vérifie si une session est déjà active
     * @return true si quelqu'un est connecté, false sinon
     */
    public static boolean isSessionActive() {
        File lockFile = new File(LOCK_FILE_PATH);
        
        if (!lockFile.exists()) {
            return false;
        }
        
        // Vérifier si le fichier est récent
        long fileAge = System.currentTimeMillis() - lockFile.lastModified();
        
        if (fileAge > MAX_LOCK_AGE) {
            // Fichier trop vieux → session abandonnée
            lockFile.delete();
            return false;
        }
        
        return true;
    }
}