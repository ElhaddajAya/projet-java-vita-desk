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
    private Runnable onUpdate;
    
    public void setData(RendezVous rdv, SecretaireDashboardController dashboardControllerSec) {
        this.rdv = rdv;
        this.dashboardControllerSec = dashboardControllerSec;

        // Afficher les infos
        lblPatient.setText("Patient : " + rdv.getPatient().getPrenom() + " " + rdv.getPatient().getNom());
        lblDocteur.setText("Docteur : Dr. " + rdv.getDocteur().getPrenom() + " " + rdv.getDocteur().getNom());
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
            dashboardControllerSec.rafraichirPlanning(); // met à jour la couleur
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
            // Supprimer du planning
            dashboardControllerSec.supprimerRDV(rdv);
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

    public void setData(RendezVous rdv, Runnable onUpdate) {
        this.rdv = rdv;
        this.onUpdate = onUpdate;

        lblPatient.setText(rdv.getPatient().getPrenom() + " " + rdv.getPatient().getNom());
        lblDocteur.setText("Dr. " + rdv.getDocteur().getPrenom() + " " + rdv.getDocteur().getNom());
        lblHeure.setText(rdv.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " à " + rdv.getHeure().format(DateTimeFormatter.ofPattern("HH:mm")));

        comboStatut.getItems().setAll(RendezVous.Statut.values());
        comboStatut.setValue(rdv.getStatut());
    }

	public MedecinDashboardController getDashboardControllerMed() {
		return dashboardControllerMed;
	}

	public void setDashboardControllerMed(MedecinDashboardController dashboardControllerMed) {
		this.dashboardControllerMed = dashboardControllerMed;
	}
}