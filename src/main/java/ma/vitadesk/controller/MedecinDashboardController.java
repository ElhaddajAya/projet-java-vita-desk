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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
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
    @FXML private TableView<RendezVous> tableConsultationsJourAcceuil;
    @FXML private TableColumn<RendezVous, String> colNumAccueil;
    @FXML private TableColumn<RendezVous, String> colHeureAccueil;
    @FXML private TableColumn<RendezVous, String> colPatientAccueil;
    @FXML private TableColumn<RendezVous, String> colMotifAccueil;
    @FXML private TableColumn<RendezVous, String> colStatutAccueil;

    @FXML private BarChart<String, Number> barRdvSemaineMed;

    // ==================== PLANNING ====================
    @FXML private DatePicker datePickerPlanningMed;
    @FXML private Label selectedDatePlanningMed;

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

        // Accueil sélectionné au démarrage
        tabPaneMain.getSelectionModel().select(tabAccueil);
        highlightButton(btnAccueil);

        // Listener date planning
        datePickerPlanningMed.valueProperty().addListener((obs, old, newVal) -> rafraichirPlanning());
        
     // Liaison des colonnes du tableau Accueil avec les propriétés du RendezVous
        colNumAccueil.setCellValueFactory(cellData -> {
            int index = tableConsultationsJourAcceuil.getItems().indexOf(cellData.getValue()) + 1;
            return new SimpleStringProperty(String.valueOf(index));
        });

        colHeureAccueil.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getHeure().format(DateTimeFormatter.ofPattern("HH:mm")))
        );

        colPatientAccueil.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getPatient().getPrenom() + " " + cellData.getValue().getPatient().getNom())
        );

        colMotifAccueil.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getMotif())
        );

        colStatutAccueil.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatut().getLabel())
        );

        // Style du statut avec couleur
        colStatutAccueil.setCellFactory(column -> new TableCell<RendezVous, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    RendezVous rdv = getTableView().getItems().get(getIndex());
                    setStyle("-fx-background-color: " + rdv.getStatut().getCouleur() + "; " +
                             "-fx-text-fill: white; -fx-font-weight: bold; -fx-alignment: center;");
                }
            }
        });
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
        btn.setStyle("-fx-background-color: #0d6efd; -fx-text-fill: white; -fx-border-width: 0.1px; -fx-border-color: black;");
    }

    // ==================== RAFRAÎCHISSEMENT ====================
    private void rafraichirTout() {
        rafraichirAccueil();
        rafraichirPlanning();
        chargerGraphiqueSemaine();
    }

    private void rafraichirAccueil() {
        LocalDate aujourdHui = LocalDate.now();
        ObservableList<RendezVous> rdvAujourdHui = FXCollections.observableArrayList();
        for (RendezVous rdv : listeRDV) {
            if (rdv.getDate().equals(aujourdHui) && rdv.getDocteur().equals(medecinConnecte)) {
                rdvAujourdHui.add(rdv);
            }
        }
        tableConsultationsJourAcceuil.setItems(rdvAujourdHui.sorted((a, b) -> a.getHeure().compareTo(b.getHeure())));
    }

    private void chargerGraphiqueSemaine() {
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

    private void rafraichirPlanning() {
        viderToutesLesCellules();
        LocalDate date = datePickerPlanningMed.getValue();
        if (date == null) return;
        selectedDatePlanningMed.setText(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        for (RendezVous rdv : listeRDV) {
            if (rdv.getDate().equals(date) && rdv.getDocteur().equals(medecinConnecte)) {
                VBox cellule = getCellulePourHeureEtJour(rdv.getHeure(), rdv.getDate().getDayOfWeek());
                if (cellule != null) {
                    Label label = new Label(rdv.getPatient().getPrenom() + " " + rdv.getPatient().getNom());
                    label.setStyle("-fx-background-color: #4D93FF; -fx-text-fill: white; -fx-padding: 8; -fx-background-radius: 6; -fx-font-weight: bold;");
                    label.setMaxWidth(Double.MAX_VALUE);
                    label.setTooltip(new Tooltip("Motif: " + rdv.getMotif() + "\nHeure: " + rdv.getHeure()));
                    cellule.getChildren().add(label);
                }
            }
        }
    }

    private void viderToutesLesCellules() {
        // Vide toutes les cellules
        cellLundi0800Med.getChildren().clear();
        cellMardi0800Med.getChildren().clear();
        cellMercredi0800Med.getChildren().clear();
        cellJeudi0800Med.getChildren().clear();
        cellVendredi0800Med.getChildren().clear();
        cellSamedi0800Med.getChildren().clear();
        
        cellLundi0900Med.getChildren().clear();
        cellMardi0900Med.getChildren().clear();
        cellMercredi0900Med.getChildren().clear();
        cellJeudi0900Med.getChildren().clear();
        cellVendredi0900Med.getChildren().clear();
        cellSamedi0900Med.getChildren().clear();
        
        cellLundi1000Med.getChildren().clear();
        cellMardi1000Med.getChildren().clear();
        cellMercredi1000Med.getChildren().clear();
        cellJeudi1000Med.getChildren().clear();
        cellVendredi1000Med.getChildren().clear();
        cellSamedi1000Med.getChildren().clear();
        
        cellLundi1100Med.getChildren().clear();
        cellMardi1100Med.getChildren().clear();
        cellMercredi1100Med.getChildren().clear();
        cellJeudi1100Med.getChildren().clear();
        cellVendredi1100Med.getChildren().clear();
        cellSamedi1100Med.getChildren().clear();
        
        cellLundi1200Med.getChildren().clear();
        cellMardi1200Med.getChildren().clear();
        cellMercredi1200Med.getChildren().clear();
        cellJeudi1200Med.getChildren().clear();
        cellVendredi1200Med.getChildren().clear();
        cellSamedi1200Med.getChildren().clear();
        
        cellLundi1300Med.getChildren().clear();
        cellMardi1300Med.getChildren().clear();
        cellMercredi1300Med.getChildren().clear();
        cellJeudi1300Med.getChildren().clear();
        cellVendredi1300Med.getChildren().clear();
        cellSamedi1300Med.getChildren().clear();
        
        cellLundi1400Med.getChildren().clear();
        cellMardi1400Med.getChildren().clear();
        cellMercredi1400Med.getChildren().clear();
        cellJeudi1400Med.getChildren().clear();
        cellVendredi1400Med.getChildren().clear();
        cellSamedi1400Med.getChildren().clear();
        
        cellLundi1500Med.getChildren().clear();
        cellMardi1500Med.getChildren().clear();
        cellMercredi1500Med.getChildren().clear();
        cellJeudi1500Med.getChildren().clear();
        cellVendredi1500Med.getChildren().clear();
        cellSamedi1500Med.getChildren().clear();
        
        cellLundi1600Med.getChildren().clear();
        cellMardi1600Med.getChildren().clear();
        cellMercredi1600Med.getChildren().clear();
        cellJeudi1600Med.getChildren().clear();
        cellVendredi1600Med.getChildren().clear();
        cellSamedi1600Med.getChildren().clear();
        
        cellLundi1700Med.getChildren().clear();
        cellMardi1700Med.getChildren().clear();
        cellMercredi1700Med.getChildren().clear();
        cellJeudi1700Med.getChildren().clear();
        cellVendredi1700Med.getChildren().clear();
        cellSamedi1700Med.getChildren().clear();
    }

    private VBox getCellulePourHeureEtJour(LocalTime heure, DayOfWeek jour) {
        if (jour == DayOfWeek.SUNDAY) return null; // Pas de dimanche

        // Normaliser l'heure : on arrondit à l'heure pleine la plus proche (ex: 10:30 → cellule 10:00)
        // Tu peux ajuster cette logique plus tard si tu veux des créneaux de 30 min dédiés
        int heureArrondie = heure.getHour();
        if (heure.getMinute() >= 30) {
            heureArrondie = heure.getHour(); // reste sur l'heure courante (ex: 10:30 → 10:00-11:00)
        }

        String heureStr = String.format("%02d00", heureArrondie); // "0800", "0900", "1000", etc.
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
            // 08:00
            case "cellLundi0800Med" -> cellLundi0800Med;
            case "cellMardi0800Med" -> cellMardi0800Med;
            case "cellMercredi0800Med" -> cellMercredi0800Med;
            case "cellJeudi0800Med" -> cellJeudi0800Med;
            case "cellVendredi0800Med" -> cellVendredi0800Med;
            case "cellSamedi0800Med" -> cellSamedi0800Med;

            // 09:00
            case "cellLundi0900Med" -> cellLundi0900Med;
            case "cellMardi0900Med" -> cellMardi0900Med;
            case "cellMercredi0900Med" -> cellMercredi0900Med;
            case "cellJeudi0900Med" -> cellJeudi0900Med;
            case "cellVendredi0900Med" -> cellVendredi0900Med;
            case "cellSamedi0900Med" -> cellSamedi0900Med;

            // 10:00 (inclut 10:30)
            case "cellLundi1000Med" -> cellLundi1000Med;
            case "cellMardi1000Med" -> cellMardi1000Med;
            case "cellMercredi1000Med" -> cellMercredi1000Med;
            case "cellJeudi1000Med" -> cellJeudi1000Med;
            case "cellVendredi1000Med" -> cellVendredi1000Med;
            case "cellSamedi1000Med" -> cellSamedi1000Med;

            // 11:00
            case "cellLundi1100Med" -> cellLundi1100Med;
            case "cellMardi1100Med" -> cellMardi1100Med;
            case "cellMercredi1100Med" -> cellMercredi1100Med;
            case "cellJeudi1100Med" -> cellJeudi1100Med;
            case "cellVendredi1100Med" -> cellVendredi1100Med;
            case "cellSamedi1100Med" -> cellSamedi1100Med;

            // 12:00
            case "cellLundi1200Med" -> cellLundi1200Med;
            case "cellMardi1200Med" -> cellMardi1200Med;
            case "cellMercredi1200Med" -> cellMercredi1200Med;
            case "cellJeudi1200Med" -> cellJeudi1200Med;
            case "cellVendredi1200Med" -> cellVendredi1200Med;
            case "cellSamedi1200Med" -> cellSamedi1200Med;

            // 13:00
            case "cellLundi1300Med" -> cellLundi1300Med;
            case "cellMardi1300Med" -> cellMardi1300Med;
            case "cellMercredi1300Med" -> cellMercredi1300Med;
            case "cellJeudi1300Med" -> cellJeudi1300Med;
            case "cellVendredi1300Med" -> cellVendredi1300Med;
            case "cellSamedi1300Med" -> cellSamedi1300Med;

            // 14:00
            case "cellLundi1400Med" -> cellLundi1400Med;
            case "cellMardi1400Med" -> cellMardi1400Med;
            case "cellMercredi1400Med" -> cellMercredi1400Med;
            case "cellJeudi1400Med" -> cellJeudi1400Med;
            case "cellVendredi1400Med" -> cellVendredi1400Med;
            case "cellSamedi1400Med" -> cellSamedi1400Med;

            // 15:00
            case "cellLundi1500Med" -> cellLundi1500Med;
            case "cellMardi1500Med" -> cellMardi1500Med;
            case "cellMercredi1500Med" -> cellMercredi1500Med;
            case "cellJeudi1500Med" -> cellJeudi1500Med;
            case "cellVendredi1500Med" -> cellVendredi1500Med;
            case "cellSamedi1500Med" -> cellSamedi1500Med;

            // 16:00
            case "cellLundi1600Med" -> cellLundi1600Med;
            case "cellMardi1600Med" -> cellMardi1600Med;
            case "cellMercredi1600Med" -> cellMercredi1600Med;
            case "cellJeudi1600Med" -> cellJeudi1600Med;
            case "cellVendredi1600Med" -> cellVendredi1600Med;
            case "cellSamedi1600Med" -> cellSamedi1600Med;

            // 17:00
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
        Patient p1 = new Patient("123456", "Karim", "Benali", "01/01/1980", "0600000000", "M", "CIN123", "Casablanca");
        Patient p2 = new Patient("789012", "Sara", "Zouhair", "15/05/1995", "0611111111", "F", "CIN456", "Rabat");
        Patient p3 = new Patient("555666", "Ahmed", "Lahlou", "10/10/1988", "0622222222", "M", "CIN789", "Marrakech");

        // RDV aujourd'hui
        listeRDV.add(new RendezVous(LocalDate.now(), LocalTime.of(9, 0), p1, medecinConnecte, "Consultation générale", RendezVous.Statut.PREVU));
        listeRDV.add(new RendezVous(LocalDate.now(), LocalTime.of(10, 30), p2, medecinConnecte, "Contrôle annuel", RendezVous.Statut.PREVU));
        listeRDV.add(new RendezVous(LocalDate.now(), LocalTime.of(14, 0), p3, medecinConnecte, "Suivi post-opératoire", RendezVous.Statut.EFFECTUE));

        // RDV autres jours pour le graphique
        listeRDV.add(new RendezVous(LocalDate.now().minusDays(1), LocalTime.of(11, 0), p1, medecinConnecte, "Vaccin", RendezVous.Statut.EFFECTUE));
        listeRDV.add(new RendezVous(LocalDate.now().plusDays(1), LocalTime.of(15, 0), p2, medecinConnecte, "Bilan sanguin", RendezVous.Statut.PREVU));
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
}