package ma.vitadesk.controller;

import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ma.vitadesk.dao.IUtilisateurDAO;
import ma.vitadesk.dao.UtilisateurDAOImpl;
import ma.vitadesk.util.ConnexionException;
import ma.vitadesk.util.SessionLockManager;
import ma.vitadesk.model.Utilisateur;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;

/**
 * Contrôleur pour la page de connexion
 * Gère l'authentification des utilisateurs (secrétaires et médecins)
 * + Empêche les connexions multiples avec un Thread
 */
public class LoginController implements Initializable {

    @FXML private TextField txtLogin;
    @FXML private TextField txtPassword;
    @FXML private ComboBox<String> cbRole;
    @FXML private Label lblMessage;
    @FXML private Button btnConnecter;
    @FXML private Label lblError;
    
    // DAO pour accéder à la base de données (pattern DAO)
    private IUtilisateurDAO utilisateurDAO;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation du DAO
        utilisateurDAO = new UtilisateurDAOImpl();
        
        // On ajoute les rôles possibles dans le ComboBox
        cbRole.getItems().addAll("SECRETAIRE", "MEDECIN");
    }
    
    /**
     * Méthode appelée quand l'utilisateur clique sur "Se Connecter"
     * Utilise maintenant les vraies données de la BDD
     * + Vérifie qu'il n'y a pas déjà quelqu'un de connecté
     */
    @FXML
    private void handleLogin() {
        // === VÉRIFICATION DU LOCK (Thread) ===
        // On vérifie si quelqu'un est déjà connecté
        if (SessionLockManager.isSessionActive()) {
            // Si oui → on affiche une alerte et on bloque la connexion
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Session active");
            alert.setHeaderText("Connexion impossible");
            alert.setContentText("Un utilisateur est déjà connecté.\nVeuillez attendre qu'il se déconnecte.");
            alert.showAndWait();
            return; // On arrête ici
        }
        
        // Réinitialiser les erreurs visuelles
        lblError.setVisible(false);
        clearErrorStyle(txtLogin);
        clearErrorStyle(txtPassword);
        clearErrorStyle(cbRole);

        // Récupérer les valeurs saisies
        String login = txtLogin.getText().trim();
        String motDePasse = txtPassword.getText();
        String role = cbRole.getValue();

        // Validation des champs vides
        boolean hasError = false;
        if (login.isEmpty()) {
            setErrorStyle(txtLogin);
            hasError = true;
        }
        if (motDePasse.isEmpty()) {
            setErrorStyle(txtPassword);
            hasError = true;
        }
        if (role == null) {
            setErrorStyle(cbRole);
            hasError = true;
        }
        if (hasError) {
            lblError.setText("Veuillez remplir tous les champs");
            lblError.setVisible(true);
            return;
        }

        try {
            // Tentative de connexion via le DAO
            Utilisateur utilisateur = authentifier(login, motDePasse, role);
            
            // === ACQUISITION DU LOCK ===
            // Si connexion réussie → on "réserve" la session
            if (!SessionLockManager.acquireLock()) {
                // Si on ne peut pas acquérir le lock (cas rare)
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setContentText("Impossible de démarrer la session.");
                alert.showAndWait();
                return;
            }
            
            // Si tout est OK, on ouvre le dashboard approprié
            ouvrirDashboard(utilisateur);
            
        } catch (ma.vitadesk.util.ConnexionException e) {
            // Affichage de l'erreur de connexion
            lblError.setText(e.getMessage());
            lblError.setVisible(true);
            
            // Mettre en rouge les champs concernés selon l'erreur
            if (e.getMessage().contains("Utilisateur")) {
                setErrorStyle(txtLogin);
            } else if (e.getMessage().contains("Mot de passe")) {
                setErrorStyle(txtPassword);
            } else if (e.getMessage().contains("rôle")) {
                setErrorStyle(cbRole);
            }
        }
    }
    
    /**
     * Authentifie l'utilisateur via le DAO
     * @return l'utilisateur authentifié
     * @throws ConnexionException si la connexion échoue
     */
    private Utilisateur authentifier(String login, String motDePasse, String roleChoisi) throws ConnexionException {
        // Vérifier d'abord si l'utilisateur existe
        Utilisateur utilisateur = utilisateurDAO.getUtilisateurByLogin(login);
        
        if (utilisateur == null) {
            throw new ConnexionException("Utilisateur non trouvé");
        }
        
        // Vérifier si le rôle correspond
        if (!utilisateur.getRole().name().equals(roleChoisi)) {
            throw new ConnexionException("Le rôle sélectionné ne correspond pas");
        }
        
        // Vérifier le mot de passe
        if (!utilisateurDAO.verifierMotDePasse(login, motDePasse)) {
            throw new ConnexionException("Mot de passe incorrect");
        }
        
        // Tout est OK, on retourne l'utilisateur
        return utilisateur;
    }
    
    /**
     * Ouvre le dashboard approprié selon le rôle de l'utilisateur
     * Passe les informations de l'utilisateur au dashboard
     */
    private void ouvrirDashboard(Utilisateur utilisateur) {
        try {
            // Fermer la fenêtre de login
            Stage loginStage = (Stage) txtLogin.getScene().getWindow();
            loginStage.close();

            // Charger le FXML approprié selon le rôle
            String fxmlPath;
            String titre;
            
            if (utilisateur.getRole() == Utilisateur.Role.SECRETAIRE) {
                fxmlPath = "/view/fxml/secretaire_dashboard.fxml";
                titre = "VitaDesk - Dashboard Secrétaire";
            } else {
                fxmlPath = "/view/fxml/medecin_dashboard.fxml";
                titre = "VitaDesk - Dashboard Médecin";
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            // Passer les infos de l'utilisateur au dashboard
            if (utilisateur.getRole() == Utilisateur.Role.SECRETAIRE) {
                SecretaireDashboardController controller = loader.getController();
                controller.setUtilisateur(utilisateur);
            } else {
                MedecinDashboardController controller = loader.getController();
                controller.setUtilisateur(utilisateur);
            }
            
            // Créer et afficher la nouvelle fenêtre
            Stage stage = new Stage();
            stage.setTitle(titre);
            stage.setScene(new Scene(root));
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
//            stage.setResizable(false);
            stage.centerOnScreen();
            
            // === LIBÉRATION DU LOCK À LA FERMETURE ===
            // Quand l'utilisateur ferme le dashboard → on libère le lock
            stage.setOnCloseRequest(event -> {
                SessionLockManager.releaseLock();
                System.out.println("Application fermée - Lock libéré");
            });
            
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            lblError.setText("Erreur lors de l'ouverture du dashboard");
            lblError.setVisible(true);
            
            // En cas d'erreur, on libère le lock
            SessionLockManager.releaseLock();
        }
    }

    // === Méthodes pour gérer les bordures d'erreur (style visuel) ===
    
    private void setErrorStyle(Control control) {
        control.setStyle("-fx-background-color: white; -fx-border-color: red; -fx-border-width: 0.3px; -fx-border-radius: 3px;");
    }

    private void clearErrorStyle(Control control) {
        control.setStyle("-fx-background-color: white; -fx-border-width: 0.2px; -fx-border-color: black; -fx-border-radius: 3;");
    }
}