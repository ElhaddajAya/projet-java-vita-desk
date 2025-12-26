package ma.vitadesk.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Classe qui gère le verrouillage des sessions avec un Thread
 * Pour empêcher plusieurs utilisateurs de se connecter en même temps
 * Utilise un fichier .lock pour savoir si quelqu'un est déjà connecté
 * 
 * Comment ça marche :
 * 1. Quand quelqu'un se connecte → on crée un fichier .lock
 * 2. Si quelqu'un d'autre essaie de se connecter → on vérifie si le fichier existe
 * 3. Si le fichier existe → on refuse la connexion
 * 4. Un Thread surveille le fichier pour éviter qu'il soit supprimé par erreur
 */
public class SessionLockManager {
    
    // Chemin du fichier de verrouillage (lock file)
    private static final String LOCK_FILE_PATH = System.getProperty("user.home") + "/.vitadesk.lock";
    
    // Thread qui va surveiller le fichier lock
    private static Thread lockMonitorThread;
    
    /**
     * Essaie d'acquérir le verrou (lock) pour se connecter
     * @return true si on peut se connecter, false si quelqu'un est déjà connecté
     */
    public static boolean acquireLock() {
        File lockFile = new File(LOCK_FILE_PATH);
        
        // Si le fichier existe déjà → quelqu'un est connecté
        if (lockFile.exists()) {
            return false;
        }
        
        // Sinon, on crée le fichier pour "réserver" la session
        try {
            lockFile.createNewFile();
            
            // On écrit la date/heure de connexion dans le fichier
            FileWriter writer = new FileWriter(lockFile);
            writer.write("Session démarrée à : " + java.time.LocalDateTime.now());
            writer.close();
            
            // Important : on supprime le fichier quand l'application se ferme
            lockFile.deleteOnExit();
            
            // On démarre un thread qui surveille le fichier
            startLockMonitor();
            
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
            // On arrête le thread de surveillance
            if (lockMonitorThread != null && lockMonitorThread.isAlive()) {
                lockMonitorThread.interrupt();
            }
            
            // On supprime le fichier lock
            Files.deleteIfExists(Paths.get(LOCK_FILE_PATH));
            System.out.println("Session libérée ✓");
            
        } catch (IOException e) {
            System.err.println("Erreur lors de la libération du lock : " + e.getMessage());
        }
    }
    
    /**
     * Démarre un thread qui surveille le fichier lock
     * Si le fichier est supprimé par erreur, on le recrée
     */
    private static void startLockMonitor() {
        lockMonitorThread = new Thread(() -> {
            File lockFile = new File(LOCK_FILE_PATH);
            
            // Boucle infinie qui vérifie toutes les 2 secondes
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Si le fichier n'existe plus, on le recrée
                    if (!lockFile.exists()) {
                        System.out.println("⚠️ Fichier lock supprimé, recréation...");
                        lockFile.createNewFile();
                    }
                    
                    // On attend 2 secondes avant de revérifier
                    Thread.sleep(2000);
                    
                } catch (InterruptedException e) {
                    // Le thread a été arrêté → on sort de la boucle
                    break;
                } catch (IOException e) {
                    System.err.println("Erreur dans le thread de surveillance : " + e.getMessage());
                }
            }
        });
        
        // On met le thread en daemon pour qu'il se ferme avec l'application
        lockMonitorThread.setDaemon(true);
        lockMonitorThread.start();
    }
    
    /**
     * Vérifie si une session est déjà active
     * @return true si quelqu'un est connecté, false sinon
     */
    public static boolean isSessionActive() {
        return new File(LOCK_FILE_PATH).exists();
    }
}