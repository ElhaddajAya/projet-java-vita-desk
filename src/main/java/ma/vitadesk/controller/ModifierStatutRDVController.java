package ma.vitadesk.controller;

import java.time.format.DateTimeFormatter;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import ma.vitadesk.model.RendezVous;

public class ModifierStatutRDVController {
	@FXML private Label lblPatient;
    @FXML private Label lblDocteur;
    @FXML private Label lblHeure;
    @FXML private ComboBox<RendezVous.Statut> comboStatut;

    private RendezVous rdv;
    private SecretaireDashboardController dashboardControllerSec;
    private MedecinDashboardController dashboardControllerMed;
    
    public void setData(RendezVous rdv, SecretaireDashboardController dashboardControllerSec) {
        this.rdv = rdv;
        this.dashboardControllerSec = dashboardControllerSec;
        this.dashboardControllerMed = null;

        // Afficher les infos
        lblPatient.setText("Patient : " + rdv.getPatient().getPrenom() + " " + rdv.getPatient().getNom());
        lblDocteur.setText("Medecin : Dr. " + rdv.getDocteur().getPrenom() + " " + rdv.getDocteur().getNom());
        lblHeure.setText("Heure : " + rdv.getHeure() + " le " + rdv.getDate());

        // ComboBox statuts
        comboStatut.getItems().addAll(RendezVous.Statut.values());
        comboStatut.setValue(rdv.getStatut()); // statut actuel
    }

    @FXML
    private void enregistrer() {
        RendezVous.Statut nouveauStatut = comboStatut.getValue();
        if (nouveauStatut != null) {
            rdv.setStatut(nouveauStatut);
            
            // Utiliser le bon contrôleur selon lequel est défini
            if (dashboardControllerSec != null) {
                dashboardControllerSec.rafraichirPlanning();
            } else if (dashboardControllerMed != null) {
                dashboardControllerMed.rafraichirPlanning();
                dashboardControllerMed.chargerGraphiqueSemaine();
            }
        }
        
        fermer();
    }
    
    @FXML
    private void supprimer() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Supprimer le rendez-vous");
        confirmation.setHeaderText("Voulez-vous vraiment supprimer ce rendez-vous ?");
        confirmation.setContentText(
            rdv.getPatient().getPrenom() + " " + rdv.getPatient().getNom() +
            " avec Dr. " + rdv.getDocteur().getNom() +
            " le " + rdv.getDate() + " à " + rdv.getHeure()
        );

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            // Supprimer réellement du planning selon le contrôleur disponible
            if (dashboardControllerSec != null) {
                dashboardControllerSec.supprimerRDV(rdv);
            } else if (dashboardControllerMed != null) {
                dashboardControllerMed.supprimerRDV(rdv);
            }
            fermer();
        }
    }

    @FXML
    private void annuler() {
        fermer();
    }

    private void fermer() {
        Stage stage = (Stage) lblPatient.getScene().getWindow();
        stage.close();
    }

    public void setData(RendezVous rdv, MedecinDashboardController dashboardControllerMed) {
        this.rdv = rdv;
        this.dashboardControllerMed = dashboardControllerMed;
        this.dashboardControllerSec = null;

        lblPatient.setText("Patient : " + rdv.getPatient().getPrenom() + " " + rdv.getPatient().getNom());
        lblDocteur.setText("Medecin : Dr. " + rdv.getDocteur().getPrenom() + " " + rdv.getDocteur().getNom());
        lblHeure.setText(rdv.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " à " + rdv.getHeure().format(DateTimeFormatter.ofPattern("HH:mm")));

        comboStatut.getItems().setAll(RendezVous.Statut.values());
        comboStatut.setValue(rdv.getStatut());
    }
}