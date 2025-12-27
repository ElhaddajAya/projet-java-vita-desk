package ma.vitadesk.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ma.vitadesk.dao.IMedecinDAO;
import ma.vitadesk.dao.MedecinDAOImpl;
import ma.vitadesk.model.Medecin;

/**
 * Contrôleur pour ajouter un nouveau médecin
 * Maintenant avec intégration MySQL via DAO
 */
public class AjouterDocteurController implements Initializable {
	@FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private ComboBox<String> comboSpecialite;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtEmail;

    // DAO pour accéder à la base de données
    private IMedecinDAO medecinDAO;
    
    private SecretaireDashboardController dashboardController;

    public void setDashboardController(SecretaireDashboardController controller) {
        this.dashboardController = controller;
    }

    @Override
	public void initialize(URL location, ResourceBundle resources) {
        // Initialiser le DAO
        medecinDAO = new MedecinDAOImpl();
        
        // Pré-remplir le ComboBox avec les spécialités courantes
        comboSpecialite.getItems().addAll(
            "Généraliste",
            "Pédiatre",
            "Cardiologue",
            "Dermatologue",
            "Dentiste",
            "Ophtalmologue",
            "Gynécologue",
            "ORL",
            "Neurologue",
            "Psychiatre",
            "Radiologue"
        );
        comboSpecialite.setPromptText("Choisir une spécialité");
    }

    @FXML
    private void enregistrer() {
        // Réinitialise les bordures
        clearErrorStyle(txtNom);
        clearErrorStyle(txtPrenom);
        clearErrorStyle(comboSpecialite);
        clearErrorStyle(txtTelephone);
        clearErrorStyle(txtEmail);

        boolean hasError = false;

        if (txtNom.getText().trim().isEmpty()) {
            setErrorStyle(txtNom);
            hasError = true;
        }
        if (txtPrenom.getText().trim().isEmpty()) {
            setErrorStyle(txtPrenom);
            hasError = true;
        }
        if (comboSpecialite.getValue() == null) {
            setErrorStyle(comboSpecialite);
            hasError = true;
        }
        if (txtTelephone.getText().trim().isEmpty()) {
            setErrorStyle(txtTelephone);
            hasError = true;
        }
        if (txtEmail.getText().trim().isEmpty()) {
            setErrorStyle(txtEmail);
            hasError = true;
        }
        
        if (hasError) {
            return;
        }

        // Création du nouveau docteur
        Medecin nouveauDocteur = new Medecin(
            txtNom.getText().trim().toUpperCase(),
            txtPrenom.getText().trim(),
            comboSpecialite.getValue(),
            txtTelephone.getText().trim(),
            txtEmail.getText().trim()
        );

        // 🆕 ENREGISTRER DANS LA BDD
        boolean success = medecinDAO.ajouterMedecin(nouveauDocteur);
        
        if (success) {
            // Si succès → rafraîchir la liste dans le dashboard
            dashboardController.chargerMedecins();
            
            // Message de succès
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Médecin ajouté avec succès !");
            alert.show();
            
            fermer();
        } else {
            // Si échec
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Erreur lors de l'ajout du médecin.");
            alert.show();
        }
    }

    @FXML
    private void annuler() {
        fermer();
    }

    private void fermer() {
        Stage stage = (Stage) txtNom.getScene().getWindow();
        stage.close();
    }

    private void setErrorStyle(Control control) {
        control.setStyle("-fx-background-color: white; -fx-border-color: red; -fx-border-width: 0.3px; -fx-border-radius: 3px; -fx-font-size: 13px;");
    }

    private void clearErrorStyle(Control control) {
        control.setStyle("-fx-background-color: white; -fx-border-width: 0.2px; -fx-border-color: black; -fx-border-radius: 3; -fx-font-size: 13px;");
    }
}