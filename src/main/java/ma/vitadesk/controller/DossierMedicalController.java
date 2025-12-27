package ma.vitadesk.controller;

import java.io.File;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

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
import ma.vitadesk.dao.IConsultationDAO;
import ma.vitadesk.dao.ConsultationDAOImpl;
import ma.vitadesk.model.Consultation;
import ma.vitadesk.model.Medecin;
import ma.vitadesk.model.Patient;

/**
 * Contrôleur pour afficher et éditer le dossier médical d'un patient
 * IMPORTANT: L'édition se fait directement dans le TableView (double-clic)
 */
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

    // DAO pour charger/modifier les consultations
    private IConsultationDAO consultationDAO;
    
    private Patient patient;
    private Medecin medecinConnecte; // null si secrétaire

    public void afficherDossier(Patient patient, Medecin medecinConnecte) {
        this.patient = patient;
        this.medecinConnecte = medecinConnecte;
        
        // Initialiser le DAO
        consultationDAO = new ConsultationDAOImpl();

        // Infos patient
        lblPatientNom.setText(patient.getNom() + " " + patient.getPrenom());
        lblDateNaissance.setText(patient.getDateNaissance());
        lblSexe.setText(patient.getSexe());
        lblGroupeSanguin.setText("-");
        lblAllergies.setText("-");
        lblAntecedents.setText("-");

        // 🆕 CHARGER LES CONSULTATIONS DEPUIS LA BDD
        ObservableList<Consultation> consultations = FXCollections.observableArrayList(
            consultationDAO.getConsultationsByPatient(patient.getNumSocial())
        );

        // Filtrer si c'est un médecin (voir seulement ses consultations)
        if (medecinConnecte != null) {
            String nomMedecin = "Dr. " + medecinConnecte.getPrenom() + " " + medecinConnecte.getNom();
            ObservableList<Consultation> filtres = FXCollections.observableArrayList();
            
            // Expression lambda pour filtrer (concept du cours)
            consultations.forEach(c -> {
                if (c.getMedecin().equals(nomMedecin)) {
                    filtres.add(c);
                }
            });
            
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

    /**
     * Configure l'édition selon le rôle
     * Médecin: TableView éditable (double-clic sur cellule)
     * Secrétaire: Lecture seule
     */
    private void configurerEditionSelonRole() {
        if (medecinConnecte != null) {
            // MODE MÉDECIN : édition activée
            tableConsultations.setEditable(true);

            // Activer l'édition sur chaque colonne + mise à jour BDD
            colDiagnostic.setCellFactory(TextFieldTableCell.forTableColumn());
            colDiagnostic.setOnEditCommit(event -> {
                // Modifier en mémoire
                event.getRowValue().setDiagnostic(event.getNewValue());
                
                // 🆕 METTRE À JOUR DANS LA BDD
                boolean success = consultationDAO.modifierConsultation(event.getRowValue());
                if (!success) {
                    // En cas d'erreur, annuler la modification
                    tableConsultations.refresh();
                    showAlert("Erreur", "Impossible de mettre à jour le diagnostic", Alert.AlertType.ERROR);
                }
            });

            colTraitement.setCellFactory(TextFieldTableCell.forTableColumn());
            colTraitement.setOnEditCommit(event -> {
                event.getRowValue().setTraitement(event.getNewValue());
                
                // Mise à jour BDD
                boolean success = consultationDAO.modifierConsultation(event.getRowValue());
                if (!success) {
                    tableConsultations.refresh();
                    showAlert("Erreur", "Impossible de mettre à jour le traitement", Alert.AlertType.ERROR);
                }
            });

            colObservations.setCellFactory(TextFieldTableCell.forTableColumn());
            colObservations.setOnEditCommit(event -> {
                event.getRowValue().setObservations(event.getNewValue());
                
                // Mise à jour BDD
                boolean success = consultationDAO.modifierConsultation(event.getRowValue());
                if (!success) {
                    tableConsultations.refresh();
                    showAlert("Erreur", "Impossible de mettre à jour les observations", Alert.AlertType.ERROR);
                }
            });

            colPrix.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
            colPrix.setOnEditCommit(event -> {
                event.getRowValue().setPrixConsultation(event.getNewValue());
                
                // Mise à jour BDD
                boolean success = consultationDAO.modifierConsultation(event.getRowValue());
                if (!success) {
                    tableConsultations.refresh();
                    showAlert("Erreur", "Impossible de mettre à jour le prix", Alert.AlertType.ERROR);
                }
            });

        } else {
            // MODE SECRÉTAIRE : lecture seule
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

            // === TITRE ===
            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 20);
            content.beginText();
            content.newLineAtOffset(50, 780);
            content.showText("Dossier Medical - VitaDesk");
            content.endText(); // ← FERMER ICI

            // === INFORMATIONS PATIENT ===
            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 14);
            content.beginText(); // ← NOUVEAU BLOC
            content.newLineAtOffset(50, 750);
            content.showText("Patient : " + patient.getPrenom() + " " + patient.getNom());
            content.endText(); // ← FERMER

            content.beginText();
            content.newLineAtOffset(50, 730);
            content.showText("N° Securite Sociale : " + patient.getNumSocial());
            content.endText();

            content.beginText();
            content.newLineAtOffset(50, 710);
            content.showText("Date de naissance : " + patient.getDateNaissance());
            content.endText();

            content.beginText();
            content.newLineAtOffset(50, 690);
            content.showText("Sexe : " + patient.getSexe());
            content.endText();

            content.beginText();
            content.newLineAtOffset(50, 670);
            content.showText("Telephone : " + patient.getTelephone());
            content.endText();

            // === TITRE CONSULTATIONS ===
            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
            content.beginText();
            content.newLineAtOffset(50, 630);
            content.showText("Historique des consultations :");
            content.endText();

            // === LISTE DES CONSULTATIONS ===
            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            int y = 600;
            
            for (Consultation c : tableConsultations.getItems()) {
                // Date et Médecin
                content.beginText();
                content.newLineAtOffset(50, y);
                content.showText("Date : " + c.getDate() + " | Medecin : " + c.getMedecin());
                content.endText();
                y -= 20;

                // Diagnostic
                content.beginText();
                content.newLineAtOffset(50, y);
                content.showText("Diagnostic : " + c.getDiagnostic());
                content.endText();
                y -= 20;

                // Traitement
                content.beginText();
                content.newLineAtOffset(50, y);
                content.showText("Traitement : " + c.getTraitement());
                content.endText();
                y -= 20;

                // Observations
                content.beginText();
                content.newLineAtOffset(50, y);
                content.showText("Observations : " + (c.getObservations() == null || c.getObservations().isEmpty() ? "-" : c.getObservations()));
                content.endText();
                y -= 20;

                // Prix
                content.beginText();
                content.newLineAtOffset(50, y);
                content.showText("Prix : " + c.getPrixConsultation() + " MAD");
                content.endText();
                y -= 20;

                // Séparateur
                content.beginText();
                content.newLineAtOffset(50, y);
                content.showText("------------------------------------------------");
                content.endText();
                y -= 30;

                // Nouvelle page si nécessaire
                if (y < 100) {
                    content.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    content = new PDPageContentStream(document, page);
                    content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
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
                showAlert("Succès", "Dossier médical exporté :\n" + file.getName(), Alert.AlertType.INFORMATION);
            } else {
                document.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur export PDF : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}