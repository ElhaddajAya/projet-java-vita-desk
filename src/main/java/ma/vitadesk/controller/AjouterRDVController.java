package ma.vitadesk.controller;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ma.vitadesk.model.Medecin;
import ma.vitadesk.model.Patient;
import ma.vitadesk.model.RendezVous;

public class AjouterRDVController implements Initializable {

	@FXML private DatePicker datePickerRDV;
    @FXML private ComboBox<String> comboHeure;
    @FXML private ComboBox<String> comboPatient;
    @FXML private ComboBox<String> comboDocteur;
    @FXML private TextField txtMotif;
    
    // Listes reçues depuis le dashboard
    private ObservableList<Patient> patients = FXCollections.observableArrayList();
    private ObservableList<Medecin> medecins = FXCollections.observableArrayList();
    
    private Medecin medecinConnecte; // Pour le mode médecin
    
    private SecretaireDashboardController dashboardController;
    private MedecinDashboardController dashboardControllerMed;
    
    public void setDashboardController(SecretaireDashboardController controller) {
        this.dashboardController = controller;
    }
    
    public void setDashboardControllerMed(MedecinDashboardController controller) {
        this.dashboardControllerMed = controller;
    }
    
	// Méthode appelée depuis le dashboard pour passer les listes
    public void initialiserAvecListes(ObservableList<Patient> patients, ObservableList<Medecin> medecins) {
        this.patients = patients;
        this.medecins = medecins;
        remplirComboPatients();
        remplirComboDocteurs();
    }
    
    public void initialiserPourMedecin(ObservableList<Patient> patients, Medecin medecinConnecte) {
        this.patients = patients;
        this.medecinConnecte = medecinConnecte;
        remplirComboPatients();
        
        // Désactiver et pré-remplir le ComboBox docteur
        comboDocteur.setDisable(true);
        comboDocteur.setValue("Dr. " + medecinConnecte.getPrenom() + " " + medecinConnecte.getNom() + " - " + medecinConnecte.getSpecialite());
        comboDocteur.setStyle("-fx-opacity: 1.0;"); // Garder visible même si désactivé
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboHeure.getItems().addAll(
            "08:00", "09:00", "10:00", "11:00", "12:00",
            "13:00", "14:00", "15:00", "16:00", "17:00"
        );
        comboHeure.setPromptText("Choisir l'heure");
        txtMotif.setPromptText("Ex: Consultation générale, Contrôle...");
    }

    // === ComboBox Patient avec recherche ===
    private void remplirComboPatients() {
        ObservableList<String> patientStrings = FXCollections.observableArrayList();
        for (Patient p : patients) {
            patientStrings.add(p.getPrenom() + " " + p.getNom() + " - " + p.getNumSocial());
        }

        FilteredList<String> filtered = new FilteredList<>(patientStrings, s -> true);
        comboPatient.setItems(filtered);

        comboPatient.getEditor().textProperty().addListener((obs, old, newVal) -> {
            String texte = newVal == null ? "" : newVal.toLowerCase().trim();
            filtered.setPredicate(str -> texte.isEmpty() || str.toLowerCase().contains(texte));
            if (!comboPatient.isShowing() && !texte.isEmpty()) comboPatient.show();
        });

        // Quand on sélectionne → garder le texte
        comboPatient.setOnAction(e -> {
            if (comboPatient.getValue() != null) {
                comboPatient.getEditor().setText(comboPatient.getValue());
            }
        });
    }

    // === ComboBox Medecin avec recherche ===
    private void remplirComboDocteurs() {
        ObservableList<String> docteurStrings = FXCollections.observableArrayList();
        for (Medecin d : medecins) {
            docteurStrings.add("Dr. " + d.getPrenom() + " " + d.getNom() + " - " + d.getSpecialite());
        }

        FilteredList<String> filtered = new FilteredList<>(docteurStrings, s -> true);
        comboDocteur.setItems(filtered);

        comboDocteur.getEditor().textProperty().addListener((obs, old, newVal) -> {
            String texte = newVal == null ? "" : newVal.toLowerCase().trim();
            filtered.setPredicate(str -> texte.isEmpty() || str.toLowerCase().contains(texte));
            if (!comboDocteur.isShowing() && !texte.isEmpty()) comboDocteur.show();
        });

        comboDocteur.setOnAction(e -> {
            if (comboDocteur.getValue() != null) {
                comboDocteur.getEditor().setText(comboDocteur.getValue());
            }
        });
    }


    // === Enregistrer ===
    @FXML
    private void enregistrer() {
        clearErrorStyle(datePickerRDV);
        clearErrorStyle(comboHeure);
        clearErrorStyle(comboPatient);
        clearErrorStyle(comboDocteur);

        boolean hasError = false;
        if (datePickerRDV.getValue() == null) { setErrorStyle(datePickerRDV); hasError = true; }
        if (comboHeure.getValue() == null) { setErrorStyle(comboHeure); hasError = true; }
        if (comboPatient.getValue() == null) { setErrorStyle(comboPatient); hasError = true; }
        
        // Ne valider comboDocteur que si on n'est pas en mode médecin
        if (medecinConnecte == null && comboDocteur.getValue() == null) { 
            setErrorStyle(comboDocteur); 
            hasError = true; 
        }

        if (hasError) return;

        // Récupérer les objets à partir du texte sélectionné
        Patient patient = trouverPatient(comboPatient.getValue());
        Medecin medecin;
        
        // Si mode médecin, utiliser le médecin connecté
        if (medecinConnecte != null) {
            medecin = medecinConnecte;
        } else {
            medecin = trouverDocteur(comboDocteur.getValue());
        }

        if (patient == null || medecin == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Erreur interne : patient ou docteur non trouvé.");
            alert.show();
            return;
        }

        RendezVous rdv = new RendezVous(
            datePickerRDV.getValue(),
            LocalTime.parse(comboHeure.getValue()),
            patient,
            medecin,
            txtMotif.getText().trim(),
            RendezVous.Statut.PREVU
        );

        // Appeler le bon dashboard controller
        if (dashboardController != null) {
            dashboardController.ajouterRDV(rdv);
        } else if (dashboardControllerMed != null) {
            dashboardControllerMed.ajouterRDV(rdv);
        }
        
        fermer();
    }
    
    // Méthode pour rechercher le patient depuis le nom/prénom saisie dans le ComboBox
    private Patient trouverPatient(String texte) {
        for (Patient p : patients) {
            String str = p.getPrenom() + " " + p.getNom() + " - " + p.getNumSocial();
            if (str.equals(texte)) return p;
        }
        return null;
    }

    // Méthode pour rechercher le docteur depuis le nom/prénom saisie dans le ComboBox
    private Medecin trouverDocteur(String texte) {
        for (Medecin d : medecins) {
            String str = "Dr. " + d.getPrenom() + " " + d.getNom() + " - " + d.getSpecialite();
            if (str.equals(texte)) return d;
        }
        return null;
    }
	
	@FXML
    private void annuler() {
        fermer();
    }

    private void fermer() {
        Stage stage = (Stage) datePickerRDV.getScene().getWindow();
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
