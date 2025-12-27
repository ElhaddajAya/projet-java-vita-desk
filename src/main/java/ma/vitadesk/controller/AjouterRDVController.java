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
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ma.vitadesk.dao.*;
import ma.vitadesk.model.Medecin;
import ma.vitadesk.model.Patient;
import ma.vitadesk.model.RendezVous;

/**
 * Contrôleur pour ajouter un RDV
 * Version intégrée avec MySQL
 */
public class AjouterRDVController implements Initializable {

	@FXML private DatePicker datePickerRDV;
    @FXML private ComboBox<String> comboHeure;
    @FXML private ComboBox<String> comboPatient;
    @FXML private ComboBox<String> comboDocteur;
    @FXML private TextField txtMotif;
    
    // DAO pour charger patients et médecins
    private IPatientDAO patientDAO;
    private IMedecinDAO medecinDAO;
    
    // Listes chargées depuis la BDD
    private ObservableList<Patient> patients = FXCollections.observableArrayList();
    private ObservableList<Medecin> medecins = FXCollections.observableArrayList();
    
    private Medecin medecinConnecte; // Pour le mode médecin
    
    private SecretaireDashboardController dashboardControllerSec;
    private MedecinDashboardController dashboardControllerMed;
    
    public void setDashboardController(SecretaireDashboardController controller) {
        this.dashboardControllerSec = controller;
    }
    
    public void setDashboardControllerMed(MedecinDashboardController controller) {
        this.dashboardControllerMed = controller;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialiser les DAO
        patientDAO = new PatientDAOImpl();
        medecinDAO = new MedecinDAOImpl();
        
        // Charger les données depuis la BDD
        patients.addAll(patientDAO.getAllPatients());
        medecins.addAll(medecinDAO.getAllMedecins());
        
        // Remplir les ComboBox
        remplirComboPatients();
        remplirComboDocteurs();
        
        // Heures disponibles
        comboHeure.getItems().addAll(
            "08:00", "09:00", "10:00", "11:00", "12:00",
            "13:00", "14:00", "15:00", "16:00", "17:00"
        );
        comboHeure.setPromptText("Choisir l'heure");
        txtMotif.setPromptText("Ex: Consultation générale, Contrôle...");
    }
    
    /**
     * Pour le mode médecin : pré-remplir le médecin
     */
    public void initialiserPourMedecin(Medecin medecinConnecte) {
        this.medecinConnecte = medecinConnecte;
        
        // Désactiver et pré-remplir le ComboBox docteur
        comboDocteur.setDisable(true);
        comboDocteur.setValue("Dr. " + medecinConnecte.getPrenom() + " " + 
                             medecinConnecte.getNom() + " - " + 
                             medecinConnecte.getSpecialite());
        comboDocteur.setStyle("-fx-opacity: 1.0;");
    }

    // === ComboBox Patient avec recherche ===
    private void remplirComboPatients() {
        ObservableList<String> patientStrings = FXCollections.observableArrayList();
        
        // 🆕 Utiliser LAMBDA pour transformer Patient → String
        patients.forEach(p -> patientStrings.add(
            p.getPrenom() + " " + p.getNom() + " - " + p.getNumSocial()
        ));

        FilteredList<String> filtered = new FilteredList<>(patientStrings, s -> true);
        comboPatient.setItems(filtered);

        comboPatient.getEditor().textProperty().addListener((obs, old, newVal) -> {
            String texte = newVal == null ? "" : newVal.toLowerCase().trim();
            filtered.setPredicate(str -> texte.isEmpty() || str.toLowerCase().contains(texte));
            if (!comboPatient.isShowing() && !texte.isEmpty()) comboPatient.show();
        });

        comboPatient.setOnAction(e -> {
            if (comboPatient.getValue() != null) {
                comboPatient.getEditor().setText(comboPatient.getValue());
            }
        });
    }

    // === ComboBox Medecin avec recherche ===
    private void remplirComboDocteurs() {
        ObservableList<String> docteurStrings = FXCollections.observableArrayList();
        
        // 🆕 Utiliser LAMBDA
        medecins.forEach(d -> docteurStrings.add(
            "Dr. " + d.getPrenom() + " " + d.getNom() + " - " + d.getSpecialite()
        ));

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
        if (dashboardControllerSec != null) {
            dashboardControllerSec.ajouterRDV(rdv);
        } else if (dashboardControllerMed != null) {
            dashboardControllerMed.ajouterRDV(rdv);
        }
        
        fermer();
    }
    
    /**
     * Trouve un patient par le texte du ComboBox
     * UTILISE LAMBDA (concept du cours)
     */
    private Patient trouverPatient(String texte) {
        return patients.stream()
            .filter(p -> {
                String str = p.getPrenom() + " " + p.getNom() + " - " + p.getNumSocial();
                return str.equals(texte);
            })
            .findFirst()
            .orElse(null);
    }

    /**
     * Trouve un médecin par le texte du ComboBox
     * UTILISE LAMBDA
     */
    private Medecin trouverDocteur(String texte) {
        return medecins.stream()
            .filter(d -> {
                String str = "Dr. " + d.getPrenom() + " " + d.getNom() + " - " + d.getSpecialite();
                return str.equals(texte);
            })
            .findFirst()
            .orElse(null);
    }
	
	@FXML
    private void annuler() {
        fermer();
    }

    private void fermer() {
        Stage stage = (Stage) datePickerRDV.getScene().getWindow();
        stage.close();
    }

    private void setErrorStyle(Control control) {
        control.setStyle("-fx-background-color: white; -fx-border-color: red; -fx-border-width: 0.3px; -fx-border-radius: 3px; -fx-font-size: 13px;");
    }

    private void clearErrorStyle(Control control) {
        control.setStyle("-fx-background-color: white; -fx-border-width: 0.2px; -fx-border-color: black; -fx-border-radius: 3; -fx-font-size: 13px;");
    }
}