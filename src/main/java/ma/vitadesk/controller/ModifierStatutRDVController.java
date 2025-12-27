package ma.vitadesk.controller;

import java.time.format.DateTimeFormatter;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import ma.vitadesk.dao.IRendezVousDAO;
import ma.vitadesk.dao.RendezVousDAOImpl;
import ma.vitadesk.model.RendezVous;

/**
 * Contrôleur pour modifier le statut d'un rendez-vous
 * Maintenant avec sauvegarde en BDD
 */
public class ModifierStatutRDVController {
	@FXML private Label lblPatient;
    @FXML private Label lblDocteur;
    @FXML private Label lblHeure;
    @FXML private ComboBox<RendezVous.Statut> comboStatut;

    // DAO pour mettre à jour le RDV dans la BDD
    private IRendezVousDAO rendezVousDAO;
    
    private RendezVous rdv;
    private SecretaireDashboardController dashboardControllerSec;
    private MedecinDashboardController dashboardControllerMed;
    
    /**
     * Constructeur - initialise le DAO
     */
    public ModifierStatutRDVController() {
        rendezVousDAO = new RendezVousDAOImpl();
    }
    
    /**
     * Initialise la fenêtre pour le mode secrétaire
     */
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
    
    /**
     * Initialise la fenêtre pour le mode médecin
     */
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

    /**
     * Enregistre le nouveau statut
     * 🆕 MAINTENANT AVEC SAUVEGARDE EN BDD
     */
    @FXML
    private void enregistrer() {
        RendezVous.Statut nouveauStatut = comboStatut.getValue();
        
        if (nouveauStatut != null) {
            rdv.setStatut(nouveauStatut);
            
            // 🆕 SAUVEGARDER LE CHANGEMENT DANS LA BDD
            boolean success = rendezVousDAO.modifierRendezVous(rdv);
            
            if (success) {
                // Rafraîchir le planning selon le contrôleur
                if (dashboardControllerSec != null) {
                    dashboardControllerSec.rafraichirPlanning();
                } else if (dashboardControllerMed != null) {
                    dashboardControllerMed.rafraichirPlanning();
                    dashboardControllerMed.chargerGraphiqueSemaine();
                    dashboardControllerMed.chargerConsultationsDuJour(); // 🔄 Rafraîchir la liste
                    dashboardControllerMed.chargerConsultationsDuJour();
                }
                
                fermer();
            } else {
                // En cas d'erreur
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setContentText("Impossible de mettre à jour le statut du RDV en BDD");
                alert.show();
            }
        }
    }
    
    /**
     * Supprime le RDV
     * La suppression BDD est gérée par les dashboards
     */
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
            // Supprimer via le dashboard (qui gère la suppression BDD)
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
}