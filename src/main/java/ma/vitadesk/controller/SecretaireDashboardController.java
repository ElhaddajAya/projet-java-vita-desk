package ma.vitadesk.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ma.vitadesk.model.Medecin;
import ma.vitadesk.model.Patient;
import ma.vitadesk.model.RendezVous;
import ma.vitadesk.model.Utilisateur;
import ma.vitadesk.util.SessionLockManager;

public class SecretaireDashboardController implements Initializable {
	@FXML private Label lblSecretaireNom;
	@FXML private Label lblSecretaireBienvennue;
	
	// Acceuil
	@FXML private BarChart<String, Number> barRdvSemaine;
	@FXML private LineChart<String, Number> lineRdv30Jours;
	
	// SIDEBAR BOUTONS
	@FXML private Button btnAccueilSec;
	@FXML private Button btnDocteursSec;
	@FXML private Button btnPatientsSec;
	@FXML private Button btnRendezVousSec;
	@FXML private Button btnDeconnexionSec;

	// TABS
	@FXML private TabPane tabPaneMainSec;
	@FXML private Tab tabAccueilSec;
	@FXML private Tab tabDocteursSec;
	@FXML private Tab tabPatientsSec;
	@FXML private Tab tabRendezVousSec;
	
	// Patients
	@FXML private TableView<Patient> tablePatients;
	@FXML private TableColumn<Patient, String> colNumSocial;
	@FXML private TableColumn<Patient, String> colNom;
	@FXML private TableColumn<Patient, String> colPrenom;
	@FXML private TableColumn<Patient, String> colDateNaissance;
	@FXML private TableColumn<Patient, String> colTelephone;
	@FXML private TableColumn<Patient, String> colCin;
	@FXML private TableColumn<Patient, String> colSexe;
	@FXML private TableColumn<Patient, String> colAdresse;
	@FXML private TableColumn<Patient, String> colActions;
	@FXML private Button btnSupprimerPatient; // Boutton au-dessous du TableView
	@FXML private Button btnNouveauPatient; // le bouton "Nouveau Patient"

    private ObservableList<Patient> patients = FXCollections.observableArrayList();

	// Docteurs
	@FXML private TableView<Medecin> tableDocteurs;
	@FXML private TableColumn<Medecin, String> colDocNom;
	@FXML private TableColumn<Medecin, String> colDocPrenom;
	@FXML private TableColumn<Medecin, String> colDocSpecialite;
	@FXML private TableColumn<Medecin, String> colDocTelephone;
	@FXML private TableColumn<Medecin, String> colDocEmail;
	@FXML private Button btnSupprimerDocteur;
	@FXML private Button btnNouveauDocteur; // le bouton dans le tab Docteurs
	
	private ObservableList<Medecin> medecins = FXCollections.observableArrayList();	

	// Rendez-vouss
	@FXML private DatePicker datePickerPlanning; // DatePicker pour choisir la date du planning
	@FXML private Label selectedDatePlanning;
	// les VBox pour chaque créneau horaire (de Lundi à Samedi, de 8:00 à 15:00)
	@FXML private VBox cellLundi0800;
	@FXML private VBox cellMardi0800;
	@FXML private VBox cellMercredi0800;
	@FXML private VBox cellJeudi0800;
	@FXML private VBox cellVendredi0800;
	@FXML private VBox cellSamedi0800;
	@FXML private VBox cellLundi0900;
	@FXML private VBox cellMardi0900;
	@FXML private VBox cellMercredi0900;
	@FXML private VBox cellJeudi0900;
	@FXML private VBox cellVendredi0900;
	@FXML private VBox cellSamedi0900;
	@FXML private VBox cellLundi1000;
	@FXML private VBox cellMardi1000;
	@FXML private VBox cellMercredi1000;
	@FXML private VBox cellJeudi1000;
	@FXML private VBox cellVendredi1000;
	@FXML private VBox cellSamedi1000;
	@FXML private VBox cellLundi1100;
	@FXML private VBox cellMardi1100;
	@FXML private VBox cellMercredi1100;
	@FXML private VBox cellJeudi1100;
	@FXML private VBox cellVendredi1100;
	@FXML private VBox cellSamedi1100;
	@FXML private VBox cellLundi1200;
	@FXML private VBox cellMardi1200;
	@FXML private VBox cellMercredi1200;
	@FXML private VBox cellJeudi1200;
	@FXML private VBox cellVendredi1200;
	@FXML private VBox cellSamedi1200;
	@FXML private VBox cellLundi1300;
	@FXML private VBox cellMardi1300;
	@FXML private VBox cellMercredi1300;
	@FXML private VBox cellJeudi1300;
	@FXML private VBox cellVendredi1300;
	@FXML private VBox cellSamedi1300;
	@FXML private VBox cellLundi1400;
	@FXML private VBox cellMardi1400;
	@FXML private VBox cellMercredi1400;
	@FXML private VBox cellJeudi1400;
	@FXML private VBox cellVendredi1400;
	@FXML private VBox cellSamedi1400;
	@FXML private VBox cellLundi1500;
	@FXML private VBox cellMardi1500;
	@FXML private VBox cellMercredi1500;
	@FXML private VBox cellJeudi1500;
	@FXML private VBox cellVendredi1500;
	@FXML private VBox cellSamedi1500;
	@FXML private VBox cellLundi1600;
	@FXML private VBox cellMardi1600;
	@FXML private VBox cellMercredi1600;
	@FXML private VBox cellJeudi1600;
	@FXML private VBox cellVendredi1600;
	@FXML private VBox cellSamedi1600;
	@FXML private VBox cellLundi1700;
	@FXML private VBox cellMardi1700;
	@FXML private VBox cellMercredi1700;
	@FXML private VBox cellJeudi1700;
	@FXML private VBox cellVendredi1700;
	@FXML private VBox cellSamedi1700;
	
	@FXML private TextField txtRechercheRDV;
	@FXML private Button btnNouveauRDV; // le bouton dans la tab RDV
	
	// Liste de tous les rendez-vous (plus tard depuis la base de données)
	private ObservableList<RendezVous> tousLesRDV = FXCollections.observableArrayList();
	
	// Variable pour stocker l'utilisateur connecté
	private Utilisateur utilisateurConnecte;
	
	/**
	 * Méthode appelée par le LoginController pour passer les infos de l'utilisateur
	 * On affiche ensuite ces infos dans l'interface
	 */
	public void setUtilisateur(Utilisateur utilisateur) {
	    this.utilisateurConnecte = utilisateur;
	    
	    // Afficher le nom complet de la secrétaire dans le header
	    if (utilisateur.getNom() != null && utilisateur.getPrenom() != null) {
	        lblSecretaireNom.setText(utilisateur.getPrenom() + " " + utilisateur.getNom());
	        lblSecretaireBienvennue.setText("Bienvenue, " + utilisateur.getPrenom());
	    } else {
	        // Si pas de nom/prénom en BDD, on affiche juste le login
	        lblSecretaireNom.setText(utilisateur.getLogin());
	        lblSecretaireBienvennue.setText("Bienvenue, " + utilisateur.getLogin());
	    }
	}
	
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Accueil sélectionné au démarrage
        tabPaneMainSec.getSelectionModel().select(tabAccueilSec);
        highlightButton(btnAccueilSec);

    		/************** Acceuil ***********/
        // Initialisation du BarChart : RDV par jour cette semaine
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nombre de RDV");
        series.getData().add(new XYChart.Data<>("Lundi", 10));
        series.getData().add(new XYChart.Data<>("Mardi", 15));
        series.getData().add(new XYChart.Data<>("Mercredi", 9));
        series.getData().add(new XYChart.Data<>("Jeudi", 14));
        series.getData().add(new XYChart.Data<>("Vendredi", 18));
        series.getData().add(new XYChart.Data<>("Samedi", 21));
        series.getData().add(new XYChart.Data<>("Dimanche", 0));

        barRdvSemaine.getData().clear();
        barRdvSemaine.getData().add(series);
        
        // Initialisation du LineChart : Évolution 30 jours
        XYChart.Series<String, Number> seriesLine = new XYChart.Series<>();
        seriesLine.setName("RDV quotidiens");

        // Données fictives pour test (du 15/11 au 14/12 2025)
        seriesLine.getData().add(new XYChart.Data<>("15/11", 25));
        seriesLine.getData().add(new XYChart.Data<>("16/11", 28));
        seriesLine.getData().add(new XYChart.Data<>("17/11", 30));
        seriesLine.getData().add(new XYChart.Data<>("18/11", 32));
        seriesLine.getData().add(new XYChart.Data<>("19/11", 29));
        seriesLine.getData().add(new XYChart.Data<>("20/11", 35));
        seriesLine.getData().add(new XYChart.Data<>("21/11", 31));
        seriesLine.getData().add(new XYChart.Data<>("22/11", 33));
        seriesLine.getData().add(new XYChart.Data<>("23/11", 27));
        seriesLine.getData().add(new XYChart.Data<>("24/11", 30));
        seriesLine.getData().add(new XYChart.Data<>("25/11", 34));
        seriesLine.getData().add(new XYChart.Data<>("26/11", 36));
        seriesLine.getData().add(new XYChart.Data<>("27/11", 29));
        seriesLine.getData().add(new XYChart.Data<>("28/11", 32));
        seriesLine.getData().add(new XYChart.Data<>("29/11", 31));
        seriesLine.getData().add(new XYChart.Data<>("30/11", 35));
        seriesLine.getData().add(new XYChart.Data<>("01/12", 28));
        seriesLine.getData().add(new XYChart.Data<>("02/12", 30));
        seriesLine.getData().add(new XYChart.Data<>("03/12", 33));
        seriesLine.getData().add(new XYChart.Data<>("04/12", 37));
        seriesLine.getData().add(new XYChart.Data<>("05/12", 35));
        seriesLine.getData().add(new XYChart.Data<>("06/12", 32));
        seriesLine.getData().add(new XYChart.Data<>("07/12", 29));
        seriesLine.getData().add(new XYChart.Data<>("08/12", 31));
        seriesLine.getData().add(new XYChart.Data<>("09/12", 34));
        seriesLine.getData().add(new XYChart.Data<>("10/12", 36));
        seriesLine.getData().add(new XYChart.Data<>("11/12", 38));
        seriesLine.getData().add(new XYChart.Data<>("12/12", 35));
        seriesLine.getData().add(new XYChart.Data<>("13/12", 39));
        seriesLine.getData().add(new XYChart.Data<>("14/12", 35));

        lineRdv30Jours.getData().clear();
        lineRdv30Jours.getData().add(seriesLine);
        
        
        /*********** Patients ***********/
        // Liaison des colonnes avec les properties
        colNumSocial.setCellValueFactory(new PropertyValueFactory<>("numSocial"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colDateNaissance.setCellValueFactory(new PropertyValueFactory<>("dateNaissance"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colCin.setCellValueFactory(new PropertyValueFactory<>("cin"));
        colSexe.setCellValueFactory(new PropertyValueFactory<>("sexe"));
        colAdresse.setCellValueFactory(cellData -> {
            Patient patient = cellData.getValue();
            return new SimpleStringProperty(patient.getAdresse()); // utilisation du getter personnalisé
        });
        
        // Rendre la TableView éditable
        tablePatients.setEditable(true);
        // Autoriser la sélection multiple (avec Ctrl ou Shift)
        tablePatients.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Pour chaque colonne, autoriser l'édition
        colNom.setCellFactory(TextFieldTableCell.forTableColumn());
        colPrenom.setCellFactory(TextFieldTableCell.forTableColumn());
        colDateNaissance.setCellFactory(TextFieldTableCell.forTableColumn());
        colTelephone.setCellFactory(TextFieldTableCell.forTableColumn());
        colCin.setCellFactory(TextFieldTableCell.forTableColumn());
        colSexe.setCellFactory(TextFieldTableCell.forTableColumn());
        colAdresse.setCellFactory(TextFieldTableCell.forTableColumn());
        
        // Quand on valide l'édition (Enter ou perte de focus)
        colNom.setOnEditCommit(event -> event.getRowValue().setNom(event.getNewValue()));
        colPrenom.setOnEditCommit(event -> event.getRowValue().setPrenom(event.getNewValue()));
        colDateNaissance.setOnEditCommit(event -> event.getRowValue().setDateNaissance(event.getNewValue()));
        colTelephone.setOnEditCommit(event -> event.getRowValue().setTelephone(event.getNewValue()));
        colCin.setOnEditCommit(event -> event.getRowValue().setCin(event.getNewValue()));
        colSexe.setOnEditCommit(event -> event.getRowValue().setSexe(event.getNewValue()));
        colAdresse.setOnEditCommit(event -> event.getRowValue().setAdresse(event.getNewValue()));
        
        // Données fictives (20 patients)
        patients = FXCollections.observableArrayList(
            new Patient("12345678", "Benali", "Karim", "15/03/1985", "0661234567", "EE123456", "M", "Adresse anonyme Nº123"),
            new Patient("87654321", "El Idrissi", "Amina", "22/07/1990", "0678901234", "FF789012", "F", "Adresse anonyme Nº456"),
            new Patient("11223344", "Hassan", "Youssef", "10/11/1978", "0654321098", "GG112233", "M", "Adresse anonyme Nº789"),
            new Patient("44332211", "Zahra", "Fatima", "05/05/2001", "0612345678", "HH445566", "F", "Adresse anonyme Nº112"),
            new Patient("99887766", "Alaoui", "Mohamed", "30/12/1982", "0687654321", "II998877", "M", "Adresse anonyme Nº134"),
            new Patient("66778899", "Rahmani", "Sara", "14/09/1995", "0698765432", "JJ667788", "F", "Adresse anonyme Nº156"),
            new Patient("55443322", "Ouazzani", "Omar", "20/01/1988", "0623456789", "KK554433", "M", "Adresse anonyme Nº178"),
            new Patient("22114455", "Lahlou", "Nadia", "08/06/1993", "0634567890", "LL221144", "F", "Adresse anonyme Nº190"),
            new Patient("33445566", "Mernissi", "Ahmed", "12/04/1975", "0645678901", "MM334455", "M", "Adresse anonyme Nº121"),
            new Patient("77889900", "Tazi", "Leila", "25/10/1998", "0656789012", "NN778899", "F", "Adresse anonyme Nº122"),
            new Patient("10101010", "Khalid", "Imane", "03/02/1980", "0667890123", "OO101010", "F", "Adresse anonyme Nº124"),
            new Patient("20202020", "Jamal", "Rachid", "17/08/1987", "0678901235", "PP202020", "M", "Adresse anonyme Nº125"),
            new Patient("30303030", "Salma", "Bouchra", "29/05/1992", "0689012346", "QQ303030", "F", "Adresse anonyme Nº173"),
            new Patient("40404040", "Driss", "Hicham", "11/11/1970", "0690123457", "RR404040", "M", "Adresse anonyme Nº128"),
            new Patient("50505050", "Naima", "Khadija", "06/07/1996", "0611234568", "SS505050", "F", "Adresse anonyme Nº183"),
            new Patient("60606060", "Anas", "Said", "19/03/1983", "0622345679", "TT606060", "M", "Adresse anonyme Nº129"),
            new Patient("70707070", "Yasmine", "Houda", "24/12/1994", "0633456780", "UU707070", "F", "Adresse anonyme Nº423"),
            new Patient("80808080", "Bilal", "Tarek", "02/09/1979", "0644567891", "VV808080", "M", "Adresse anonyme Nº453"),
            new Patient("90909090", "Sofia", "Mariam", "16/04/2000", "0655678902", "WW909090", "F", "Adresse anonyme Nº458"),
            new Patient("11111111", "Reda", "Amine", "28/10/1986", "0666789013", "XX111111", "M", "Adresse anonyme Nº131")
        );

        tablePatients.setItems(patients);
        
        // === AJOUT DE LA COLONNE "Actions" ===
        TableColumn<Patient, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(200);
        colActions.setSortable(false);
        colActions.setStyle("-fx-alignment: CENTER;"); // centre les boutons

        colActions.setCellFactory(param -> new TableCell<Patient, Void>() {

            private final Button btnDossier = new Button("Dossier Médical");
            private final Button btnSupprimerLigne = new Button("Supprimer");

            private final HBox hbox = new HBox(10, btnDossier, btnSupprimerLigne);

            {
            		// Bouton Dossier Médical
                btnDossier.setStyle(
                    "-fx-background-color: #FF9000; " +
                    "-fx-text-fill: white; " +
                    "-fx-cursor: HAND; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 8 16 8 16; " +     /* plus de padding → bouton plus grand et confortable */
                    "-fx-background-radius: 6;"      /* coins légèrement arrondis */
                );

                // Bouton Supprimer : rouge avec padding
                btnSupprimerLigne.setStyle(
                    "-fx-background-color: red; " +
                    "-fx-text-fill: white; " +
                    "-fx-cursor: HAND; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 8 16 8 16; " +
                    "-fx-background-radius: 6;"
                );

                hbox.setAlignment(Pos.CENTER);

                // Action : ouvrir le dossier médical
                btnDossier.setOnAction(event -> {
                    Patient patient = getTableView().getItems().get(getIndex());
                    ouvrirDossierMedical(patient);
                });

                // Action : supprimer un seul patient (celui de la ligne)
                btnSupprimerLigne.setOnAction(event -> {
                    Patient patient = getTableView().getItems().get(getIndex());
                    supprimerUnPatient(patient);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        });
        
        // AJOUTE LA COLONNE À LA FIN
        tablePatients.getColumns().add(colActions);

        // Activer/désactiver le bouton Supprimer en bas
        tablePatients.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            btnSupprimerPatient.setDisable(tablePatients.getSelectionModel().getSelectedItems().isEmpty());
        });

        // Désactiver le bouton Supprimer au démarrage
        btnSupprimerPatient.setDisable(true);
        
        
        /*********** Docteurs ***********/
        // Liaison colonnes medecins
        colDocNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colDocPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colDocSpecialite.setCellValueFactory(new PropertyValueFactory<>("specialite"));
        colDocTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colDocEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        tableDocteurs.setItems(medecins);

        // Données fictives
        medecins.addAll(
            new Medecin("Ahmadi", "Karim", "Généraliste", "0661234567", "k.ahmadi@clinic.ma"),
            new Medecin("Fatima", "Zahra", "Pédiatre", "0678901234", "z.fatima@clinic.ma"),
            new Medecin("Karim", "Mohamed", "Dentiste", "0654321098", "m.karim@clinic.ma")
        );
        
        // Rendre la TableView éditable
        tableDocteurs.setEditable(true);
        // Autoriser la sélection multiple (avec Ctrl ou Shift)
        tableDocteurs.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        // Activer/désactiver le bouton Supprimer en bas
        tableDocteurs.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
        		btnSupprimerDocteur.setDisable(tableDocteurs.getSelectionModel().getSelectedItems().isEmpty());
        });

        // Désactiver le bouton Supprimer au démarrage
        btnSupprimerDocteur.setDisable(true);
        
        /*********** RDV ***********/
        // 1. Mettre la date d'aujourd'hui dans le DatePicker
        datePickerPlanning.setValue(LocalDate.now());
        
        // Afficher la date dans la Label
        LocalDate date = datePickerPlanning.getValue();
        selectedDatePlanning.setText(date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));        

        // 2. Ajouter des rendez-vous fictifs pour tester
        chargerDonneesFictivesRDV();

        // 3. Afficher le planning du jour actuel
        rafraichirPlanning();

        // 4. Quand l'utilisateur change la date → rafraîchir le planning
        datePickerPlanning.valueProperty().addListener((observable, oldValue, newValue) -> {
            rafraichirPlanning();
        });
        
        // Recherche en temps réel par médecin
        txtRechercheRDV.textProperty().addListener((observable, oldValue, newValue) -> {
            rafraichirPlanning(); // on rafraîchit le planning à chaque frappe
        });
    }
    
    // Méthode pour gerer la navigation dans les Tabs
    @FXML
    private void handleMenuClick(ActionEvent event) {
        Button clicked = (Button) event.getSource();
        resetSidebarStyleSec();

        if (clicked == btnAccueilSec) {
            tabPaneMainSec.getSelectionModel().select(tabAccueilSec);
            clicked.setStyle("-fx-background-color: #4D93FF; -fx-text-fill: white; -fx-border-width: 0.1px; -fx-border-color: black;");
        } else if (clicked == btnDocteursSec) {
            tabPaneMainSec.getSelectionModel().select(tabDocteursSec);
            clicked.setStyle("-fx-background-color: #4D93FF; -fx-text-fill: white; -fx-border-width: 0.1px; -fx-border-color: black;");
        } else if (clicked == btnPatientsSec) {
            tabPaneMainSec.getSelectionModel().select(tabPatientsSec);
            clicked.setStyle("-fx-background-color: #4D93FF; -fx-text-fill: white; -fx-border-width: 0.1px; -fx-border-color: black;");
        } else if (clicked == btnRendezVousSec) {
            tabPaneMainSec.getSelectionModel().select(tabRendezVousSec);
            clicked.setStyle("-fx-background-color: #4D93FF; -fx-text-fill: white; -fx-border-width: 0.1px; -fx-border-color: black;");
        } else if (clicked == btnDeconnexionSec) {
            deconnecter();
        }
    }

    private void resetSidebarStyleSec() {
        btnAccueilSec.setStyle("-fx-background-color: #EFF2F5; -fx-border-width: 0.1px; -fx-border-color: black;");
        btnPatientsSec.setStyle("-fx-background-color: #EFF2F5; -fx-border-width: 0.1px; -fx-border-color: black;");
        btnDocteursSec.setStyle("-fx-background-color: #EFF2F5; -fx-border-width: 0.1px; -fx-border-color: black;");
        btnRendezVousSec.setStyle("-fx-background-color: #EFF2F5; -fx-border-width: 0.1px; -fx-border-color: black;");
        btnDeconnexionSec.setStyle("-fx-background-color: #EFF2F5; -fx-border-width: 0.1px; -fx-border-color: black;");
    }
    
    private void highlightButton(Button btn) {
	    	resetSidebarStyleSec();
	    	btn.setStyle("-fx-background-color: #4D93FF; -fx-text-fill: white; -fx-border-width: 0.1px; -fx-border-color: black;");
    }
    
    /**
     * Méthode pour se déconnecter
     * Libère le lock (Thread) et retourne à l'écran de connexion
     */
    @FXML
    private void deconnecter() {
        // === LIBÉRATION DU LOCK ===
        // Important : on libère le lock pour permettre à quelqu'un d'autre de se connecter
        SessionLockManager.releaseLock();
        
        // Fermer la fenêtre actuelle (dashboard)
        Stage dashboardStage = (Stage) tabPaneMainSec.getScene().getWindow(); // pour SecretaireDashboard
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
            
            System.out.println("Déconnexion OK - Lock libéré ✓");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	// === Méthode pour supprimer plusieurs patients sélectionnés ===
    @FXML
    private void supprimerPatientsSelectionnes(ActionEvent event) {
        ObservableList<Patient> selection = tablePatients.getSelectionModel().getSelectedItems();

        if (selection.isEmpty()) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer " + selection.size() + " patient(s) ?");
        alert.setContentText("Cette action est irréversible.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            patients.removeAll(selection);
        }
    }

    // === Supprimer un seul patient (depuis le bouton dans la ligne) ===
    private void supprimerUnPatient(Patient patient) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer " + patient.getNom() + " " + patient.getPrenom() + " ?");
        alert.setContentText("Cette action est irréversible.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            patients.remove(patient);
        }
    }
    
    // === Ouvrir la fenetre d'ajout de patients ===
    @FXML
    private void ouvrirAjouterPatient(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/ajouter_patient.fxml"));
            Parent root = loader.load();

            AjouterPatientController controller = loader.getController();
            controller.setDashboardController(this); // on passe la référence

            Stage stage = new Stage();
            stage.setTitle("Ajouter un nouveau patient");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(btnNouveauPatient.getScene().getWindow());
            stage.setResizable(false);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Méthode appelée par le controller d'ajout
    public void ajouterPatient(Patient patient) {
        patients.add(patient);
        tablePatients.scrollTo(patient); // scroll vers le nouveau
    }

    // === Ouvrir le dossier médical ===
    private void ouvrirDossierMedical(Patient patient) {
        if (patient == null) {
            System.out.println("Aucun patient sélectionné");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/dossier_medical.fxml"));
            // Charge le layout (tous les composants définis dans le FXML)
            Parent root = loader.load();

            // Crée une nouvelle fenêtre (Stage) pour le dossier médical
            DossierMedicalController controller = loader.getController();
            controller.afficherDossier(patient, null); // null = secrétaire
            
            Stage stage = new Stage();
            stage.setTitle("Dossier Médical - " + patient.getNom() + " " + patient.getPrenom());
            stage.setScene(new Scene(root));
            // Rend la fenêtre modale : l'utilisateur ne peut pas revenir au dashboard tant qu'il n'a pas fermé le dossier
            stage.initModality(Modality.APPLICATION_MODAL);
            // Lie cette fenetre à la fenetre principale (le dashboard) pour un bon comportement (centrage, fermeture, etc.)
            stage.initOwner(tablePatients.getScene().getWindow());
            stage.setResizable(false);
            stage.showAndWait(); // Affiche la fenetre et attend que l'utilisateur la ferme avant de continuer

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // === Méthode pour supprimer plusieurs medecins sélectionnés ===
    @FXML
    private void supprimerDocteurs(ActionEvent event) {
    		ObservableList<Medecin> selection = tableDocteurs.getSelectionModel().getSelectedItems();

        if (selection.isEmpty()) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer " + selection.size() + " docteur(s) ?");
        alert.setContentText("Cette action est irréversible.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            medecins.removeAll(selection);
        }
    }
    
    // mèthode pour ouvrir la fenetre d'ajout d'un docteur
    @FXML
    private void ouvrirAjouterDocteur(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/ajouter_docteur.fxml"));
            Parent root = loader.load();

            AjouterDocteurController controller = loader.getController();
            controller.setDashboardController(this);

            Stage stage = new Stage();
            stage.setTitle("Ajouter un nouveau docteur");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(btnNouveauDocteur.getScene().getWindow());
            stage.setResizable(false);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Méthode appelée par le controller d'ajout
    public void ajouterDocteur(Medecin medecin) {
        medecins.add(medecin);
    }
    
    // Méthode pour charger des RDV fictifs (que je vais supprimer plus tard)
    private void chargerDonneesFictivesRDV() {
        // Exemple de quelques rendez-vous
        tousLesRDV.add(new RendezVous(
            LocalDate.now(),                    // aujourd'hui
            LocalTime.of(8, 0),                 // 08:00
            patients.get(0),                    // premier patient de la liste
            medecins.get(0),                    // premier docteur
            "Consultation générale",
            RendezVous.Statut.PREVU
        ));

        tousLesRDV.add(new RendezVous(
            LocalDate.now(),
            LocalTime.of(8, 0),                  // même heure → 2 RDV au même créneau
            patients.get(1),
            medecins.get(1),
            "Vaccin",
            RendezVous.Statut.PREVU
        ));

        tousLesRDV.add(new RendezVous(
            LocalDate.now(),
            LocalTime.of(9, 30),
            patients.get(2),
            medecins.get(0),
            "Contrôle annuel",
            RendezVous.Statut.EFFECTUE
        ));

        tousLesRDV.add(new RendezVous(
            LocalDate.now(),
            LocalTime.of(10, 0),
            patients.get(3),
            medecins.get(1),
            "Douleur dentaire",
            RendezVous.Statut.ANNULE
        ));
    }

    // La méthode principale pour gestion des RDV
    public void rafraichirPlanning() {
    		// 0. Vider toutes les cellules
    		viderToutesLesCellules();
    	
        // 1. Récupérer la date sélectionnée
        LocalDate dateSelectionnee = datePickerPlanning.getValue();
        if (dateSelectionnee == null) {
            dateSelectionnee = LocalDate.now();
        }

        // 2. Récupérer le texte de recherche (nom du médecin)
        String recherche = txtRechercheRDV.getText().trim().toLowerCase();

        // 3. Vider toutes les cellules
        viderToutesLesCellules();

        // 4. Parcourir tous les RDV
        for (RendezVous rdv : tousLesRDV) {
            // Filtrer par date
            if (!rdv.getDate().equals(dateSelectionnee)) {
                continue;
            }


            // Filtrer par médecin OU par patient
            if (!recherche.isEmpty()) {
                String nomDocteur = (rdv.getDocteur().getPrenom() + " " + rdv.getDocteur().getNom()).toLowerCase();
                String nomPatient = (rdv.getPatient().getPrenom() + " " + rdv.getPatient().getNom()).toLowerCase();

                if (!nomDocteur.contains(recherche) && !nomPatient.contains(recherche)) {
                    continue; // on saute ce RDV s'il ne correspond ni au médecin ni au patient
                }
            }

            // Si on arrive ici, le RDV doit être affiché
            VBox cellule = getCellulePourJourEtHeure(rdv.getDate().getDayOfWeek(), rdv.getHeure());
            if (cellule != null) {
                Label labelRDV = new Label(rdv.getAffichageCellule());

	             // Style avec couleur du statut
	             labelRDV.setStyle(
	                 "-fx-background-color: " + rdv.getStatut().getCouleur() + "; " +
	                 "-fx-text-fill: white; " +
	                 "-fx-padding: 10 15; " +           // padding horizontal + vertical
	                 "-fx-background-radius: 12; " +
	                 "-fx-font-weight: bold; " +
	                 "-fx-font-size: 13; " +
	                 "-fx-alignment: center-left;"
	             );
	
	             // === ELLIPSIS quand le texte est trop long ===
	             labelRDV.setMaxWidth(Double.MAX_VALUE); // prend toute la largeur
	             labelRDV.setWrapText(false);            // pas de retour à la ligne
	             labelRDV.setTextOverrun(OverrunStyle.ELLIPSIS); // affiche "..." à la fin
	
	             // Tooltip complet au survol
	             String texteComplet = rdv.getPatient().getPrenom() + " " + rdv.getPatient().getNom() +
	                                   "\nDr. " + rdv.getDocteur().getPrenom() + " " + rdv.getDocteur().getNom() +
	                                   "\nMotif : " + rdv.getMotif() +
	                                   "\nStatut : " + rdv.getStatut().getLabel();
	             Tooltip tooltip = new Tooltip(texteComplet);
	             tooltip.setStyle("-fx-font-size: 14; -fx-background-color: #333; -fx-text-fill: white;");
	             Tooltip.install(labelRDV, tooltip);
	
	             // Ajouter un petit espace en bas
	             labelRDV.setPadding(new Insets(0, 0, 8, 0)); // espace sous le label

                // Ajouter le RDV dans la cellule
                cellule.getChildren().add(labelRDV);
                
                // Clic pour modifier le statut
                labelRDV.setOnMouseClicked(e -> ouvrirModifierStatut(rdv));
            }
        }
    }
    
    private void viderToutesLesCellules() {
        // 08:00
        cellLundi0800.getChildren().clear();
        cellMardi0800.getChildren().clear();
        cellMercredi0800.getChildren().clear();
        cellJeudi0800.getChildren().clear();
        cellVendredi0800.getChildren().clear();
        cellSamedi0800.getChildren().clear();

        // 09:00
        cellLundi0900.getChildren().clear();
        cellMardi0900.getChildren().clear();
        cellMercredi0900.getChildren().clear();
        cellJeudi0900.getChildren().clear();
        cellVendredi0900.getChildren().clear();
        cellSamedi0900.getChildren().clear();

        // 10:00
        cellLundi1000.getChildren().clear();
        cellMardi1000.getChildren().clear();
        cellMercredi1000.getChildren().clear();
        cellJeudi1000.getChildren().clear();
        cellVendredi1000.getChildren().clear();
        cellSamedi1000.getChildren().clear();

        // 11:00
        cellLundi1100.getChildren().clear();
        cellMardi1100.getChildren().clear();
        cellMercredi1100.getChildren().clear();
        cellJeudi1100.getChildren().clear();
        cellVendredi1100.getChildren().clear();
        cellSamedi1100.getChildren().clear();

        // 12:00
        cellLundi1200.getChildren().clear();
        cellMardi1200.getChildren().clear();
        cellMercredi1200.getChildren().clear();
        cellJeudi1200.getChildren().clear();
        cellVendredi1200.getChildren().clear();
        cellSamedi1200.getChildren().clear();

        // 13:00
        cellLundi1300.getChildren().clear();
        cellMardi1300.getChildren().clear();
        cellMercredi1300.getChildren().clear();
        cellJeudi1300.getChildren().clear();
        cellVendredi1300.getChildren().clear();
        cellSamedi1300.getChildren().clear();

        // 14:00
        cellLundi1400.getChildren().clear();
        cellMardi1400.getChildren().clear();
        cellMercredi1400.getChildren().clear();
        cellJeudi1400.getChildren().clear();
        cellVendredi1400.getChildren().clear();
        cellSamedi1400.getChildren().clear();

        // 15:00
        cellLundi1500.getChildren().clear();
        cellMardi1500.getChildren().clear();
        cellMercredi1500.getChildren().clear();
        cellJeudi1500.getChildren().clear();
        cellVendredi1500.getChildren().clear();
        cellSamedi1500.getChildren().clear();

        // 16:00
        cellLundi1600.getChildren().clear();
        cellMardi1600.getChildren().clear();
        cellMercredi1600.getChildren().clear();
        cellJeudi1600.getChildren().clear();
        cellVendredi1600.getChildren().clear();
        cellSamedi1600.getChildren().clear();

        // 17:00
        cellLundi1700.getChildren().clear();
        cellMardi1700.getChildren().clear();
        cellMercredi1700.getChildren().clear();
        cellJeudi1700.getChildren().clear();
        cellVendredi1700.getChildren().clear();
        cellSamedi1700.getChildren().clear();
    }
    
    // Mèthode pour trouver la bonne cellule selon l'heure
    private VBox getCellulePourJourEtHeure(java.time.DayOfWeek jour, LocalTime heure) {
        String heureStr = heure.toString(); // "08:00", "09:00", ....
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

        String id = "cell" + jourStr + heureStr.replace(":", ""); // Ex: "cellLundi0800"

        return switch (id) {
	     // 08:00
	        case "cellLundi0800" -> cellLundi0800;
	        case "cellMardi0800" -> cellMardi0800;
	        case "cellMercredi0800" -> cellMercredi0800;
	        case "cellJeudi0800" -> cellJeudi0800;
	        case "cellVendredi0800" -> cellVendredi0800;
	        case "cellSamedi0800" -> cellSamedi0800;
	
	        // 09:00
	        case "cellLundi0900" -> cellLundi0900;
	        case "cellMardi0900" -> cellMardi0900;
	        case "cellMercredi0900" -> cellMercredi0900;
	        case "cellJeudi0900" -> cellJeudi0900;
	        case "cellVendredi0900" -> cellVendredi0900;
	        case "cellSamedi0900" -> cellSamedi0900;
	
	        // 10:00
	        case "cellLundi1000" -> cellLundi1000;
	        case "cellMardi1000" -> cellMardi1000;
	        case "cellMercredi1000" -> cellMercredi1000;
	        case "cellJeudi1000" -> cellJeudi1000;
	        case "cellVendredi1000" -> cellVendredi1000;
	        case "cellSamedi1000" -> cellSamedi1000;
	
	        // 11:00
	        case "cellLundi1100" -> cellLundi1100;
	        case "cellMardi1100" -> cellMardi1100;
	        case "cellMercredi1100" -> cellMercredi1100;
	        case "cellJeudi1100" -> cellJeudi1100;
	        case "cellVendredi1100" -> cellVendredi1100;
	        case "cellSamedi1100" -> cellSamedi1100;
	
	        // 12:00
	        case "cellLundi1200" -> cellLundi1200;
	        case "cellMardi1200" -> cellMardi1200;
	        case "cellMercredi1200" -> cellMercredi1200;
	        case "cellJeudi1200" -> cellJeudi1200;
	        case "cellVendredi1200" -> cellVendredi1200;
	        case "cellSamedi1200" -> cellSamedi1200;
	
	        // 13:00
	        case "cellLundi1300" -> cellLundi1300;
	        case "cellMardi1300" -> cellMardi1300;
	        case "cellMercredi1300" -> cellMercredi1300;
	        case "cellJeudi1300" -> cellJeudi1300;
	        case "cellVendredi1300" -> cellVendredi1300;
	        case "cellSamedi1300" -> cellSamedi1300;
	
	        // 14:00
	        case "cellLundi1400" -> cellLundi1400;
	        case "cellMardi1400" -> cellMardi1400;
	        case "cellMercredi1400" -> cellMercredi1400;
	        case "cellJeudi1400" -> cellJeudi1400;
	        case "cellVendredi1400" -> cellVendredi1400;
	        case "cellSamedi1400" -> cellSamedi1400;
	
	        // 15:00
	        case "cellLundi1500" -> cellLundi1500;
	        case "cellMardi1500" -> cellMardi1500;
	        case "cellMercredi1500" -> cellMercredi1500;
	        case "cellJeudi1500" -> cellJeudi1500;
	        case "cellVendredi1500" -> cellVendredi1500;
	        case "cellSamedi1500" -> cellSamedi1500;
	
	        // 16:00
	        case "cellLundi1600" -> cellLundi1600;
	        case "cellMardi1600" -> cellMardi1600;
	        case "cellMercredi1600" -> cellMercredi1600;
	        case "cellJeudi1600" -> cellJeudi1600;
	        case "cellVendredi1600" -> cellVendredi1600;
	        case "cellSamedi1600" -> cellSamedi1600;
	
	        // 17:00
	        case "cellLundi1700" -> cellLundi1700;
	        case "cellMardi1700" -> cellMardi1700;
	        case "cellMercredi1700" -> cellMercredi1700;
	        case "cellJeudi1700" -> cellJeudi1700;
	        case "cellVendredi1700" -> cellVendredi1700;
	        case "cellSamedi1700" -> cellSamedi1700;
	
	        default -> null;
        };
    }
    
    // === Ouvrir la modal (fenetre) d'ajout d'un rdv
    @FXML
    private void ouvrirAjouterRDV(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/ajouter_rdv.fxml"));
            Parent root = loader.load();

            AjouterRDVController controller = loader.getController();
            controller.setDashboardController(this);
            controller.initialiserAvecListes(patients, medecins);

            Stage stage = new Stage();
            stage.setTitle("Nouveau Rendez-vous");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(btnNouveauRDV.getScene().getWindow());
            stage.setResizable(false);
            stage.showAndWait();

            // Rafraîchir le planning après ajout
            rafraichirPlanning();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Méthode appelée par le modal
    public void ajouterRDV(RendezVous rdv) {
        tousLesRDV.add(rdv);
        rafraichirPlanning();
    }
    
    // Méthofe appelée par le modal
    public void supprimerRDV(RendezVous rdv) {
        tousLesRDV.remove(rdv);
        rafraichirPlanning();
    }
    
    private void ouvrirModifierStatut(RendezVous rdv) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/modifier_statut_rdv.fxml"));
            Parent root = loader.load();

            ModifierStatutRDVController controller = loader.getController();
            controller.setData(rdv, this);

            Stage stage = new Stage();
            stage.setTitle("Modifier le statut");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(tabPaneMainSec.getScene().getWindow());
            stage.setResizable(false);
            stage.showAndWait();

            rafraichirPlanning(); // au cas où annulé

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Méthode pour exporter les RDV (avec date selectionée)
    @FXML
    private void exporterRDVExcel() {
        // Filtrer les RDV comme dans le planning actuel
        LocalDate dateDebut = datePickerPlanning.getValue();
        if (dateDebut == null) dateDebut = LocalDate.now();
        LocalDate dateFin = dateDebut; // export du jour sélectionné

        String recherche = txtRechercheRDV.getText().trim().toLowerCase();

        List<RendezVous> rdvAExporter = new ArrayList<>();
        for (RendezVous rdv : tousLesRDV) {
            if (rdv.getDate().isBefore(dateDebut) || rdv.getDate().isAfter(dateFin)) continue;

            if (!recherche.isEmpty()) {
                String nomDocteur = (rdv.getDocteur().getPrenom() + " " + rdv.getDocteur().getNom()).toLowerCase();
                String nomPatient = (rdv.getPatient().getPrenom() + " " + rdv.getPatient().getNom()).toLowerCase();
                if (!nomDocteur.contains(recherche) && !nomPatient.contains(recherche)) continue;
            }

            rdvAExporter.add(rdv);
        }

        if (rdvAExporter.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Aucun rendez-vous à exporter pour cette période.");
            alert.show();
            return;
        }

        // Créer le fichier Excel
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Rendez-vous " + dateDebut.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

            // En-tête
            Row header = sheet.createRow(0);
            String[] columns = {"Date", "Heure", "Patient", "Medecin", "Motif", "Statut"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(createHeaderStyle(workbook));
            }

            // Données
            int rowNum = 1;
            for (RendezVous rdv : rdvAExporter) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rdv.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                row.createCell(1).setCellValue(rdv.getHeure().toString());
                row.createCell(2).setCellValue(rdv.getPatient().getPrenom() + " " + rdv.getPatient().getNom());
                row.createCell(3).setCellValue("Dr. " + rdv.getDocteur().getPrenom() + " " + rdv.getDocteur().getNom());
                row.createCell(4).setCellValue(rdv.getMotif());
                row.createCell(5).setCellValue(rdv.getStatut().getLabel());
            }

            // Ajuster la largeur des colonnes
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Sauvegarder le fichier
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter les rendez-vous");
            fileChooser.setInitialFileName("RDV_" + dateDebut.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + ".xlsx");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
            File file = fileChooser.showSaveDialog(tabPaneMainSec.getScene().getWindow());

            if (file != null) {
                try (FileOutputStream out = new FileOutputStream(file)) {
                    workbook.write(out);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("Export réussi : " + file.getName());
                    alert.show();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Erreur lors de l'export : " + e.getMessage());
            alert.show();
        }
    }
    
    // Methode pour exporter les patients
    @FXML
    private void exporterPatientsExcel() {
        if (patients.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export");
            alert.setContentText("Aucun patient à exporter.");
            alert.show();
            return;
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Patients");

            // En-tête
            Row header = sheet.createRow(0);
            String[] columns = {"N° Social", "Nom", "Prénom", "Date Naissance", "Téléphone", "CIN", "Sexe", "Adresse"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(createHeaderStyle(workbook));
            }

            // Données
            int rowNum = 1;
            for (Patient p : patients) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(p.getNumSocial());
                row.createCell(1).setCellValue(p.getNom());
                row.createCell(2).setCellValue(p.getPrenom());
                row.createCell(3).setCellValue(p.getDateNaissance());
                row.createCell(4).setCellValue(p.getTelephone());
                row.createCell(5).setCellValue(p.getCin());
                row.createCell(6).setCellValue(p.getSexe());
                row.createCell(7).setCellValue(p.getAdresse());
            }

            // Ajuster la largeur des colonnes
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Choisir où sauvegarder
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter les patients");
            fileChooser.setInitialFileName("Patients_VitaDesk_" + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + ".xlsx");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
            File file = fileChooser.showSaveDialog(tablePatients.getScene().getWindow());

            if (file != null) {
                try (FileOutputStream out = new FileOutputStream(file)) {
                    workbook.write(out);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Succès");
                    alert.setContentText("Patients exportés avec succès !\nFichier : " + file.getName());
                    alert.show();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Erreur lors de l'export : " + e.getMessage());
            alert.show();
        }
    }

    // Méthode réutilisable pour le style d'en-tête
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
    
    // Methode pour exporter le rapport (Acceuil)
    @FXML
    private void exporterRapportPDF() {
        try {
            // 1. Création d'un nouveau document PDF vide
            PDDocument document = new PDDocument();

            // 2. Création d'une page au format A4
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page); // On ajoute la page au document

            // 3. Création du "stylo" qui va nous permettre d'écrire et dessiner sur la page
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // ==================== TITRE DU RAPPORT ====================
            // Définit la police Helvetica Gras, taille 24
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 24);

            // Commence à écrire du texte
            contentStream.beginText();

            // Positionne le curseur : 50px du bord gauche, 750px du bas (près du haut de la page A4)
            contentStream.newLineAtOffset(50, 750);

            // Écrit le titre principal
            contentStream.showText("Rapport Statistiques - VitaDesk");

            // Termine l'écriture du texte
            contentStream.endText();

            // ==================== DATE DU RAPPORT ====================
            // Police Helvetica normale, taille 14
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 14);
            contentStream.beginText();

            // Un peu plus bas que le titre (720px du bas)
            contentStream.newLineAtOffset(50, 720);

            // Affiche la date actuelle au format jour/mois/année
            contentStream.showText("Généré le : " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            contentStream.endText();

            // ==================== CAPTURE DES GRAPHIQUES JAVA FX ====================
            // Prend une "photo" (snapshot) du graphique en barres (barRdvSemaine)
            WritableImage barImage = barRdvSemaine.snapshot(new SnapshotParameters(), null);

            // Prend une "photo" du graphique en ligne (lineRdv30Jours)
            WritableImage lineImage = lineRdv30Jours.snapshot(new SnapshotParameters(), null);

            // Prépare deux flux mémoire pour stocker les images en format PNG
            ByteArrayOutputStream baosBar = new ByteArrayOutputStream();
            ByteArrayOutputStream baosLine = new ByteArrayOutputStream();

            // Convertit l'image JavaFX du graphique en barres en bytes PNG
            ImageIO.write(SwingFXUtils.fromFXImage(barImage, null), "png", baosBar);

            // Même chose pour le graphique en ligne
            ImageIO.write(SwingFXUtils.fromFXImage(lineImage, null), "png", baosLine);

            // Transforme les bytes PNG en objets image utilisables dans le PDF
            PDImageXObject pdBarImage = PDImageXObject.createFromByteArray(document, baosBar.toByteArray(), "bar.png");
            PDImageXObject pdLineImage = PDImageXObject.createFromByteArray(document, baosLine.toByteArray(), "line.png");

            // ==================== INSÉRER LES GRAPHIQUES DANS LE PDF ====================
            // Dessine le graphique en barres :
            // Position : x=50, y=400 (du bas), largeur=500px, hauteur=400px
            contentStream.drawImage(pdBarImage, 50, 400, 400, 300);

            // Dessine le graphique en ligne plus bas sur la page
            contentStream.drawImage(pdLineImage, 50, 50, 400, 300);

            // Ferme le "stylo" → obligatoire pour valider tout le contenu
            contentStream.close();

            // ==================== SAUVEGARDE DU FICHIER ====================
            // Ouvre une boîte de dialogue pour choisir où sauvegarder le PDF
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter le rapport");
            
            // Nom par défaut avec la date du jour
            fileChooser.setInitialFileName("Rapport_Statistiques_" + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + ".pdf");
            
            // Filtre pour n'afficher que les fichiers PDF
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            
            // Affiche la boîte de dialogue et récupère le fichier choisi
            File file = fileChooser.showSaveDialog(tabPaneMainSec.getScene().getWindow());

            // Si l'utilisateur a choisi un emplacement
            if (file != null) {
                // Sauvegarde le document PDF sur le disque
                document.save(file);
                
                // Ferme proprement le document (libère la mémoire)
                document.close();

                // Message de succès
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setContentText("Rapport exporté avec succès !\nFichier : " + file.getName());
                alert.show();
            }

        } catch (Exception e) {
            // En cas d'erreur (police, image, sauvegarde, etc.)
            e.printStackTrace(); // Affiche l'erreur dans la console pour debug
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Erreur lors de l'export PDF : " + e.getMessage());
            alert.show();
        }
    }
    
}
