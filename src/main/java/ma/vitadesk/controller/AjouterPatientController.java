package ma.vitadesk.controller;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ma.vitadesk.model.Patient;

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
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		comboSexe.getItems().addAll("F", "M"); // ajouter les valeurs au Combo (select) Sexe

	}
    
    // Reference vers le dashboard pour ajouter le patient à la liste
    private SecretaireDashboardController dashboardController;
    
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

        // Tous les champs sont valides -< création du patient
        Patient nouveauPatient = new Patient(
            txtNumSocial.getText().trim(),
            txtNom.getText().trim().toUpperCase(),
            txtPrenom.getText().trim(),
            dateNaissanceStr,  // <- String formatée            
            txtTelephone.getText().trim(),
            txtCin.getText().trim(),
            comboSexe.getValue(),
            txtAdresse.getText().trim() // adresse non obligatoire
        );

        // Ajout au dashboard
        dashboardController.ajouterPatient(nouveauPatient);

        // Fermeture du modal
        fermer();
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
        control.setStyle("-fx-background-color: white; -fx-border-width: 0.2px; -fx-border-color: black; -fx-border-radius: 3; -fx-font-size: 13px;"); // ou tu peux définir un style normal si tu veux
    }

}
