package ma.vitadesk.controller;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ma.vitadesk.dao.IPatientDAO;
import ma.vitadesk.dao.PatientDAOImpl;
import ma.vitadesk.model.Patient;

/**
 * Contrôleur pour ajouter un nouveau patient
 * Maintenant avec intégration MySQL via DAO
 */
public class AjouterPatientController implements Initializable {
	
	@FXML private TextField txtNumSocial;
    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private DatePicker txtDateNaissance;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtCin;
    @FXML private ComboBox<String> comboSexe;
    @FXML private TextArea txtAdresse;
	@FXML private Label lblMessage;
	
    // DAO pour accéder à la base de données
    private IPatientDAO patientDAO;
    
    // Reference vers le dashboard pour rafraîchir la liste
    private SecretaireDashboardController dashboardController;
    
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Initialiser le DAO
        patientDAO = new PatientDAOImpl();
        
        // Ajouter les valeurs au Combo (select) Sexe
		comboSexe.getItems().addAll("F", "M");
	}
    
    public void setDashboardController(SecretaireDashboardController controller) {
        this.dashboardController = controller;
    }
    
    @FXML
    private void enregistrer() {
    	// Réinitialise tous les champs (au cas où il y avait des erreurs précédentes)
        clearErrorStyle(txtNumSocial);
        clearErrorStyle(txtNom);
        clearErrorStyle(txtPrenom);
        clearErrorStyle(txtCin);
        clearErrorStyle(txtDateNaissance);
        clearErrorStyle(txtTelephone);
        clearErrorStyle(comboSexe);

        boolean hasError = false;

        // Vérification des champs obligatoires
        if (txtNumSocial.getText().trim().isEmpty()) {
            setErrorStyle(txtNumSocial);
            hasError = true;
        }
        if (txtNom.getText().trim().isEmpty()) {
            setErrorStyle(txtNom);
            hasError = true;
        }
        if (txtPrenom.getText().trim().isEmpty()) {
            setErrorStyle(txtPrenom);
            hasError = true;
        }
        if (txtCin.getText().trim().isEmpty()) {
            setErrorStyle(txtCin);
            hasError = true;
        }
        if (txtTelephone.getText().trim().isEmpty()) {
            setErrorStyle(txtTelephone);
            hasError = true;
        }
        if (comboSexe.getValue() == null || comboSexe.getValue().trim().isEmpty()) {
            setErrorStyle(comboSexe);
            hasError = true;
        }
        
        // Formatage de la date (de LocalDate → String "dd/MM/yyyy")
        String dateNaissanceStr = "";
        if (txtDateNaissance.getValue() != null) {
            dateNaissanceStr = txtDateNaissance.getValue()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } else {
            setErrorStyle(txtDateNaissance);
            hasError = true;
        }

        // Si erreur → on arrête ici, pas d'enregistrement
        if (hasError) {
            return;
        }

        // Tous les champs sont valides → création du patient
        Patient nouveauPatient = new Patient(
            txtNumSocial.getText().trim(),
            txtNom.getText().trim().toUpperCase(),
            txtPrenom.getText().trim(),
            dateNaissanceStr,           
            txtTelephone.getText().trim(),
            txtCin.getText().trim(),
            comboSexe.getValue(),
            txtAdresse.getText().trim() // adresse non obligatoire
        );

        // 🆕 ENREGISTRER DANS LA BDD
        boolean success = patientDAO.ajouterPatient(nouveauPatient);
        
        if (success) {
            // Si succès → rafraîchir la liste dans le dashboard
            dashboardController.chargerPatients();
            
            // Message de succès
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Patient ajouté avec succès !");
            alert.show();
            
            // Fermeture du modal
            fermer();
        } else {
            // Si échec (ex: numéro de sécu déjà existant)
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Erreur lors de l'ajout du patient.\nLe numéro de sécurité sociale existe peut-être déjà.");
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
    
    // Applique une bordure rouge à un TextField ou TextArea
    private void setErrorStyle(Control control) {
        control.setStyle("-fx-background-color: white; -fx-border-color: red; -fx-border-width: 0.3px; -fx-border-radius: 3px; -fx-font-size: 13px;");
    }

    // Retire la bordure d'erreur (retour au style normal)
    private void clearErrorStyle(Control control) {
        control.setStyle("-fx-background-color: white; -fx-border-width: 0.2px; -fx-border-color: black; -fx-border-radius: 3; -fx-font-size: 13px;");
    }
}