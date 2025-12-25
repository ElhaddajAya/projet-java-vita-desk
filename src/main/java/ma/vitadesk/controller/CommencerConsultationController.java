package ma.vitadesk.controller;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ma.vitadesk.model.Consultation;
import ma.vitadesk.model.Docteur;
import ma.vitadesk.model.Patient;
import ma.vitadesk.model.RendezVous;

public class CommencerConsultationController {

    @FXML private Label lblPatientConsult;
    @FXML private Label lblDateConsult;
    @FXML private TextArea txtDiagnostic;
    @FXML private TextArea txtTraitement;
    @FXML private TextArea txtObservations;
    @FXML private Button btnEnregistrer;

    private RendezVous rdv;
    private MedecinDashboardController dashboardController;
    private double prixConsultation = 300.0; // Prix par défaut

    public void setData(RendezVous rdv, MedecinDashboardController dashboardController) {
        this.rdv = rdv;
        this.dashboardController = dashboardController;

        // Afficher les informations du patient
        lblPatientConsult.setText(rdv.getPatient().getPrenom() + " " + rdv.getPatient().getNom());
        lblDateConsult.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    @FXML
    private void enregistrer() {
        // 1. Réinitialiser les bordures
        clearErrorStyle(txtDiagnostic);
        clearErrorStyle(txtTraitement);

        boolean hasError = false;

        // 2. Vérification des champs obligatoires
        if (txtDiagnostic.getText() == null || txtDiagnostic.getText().trim().isEmpty()) {
            setErrorStyle(txtDiagnostic);
            hasError = true;
        }

        if (txtTraitement.getText() == null || txtTraitement.getText().trim().isEmpty()) {
            setErrorStyle(txtTraitement);
            hasError = true;
        }

        // 3. S’il y a une erreur → on bloque tout
        if (hasError) {
            showAlert("Champs obligatoires", "Le diagnostic et le traitement sont requis.", Alert.AlertType.WARNING);
            return; // ← IMPORTANT : on sort de la méthode, rien d'autre ne s'exécute
        }

        // 4. Tout est OK → on enregistre
        Consultation consultation = new Consultation(
            LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            "Dr. " + rdv.getDocteur().getPrenom() + " " + rdv.getDocteur().getNom(),
            txtDiagnostic.getText().trim(),
            txtTraitement.getText().trim(),
            txtObservations.getText() == null ? "" : txtObservations.getText().trim(),
            prixConsultation
        );

        // Marquer le RDV comme effectué
        rdv.setStatut(RendezVous.Statut.EFFECTUE);

        // Enregistrer dans le dashboard
        dashboardController.ajouterConsultation(rdv, consultation);

        // Message de succès
        showAlert("Succès", "Consultation enregistrée avec succès !\nVous pouvez maintenant générer l'ordonnance.", Alert.AlertType.INFORMATION);

        // Désactiver le bouton Enregistrer
        btnEnregistrer.setDisable(true);

        // Optionnel : désactiver les champs pour éviter les modifications
        txtDiagnostic.setEditable(false);
        txtTraitement.setEditable(false);
        txtObservations.setEditable(false);
    }

    @FXML
    private void genererOrdonnance() {
        // Validation
        if (txtDiagnostic.getText().trim().isEmpty() || txtTraitement.getText().trim().isEmpty()) {
            showAlert("Erreur", "Veuillez remplir le diagnostic et le traitement avant de générer l'ordonnance !", Alert.AlertType.ERROR);
            return;
        }

        try {
            PDDocument document = new PDDocument();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream content = new PDPageContentStream(document, page);

            // === EN-TÊTE AVEC LOGO ===
            try {
                // Charger le logo depuis les ressources
                InputStream logoStream = getClass().getResourceAsStream("/images/logo.png");
                if (logoStream != null) {
                    PDImageXObject logo = PDImageXObject.createFromByteArray(document, 
                        logoStream.readAllBytes(), "logo");
                    content.drawImage(logo, 50, 750, 60, 60);
                }
            } catch (Exception e) {
                System.out.println("Logo non trouvé, continuation sans logo...");
            }

            // Informations du médecin à côté du logo
            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
            content.beginText();
            content.newLineAtOffset(120, 785);
            content.showText("Dr. " + rdv.getDocteur().getPrenom() + " " + rdv.getDocteur().getNom());
            content.endText();

            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            content.beginText();
            content.newLineAtOffset(120, 770);
            content.showText(rdv.getDocteur().getSpecialite());
            content.endText();

            content.beginText();
            content.newLineAtOffset(120, 755);
            content.showText("Tel: " + rdv.getDocteur().getTelephone());
            content.endText();

            // Ligne de séparation
            content.setLineWidth(1);
            content.moveTo(50, 740);
            content.lineTo(545, 740);
            content.stroke();

            // === TITRE ORDONNANCE ===
            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 20);
            content.beginText();
            content.newLineAtOffset(220, 710);
            content.showText("ORDONNANCE");
            content.endText();

            // === INFORMATIONS PATIENT ===
            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 13);
            content.beginText();
            content.newLineAtOffset(50, 670);
            content.showText("Informations du Patient");
            content.endText();

            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            int y = 650;
            
            content.beginText();
            content.newLineAtOffset(50, y);
            content.showText("Nom: " + rdv.getPatient().getNom() + " " + rdv.getPatient().getPrenom());
            content.endText();
            y -= 20;

            content.beginText();
            content.newLineAtOffset(50, y);
            content.showText("Date de naissance: " + rdv.getPatient().getDateNaissance());
            content.endText();
            y -= 20;

            content.beginText();
            content.newLineAtOffset(50, y);
            content.showText("N° Securite Sociale: " + rdv.getPatient().getNumSocial());
            content.endText();
            y -= 20;

            content.beginText();
            content.newLineAtOffset(50, y);
            content.showText("Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            content.endText();
            y -= 40;

            // === DIAGNOSTIC ===
            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 13);
            content.beginText();
            content.newLineAtOffset(50, y);
            content.showText("Diagnostic:");
            content.endText();
            y -= 20;

            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            String[] diagnosticLines = wrapText(txtDiagnostic.getText(), 80);
            for (String line : diagnosticLines) {
                content.beginText();
                content.newLineAtOffset(50, y);
                content.showText(line);
                content.endText();
                y -= 15;
            }
            y -= 20;

            // === TRAITEMENT ===
            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 13);
            content.beginText();
            content.newLineAtOffset(50, y);
            content.showText("Traitement:");
            content.endText();
            y -= 20;

            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            String[] traitementLines = wrapText(txtTraitement.getText(), 80);
            for (String line : traitementLines) {
                content.beginText();
                content.newLineAtOffset(50, y);
                content.showText(line);
                content.endText();
                y -= 15;
            }
            y -= 20;

            // === OBSERVATIONS (si présentes) ===
            if (!txtObservations.getText().trim().isEmpty()) {
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 13);
                content.beginText();
                content.newLineAtOffset(50, y);
                content.showText("Observations:");
                content.endText();
                y -= 20;

                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                String[] observationsLines = wrapText(txtObservations.getText(), 80);
                for (String line : observationsLines) {
                    content.beginText();
                    content.newLineAtOffset(50, y);
                    content.showText(line);
                    content.endText();
                    y -= 15;
                }
            }

            // === SIGNATURE ===
            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
            content.beginText();
            content.newLineAtOffset(350, 150);
            content.showText("Signature et cachet du medecin");
            content.endText();

            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
            content.beginText();
            content.newLineAtOffset(350, 120);
            content.showText("Dr. " + rdv.getDocteur().getNom());
            content.endText();

            content.close();

            // === SAUVEGARDER ===
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer l'ordonnance");
            fileChooser.setInitialFileName("Ordonnance_" + rdv.getPatient().getNom() + "_" + 
                LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".pdf");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF", "*.pdf")
            );

            File file = fileChooser.showSaveDialog(lblPatientConsult.getScene().getWindow());
            if (file != null) {
                document.save(file);
                document.close();
                showAlert("Succès", "Ordonnance générée avec succès !\n" + file.getName(), Alert.AlertType.INFORMATION);
            } else {
                document.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la génération de l'ordonnance: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void annuler() {
        fermer();
    }

    private void fermer() {
        Stage stage = (Stage) lblPatientConsult.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    // Méthode pour découper le texte en lignes
    private String[] wrapText(String text, int maxChars) {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        java.util.List<String> lines = new java.util.ArrayList<>();

        for (String word : words) {
            if (line.length() + word.length() + 1 > maxChars) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                if (line.length() > 0) line.append(" ");
                line.append(word);
            }
        }
        if (line.length() > 0) lines.add(line.toString());

        return lines.toArray(new String[0]);
    }
    
 // Méthodes de style (comme dans tes autres controllers)
    private void setErrorStyle(javafx.scene.control.Control control) {
        control.setStyle("-fx-background-color: white; -fx-border-color: red; -fx-border-width: 0.3px; -fx-border-radius: 3px; -fx-font-size: 13px;");
    }

    private void clearErrorStyle(javafx.scene.control.Control control) {
        control.setStyle("-fx-background-color: white; -fx-border-width: 0.2px; -fx-border-color: black; -fx-border-radius: 3; -fx-font-size: 13px;");
    }
}