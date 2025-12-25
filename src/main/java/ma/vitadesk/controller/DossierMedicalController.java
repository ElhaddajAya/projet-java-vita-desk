package ma.vitadesk.controller;

import java.io.File;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.FileChooser;
import javafx.util.converter.DoubleStringConverter;
import ma.vitadesk.model.Consultation;
import ma.vitadesk.model.Medecin;
import ma.vitadesk.model.Patient;

public class DossierMedicalController {

	@FXML private Label lblPatientNom;
    @FXML private Label lblDateNaissance;
    @FXML private Label lblSexe;
    @FXML private Label lblGroupeSanguin;
    @FXML private Label lblAllergies;
    @FXML private Label lblAntecedents;

    @FXML private TableView<Consultation> tableConsultations;
    @FXML private TableColumn<Consultation, String> colDateConsult;
    @FXML private TableColumn<Consultation, String> colMedecin;
    @FXML private TableColumn<Consultation, String> colDiagnostic;
    @FXML private TableColumn<Consultation, String> colTraitement;
    @FXML private TableColumn<Consultation, String> colObservations;
    @FXML private TableColumn<Consultation, Double> colPrix;

    @FXML private Button btnExporterDossierMedical;

    private Patient patient;
    private Medecin medecinConnecte; // null si secrétaire

    public void afficherDossier(Patient patient, Medecin medecinConnecte) {
        this.patient = patient;
        this.medecinConnecte = medecinConnecte;

        // === Infos patient ===
        lblPatientNom.setText(patient.getNom() + " " + patient.getPrenom());
        lblDateNaissance.setText(patient.getDateNaissance());
        lblSexe.setText(patient.getSexe());
        lblGroupeSanguin.setText("-");
        lblAllergies.setText("-");
        lblAntecedents.setText("-");

        // === Charger les consultations du patient ===
        ObservableList<Consultation> consultations = patient.getConsultations();

        // Filtrer si c'est un médecin
        if (medecinConnecte != null) {
            String nomMedecin = "Dr. " + medecinConnecte.getPrenom() + " " + medecinConnecte.getNom();
            ObservableList<Consultation> filtres = FXCollections.observableArrayList();
            for (Consultation c : consultations) {
                if (c.getMedecin().equals(nomMedecin)) {
                    filtres.add(c);
                }
            }
            tableConsultations.setItems(filtres);
        } else {
            tableConsultations.setItems(consultations);
        }

        configurerColonnes();
        configurerEditionSelonRole();
    }

    private void configurerColonnes() {
        colDateConsult.setCellValueFactory(new PropertyValueFactory<>("date"));
        colMedecin.setCellValueFactory(new PropertyValueFactory<>("medecin"));
        colDiagnostic.setCellValueFactory(new PropertyValueFactory<>("diagnostic"));
        colTraitement.setCellValueFactory(new PropertyValueFactory<>("traitement"));
        colObservations.setCellValueFactory(new PropertyValueFactory<>("observations"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prixConsultation"));

        // Masquer colonne médecin si c'est le médecin connecté
        colMedecin.setVisible(medecinConnecte == null);
    }

    private void configurerEditionSelonRole() {
        if (medecinConnecte != null) {
            // === MODE MÉDECIN : édition activée ===
            tableConsultations.setEditable(true);

            // Activer l'édition sur chaque colonne
            colDiagnostic.setCellFactory(TextFieldTableCell.forTableColumn());
            colDiagnostic.setOnEditCommit(event -> event.getRowValue().setDiagnostic(event.getNewValue()));

            colTraitement.setCellFactory(TextFieldTableCell.forTableColumn());
            colTraitement.setOnEditCommit(event -> event.getRowValue().setTraitement(event.getNewValue()));

            colObservations.setCellFactory(TextFieldTableCell.forTableColumn());
            colObservations.setOnEditCommit(event -> event.getRowValue().setObservations(event.getNewValue()));

            colPrix.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
            colPrix.setOnEditCommit(event -> event.getRowValue().setPrixConsultation(event.getNewValue()));

        } else {
            // === MODE SECRÉTAIRE : lecture seule ===
            tableConsultations.setEditable(false);
        }
    }
    
    @FXML
    private void exporterDossierPDF() {
        if (patient == null) return;

        try {
            PDDocument document = new PDDocument();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream content = new PDPageContentStream(document, page);

            // Police
            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 20);
            content.beginText();
            content.newLineAtOffset(50, 780);
            content.showText("Dossier Médical - VitaDesk");
            content.endText();

            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 14);
            content.beginText();
            content.newLineAtOffset(50, 750);
            content.showText("Patient : " + patient.getPrenom() + " " + patient.getNom());
            content.newLineAtOffset(0, -20);
            content.showText("N° Sécurité Sociale : " + patient.getNumSocial());
            content.newLineAtOffset(0, -20);
            content.showText("Date de naissance : " + patient.getDateNaissance());
            content.newLineAtOffset(0, -20);
            content.showText("Sexe : " + patient.getSexe());
            content.newLineAtOffset(0, -20);
            content.showText("Téléphone : " + patient.getTelephone());            
            content.newLineAtOffset(0, -40);
            content.showText("Historique des consultations :");
            content.endText();

            // Liste des consultations
            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
            int y = 650;
            for (Consultation c : tableConsultations.getItems()) {
                content.beginText();
                content.newLineAtOffset(50, y);
                content.showText("Date : " + c.getDate() + " | Médecin : " + c.getMedecin());
                content.newLineAtOffset(0, -15);
                content.showText("Diagnostic : " + c.getDiagnostic());
                content.newLineAtOffset(0, -15);
                content.showText("Traitement : " + c.getTraitement());
                content.newLineAtOffset(0, -15);
                content.showText("Observations : " + c.getObservations());
                content.newLineAtOffset(0, -15);
                content.showText("Prix : " + c.getPrixConsultation() + " MAD");
                content.newLineAtOffset(0, -20);
                content.showText("──────────────────────────────────────────────");
                content.endText();
                y -= 100;
                if (y < 100) {
                    content.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    content = new PDPageContentStream(document, page);
                    y = 750;
                }
            }

            content.close();

            // Sauvegarde
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Exporter le dossier médical");
            chooser.setInitialFileName("Dossier_" + patient.getNom() + "_" + patient.getPrenom() + ".pdf");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            File file = chooser.showSaveDialog(lblPatientNom.getScene().getWindow());

            if (file != null) {
                document.save(file);
                document.close();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setContentText("Dossier médical exporté :\n" + file.getName());
                alert.show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Erreur export PDF : " + e.getMessage());
            alert.show();
        }
    }

//    @FXML
//    private void fermer() {
//        Stage stage = (Stage) ((Node) lblPatientNom).getScene().getWindow();
//        stage.close();
//    }
	
}
