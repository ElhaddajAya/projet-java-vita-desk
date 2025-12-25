package ma.vitadesk.controller;

import java.io.IOException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ma.vitadesk.model.Docteur;
import ma.vitadesk.model.Patient;
import ma.vitadesk.model.RendezVous;

public class MedecinDashboardController implements Initializable {

	// ==================== SIDEBAR ====================
    @FXML private Button btnAccueil;
    @FXML private Button btnPlanning;
    @FXML private Button btnHistorique;
    @FXML private Button btnConsultationsJour;
    @FXML private Button btnDeconnexion;

    // ==================== HEADER ====================
    @FXML private Label lblMedecinNom;
    @FXML private Label lblSpecialite;

    // ==================== TABS ====================
    @FXML private TabPane tabPaneMain;
    @FXML private Tab tabAccueil;
    @FXML private Tab tabMonPlanning;
    @FXML private Tab tabHistoriquePatients;
    @FXML private Tab tabConsultationsDuJour;

    // ==================== ACCUEIL ====================
    @FXML private BarChart<String, Number> barRdvSemaineMed;

    // ==================== HISTORIQUE PATIENTS ====================
    @FXML private TableView<Patient> tablePatientsMed;
    @FXML private TableColumn<Patient, String> colNumSocial;
    @FXML private TableColumn<Patient, String> colNom;
    @FXML private TableColumn<Patient, String> colPrenom;
    @FXML private TableColumn<Patient, String> colDateNaissance;
    @FXML private TableColumn<Patient, String> colSexe;
    @FXML private TableColumn<Patient, String> colDerniereConsultation;
    @FXML private TableColumn<Patient, Void> colActionDossier; // seule action
    
    // ==================== PLANNING ====================
    @FXML private DatePicker datePickerPlanningMed;
    @FXML private Label selectedDatePlanningMed;
    @FXML private Label todayConsultationsDateMed;

    // Toutes les cellules du planning (avec "Med")
    @FXML private VBox cellLundi0800Med, cellMardi0800Med, cellMercredi0800Med, cellJeudi0800Med, cellVendredi0800Med, cellSamedi0800Med;
    @FXML private VBox cellLundi0900Med, cellMardi0900Med, cellMercredi0900Med, cellJeudi0900Med, cellVendredi0900Med, cellSamedi0900Med;
    @FXML private VBox cellLundi1000Med, cellMardi1000Med, cellMercredi1000Med, cellJeudi1000Med, cellVendredi1000Med, cellSamedi1000Med;
    @FXML private VBox cellLundi1100Med, cellMardi1100Med, cellMercredi1100Med, cellJeudi1100Med, cellVendredi1100Med, cellSamedi1100Med;
    @FXML private VBox cellLundi1200Med, cellMardi1200Med, cellMercredi1200Med, cellJeudi1200Med, cellVendredi1200Med, cellSamedi1200Med;
    @FXML private VBox cellLundi1300Med, cellMardi1300Med, cellMercredi1300Med, cellJeudi1300Med, cellVendredi1300Med, cellSamedi1300Med;
    @FXML private VBox cellLundi1400Med, cellMardi1400Med, cellMercredi1400Med, cellJeudi1400Med, cellVendredi1400Med, cellSamedi1400Med;
    @FXML private VBox cellLundi1500Med, cellMardi1500Med, cellMercredi1500Med, cellJeudi1500Med, cellVendredi1500Med, cellSamedi1500Med;
    @FXML private VBox cellLundi1600Med, cellMardi1600Med, cellMercredi1600Med, cellJeudi1600Med, cellVendredi1600Med, cellSamedi1600Med;
    @FXML private VBox cellLundi1700Med, cellMardi1700Med, cellMercredi1700Med, cellJeudi1700Med, cellVendredi1700Med, cellSamedi1700Med;

    // ==================== DONNÉES ====================
    private ObservableList<RendezVous> listeRDV = FXCollections.observableArrayList();
    private ObservableList<Patient> listePatients = FXCollections.observableArrayList();
    private Docteur medecinConnecte;

    // ==================== PASSAGE DU MÉDECIN ====================
    public void setMedecin(Docteur medecin) {
        this.medecinConnecte = medecin;
        lblMedecinNom.setText("Dr. " + medecin.getPrenom() + " " + medecin.getNom());
        lblSpecialite.setText(medecin.getSpecialite());

        chargerDonneesFictives();
        rafraichirTout();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Date du jour
        datePickerPlanningMed.setValue(LocalDate.now());
        selectedDatePlanningMed.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        todayConsultationsDateMed.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        
        // Accueil sélectionné au démarrage
        tabPaneMain.getSelectionModel().select(tabAccueil);
        highlightButton(btnAccueil);

        // Listener date planning
        datePickerPlanningMed.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                selectedDatePlanningMed.setText(newVal.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                rafraichirPlanning();
            }
        });
        
        // === Configuration du tableau Historique Patients ===        
        colNumSocial.setCellValueFactory(new PropertyValueFactory<>("numSocial"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colDateNaissance.setCellValueFactory(new PropertyValueFactory<>("dateNaissance"));
        colSexe.setCellValueFactory(new PropertyValueFactory<>("sexe"));
        colDerniereConsultation.setCellValueFactory(cellData -> new SimpleStringProperty("22/12/2025")); // fictif

        // Bouton "Dossier Médical" unique
        colActionDossier.setCellFactory(param -> new TableCell<Patient, Void>() {
            private final Button btnDossier = new Button("Dossier Médical");

            {
                btnDossier.setStyle(
                    "-fx-background-color: #FF9000; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 8 20; " +
                    "-fx-background-radius: 6;" + 
                    "-fx-cursor: HAND;"
                );
                btnDossier.setOnAction(e -> {
                    Patient patient = getTableView().getItems().get(getIndex());
                    ouvrirDossierMedical(patient);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnDossier);
            }
        });

        // Données fictives patients pour le médecin
        ObservableList<Patient> patientsMed = FXCollections.observableArrayList();
        patientsMed.addAll(
            new Patient("123456", "Karim", "Benali", "01/01/1980", "0600000000", "CIN123", "M", "Casablanca"),
            new Patient("789012", "Sara", "Zouhair", "15/05/1995", "0611111111", "CIN456", "F", "Rabat"),
            new Patient("555666", "Ahmed", "Lahlou", "10/10/1988", "0622222222", "CIN789", "M", "Marrakech")
        );
        tablePatientsMed.setItems(patientsMed);
    }

    // ==================== NAVIGATION SIDEBAR ====================
    @FXML
    private void handleMenuClick(ActionEvent event) {
        Button clicked = (Button) event.getSource();
        resetSidebarStyle();

        if (clicked == btnAccueil) {
            tabPaneMain.getSelectionModel().select(tabAccueil);
            highlightButton(btnAccueil);
        } else if (clicked == btnPlanning) {
            tabPaneMain.getSelectionModel().select(tabMonPlanning);
            highlightButton(btnPlanning);
        } else if (clicked == btnHistorique) {
            tabPaneMain.getSelectionModel().select(tabHistoriquePatients);
            highlightButton(btnHistorique);
        } else if (clicked == btnConsultationsJour) {
            tabPaneMain.getSelectionModel().select(tabConsultationsDuJour);
            highlightButton(btnConsultationsJour);
        } else if (clicked == btnDeconnexion) {
            deconnecter();
        }
    }

    private void resetSidebarStyle() {
        btnAccueil.setStyle("-fx-background-color: #EFF2F5; -fx-border-width: 0.1px; -fx-border-color: black;");
        btnPlanning.setStyle("-fx-background-color: #EFF2F5; -fx-border-width: 0.1px; -fx-border-color: black;");
        btnHistorique.setStyle("-fx-background-color: #EFF2F5; -fx-border-width: 0.1px; -fx-border-color: black;");
        btnConsultationsJour.setStyle("-fx-background-color: #EFF2F5; -fx-border-width: 0.1px; -fx-border-color: black;");
        btnDeconnexion.setStyle("-fx-background-color: #EFF2F5; -fx-border-width: 0.1px; -fx-border-color: black;");
    }

    private void highlightButton(Button btn) {
        resetSidebarStyle();
        btn.setStyle("-fx-background-color: #4D93FF; -fx-text-fill: white; -fx-border-width: 0.1px; -fx-border-color: black;");
    }

    // ==================== RAFRAÎCHISSEMENT ====================
    private void rafraichirTout() {
        rafraichirPlanning();
        chargerGraphiqueSemaine();
    }

    public void chargerGraphiqueSemaine() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("RDV");

        LocalDate debut = LocalDate.now().with(DayOfWeek.MONDAY);
        String[] jours = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};

        for (int i = 0; i < 7; i++) {
            LocalDate jour = debut.plusDays(i);
            long count = listeRDV.stream()
                    .filter(r -> r.getDate().equals(jour) && r.getDocteur().equals(medecinConnecte))
                    .count();
            series.getData().add(new XYChart.Data<>(jours[i], count));
        }

        barRdvSemaineMed.getData().clear();
        barRdvSemaineMed.getData().add(series);
    }

    public void rafraichirPlanning() {
        viderToutesLesCellules();
        LocalDate date = datePickerPlanningMed.getValue();
        if (date == null) return;
        selectedDatePlanningMed.setText(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        for (RendezVous rdv : listeRDV) {
            if (rdv.getDate().equals(date) && rdv.getDocteur().equals(medecinConnecte)) {
                VBox cellule = getCellulePourHeureEtJour(rdv.getHeure(), rdv.getDate().getDayOfWeek());
                if (cellule != null) {
                    Label labelRDV = new Label(rdv.getAffichageCellule());

                    // === STYLE IDENTIQUE À SECRÉTAIRE ===
                    labelRDV.setStyle(
                        "-fx-background-color: " + rdv.getStatut().getCouleur() + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 10 15; " +
                        "-fx-background-radius: 12; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 13; " +
                        "-fx-alignment: center-left;"
                    );

                    labelRDV.setMaxWidth(Double.MAX_VALUE);
                    labelRDV.setTextOverrun(OverrunStyle.ELLIPSIS);

                    // Tooltip complet
                    String texteTooltip = rdv.getPatient().getPrenom() + " " + rdv.getPatient().getNom() +
                                          "\nDr. " + rdv.getDocteur().getPrenom() + " " + rdv.getDocteur().getNom() +
                                          "\nMotif : " + rdv.getMotif() +
                                          "\nStatut : " + rdv.getStatut().getLabel();
                    Tooltip tooltip = new Tooltip(texteTooltip);
                    tooltip.setStyle("-fx-font-size: 14; -fx-background-color: #333; -fx-text-fill: white;");
                    Tooltip.install(labelRDV, tooltip);

                    labelRDV.setPadding(new Insets(0, 0, 8, 0));

                    // === CLIC → MODIFIER STATUT ===
                    labelRDV.setOnMouseClicked(e -> ouvrirModifierStatut(rdv));

                    cellule.getChildren().add(labelRDV);
                }
            }
        }
    }

    private void viderToutesLesCellules() {
        VBox[] cellules = {
            cellLundi0800Med, cellMardi0800Med, cellMercredi0800Med, cellJeudi0800Med, cellVendredi0800Med, cellSamedi0800Med,
            cellLundi0900Med, cellMardi0900Med, cellMercredi0900Med, cellJeudi0900Med, cellVendredi0900Med, cellSamedi0900Med,
            cellLundi1000Med, cellMardi1000Med, cellMercredi1000Med, cellJeudi1000Med, cellVendredi1000Med, cellSamedi1000Med,
            cellLundi1100Med, cellMardi1100Med, cellMercredi1100Med, cellJeudi1100Med, cellVendredi1100Med, cellSamedi1100Med,
            cellLundi1200Med, cellMardi1200Med, cellMercredi1200Med, cellJeudi1200Med, cellVendredi1200Med, cellSamedi1200Med,
            cellLundi1300Med, cellMardi1300Med, cellMercredi1300Med, cellJeudi1300Med, cellVendredi1300Med, cellSamedi1300Med,
            cellLundi1400Med, cellMardi1400Med, cellMercredi1400Med, cellJeudi1400Med, cellVendredi1400Med, cellSamedi1400Med,
            cellLundi1500Med, cellMardi1500Med, cellMercredi1500Med, cellJeudi1500Med, cellVendredi1500Med, cellSamedi1500Med,
            cellLundi1600Med, cellMardi1600Med, cellMercredi1600Med, cellJeudi1600Med, cellVendredi1600Med, cellSamedi1600Med,
            cellLundi1700Med, cellMardi1700Med, cellMercredi1700Med, cellJeudi1700Med, cellVendredi1700Med, cellSamedi1700Med
        };
        
        for (VBox cellule : cellules) {
            if (cellule != null) {
                cellule.getChildren().clear();
            }
        }
    }
    
        private VBox getCellulePourHeureEtJour(LocalTime heure, DayOfWeek jour) {
        if (jour == DayOfWeek.SUNDAY) return null;

        int heureArrondie = heure.getHour();
        String heureStr = String.format("%02d00", heureArrondie);
        String jourStr = switch (jour) {
            case MONDAY -> "Lundi";
            case TUESDAY -> "Mardi";
            case WEDNESDAY -> "Mercredi";
            case THURSDAY -> "Jeudi";
            case FRIDAY -> "Vendredi";
            case SATURDAY -> "Samedi";
            default -> null;
        };

        if (jourStr == null) return null;

        String idCellule = "cell" + jourStr + heureStr + "Med";

        return switch (idCellule) {
            case "cellLundi0800Med" -> cellLundi0800Med;
            case "cellMardi0800Med" -> cellMardi0800Med;
            case "cellMercredi0800Med" -> cellMercredi0800Med;
            case "cellJeudi0800Med" -> cellJeudi0800Med;
            case "cellVendredi0800Med" -> cellVendredi0800Med;
            case "cellSamedi0800Med" -> cellSamedi0800Med;
            case "cellLundi0900Med" -> cellLundi0900Med;
            case "cellMardi0900Med" -> cellMardi0900Med;
            case "cellMercredi0900Med" -> cellMercredi0900Med;
            case "cellJeudi0900Med" -> cellJeudi0900Med;
            case "cellVendredi0900Med" -> cellVendredi0900Med;
            case "cellSamedi0900Med" -> cellSamedi0900Med;
            case "cellLundi1000Med" -> cellLundi1000Med;
            case "cellMardi1000Med" -> cellMardi1000Med;
            case "cellMercredi1000Med" -> cellMercredi1000Med;
            case "cellJeudi1000Med" -> cellJeudi1000Med;
            case "cellVendredi1000Med" -> cellVendredi1000Med;
            case "cellSamedi1000Med" -> cellSamedi1000Med;
            case "cellLundi1100Med" -> cellLundi1100Med;
            case "cellMardi1100Med" -> cellMardi1100Med;
            case "cellMercredi1100Med" -> cellMercredi1100Med;
            case "cellJeudi1100Med" -> cellJeudi1100Med;
            case "cellVendredi1100Med" -> cellVendredi1100Med;
            case "cellSamedi1100Med" -> cellSamedi1100Med;
            case "cellLundi1200Med" -> cellLundi1200Med;
            case "cellMardi1200Med" -> cellMardi1200Med;
            case "cellMercredi1200Med" -> cellMercredi1200Med;
            case "cellJeudi1200Med" -> cellJeudi1200Med;
            case "cellVendredi1200Med" -> cellVendredi1200Med;
            case "cellSamedi1200Med" -> cellSamedi1200Med;
            case "cellLundi1300Med" -> cellLundi1300Med;
            case "cellMardi1300Med" -> cellMardi1300Med;
            case "cellMercredi1300Med" -> cellMercredi1300Med;
            case "cellJeudi1300Med" -> cellJeudi1300Med;
            case "cellVendredi1300Med" -> cellVendredi1300Med;
            case "cellSamedi1300Med" -> cellSamedi1300Med;
            case "cellLundi1400Med" -> cellLundi1400Med;
            case "cellMardi1400Med" -> cellMardi1400Med;
            case "cellMercredi1400Med" -> cellMercredi1400Med;
            case "cellJeudi1400Med" -> cellJeudi1400Med;
            case "cellVendredi1400Med" -> cellVendredi1400Med;
            case "cellSamedi1400Med" -> cellSamedi1400Med;
            case "cellLundi1500Med" -> cellLundi1500Med;
            case "cellMardi1500Med" -> cellMardi1500Med;
            case "cellMercredi1500Med" -> cellMercredi1500Med;
            case "cellJeudi1500Med" -> cellJeudi1500Med;
            case "cellVendredi1500Med" -> cellVendredi1500Med;
            case "cellSamedi1500Med" -> cellSamedi1500Med;
            case "cellLundi1600Med" -> cellLundi1600Med;
            case "cellMardi1600Med" -> cellMardi1600Med;
            case "cellMercredi1600Med" -> cellMercredi1600Med;
            case "cellJeudi1600Med" -> cellJeudi1600Med;
            case "cellVendredi1600Med" -> cellVendredi1600Med;
            case "cellSamedi1600Med" -> cellSamedi1600Med;
            case "cellLundi1700Med" -> cellLundi1700Med;
            case "cellMardi1700Med" -> cellMardi1700Med;
            case "cellMercredi1700Med" -> cellMercredi1700Med;
            case "cellJeudi1700Med" -> cellJeudi1700Med;
            case "cellVendredi1700Med" -> cellVendredi1700Med;
            case "cellSamedi1700Med" -> cellSamedi1700Med;
            default -> null;
        };
    }

    // ==================== DONNÉES FICTIVES ====================
    private void chargerDonneesFictives() {
        Patient p1 = new Patient("123456", "BENALI", "Karim", "01/01/1980", "0600000000", "CIN123", "M", "Casablanca");
        Patient p2 = new Patient("789012", "ZOUHAIR", "Sara", "15/05/1995", "0611111111", "CIN456", "F", "Rabat");
        Patient p3 = new Patient("555666", "LAHLOU", "Ahmed", "10/10/1988", "0622222222", "CIN789", "M", "Marrakech");

        // Ajouter à la liste globale des patients (pour la ComboBox)
        listePatients.addAll(p1, p2, p3);
        
        // RDV aujourd'hui
        listeRDV.add(new RendezVous(LocalDate.now(), LocalTime.of(9, 0), p1, medecinConnecte, "Consultation générale", RendezVous.Statut.PREVU));
        listeRDV.add(new RendezVous(LocalDate.now(), LocalTime.of(10, 30), p2, medecinConnecte, "Contrôle annuel", RendezVous.Statut.PREVU));
        listeRDV.add(new RendezVous(LocalDate.now(), LocalTime.of(14, 0), p3, medecinConnecte, "Suivi post-opératoire", RendezVous.Statut.EFFECTUE));

        // RDV autres jours pour le graphique
        LocalDate lundi = LocalDate.now().with(DayOfWeek.MONDAY);
        listeRDV.add(new RendezVous(lundi, LocalTime.of(11, 0), p1, medecinConnecte, "Vaccin", RendezVous.Statut.EFFECTUE));
        listeRDV.add(new RendezVous(lundi.plusDays(1), LocalTime.of(15, 0), p2, medecinConnecte, "Bilan sanguin", RendezVous.Statut.PREVU));
        listeRDV.add(new RendezVous(lundi.plusDays(2), LocalTime.of(9, 0), p3, medecinConnecte, "Consultation", RendezVous.Statut.PREVU));
    }

    // ==================== DÉCONNEXION ====================
    @FXML
    private void deconnecter() {
    	// Fermer la fenêtre actuelle (dashboard)
        Stage dashboardStage = (Stage) tabPaneMain.getScene().getWindow();
        dashboardStage.close();

        // Ouvrir la fenêtre login
        try {
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/view/fxml/login.fxml"));
            Stage loginStage = new Stage();
            loginStage.setTitle("VitaDesk – Connexion");
            loginStage.setScene(new Scene(loginRoot));
            loginStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
            loginStage.setResizable(false);
            loginStage.centerOnScreen();
            loginStage.show();
            
            System.out.println("Déconnexion OK!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void ouvrirDossierMedical(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/dossier_medical.fxml"));
            Parent root = loader.load();

            DossierMedicalController controller = loader.getController();
            controller.afficherDossier(patient);

            Stage stage = new Stage();
            stage.setTitle("Dossier Médical - " + patient.getNom() + " " + patient.getPrenom());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(tablePatientsMed.getScene().getWindow());
            stage.setResizable(false);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void ouvrirModifierStatut(RendezVous rdv) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/modifier_statut_rdv.fxml"));
            Parent root = loader.load();

            ModifierStatutRDVController controller = loader.getController();

            // on passe le RDV + le contrôleur médecin pour permettre la suppression réelle
            controller.setData(rdv, this);

            Stage stage = new Stage();
            stage.setTitle("Modifier le statut du RDV");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(tabPaneMain.getScene().getWindow());
            stage.setResizable(false);
            stage.showAndWait();

            // Rafraîchir après fermeture
            rafraichirPlanning();
            chargerGraphiqueSemaine();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Méthode pour supprimer un RDV
    public void supprimerRDV(RendezVous rdv) {
        listeRDV.remove(rdv);
        rafraichirPlanning();
        chargerGraphiqueSemaine();
    }
    
    // Méthode pour ajouter un RDV
    public void ajouterRDV(RendezVous rdv) {
        listeRDV.add(rdv);
        rafraichirPlanning();
        chargerGraphiqueSemaine();
    }
    
    // Méthode pour ouvrir la fenêtre d'ajout de RDV
    @FXML
    private void ouvrirAjouterRDV() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/ajouter_rdv.fxml"));
            Parent root = loader.load();

            AjouterRDVController controller = loader.getController();
            controller.setDashboardControllerMed(this);
            controller.initialiserPourMedecin(listePatients, medecinConnecte);

            Stage stage = new Stage();
            stage.setTitle("Ajouter un Rendez-vous");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(tabPaneMain.getScene().getWindow());
            stage.setResizable(false);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
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