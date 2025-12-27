package ma.vitadesk.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.DayOfWeek;
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
import ma.vitadesk.dao.*;
import ma.vitadesk.model.Medecin;
import ma.vitadesk.model.Patient;
import ma.vitadesk.model.RendezVous;
import ma.vitadesk.model.Secretaire;
import ma.vitadesk.model.Utilisateur;
import ma.vitadesk.service.FiltreService;
import ma.vitadesk.util.SessionLockManager;

/**
 * Contrôleur principal pour le dashboard de la secrétaire
 * VERSION INTÉGRÉE MYSQL - PARTIE 1
 * 
 * Cette partie contient :
 * - Déclarations des attributs et @FXML
 * - Méthode initialize()
 * - Méthodes de chargement depuis BDD
 * - Configuration des TableViews
 */
public class SecretaireDashboardController implements Initializable {
	
	// ==================== ATTRIBUTS FXML - LABELS UTILISATEUR ====================
	@FXML private Label lblSecretaireNom;       // Nom dans la sidebar
	@FXML private Label lblSecretaireBienvennue; // "Bienvenue X" dans le header
	@FXML private Label lblConsultationsCeMois;
	@FXML private Label lblNouveauxPatients;
	@FXML private Label lblRevenuJour;
	@FXML private Label lblRdvPrevus;

	// DAO pour charger la secrétaire
	private ISecretaireDAO secretaireDAO;
	
	@FXML private Button btnAccueilSec;
	@FXML private Button btnDocteursSec;
	@FXML private Button btnPatientsSec;
	@FXML private Button btnRendezVousSec;
	@FXML private Button btnDeconnexionSec;

	// ==================== ATTRIBUTS FXML - TABS ====================
	@FXML private TabPane tabPaneMainSec;
	@FXML private Tab tabAccueilSec;
	@FXML private Tab tabDocteursSec;
	@FXML private Tab tabPatientsSec;
	@FXML private Tab tabRendezVousSec;
	
	// ==================== ATTRIBUTS FXML - ACCUEIL (Graphiques) ====================
	@FXML private BarChart<String, Number> barRdvSemaine;
	@FXML private LineChart<String, Number> lineRdv30Jours;
	
	// ==================== ATTRIBUTS FXML - PATIENTS ====================
	@FXML private TableView<Patient> tablePatients;
	@FXML private TableColumn<Patient, String> colNumSocial;
	@FXML private TableColumn<Patient, String> colNom;
	@FXML private TableColumn<Patient, String> colPrenom;
	@FXML private TableColumn<Patient, String> colDateNaissance;
	@FXML private TableColumn<Patient, String> colTelephone;
	@FXML private TableColumn<Patient, String> colCin;
	@FXML private TableColumn<Patient, String> colSexe;
	@FXML private TableColumn<Patient, String> colAdresse;
	@FXML private Button btnSupprimerPatient;
	@FXML private Button btnNouveauPatient;
	@FXML private TextField txtRecherchePatient;

	// ==================== ATTRIBUTS FXML - DOCTEURS ====================
	@FXML private TableView<Medecin> tableDocteurs;
	@FXML private TableColumn<Medecin, String> colID;
	@FXML private TableColumn<Medecin, String> colDocNom;
	@FXML private TableColumn<Medecin, String> colDocPrenom;
	@FXML private TableColumn<Medecin, String> colDocSpecialite;
	@FXML private TableColumn<Medecin, String> colDocTelephone;
	@FXML private TableColumn<Medecin, String> colDocEmail;
	@FXML private Button btnSupprimerDocteur;
	@FXML private Button btnNouveauDocteur;
	@FXML private TextField txtRechercheMedecin;

	// ==================== ATTRIBUTS FXML - PLANNING (toutes les cellules) ====================
	@FXML private DatePicker datePickerPlanning;
	@FXML private Label selectedDatePlanning;
	
	// VBox pour chaque créneau horaire (Lundi à Samedi, 8h à 17h)
	@FXML private VBox cellLundi0800, cellMardi0800, cellMercredi0800, cellJeudi0800, cellVendredi0800, cellSamedi0800;
	@FXML private VBox cellLundi0900, cellMardi0900, cellMercredi0900, cellJeudi0900, cellVendredi0900, cellSamedi0900;
	@FXML private VBox cellLundi1000, cellMardi1000, cellMercredi1000, cellJeudi1000, cellVendredi1000, cellSamedi1000;
	@FXML private VBox cellLundi1100, cellMardi1100, cellMercredi1100, cellJeudi1100, cellVendredi1100, cellSamedi1100;
	@FXML private VBox cellLundi1200, cellMardi1200, cellMercredi1200, cellJeudi1200, cellVendredi1200, cellSamedi1200;
	@FXML private VBox cellLundi1300, cellMardi1300, cellMercredi1300, cellJeudi1300, cellVendredi1300, cellSamedi1300;
	@FXML private VBox cellLundi1400, cellMardi1400, cellMercredi1400, cellJeudi1400, cellVendredi1400, cellSamedi1400;
	@FXML private VBox cellLundi1500, cellMardi1500, cellMercredi1500, cellJeudi1500, cellVendredi1500, cellSamedi1500;
	@FXML private VBox cellLundi1600, cellMardi1600, cellMercredi1600, cellJeudi1600, cellVendredi1600, cellSamedi1600;
	@FXML private VBox cellLundi1700, cellMardi1700, cellMercredi1700, cellJeudi1700, cellVendredi1700, cellSamedi1700;

	// ==================== ATTRIBUTS DAO (INTÉGRATION MYSQL) ====================
	private IPatientDAO patientDAO;
	private IMedecinDAO medecinDAO;
	private IRendezVousDAO rendezVousDAO;
	private IConsultationDAO consultationDAO;
	
	// ==================== DONNÉES ====================
	private ObservableList<Patient> patients = FXCollections.observableArrayList();
	private ObservableList<Medecin> medecins = FXCollections.observableArrayList();
	private LocalDate selectedDate = LocalDate.now();
	private VBox[][] planningCells = new VBox[10][6]; // 10 heures x 6 jours
	
	// ==================== INITIALIZE ====================
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
	    // 🆕 INITIALISER LES DAO
	    patientDAO = new PatientDAOImpl();
	    medecinDAO = new MedecinDAOImpl();
	    rendezVousDAO = new RendezVousDAOImpl();
	    consultationDAO = new ConsultationDAOImpl();
	    secretaireDAO = new SecretaireDAOImpl(); // ← NE PAS OUBLIER
	    
	    // Configuration des graphiques
	    configurerGraphiques();
	    
	    // Configuration de la page Patients
	    configurerPagePatients();
	    
	    // Configuration de la page Docteurs
	    configurerPageDocteurs();
	    
	    // Configuration du planning
	    configurerPlanning();
	    
	    // 🆕 SÉLECTIONNER L'ONGLET ACCUEIL PAR DÉFAUT
	    tabPaneMainSec.getSelectionModel().select(tabAccueilSec);
	    highlightButton(btnAccueilSec);
	    
	    chargerStatistiquesAccueil();
	    
	    System.out.println("✅ SecretaireDashboardController initialisé");
	}
	
	/**
	 * Méthode appelée depuis LoginController pour passer les infos de l'utilisateur
	 */
	/**
	 * 🆕 Méthode appelée depuis LoginController pour passer les infos de l'utilisateur
	 * C'EST ICI QU'ON CHARGE TOUTES LES DONNÉES DEPUIS LA BDD
	 */
	public void setUtilisateur(Utilisateur utilisateur) {
	    if (utilisateur == null) {
	        System.err.println("❌ setUtilisateur appelé avec utilisateur NULL");
	        return;
	    }
	    
	    System.out.println("✅ setUtilisateur appelé pour utilisateur ID=" + utilisateur.getId());
	    
	    // 🆕 RÉCUPÉRER LES INFOS COMPLÈTES DE LA SECRÉTAIRE DEPUIS LA BDD
	    Secretaire secretaire = secretaireDAO.getSecretaireById(utilisateur.getId());
	    
	    if (secretaire != null) {
	        System.out.println("✅ Secrétaire chargée: " + secretaire.getPrenom() + " " + secretaire.getNom());
	        
	        // 🆕 AFFICHER LE NOM DANS L'INTERFACE
	        // Dans la sidebar
	        if (lblSecretaireNom != null) {
	            lblSecretaireNom.setText(secretaire.getPrenom() + " " + secretaire.getNom());
	        } else {
	            System.err.println("⚠️ lblSecretaireNom est NULL");
	        }
	        
	        // Dans le header
	        if (lblSecretaireBienvennue != null) {
	            lblSecretaireBienvennue.setText("Bienvenue " + secretaire.getPrenom() + " " + secretaire.getNom());
	        } else {
	            System.err.println("⚠️ lblSecretaireBienvennue est NULL");
	        }
	        
	        // 🆕 CHARGER TOUTES LES DONNÉES DEPUIS LA BDD
	        chargerPatients();
	        chargerMedecins();
	        chargerRendezVous(selectedDate);
	        
	        System.out.println("✅ Toutes les données chargées");
	        
	    } else {
	        System.err.println("❌ ERREUR: Impossible de charger la secrétaire ID=" + utilisateur.getId());
	        Alert alert = new Alert(Alert.AlertType.ERROR);
	        alert.setTitle("Erreur");
	        alert.setContentText("Impossible de charger les informations de l'utilisateur");
	        alert.show();
	    }
    }
		
	// ==================== CONFIGURATION GRAPHIQUES ====================
	
	/**
	 * Configure les graphiques de la page d'accueil
	 * 🆕 TODO: Charger les vraies données depuis la BDD avec consultationDAO
	 */
	private void configurerGraphiques() {
		// Graphique en barres - RDV de la semaine
		XYChart.Series<String, Number> seriesBar = new XYChart.Series<>();
		seriesBar.setName("Rendez-vous");
		seriesBar.getData().add(new XYChart.Data<>("Lundi", 25));
		seriesBar.getData().add(new XYChart.Data<>("Mardi", 30));
		seriesBar.getData().add(new XYChart.Data<>("Mercredi", 22));
		seriesBar.getData().add(new XYChart.Data<>("Jeudi", 28));
		seriesBar.getData().add(new XYChart.Data<>("Vendredi", 35));
		seriesBar.getData().add(new XYChart.Data<>("Samedi", 18));
		
		barRdvSemaine.getData().clear();
		barRdvSemaine.getData().add(seriesBar);
		
		// Graphique linéaire - 30 derniers jours (garder les données fictives)
		XYChart.Series<String, Number> seriesLine = new XYChart.Series<>();
		seriesLine.setName("Consultations");
		// [Garder toutes les données du graphique linéaire...]
		seriesLine.getData().add(new XYChart.Data<>("15/11", 28));
		seriesLine.getData().add(new XYChart.Data<>("16/11", 32));
		// ... etc (garder tout comme avant)
		
		lineRdv30Jours.getData().clear();
		lineRdv30Jours.getData().add(seriesLine);
	}
	
	// ==================== CONFIGURATION PAGE PATIENTS ====================
	
	/**
	 * Configure la TableView des patients et charge les données depuis la BDD
	 */
	private void configurerPagePatients() {
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
			return new SimpleStringProperty(patient.getAdresse());
		});
		
		// Rendre la TableView éditable
		tablePatients.setEditable(true);
		tablePatients.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		// Configuration de l'édition inline avec SAUVEGARDE EN BDD
		configurerEditionPatients();
		
		// Colonne Actions (Dossier Médical + Supprimer)
		ajouterColonneActionsPatients();
		
		// 🆕 CHARGER LES PATIENTS DEPUIS LA BDD
		chargerPatients();
	}
	
	/**
	 * Configure l'édition inline des patients avec sauvegarde automatique en BDD
	 */
	private void configurerEditionPatients() {
		// Pour chaque colonne, autoriser l'édition
		colNom.setCellFactory(TextFieldTableCell.forTableColumn());
		colPrenom.setCellFactory(TextFieldTableCell.forTableColumn());
		colDateNaissance.setCellFactory(TextFieldTableCell.forTableColumn());
		colTelephone.setCellFactory(TextFieldTableCell.forTableColumn());
		colCin.setCellFactory(TextFieldTableCell.forTableColumn());
		colSexe.setCellFactory(TextFieldTableCell.forTableColumn());
		colAdresse.setCellFactory(TextFieldTableCell.forTableColumn());
		
		// 🆕 Quand on valide l'édition → SAUVEGARDER EN BDD
		colNom.setOnEditCommit(event -> {
			Patient patient = event.getRowValue();
			patient.setNom(event.getNewValue());
			sauvegarderPatient(patient);
		});
		
		colPrenom.setOnEditCommit(event -> {
			Patient patient = event.getRowValue();
			patient.setPrenom(event.getNewValue());
			sauvegarderPatient(patient);
		});
		
		colDateNaissance.setOnEditCommit(event -> {
			Patient patient = event.getRowValue();
			patient.setDateNaissance(event.getNewValue());
			sauvegarderPatient(patient);
		});
		
		colTelephone.setOnEditCommit(event -> {
			Patient patient = event.getRowValue();
			patient.setTelephone(event.getNewValue());
			sauvegarderPatient(patient);
		});
		
		colCin.setOnEditCommit(event -> {
			Patient patient = event.getRowValue();
			patient.setCin(event.getNewValue());
			sauvegarderPatient(patient);
		});
		
		colSexe.setOnEditCommit(event -> {
			Patient patient = event.getRowValue();
			patient.setSexe(event.getNewValue());
			sauvegarderPatient(patient);
		});
		
		colAdresse.setOnEditCommit(event -> {
			Patient patient = event.getRowValue();
			patient.setAdresse(event.getNewValue());
			sauvegarderPatient(patient);
		});
	}
	
	/**
	 * Ajoute la colonne Actions (Dossier Médical + Supprimer)
	 */
	private void ajouterColonneActionsPatients() {
		TableColumn<Patient, Void> colActions = new TableColumn<>("Actions");
		colActions.setPrefWidth(200);
		colActions.setSortable(false);
		colActions.setStyle("-fx-alignment: CENTER;");

		colActions.setCellFactory(param -> new TableCell<Patient, Void>() {
			private final Button btnDossier = new Button("Dossier Médical");
			private final Button btnSupprimerLigne = new Button("Supprimer");
			private final HBox hbox = new HBox(10, btnDossier, btnSupprimerLigne);

			{
				// Style bouton Dossier Médical
				btnDossier.setStyle(
					"-fx-background-color: #FF9000; " +
					"-fx-text-fill: white; " +
					"-fx-cursor: HAND; " +
					"-fx-font-weight: bold; " +
					"-fx-padding: 8 16 8 16; " +
					"-fx-background-radius: 6;"
				);

				// Style bouton Supprimer
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

				// Action : supprimer un patient
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

		tablePatients.getColumns().add(colActions);
	}
	
	// ==================== CHARGEMENT DONNÉES DEPUIS BDD ====================
	
	/**
	 * 🆕 Charge tous les patients depuis la BDD
	 * Appelée au démarrage et après chaque modification
	 */
	public void chargerPatients() {
		patients.clear();
		List<Patient> listePatients = patientDAO.getAllPatients();
		patients.addAll(listePatients);
		tablePatients.setItems(patients);
		
		System.out.println("✅ " + patients.size() + " patients chargés depuis la BDD");
	}
	
	/**
	 * 🆕 Charge tous les médecins depuis la BDD
	 */
	public void chargerMedecins() {
		medecins.clear();
		List<Medecin> listeMedecins = medecinDAO.getAllMedecins();
		medecins.addAll(listeMedecins);
		tableDocteurs.setItems(medecins);
		
		System.out.println("✅ " + medecins.size() + " médecins chargés depuis la BDD");
	}
	
	/**
	 * 🆕 Charge les rendez-vous d'une date depuis la BDD
	 */
	private void chargerRendezVous(LocalDate date) {
		// Récupérer les RDV de la date depuis la BDD
		List<RendezVous> rdvs = rendezVousDAO.getRendezVousByDate(date);
		
		// Vider toutes les cellules du planning
		viderToutesCellules();
		
		// Remplir le planning avec les RDV de la BDD
		for (RendezVous rdv : rdvs) {
			ajouterRDVAuPlanning(rdv);
		}
		
		System.out.println("✅ " + rdvs.size() + " RDV chargés pour le " + date);
	}
	
	// ==================== SAUVEGARDE EN BDD ====================
	
	/**
	 * 🆕 Sauvegarde un patient modifié dans la BDD
	 * Appelée après chaque édition inline
	 */
	private void sauvegarderPatient(Patient patient) {
		boolean success = patientDAO.modifierPatient(patient);
		
		if (!success) {
			// En cas d'erreur, annuler la modification
			tablePatients.refresh();
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Erreur");
			alert.setContentText("Impossible de sauvegarder les modifications en BDD");
			alert.show();
		} else {
			System.out.println("✅ Patient modifié : " + patient.getNom());
		}
	}
	
	/**
	 * 🆕 Sauvegarde un médecin modifié dans la BDD
	 */
	private void sauvegarderMedecin(Medecin medecin) {
		boolean success = medecinDAO.modifierMedecin(medecin);
		
		if (!success) {
			tableDocteurs.refresh();
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Erreur");
			alert.setContentText("Impossible de sauvegarder les modifications en BDD");
			alert.show();
		} else {
			System.out.println("✅ Médecin modifié : " + medecin.getNom());
		}
	}
	
	/**
	 * Configure la TableView des médecins
	 */
	private void configurerPageDocteurs() {
	    // === COLONNE # (NUMÉRO DE LIGNE) ===
		colID.setCellValueFactory(cellData -> {
	        int index = tableDocteurs.getItems().indexOf(cellData.getValue()) + 1;
	        return new javafx.beans.property.SimpleStringProperty(String.valueOf(index));
	    });
	    
		// Liaison des colonnes
		colDocNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
		colDocPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
		colDocSpecialite.setCellValueFactory(new PropertyValueFactory<>("specialite"));
		colDocTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
		colDocEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
		
		// Rendre la TableView éditable
		tableDocteurs.setEditable(true);
		tableDocteurs.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		// Configuration de l'édition inline avec SAUVEGARDE EN BDD
		configurerEditionMedecins();
		
		// 🆕 CHARGER LES MÉDECINS DEPUIS LA BDD
		chargerMedecins();
	}
	
	/**
	 * Configure l'édition inline des médecins avec sauvegarde automatique en BDD
	 */
	private void configurerEditionMedecins() {
		colDocNom.setCellFactory(TextFieldTableCell.forTableColumn());
		colDocPrenom.setCellFactory(TextFieldTableCell.forTableColumn());
		colDocSpecialite.setCellFactory(TextFieldTableCell.forTableColumn());
		colDocTelephone.setCellFactory(TextFieldTableCell.forTableColumn());
		colDocEmail.setCellFactory(TextFieldTableCell.forTableColumn());
		
		// 🆕 Quand on valide l'édition → SAUVEGARDER EN BDD
		colDocNom.setOnEditCommit(event -> {
			Medecin medecin = event.getRowValue();
			medecin.setNom(event.getNewValue());
			sauvegarderMedecin(medecin);
		});
		
		colDocPrenom.setOnEditCommit(event -> {
			Medecin medecin = event.getRowValue();
			medecin.setPrenom(event.getNewValue());
			sauvegarderMedecin(medecin);
		});
		
		colDocSpecialite.setOnEditCommit(event -> {
			Medecin medecin = event.getRowValue();
			medecin.setSpecialite(event.getNewValue());
			sauvegarderMedecin(medecin);
		});
		
		colDocTelephone.setOnEditCommit(event -> {
			Medecin medecin = event.getRowValue();
			medecin.setTelephone(event.getNewValue());
			sauvegarderMedecin(medecin);
		});
		
		colDocEmail.setOnEditCommit(event -> {
			Medecin medecin = event.getRowValue();
			medecin.setEmail(event.getNewValue());
			sauvegarderMedecin(medecin);
		});
	}
	
	// ==================== CONFIGURATION PLANNING ====================
	
	/**
	 * Configure le planning (GridPane avec cellules)
	 */
	private void configurerPlanning() {
	    // Initialiser le tableau des cellules
	    initializePlanningCells();
	    
	    // DatePicker : changer de date
	    datePickerPlanning.setValue(selectedDate);
	    datePickerPlanning.setOnAction(event -> {
	        selectedDate = datePickerPlanning.getValue();
	        if (selectedDate != null) {
	            // 🆕 METTRE À JOUR LE LABEL
	            selectedDatePlanning.setText("Planning du " + 
	                selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
	            
	            // 🆕 CHARGER LES RDV DE LA NOUVELLE DATE DEPUIS LA BDD
	            chargerRendezVous(selectedDate);
	        }
	    });
	    
	    // INITIALISER LE LABEL AU DÉMARRAGE
	    selectedDatePlanning.setText("Planning du " + 
	        selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
	}
	
	/**
	 * Initialise le tableau des cellules du planning
	 */
	private void initializePlanningCells() {
		// Lundi = 0, Mardi = 1, ..., Samedi = 5
		// 8h = 0, 9h = 1, ..., 17h = 9
		planningCells[0][0] = cellLundi0800;  planningCells[0][1] = cellMardi0800;  planningCells[0][2] = cellMercredi0800;
		planningCells[0][3] = cellJeudi0800;  planningCells[0][4] = cellVendredi0800; planningCells[0][5] = cellSamedi0800;
		
		planningCells[1][0] = cellLundi0900;  planningCells[1][1] = cellMardi0900;  planningCells[1][2] = cellMercredi0900;
		planningCells[1][3] = cellJeudi0900;  planningCells[1][4] = cellVendredi0900; planningCells[1][5] = cellSamedi0900;
		
		planningCells[2][0] = cellLundi1000;  planningCells[2][1] = cellMardi1000;  planningCells[2][2] = cellMercredi1000;
		planningCells[2][3] = cellJeudi1000;  planningCells[2][4] = cellVendredi1000; planningCells[2][5] = cellSamedi1000;
		
		planningCells[3][0] = cellLundi1100;  planningCells[3][1] = cellMardi1100;  planningCells[3][2] = cellMercredi1100;
		planningCells[3][3] = cellJeudi1100;  planningCells[3][4] = cellVendredi1100; planningCells[3][5] = cellSamedi1100;
		
		planningCells[4][0] = cellLundi1200;  planningCells[4][1] = cellMardi1200;  planningCells[4][2] = cellMercredi1200;
		planningCells[4][3] = cellJeudi1200;  planningCells[4][4] = cellVendredi1200; planningCells[4][5] = cellSamedi1200;
		
		planningCells[5][0] = cellLundi1300;  planningCells[5][1] = cellMardi1300;  planningCells[5][2] = cellMercredi1300;
		planningCells[5][3] = cellJeudi1300;  planningCells[5][4] = cellVendredi1300; planningCells[5][5] = cellSamedi1300;
		
		planningCells[6][0] = cellLundi1400;  planningCells[6][1] = cellMardi1400;  planningCells[6][2] = cellMercredi1400;
		planningCells[6][3] = cellJeudi1400;  planningCells[6][4] = cellVendredi1400; planningCells[6][5] = cellSamedi1400;
		
		planningCells[7][0] = cellLundi1500;  planningCells[7][1] = cellMardi1500;  planningCells[7][2] = cellMercredi1500;
		planningCells[7][3] = cellJeudi1500;  planningCells[7][4] = cellVendredi1500; planningCells[7][5] = cellSamedi1500;
		
		planningCells[8][0] = cellLundi1600;  planningCells[8][1] = cellMardi1600;  planningCells[8][2] = cellMercredi1600;
		planningCells[8][3] = cellJeudi1600;  planningCells[8][4] = cellVendredi1600; planningCells[8][5] = cellSamedi1600;
		
		planningCells[9][0] = cellLundi1700;  planningCells[9][1] = cellMardi1700;  planningCells[9][2] = cellMercredi1700;
		planningCells[9][3] = cellJeudi1700;  planningCells[9][4] = cellVendredi1700; planningCells[9][5] = cellSamedi1700;
	}
	
	/**
	 * Vide toutes les cellules du planning
	 */
	private void viderToutesCellules() {
		for (int h = 0; h < 10; h++) {
			for (int j = 0; j < 6; j++) {
				if (planningCells[h][j] != null) {
					planningCells[h][j].getChildren().clear();
					planningCells[h][j].setStyle("-fx-background-color: #EFF2F5; -fx-background-radius: 10; -fx-border-color: black; -fx-border-radius: 10; -fx-border-width: 0.1px;");
				}
			}
		}
	}
	
	/**
	 * Ajoute un RDV dans le planning (cellule correspondante)
	 */
	private void ajouterRDVAuPlanning(RendezVous rdv) {
		// Trouver la cellule correspondante
		DayOfWeek jour = rdv.getDate().getDayOfWeek();
		int jourIndex = jour.getValue() - 1; // MONDAY=1 → index 0
		if (jourIndex == 6) return; // Dimanche = pas affiché
		
		int heure = rdv.getHeure().getHour();
		int heureIndex = heure - 8; // 8h → index 0
		
		if (heureIndex < 0 || heureIndex > 9 || jourIndex < 0 || jourIndex > 5) {
			return; // Hors planning
		}
		
		VBox cell = planningCells[heureIndex][jourIndex];
		if (cell == null) return;
		
		// Créer le label pour le RDV
		Label lblRDV = new Label(rdv.getAffichageCellule());
		lblRDV.setStyle(
			"-fx-background-color: " + rdv.getStatut().getCouleur() + "; " +
			"-fx-text-fill: white; " +
			"-fx-padding: 5; " +
			"-fx-background-radius: 5; " +
			"-fx-font-size: 11px; " +
			"-fx-cursor: hand;"
		);
		lblRDV.setMaxWidth(Double.MAX_VALUE);
		lblRDV.setWrapText(true);
		lblRDV.setTextOverrun(OverrunStyle.ELLIPSIS);
		
		// Tooltip pour voir le texte complet
		Tooltip tooltip = new Tooltip(rdv.getAffichageCellule());
		Tooltip.install(lblRDV, tooltip);
		
		// Clic pour modifier le statut
		lblRDV.setOnMouseClicked(event -> ouvrirModifierStatutRDV(rdv));
		
		// Ajouter à la cellule
		cell.getChildren().add(lblRDV);
		VBox.setMargin(lblRDV, new Insets(2));
	}
	
	// ==================== ACTIONS MENU SIDEBAR ====================
	
	@FXML
	private void handleMenuClick(ActionEvent event) {
	    Button clicked = (Button) event.getSource();
	    resetSidebarStyle();

	    if (clicked == btnAccueilSec) {
	        tabPaneMainSec.getSelectionModel().select(tabAccueilSec);
	        highlightButton(btnAccueilSec);
	    } else if (clicked == btnPatientsSec) {
	        tabPaneMainSec.getSelectionModel().select(tabPatientsSec);
	        highlightButton(btnPatientsSec);
	    } else if (clicked == btnDocteursSec) {
	        tabPaneMainSec.getSelectionModel().select(tabDocteursSec);
	        highlightButton(btnDocteursSec);
	    } else if (clicked == btnRendezVousSec) {
	        tabPaneMainSec.getSelectionModel().select(tabRendezVousSec);
	        highlightButton(btnRendezVousSec);
	    } else if (clicked == btnDeconnexionSec) {
	        deconnecter();
	    }
	}

	private void resetSidebarStyle() {
	    btnAccueilSec.setStyle("-fx-background-color: #EFF2F5; -fx-border-width: 0.1px; -fx-border-color: black;");
	    btnPatientsSec.setStyle("-fx-background-color: #EFF2F5; -fx-border-width: 0.1px; -fx-border-color: black;");
	    btnDocteursSec.setStyle("-fx-background-color: #EFF2F5; -fx-border-width: 0.1px; -fx-border-color: black;");
	    btnRendezVousSec.setStyle("-fx-background-color: #EFF2F5; -fx-border-width: 0.1px; -fx-border-color: black;");
	    btnDeconnexionSec.setStyle("-fx-background-color: #EFF2F5; -fx-border-width: 0.1px; -fx-border-color: black;");
	}

	private void highlightButton(Button btn) {
	    resetSidebarStyle();
	    btn.setStyle("-fx-background-color: #4D93FF; -fx-text-fill: white; -fx-border-width: 0.1px; -fx-border-color: black;");
	}
	
	/**
	 * Déconnexion : libère le lock et retourne au login
	 */
	private void deconnecter() {
		Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
		confirmation.setTitle("Déconnexion");
		confirmation.setHeaderText("Voulez-vous vraiment vous déconnecter ?");
		
		if (confirmation.showAndWait().get() == ButtonType.OK) {
			// 🆕 LIBÉRER LE LOCK (Thread)
			SessionLockManager.releaseLock();
			
			// Fermer le dashboard et retourner au login
			try {
				Stage currentStage = (Stage) btnDeconnexionSec.getScene().getWindow();
				currentStage.close();
				
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/login.fxml"));
				Parent root = loader.load();
				
				Stage loginStage = new Stage();
				loginStage.setTitle("VitaDesk - Connexion");
				loginStage.setScene(new Scene(root));
				loginStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
				loginStage.show();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	// ==================== ACTIONS PATIENTS ====================
	
	/**
	 * Ouvre la fenêtre pour ajouter un patient
	 */
	@FXML
	private void ajouterNouveauPatient() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/ajouter_patient.fxml"));
			Parent root = loader.load();
			
			AjouterPatientController controller = loader.getController();
			controller.setDashboardController(this);
			
			Stage stage = new Stage();
			stage.setTitle("Nouveau Patient");
			stage.setScene(new Scene(root));
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
			stage.show();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Ouvre le dossier médical d'un patient
	 */
	private void ouvrirDossierMedical(Patient patient) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/dossier_medical.fxml"));
			Parent root = loader.load();
			
			DossierMedicalController controller = loader.getController();
			controller.afficherDossier(patient, null); // null = mode secrétaire
			
			Stage stage = new Stage();
			stage.setTitle("Dossier Médical - " + patient.getPrenom() + " " + patient.getNom());
			stage.setScene(new Scene(root));
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
			stage.show();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Supprime UN patient (depuis le bouton de la ligne)
	 * 🆕 AVEC SUPPRESSION EN BDD
	 */
	private void supprimerUnPatient(Patient patient) {
		Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
		confirmation.setTitle("Supprimer le patient");
		confirmation.setHeaderText("Voulez-vous vraiment supprimer ce patient ?");
		confirmation.setContentText(patient.getPrenom() + " " + patient.getNom());
		
		if (confirmation.showAndWait().get() == ButtonType.OK) {
			// 🆕 SUPPRIMER DE LA BDD
			boolean success = patientDAO.supprimerPatient(patient.getNumSocial());
			
			if (success) {
				patients.remove(patient);
				System.out.println("✅ Patient supprimé : " + patient.getNom());
			} else {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("Erreur");
				alert.setContentText("Impossible de supprimer le patient de la BDD");
				alert.show();
			}
		}
	}
	
	/**
	 * Supprime PLUSIEURS patients sélectionnés
	 * 🆕 AVEC SUPPRESSION EN BDD
	 */
	@FXML
	private void supprimerPatientsSelectionnes() {
		ObservableList<Patient> selectionnes = tablePatients.getSelectionModel().getSelectedItems();
		
		if (selectionnes.isEmpty()) {
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.setTitle("Aucune sélection");
			alert.setContentText("Veuillez sélectionner au moins un patient à supprimer");
			alert.show();
			return;
		}
		
		Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
		confirmation.setTitle("Supprimer les patients");
		confirmation.setHeaderText("Voulez-vous vraiment supprimer " + selectionnes.size() + " patient(s) ?");
		
		if (confirmation.showAndWait().get() == ButtonType.OK) {
			// 🆕 SUPPRIMER DE LA BDD (créer une copie pour éviter ConcurrentModificationException)
			List<Patient> copie = new ArrayList<>(selectionnes);
			int compteur = 0;
			
			for (Patient p : copie) {
				boolean success = patientDAO.supprimerPatient(p.getNumSocial());
				if (success) {
					patients.remove(p);
					compteur++;
				}
			}
			
			System.out.println("✅ " + compteur + " patient(s) supprimé(s)");
		}
	}
	
	/**
	 * 🆕 Recherche patients avec LAMBDA (FiltreService)
	 */
	@FXML
	private void rechercherPatient() {
		String recherche = txtRecherchePatient.getText();
		
		if (recherche == null || recherche.trim().isEmpty()) {
			// Si vide, recharger tous les patients
			chargerPatients();
		} else {
			// 🆕 UTILISER LE SERVICE AVEC LAMBDA
			List<Patient> resultats = FiltreService.rechercherPatients(
				new ArrayList<>(patients), 
				recherche
			);
			
			tablePatients.setItems(FXCollections.observableArrayList(resultats));
			System.out.println("🔍 " + resultats.size() + " résultat(s) trouvé(s)");
		}
	}
	
	// ==================== ACTIONS MÉDECINS ====================
	
	/**
	 * Ouvre la fenêtre pour ajouter un médecin
	 */
	@FXML
	private void ajouterNouveauDocteur() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/ajouter_docteur.fxml"));
			Parent root = loader.load();
			
			AjouterDocteurController controller = loader.getController();
			controller.setDashboardController(this);
			
			Stage stage = new Stage();
			stage.setTitle("Nouveau Médecin");
			stage.setScene(new Scene(root));
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
			stage.show();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Supprime les médecins sélectionnés
	 * 🆕 AVEC SUPPRESSION EN BDD
	 */
	@FXML
	private void supprimerMedecinsSelectionnes() {
		ObservableList<Medecin> selectionnes = tableDocteurs.getSelectionModel().getSelectedItems();
		
		if (selectionnes.isEmpty()) {
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.setTitle("Aucune sélection");
			alert.setContentText("Veuillez sélectionner au moins un médecin à supprimer");
			alert.show();
			return;
		}
		
		Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
		confirmation.setTitle("Supprimer les médecins");
		confirmation.setHeaderText("Voulez-vous vraiment supprimer " + selectionnes.size() + " médecin(s) ?");
		
		if (confirmation.showAndWait().get() == ButtonType.OK) {
			List<Medecin> copie = new ArrayList<>(selectionnes);
			int compteur = 0;
			
			for (Medecin m : copie) {
				// 🆕 SUPPRIMER DE LA BDD
				boolean success = medecinDAO.supprimerMedecin(m.getIdMedecin());
				if (success) {
					medecins.remove(m);
					compteur++;
				}
			}
			
			System.out.println("✅ " + compteur + " médecin(s) supprimé(s)");
		}
	}
	
	/**
	 * 🆕 Recherche médecins avec LAMBDA
	 */
	@FXML
	private void rechercherMedecin() {
		String recherche = txtRechercheMedecin.getText();
		
		if (recherche == null || recherche.trim().isEmpty()) {
			chargerMedecins();
		} else {
			List<Medecin> resultats = FiltreService.rechercherMedecins(
				new ArrayList<>(medecins), 
				recherche
			);
			
			tableDocteurs.setItems(FXCollections.observableArrayList(resultats));
			System.out.println("🔍 " + resultats.size() + " résultat(s) trouvé(s)");
		}
	}
	
	// ==================== ACTIONS PLANNING ====================
	
	/**
	 * Ouvre la fenêtre pour ajouter un RDV
	 */
	@FXML
	private void ajouterNouveauRDV() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/ajouter_rdv.fxml"));
			Parent root = loader.load();
			
			AjouterRDVController controller = loader.getController();
			controller.setDashboardController(this);
			
			Stage stage = new Stage();
			stage.setTitle("Nouveau Rendez-vous");
			stage.setScene(new Scene(root));
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
			stage.show();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Ouvre la fenêtre pour modifier le statut d'un RDV
	 */
	private void ouvrirModifierStatutRDV(RendezVous rdv) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/modifier_statut_rdv.fxml"));
			Parent root = loader.load();
			
			ModifierStatutRDVController controller = loader.getController();
			controller.setData(rdv, this);
			
			Stage stage = new Stage();
			stage.setTitle("Modifier Rendez-vous");
			stage.setScene(new Scene(root));
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
			stage.show();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 🆕 Ajoute un RDV (appelée depuis AjouterRDVController)
	 * SAUVEGARDE EN BDD
	 */
	public void ajouterRDV(RendezVous rdv) {
		boolean success = rendezVousDAO.ajouterRendezVous(rdv);
		
		if (success) {
			rafraichirPlanning();
			System.out.println("✅ RDV ajouté");
		} else {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Erreur");
			alert.setContentText("Impossible d'ajouter le RDV en BDD");
			alert.show();
		}
	}
	
	/**
	 * 🆕 Supprime un RDV (appelée depuis ModifierStatutRDVController)
	 * SUPPRESSION EN BDD
	 */
	public void supprimerRDV(RendezVous rdv) {
		boolean success = rendezVousDAO.supprimerRendezVous(rdv);
		
		if (success) {
			rafraichirPlanning();
			System.out.println("✅ RDV supprimé");
		} else {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Erreur");
			alert.setContentText("Impossible de supprimer le RDV de la BDD");
			alert.show();
		}
	}
	
	/**
	 * Rafraîchit le planning (recharge depuis la BDD)
	 */
	public void rafraichirPlanning() {
		chargerRendezVous(selectedDate);
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
    
 // ==================== MÉTHODES D'EXPORT ====================
	
 	/**
 	 * Exporte le rapport d'accueil en PDF
 	 * Appelée par le bouton "Exporter Rapport" dans l'onglet Accueil (ligne 372)
 	 */
 	@FXML
 	private void exporterRapportPDF() {
 		try {
 			FileChooser fileChooser = new FileChooser();
 			fileChooser.setTitle("Enregistrer le rapport d'accueil");
 			fileChooser.setInitialFileName("rapport_accueil_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf");
 			fileChooser.getExtensionFilters().add(
 				new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
 			);
 			
 			File file = fileChooser.showSaveDialog(btnAccueilSec.getScene().getWindow());
 			if (file != null) {
 				genererRapportAccueilPDF(file);
 				
 				Alert alert = new Alert(Alert.AlertType.INFORMATION);
 				alert.setTitle("Succès");
 				alert.setHeaderText(null);
 				alert.setContentText("Rapport exporté avec succès !");
 				alert.show();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			Alert alert = new Alert(Alert.AlertType.ERROR);
 			alert.setTitle("Erreur");
 			alert.setHeaderText("Erreur lors de l'export");
 			alert.setContentText("Impossible d'exporter le rapport : " + e.getMessage());
 			alert.show();
 		}
 	}
 	
 	/**
 	 * Exporte la liste des patients en Excel
 	 * Appelée par le bouton "Exporter Patients" dans l'onglet Patients (ligne 483)
 	 */
 	@FXML
 	private void exporterPatientsExcel() {
 		try {
 			FileChooser fileChooser = new FileChooser();
 			fileChooser.setTitle("Enregistrer la liste des patients");
 			fileChooser.setInitialFileName("patients_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".xlsx");
 			fileChooser.getExtensionFilters().add(
 				new FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx")
 			);
 			
 			File file = fileChooser.showSaveDialog(btnPatientsSec.getScene().getWindow());
 			if (file != null) {
 				genererExcelPatients(file);
 				
 				Alert alert = new Alert(Alert.AlertType.INFORMATION);
 				alert.setTitle("Succès");
 				alert.setHeaderText(null);
 				alert.setContentText("Liste des patients exportée avec succès !");
 				alert.show();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			Alert alert = new Alert(Alert.AlertType.ERROR);
 			alert.setTitle("Erreur");
 			alert.setHeaderText("Erreur lors de l'export");
 			alert.setContentText("Impossible d'exporter la liste : " + e.getMessage());
 			alert.show();
 		}
 	}
 	
 	/**
 	 * Exporte les rendez-vous en Excel
 	 * Appelée par le bouton "Exporter RDV" dans l'onglet Rendez-vous (ligne 1286)
 	 */
 	@FXML
 	private void exporterRDVExcel() {
 		try {
 			FileChooser fileChooser = new FileChooser();
 			fileChooser.setTitle("Enregistrer les rendez-vous");
 			fileChooser.setInitialFileName("rdv_" + selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".xlsx");
 			fileChooser.getExtensionFilters().add(
 				new FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx")
 			);
 			
 			File file = fileChooser.showSaveDialog(btnRendezVousSec.getScene().getWindow());
 			if (file != null) {
 				genererExcelRDV(file);
 				
 				Alert alert = new Alert(Alert.AlertType.INFORMATION);
 				alert.setTitle("Succès");
 				alert.setHeaderText(null);
 				alert.setContentText("Rendez-vous exportés avec succès !");
 				alert.show();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			Alert alert = new Alert(Alert.AlertType.ERROR);
 			alert.setTitle("Erreur");
 			alert.setHeaderText("Erreur lors de l'export");
 			alert.setContentText("Impossible d'exporter les RDV : " + e.getMessage());
 			alert.show();
 		}
 	}
 	
 	// ==================== MÉTHODES PRIVÉES DE GÉNÉRATION ====================
 	
 	/**
 	 * Génère le PDF du rapport d'accueil avec les statistiques et graphiques
 	 */
 	private void genererRapportAccueilPDF(File file) throws Exception {
 		PDDocument document = new PDDocument();
 		PDPage page = new PDPage(PDRectangle.A4);
 		document.addPage(page);
 		
 		PDPageContentStream contentStream = new PDPageContentStream(document, page);
 		
 		// === EN-TÊTE ===
 		contentStream.beginText();
 		contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 22);
 		contentStream.newLineAtOffset(50, 750);
 		contentStream.showText("VitaDesk - Rapport d'Accueil");
 		contentStream.endText();
 		
 		// === LIGNE DE SÉPARATION ===
 		contentStream.setLineWidth(2);
 		contentStream.moveTo(50, 740);
 		contentStream.lineTo(550, 740);
 		contentStream.stroke();
 		
 		// === DATE ===
 		contentStream.beginText();
 		contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
 		contentStream.newLineAtOffset(50, 710);
 		contentStream.showText("Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
 		contentStream.endText();
 		
 		// === STATISTIQUES GLOBALES ===
 		contentStream.beginText();
 		contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
 		contentStream.newLineAtOffset(50, 670);
 		contentStream.showText("Statistiques Globales");
 		contentStream.endText();
 		
 		int yPosition = 640;
 		
 		// Nombre de patients
 		contentStream.beginText();
 		contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
 		contentStream.newLineAtOffset(70, yPosition);
 		contentStream.showText("Nombre total de patients : " + patients.size());
 		contentStream.endText();
 		yPosition -= 25;
 		
 		// Nombre de médecins
 		contentStream.beginText();
 		contentStream.newLineAtOffset(70, yPosition);
 		contentStream.showText("Nombre total de medecins : " + medecins.size());
 		contentStream.endText();
 		yPosition -= 25;
 		
 		// Nombre de RDV aujourd'hui
 		List<RendezVous> rdvAujourdhui = rendezVousDAO.getRendezVousByDate(LocalDate.now());
 		contentStream.beginText();
 		contentStream.newLineAtOffset(70, yPosition);
 		contentStream.showText("Rendez-vous aujourd'hui : " + rdvAujourdhui.size());
 		contentStream.endText();
 		yPosition -= 25;
 		
 		// RDV prévus
 		long rdvPrevus = rdvAujourdhui.stream()
 			.filter(rdv -> rdv.getStatut() == RendezVous.Statut.PREVU)
 			.count();
 		contentStream.beginText();
 		contentStream.newLineAtOffset(70, yPosition);
 		contentStream.showText("RDV prevus : " + rdvPrevus);
 		contentStream.endText();
 		yPosition -= 25;
 		
 		// RDV effectués
 		long rdvEffectues = rdvAujourdhui.stream()
 			.filter(rdv -> rdv.getStatut() == RendezVous.Statut.EFFECTUE)
 			.count();
 		contentStream.beginText();
 		contentStream.newLineAtOffset(70, yPosition);
 		contentStream.showText("RDV effectues : " + rdvEffectues);
 		contentStream.endText();
 		yPosition -= 25;
 		
 		// RDV annulés
 		long rdvAnnules = rdvAujourdhui.stream()
 			.filter(rdv -> rdv.getStatut() == RendezVous.Statut.ANNULE)
 			.count();
 		contentStream.beginText();
 		contentStream.newLineAtOffset(70, yPosition);
 		contentStream.showText("RDV annules : " + rdvAnnules);
 		contentStream.endText();
 		yPosition -= 40;
 		
 		// === STATISTIQUES PAR SPÉCIALITÉ ===
 		contentStream.beginText();
 		contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
 		contentStream.newLineAtOffset(50, yPosition);
 		contentStream.showText("Repartition par Specialite");
 		contentStream.endText();
 		yPosition -= 30;
 		
 		// Compter par spécialité
 		java.util.Map<String, Long> parSpecialite = medecins.stream()
 			.collect(java.util.stream.Collectors.groupingBy(
 				Medecin::getSpecialite, 
 				java.util.stream.Collectors.counting()
 			));
 		
 		contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
 		for (java.util.Map.Entry<String, Long> entry : parSpecialite.entrySet()) {
 			contentStream.beginText();
 			contentStream.newLineAtOffset(70, yPosition);
 			contentStream.showText(entry.getKey() + " : " + entry.getValue() + " medecin(s)");
 			contentStream.endText();
 			yPosition -= 20;
 		}
 		
 		// === PIED DE PAGE ===
 		contentStream.beginText();
 		contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 10);
 		contentStream.newLineAtOffset(50, 50);
 		contentStream.showText("Genere par VitaDesk le " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " - Page 1/1");
 		contentStream.endText();
 		
 		contentStream.close();
 		document.save(file);
 		document.close();
 		
 		System.out.println("✅ Rapport PDF généré : " + file.getAbsolutePath());
 	}
 	
 	/**
 	 * Génère le fichier Excel des patients
 	 */
 	private void genererExcelPatients(File file) throws Exception {
 		Workbook workbook = new XSSFWorkbook();
 		Sheet sheet = workbook.createSheet("Patients");
 		
 		// === STYLE EN-TÊTE ===
 		CellStyle headerStyle = workbook.createCellStyle();
 		Font headerFont = workbook.createFont();
 		headerFont.setBold(true);
 		headerFont.setFontHeightInPoints((short) 12);
 		headerFont.setColor(IndexedColors.WHITE.getIndex());
 		headerStyle.setFont(headerFont);
 		headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
 		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
 		headerStyle.setAlignment(HorizontalAlignment.CENTER);
 		
 		// === EN-TÊTE ===
 		Row headerRow = sheet.createRow(0);
 		String[] columns = {"N° Sécurité Sociale", "Nom", "Prénom", "Date Naissance", "Téléphone", "CIN", "Sexe", "Adresse"};
 		
 		for (int i = 0; i < columns.length; i++) {
 			Cell cell = headerRow.createCell(i);
 			cell.setCellValue(columns[i]);
 			cell.setCellStyle(headerStyle);
 			sheet.setColumnWidth(i, 5000); // Largeur automatique
 		}
 		
 		// === DONNÉES ===
 		int rowNum = 1;
 		for (Patient patient : patients) {
 			Row row = sheet.createRow(rowNum++);
 			row.createCell(0).setCellValue(patient.getNumSocial());
 			row.createCell(1).setCellValue(patient.getNom());
 			row.createCell(2).setCellValue(patient.getPrenom());
 			row.createCell(3).setCellValue(patient.getDateNaissance());
 			row.createCell(4).setCellValue(patient.getTelephone());
 			row.createCell(5).setCellValue(patient.getCin());
 			row.createCell(6).setCellValue(patient.getSexe());
 			row.createCell(7).setCellValue(patient.getAdresse());
 		}
 		
 		// === AUTO-SIZE COLONNES ===
 		for (int i = 0; i < columns.length; i++) {
 			sheet.autoSizeColumn(i);
 		}
 		
 		// === SAUVEGARDER ===
 		FileOutputStream outputStream = new FileOutputStream(file);
 		workbook.write(outputStream);
 		workbook.close();
 		outputStream.close();
 		
 		System.out.println("✅ Excel patients généré : " + file.getAbsolutePath());
 	}
 	
 	/**
 	 * Génère le fichier Excel des rendez-vous
 	 */
 	private void genererExcelRDV(File file) throws Exception {
 		Workbook workbook = new XSSFWorkbook();
 		Sheet sheet = workbook.createSheet("Rendez-vous");
 		
 		// === STYLE EN-TÊTE ===
 		CellStyle headerStyle = workbook.createCellStyle();
 		Font headerFont = workbook.createFont();
 		headerFont.setBold(true);
 		headerFont.setFontHeightInPoints((short) 12);
 		headerFont.setColor(IndexedColors.WHITE.getIndex());
 		headerStyle.setFont(headerFont);
 		headerStyle.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
 		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
 		headerStyle.setAlignment(HorizontalAlignment.CENTER);
 		
 		// === EN-TÊTE ===
 		Row headerRow = sheet.createRow(0);
 		String[] columns = {"Date", "Heure", "Patient", "Médecin", "Spécialité", "Statut", "Motif"};
 		
 		for (int i = 0; i < columns.length; i++) {
 			Cell cell = headerRow.createCell(i);
 			cell.setCellValue(columns[i]);
 			cell.setCellStyle(headerStyle);
 		}
 		
 		// === RÉCUPÉRER LES RDV DE LA DATE SÉLECTIONNÉE ===
 		List<RendezVous> rdvs = rendezVousDAO.getRendezVousByDate(selectedDate);
 		
 		// === DONNÉES ===
 		int rowNum = 1;
 		for (RendezVous rdv : rdvs) {
 			Row row = sheet.createRow(rowNum++);
 			row.createCell(0).setCellValue(rdv.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
 			row.createCell(1).setCellValue(rdv.getHeure().format(DateTimeFormatter.ofPattern("HH:mm")));
 			row.createCell(2).setCellValue(rdv.getPatient().getPrenom() + " " + rdv.getPatient().getNom());
 			row.createCell(3).setCellValue("Dr. " + rdv.getDocteur().getPrenom() + " " + rdv.getDocteur().getNom());
 			row.createCell(4).setCellValue(rdv.getDocteur().getSpecialite());
 			row.createCell(5).setCellValue(rdv.getStatut().getLabel());
 			row.createCell(6).setCellValue(rdv.getMotif() != null ? rdv.getMotif() : "-");
 		}
 		
 		// === AUTO-SIZE COLONNES ===
 		for (int i = 0; i < columns.length; i++) {
 			sheet.autoSizeColumn(i);
 		}
 		
 		// === SAUVEGARDER ===
 		FileOutputStream outputStream = new FileOutputStream(file);
 		workbook.write(outputStream);
 		workbook.close();
 		outputStream.close();
 		
 		System.out.println("✅ Excel RDV généré : " + file.getAbsolutePath());
 	}
    
    // ==================== AUTRES MÉTHODES APPELÉES PAR LE FXML ====================
    @FXML
    private void ouvrirAjouterPatient() {
        ajouterNouveauPatient();
    }

    @FXML
    private void ouvrirAjouterDocteur() {
        ajouterNouveauDocteur();
    }

    @FXML
    private void ouvrirAjouterRDV() {
        ajouterNouveauRDV();
    }

    @FXML
    private void supprimerDocteurs() {
        supprimerMedecinsSelectionnes();
    }
    
    /**
     * 🆕 Charge les statistiques de l'accueil depuis la BDD
     */
    private void chargerStatistiquesAccueil() {
        // Nombre de RDV prévus aujourd'hui
        List<RendezVous> rdvAujourdhui = rendezVousDAO.getRendezVousByDate(LocalDate.now());
        long rdvPrevus = rdvAujourdhui.stream()
            .filter(rdv -> rdv.getStatut() == RendezVous.Statut.PREVU)
            .count();
        
        if (lblRdvPrevus != null) {
            lblRdvPrevus.setText(String.valueOf(rdvPrevus));
        }
        
        // Nombre de consultations réalisées ce mois
        // Vous devrez ajouter une méthode dans ConsultationDAO pour ça
        int consultationsCeMois = consultationDAO.getConsultationsCeMois();
        if (lblConsultationsCeMois != null) {
            lblConsultationsCeMois.setText(String.valueOf(consultationsCeMois));
        }
        
        // Nombre de nouveaux patients ce mois
        int nouveauxPatients = patientDAO.getNouveauxPatientsCeMois();
        if (lblNouveauxPatients != null) {
            lblNouveauxPatients.setText(String.valueOf(nouveauxPatients));
        }
        
        // Revenu estimé du jour
        double revenuJour = calculerRevenuJour(rdvAujourdhui);
        if (lblRevenuJour != null) {
            lblRevenuJour.setText(String.format("%.0f", revenuJour));
        }
    }

    /**
     * Calcule le revenu estimé du jour (300 MAD par RDV effectué)
     */
    private double calculerRevenuJour(List<RendezVous> rdvJour) {
        long rdvEffectues = rdvJour.stream()
            .filter(rdv -> rdv.getStatut() == RendezVous.Statut.EFFECTUE)
            .count();
        return rdvEffectues * 300.0; // Prix moyen consultation
    }

}
