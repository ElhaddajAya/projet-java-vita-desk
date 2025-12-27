package ma.vitadesk.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ma.vitadesk.dao.*;
import ma.vitadesk.model.Consultation;
import ma.vitadesk.model.ConsultationDuJour;
import ma.vitadesk.model.Medecin;
import ma.vitadesk.model.Patient;
import ma.vitadesk.model.RendezVous;
import ma.vitadesk.model.Utilisateur;
import ma.vitadesk.service.FiltreService;
import ma.vitadesk.util.SessionLockManager;

/**
 * Contrôleur principal pour le dashboard du médecin
 * VERSION INTÉGRÉE MYSQL - PARTIE 1
 * 
 * Cette partie contient :
 * - Déclarations des attributs et @FXML
 * - Méthode initialize()
 * - Méthodes de chargement depuis BDD
 * - Configuration des TableViews
 */
public class MedecinDashboardController implements Initializable {

	// ==================== ATTRIBUTS FXML - SIDEBAR ====================
    @FXML private Button btnAccueil;
    @FXML private Button btnPlanning;
    @FXML private Button btnHistorique;
    @FXML private Button btnConsultationsJour;
    @FXML private Button btnDeconnexion;
    @FXML private Label lblMedecinNom;
    @FXML private Label lblSpecialite;
    @FXML private Label lblMedecinBienvennue;
    @FXML private Label lblRdvAujourdhui;
    @FXML private Label lblTotalConsultations;
    @FXML private Label lblTotalPatients;
    @FXML private Label lblRevenusMois;

    // ==================== ATTRIBUTS FXML - TABS ====================
    @FXML private TabPane tabPaneMain;
    @FXML private Tab tabAccueil;
    @FXML private Tab tabMonPlanning;
    @FXML private Tab tabHistoriquePatients;
    @FXML private Tab tabConsultationsDuJour;

    // ==================== ATTRIBUTS FXML - ACCUEIL ====================
    @FXML private BarChart<String, Number> barRdvSemaineMed;

    // ==================== ATTRIBUTS FXML - HISTORIQUE PATIENTS ====================
    @FXML private TableView<Patient> tablePatientsMed;
    @FXML private TableColumn<Patient, String> colNumSocial;
    @FXML private TableColumn<Patient, String> colNom;
    @FXML private TableColumn<Patient, String> colPrenom;
    @FXML private TableColumn<Patient, String> colDateNaissance;
    @FXML private TableColumn<Patient, String> colSexe;
    @FXML private TableColumn<Patient, String> colDerniereConsultation;
    @FXML private TableColumn<Patient, Void> colActionDossier;
    @FXML private TextField txtRecherchePatientMed;
    
    // ==================== ATTRIBUTS FXML - CONSULTATIONS DU JOUR ====================
    @FXML private TableView<ConsultationDuJour> tableConsultationsJour;
    @FXML private TableColumn<ConsultationDuJour, String> colNum;
    @FXML private TableColumn<ConsultationDuJour, String> colHeure;
    @FXML private TableColumn<ConsultationDuJour, String> colPatient;
    @FXML private TableColumn<ConsultationDuJour, String> colDateNaissanceConsult;
    @FXML private TableColumn<ConsultationDuJour, String> colDerniereVisite;
    @FXML private TableColumn<ConsultationDuJour, Void> colActionConsultJour;
    @FXML private Label todayConsultationsDateMed;

    // ==================== ATTRIBUTS FXML - PLANNING (toutes les cellules) ====================
    @FXML private DatePicker datePickerPlanningMed;
    @FXML private Label selectedDatePlanningMed;
    
    // VBox pour chaque créneau horaire (Lundi à Samedi, 8h à 17h)
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

    // ==================== ATTRIBUTS DAO (INTÉGRATION MYSQL) ====================
    private IPatientDAO patientDAO;
    private IMedecinDAO medecinDAO;
    private IRendezVousDAO rendezVousDAO;
    private IConsultationDAO consultationDAO;
    
    // ==================== DONNÉES ====================
    private Medecin medecinConnecte; // Le médecin actuellement connecté
    private ObservableList<Patient> listePatients = FXCollections.observableArrayList();
    private ObservableList<RendezVous> listeRDV = FXCollections.observableArrayList();
    private ObservableList<ConsultationDuJour> listeConsultationsDuJour = FXCollections.observableArrayList();
    private LocalDate selectedDate = LocalDate.now();
    private VBox[][] planningCellsMed = new VBox[10][6]; // 10 heures x 6 jours

    // ==================== INITIALIZE ====================
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        // 🆕 INITIALISER LES DAO
        patientDAO = new PatientDAOImpl();
        medecinDAO = new MedecinDAOImpl();
        rendezVousDAO = new RendezVousDAOImpl();
        consultationDAO = new ConsultationDAOImpl();
        
        // Configuration du planning
        configurerPlanning();
        
        // Configuration de la page Historique Patients
        configurerHistoriquePatients();
        
        // Configuration de la page Consultations du Jour
        configurerConsultationsDuJour();
        
		// Sélectionner l'onglet Accueil par défaut
		tabPaneMain.getSelectionModel().select(tabAccueil);
		highlightButton(btnAccueil);
    }
    
    /**
     * Méthode appelée depuis LoginController pour passer les infos de l'utilisateur
     * C'EST ICI QU'ON CHARGE TOUTES LES DONNÉES DEPUIS LA BDD
     */
    public void setUtilisateur(Utilisateur utilisateur) {
        if (utilisateur == null) {
            System.err.println("❌ setUtilisateur appelé avec utilisateur NULL");
            return;
        }
        
        System.out.println("✅ setUtilisateur appelé pour utilisateur ID=" + utilisateur.getId());
        
        // 🆕 RÉCUPÉRER LES INFOS COMPLÈTES DU MÉDECIN DEPUIS LA BDD
        medecinConnecte = medecinDAO.getMedecinById(utilisateur.getId());
        
        if (medecinConnecte != null) {
            System.out.println("✅ Médecin chargé: Dr. " + medecinConnecte.getPrenom() + " " + medecinConnecte.getNom());
            System.out.println("   Spécialité: " + medecinConnecte.getSpecialite());
            System.out.println("   ID: " + medecinConnecte.getIdMedecin());
            
            // 🆕 AFFICHER LE NOM DANS L'INTERFACE
            // Dans la sidebar
            if (lblMedecinNom != null) {
                lblMedecinNom.setText("Dr. " + medecinConnecte.getPrenom() + " " + medecinConnecte.getNom());
            } else {
                System.err.println("⚠️ lblMedecinNom est NULL");
            }
            
            // Spécialité dans la sidebar
            if (lblSpecialite != null) {
                lblSpecialite.setText(medecinConnecte.getSpecialite());
            } else {
                System.err.println("⚠️ lblSpecialite est NULL");
            }
            
            // Dans le header
            if (lblMedecinBienvennue != null) {
                lblMedecinBienvennue.setText("Bienvenue Dr. " + medecinConnecte.getNom());
            } else {
                System.err.println("⚠️ lblMedecinBienvennue est NULL");
            }
            
            // 🆕 CHARGER TOUTES LES DONNÉES DEPUIS LA BDD
            System.out.println("📊 Chargement des patients...");
            chargerPatients();
            
            System.out.println("📊 Chargement des RDV...");
            chargerRendezVous(selectedDate);
            
            System.out.println("📊 Chargement des consultations du jour...");
            chargerConsultationsDuJour();
            
            System.out.println("📊 Chargement du graphique...");
            chargerGraphiqueSemaine();
            
            System.out.println("✅ Toutes les données chargées");
            
            System.out.println("📊 Chargement statistiques...");            
            chargerStatistiquesAccueil();
            
            System.out.println("✅ Toutes les données chargées");            
        } else {
            System.err.println("❌ ERREUR: Impossible de charger le médecin ID=" + utilisateur.getId());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Impossible de charger les informations du médecin");
            alert.show();
        }
    }
    
    // ==================== CONFIGURATION HISTORIQUE PATIENTS ====================
    
    /**
     * Configure la TableView de l'historique des patients
     */
    private void configurerHistoriquePatients() {
        System.out.println("🔧 Configuration table patients...");
        
        // Configurer les colonnes
        colNumSocial.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("numSocial"));
        colNom.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("prenom"));
        colDateNaissance.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("dateNaissance"));
        colSexe.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("sexe"));
        
        // Colonne "Dernière Consultation"
        colDerniereConsultation.setCellValueFactory(cellData -> {
            Patient patient = cellData.getValue();
            List<Consultation> consultations = consultationDAO.getConsultationsByPatient(patient.getNumSocial());
            
            if (consultations.isEmpty()) {
                return new javafx.beans.property.SimpleStringProperty("Aucune");
            } else {
                Consultation derniere = consultations.get(consultations.size() - 1);
                return new javafx.beans.property.SimpleStringProperty(derniere.getDate());
            }
        });
        
        // Colonne "Action" - Bouton "Voir Dossier"
        colActionDossier.setCellFactory(param -> new javafx.scene.control.TableCell<>() {
            private final javafx.scene.control.Button btnVoir = new javafx.scene.control.Button("Dossier Médical");
            
            {
            		btnVoir.setStyle(
    					"-fx-background-color: #FF9000; " +
    					"-fx-text-fill: white; " +
    					"-fx-cursor: HAND; " +
    					"-fx-font-weight: bold; " +
    					"-fx-padding: 8 16 8 16; " +
    					"-fx-background-radius: 6;"
    				);
                
                btnVoir.setOnAction(event -> {
                    Patient patient = getTableView().getItems().get(getIndex());
                    ouvrirDossierMedical(patient);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnVoir);
            }
        });
        
        // Lier au TableView
        tablePatientsMed.setItems(listePatients);
        
        System.out.println("✅ Table patients configurée");
    }
    
    // ==================== CONFIGURATION CONSULTATIONS DU JOUR ====================
    
    /**
     * Configure la TableView des consultations du jour
     */
    private void configurerConsultationsDuJour() {
        colNum.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colHeure.setCellValueFactory(new PropertyValueFactory<>("heure"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patient"));
        colDateNaissanceConsult.setCellValueFactory(new PropertyValueFactory<>("dateNaissance"));
        colDerniereVisite.setCellValueFactory(new PropertyValueFactory<>("derniereVisite"));
        
        // Bouton "Commencer" pour chaque consultation
        colActionConsultJour.setCellFactory(param -> new TableCell<ConsultationDuJour, Void>() {
            private final Button btnCommencer = new Button("Commencer");

            {
                btnCommencer.setStyle(
                    "-fx-background-color: #FF9000; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 8 20; " +
                    "-fx-background-radius: 6;" + 
                    "-fx-cursor: HAND;"
                );
                btnCommencer.setOnAction(e -> {
                    ConsultationDuJour consultDuJour = getTableView().getItems().get(getIndex());
                    ouvrirConsultation(consultDuJour.getRendezVous());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnCommencer);
            }
        });
        
        // 🆕 METTRE À JOUR LE LABEL DE DATE
        if (todayConsultationsDateMed != null) {
        		todayConsultationsDateMed.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
        
        // Les données seront chargées dans setUtilisateur()
    }
    
    // ==================== CHARGEMENT DONNÉES DEPUIS BDD ====================
    
    /**
     * Charge UNIQUEMENT les patients ayant consulté avec ce médecin
     */
    private void chargerPatients() {
        if (medecinConnecte == null) {
            System.err.println("❌ medecinConnecte NULL");
            return;
        }
        
        System.out.println("📊 Chargement patients (médecin ID=" + medecinConnecte.getIdMedecin() + ")");
        
        // 1. Récupérer numéros de sécu
        List<String> numSecuConsultes = consultationDAO.getNumSecuPatientsConsultes(
            medecinConnecte.getIdMedecin()
        );
        
        System.out.println("   → DAO: " + numSecuConsultes.size() + " numéro(s)");
        if (!numSecuConsultes.isEmpty()) {
            System.out.println("   → " + numSecuConsultes);
        }
        
        // 2. Charger tous les patients
        List<Patient> tousPatients = patientDAO.getAllPatients();
        
        // 3. Filtrer
        listePatients.clear();
        tousPatients.stream()
            .filter(p -> numSecuConsultes.contains(p.getNumSocial()))
            .forEach(p -> {
                listePatients.add(p);
                System.out.println("      ✓ " + p.getPrenom() + " " + p.getNom());
            });
        
        System.out.println("✅ " + listePatients.size() + " patient(s) dans l'historique");
        
        // FORCER RAFRAÎCHISSEMENT
        tablePatientsMed.refresh();
    }
    
    /**
     * 🆕 Vérifie si un patient a consulté avec le médecin connecté
     * UTILISE LAMBDA (concept du cours)
     */
    private boolean aConsulteAvecMedecin(Patient patient) {
        List<Consultation> consultations = consultationDAO.getConsultationsByPatient(
            patient.getNumSocial()
        );
        
        String nomMedecin = "Dr. " + medecinConnecte.getPrenom() + " " + medecinConnecte.getNom();
        
        // 🆕 Lambda : vérifie si au moins une consultation est avec ce médecin
        return consultations.stream()
            .anyMatch(c -> c.getMedecin().equals(nomMedecin));
    }
    
    /**
     * Charge les RDV du médecin connecté pour une date donnée
     * NETTOIE les cellules avant de charger
     */
    private void chargerRendezVous(LocalDate date) {
        System.out.println("📅 Chargement RDV pour la semaine du " + date);
        
        if (medecinConnecte == null) {
            System.err.println("❌ medecinConnecte NULL");
            return;
        }
        
        viderToutesLesCellules();
        
		
		// 🔄 VIDER listeRDV pour éviter les doublons
		listeRDV.clear();
        
        // 🔄 VIDER listeRDV pour éviter les doublons
        LocalDate lundi = date.with(DayOfWeek.MONDAY);
        LocalDate samedi = lundi.plusDays(5);
        
        System.out.println("   📆 Semaine: du " + lundi + " au " + samedi);
        System.out.println("   👨‍⚕️ Médecin: Dr. " + medecinConnecte.getNom() + " (ID=" + medecinConnecte.getIdMedecin() + ")");
        
        // CHARGER TOUS LES JOURS + FILTRER PAR MÉDECIN
        LocalDate jour = lundi;
        int totalRdv = 0;
        
        while (!jour.isAfter(samedi)) {
            List<RendezVous> rdvDuJour = rendezVousDAO.getRendezVousByDate(jour);
			System.out.println("   🔍 " + jour + " : " + rdvDuJour.size() + " RDV trouvé(s)");
            
            for (RendezVous rdv : rdvDuJour) {
                // FILTRER PAR MÉDECIN !
                if (rdv.getMedecin().getIdMedecin() == medecinConnecte.getIdMedecin()) {
                    ajouterRDVAuPlanning(rdv);
                    listeRDV.add(rdv); // 🔄 Ajouter à la liste pour les consultations du jour
                    totalRdv++;
                }
            }
            
            jour = jour.plusDays(1);
        }
        
        System.out.println("✅ " + totalRdv + " RDV chargés pour Dr. " + medecinConnecte.getNom());
    }
    
    /**
     * 🆕 Charge les consultations du jour (RDV avec statut PREVU)
     */
    public void chargerConsultationsDuJour() {
        listeConsultationsDuJour.clear();
        
        if (medecinConnecte == null) return;
        
        LocalDate aujourdhui = LocalDate.now();
        
        // 🆕 Filtrer avec LAMBDA : seulement aujourd'hui + statut PREVU
        List<RendezVous> rdvPrevus = listeRDV.stream()
            .filter(rdv -> rdv.getDate().equals(aujourdhui))
            .filter(rdv -> rdv.getStatut() == RendezVous.Statut.PREVU)
            .collect(Collectors.toList());
        
        // Convertir en ConsultationDuJour
        int numero = 1;
        for (RendezVous rdv : rdvPrevus) {
            listeConsultationsDuJour.add(new ConsultationDuJour(String.valueOf(numero++), rdv));
        }
        
        tableConsultationsJour.setItems(listeConsultationsDuJour);
        
        System.out.println("✅ " + listeConsultationsDuJour.size() + " consultation(s) prévue(s) aujourd'hui");
    }
    
    /**
     * 🆕 Charge le graphique de la semaine depuis les stats BDD
     */
    public void chargerGraphiqueSemaine() {
        if (medecinConnecte == null) return;
        
        System.out.println("📊 Chargement graphique semaine...");
        
        int[] consultationsParJour = consultationDAO.getConsultationsParJourSemaine(
            medecinConnecte.getIdMedecin()
        );
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Consultations");
        String[] jours = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
        for (int i = 0; i < 7; i++) {
            series.getData().add(new XYChart.Data<>(jours[i], consultationsParJour[i]));
        }
        
        barRdvSemaineMed.getData().clear();
        barRdvSemaineMed.getData().add(series);
        
        System.out.println("✅ Graphique médecin chargé");
    }
    
	// ==================== CONFIGURATION PLANNING ====================
	
	/**
	 * Configure le planning du médecin
	 */
	private void configurerPlanning() {
		// Initialiser le tableau des cellules
		initializePlanningCells();
		
		// 🔄 CONFIGURER LE LISTENER AVANT setValue()
		datePickerPlanningMed.valueProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null && (oldValue == null || !newValue.equals(oldValue))) {
				selectedDate = newValue;
				System.out.println("🗓️ DATE CHANGÉE (Médecin) : " + oldValue + " → " + newValue);
				
				// Calculer la semaine complète
				LocalDate lundi = selectedDate.with(java.time.DayOfWeek.MONDAY);
				LocalDate samedi = lundi.plusDays(5);
				
				// Mettre à jour le label avec la SEMAINE
				selectedDatePlanningMed.setText(
					lundi.format(DateTimeFormatter.ofPattern("dd/MM")) + 
					" au " + 
					samedi.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
				);
				
				// Recharger le planning pour cette semaine
				System.out.println("📅 Rechargement planning semaine du " + lundi + " au " + samedi);
				rafraichirPlanning();
			}
		});
		
		// Initialiser la valeur APRÈS le listener
		datePickerPlanningMed.setValue(selectedDate);
		
		// Afficher la semaine complète dans le label au démarrage
		LocalDate lundi = selectedDate.with(java.time.DayOfWeek.MONDAY);
		LocalDate samedi = lundi.plusDays(5);
		selectedDatePlanningMed.setText(
			lundi.format(DateTimeFormatter.ofPattern("dd/MM")) + 
			" au " + 
			samedi.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
		);
	}
	
	/**
	 * Initialise le tableau des cellules du planning
	 */
	private void initializePlanningCells() {
		// Lundi = 0, Mardi = 1, ..., Samedi = 5
		// 8h = 0, 9h = 1, ..., 17h = 9
		planningCellsMed[0][0] = cellLundi0800Med;  planningCellsMed[0][1] = cellMardi0800Med;  planningCellsMed[0][2] = cellMercredi0800Med;
		planningCellsMed[0][3] = cellJeudi0800Med;  planningCellsMed[0][4] = cellVendredi0800Med; planningCellsMed[0][5] = cellSamedi0800Med;
		
		planningCellsMed[1][0] = cellLundi0900Med;  planningCellsMed[1][1] = cellMardi0900Med;  planningCellsMed[1][2] = cellMercredi0900Med;
		planningCellsMed[1][3] = cellJeudi0900Med;  planningCellsMed[1][4] = cellVendredi0900Med; planningCellsMed[1][5] = cellSamedi0900Med;
		
		planningCellsMed[2][0] = cellLundi1000Med;  planningCellsMed[2][1] = cellMardi1000Med;  planningCellsMed[2][2] = cellMercredi1000Med;
		planningCellsMed[2][3] = cellJeudi1000Med;  planningCellsMed[2][4] = cellVendredi1000Med; planningCellsMed[2][5] = cellSamedi1000Med;
		
		planningCellsMed[3][0] = cellLundi1100Med;  planningCellsMed[3][1] = cellMardi1100Med;  planningCellsMed[3][2] = cellMercredi1100Med;
		planningCellsMed[3][3] = cellJeudi1100Med;  planningCellsMed[3][4] = cellVendredi1100Med; planningCellsMed[3][5] = cellSamedi1100Med;
		
		planningCellsMed[4][0] = cellLundi1200Med;  planningCellsMed[4][1] = cellMardi1200Med;  planningCellsMed[4][2] = cellMercredi1200Med;
		planningCellsMed[4][3] = cellJeudi1200Med;  planningCellsMed[4][4] = cellVendredi1200Med; planningCellsMed[4][5] = cellSamedi1200Med;
		
		planningCellsMed[5][0] = cellLundi1300Med;  planningCellsMed[5][1] = cellMardi1300Med;  planningCellsMed[5][2] = cellMercredi1300Med;
		planningCellsMed[5][3] = cellJeudi1300Med;  planningCellsMed[5][4] = cellVendredi1300Med; planningCellsMed[5][5] = cellSamedi1300Med;
		
		planningCellsMed[6][0] = cellLundi1400Med;  planningCellsMed[6][1] = cellMardi1400Med;  planningCellsMed[6][2] = cellMercredi1400Med;
		planningCellsMed[6][3] = cellJeudi1400Med;  planningCellsMed[6][4] = cellVendredi1400Med; planningCellsMed[6][5] = cellSamedi1400Med;
		
		planningCellsMed[7][0] = cellLundi1500Med;  planningCellsMed[7][1] = cellMardi1500Med;  planningCellsMed[7][2] = cellMercredi1500Med;
		planningCellsMed[7][3] = cellJeudi1500Med;  planningCellsMed[7][4] = cellVendredi1500Med; planningCellsMed[7][5] = cellSamedi1500Med;
		
		planningCellsMed[8][0] = cellLundi1600Med;  planningCellsMed[8][1] = cellMardi1600Med;  planningCellsMed[8][2] = cellMercredi1600Med;
		planningCellsMed[8][3] = cellJeudi1600Med;  planningCellsMed[8][4] = cellVendredi1600Med; planningCellsMed[8][5] = cellSamedi1600Med;
		
		planningCellsMed[9][0] = cellLundi1700Med;  planningCellsMed[9][1] = cellMardi1700Med;  planningCellsMed[9][2] = cellMercredi1700Med;
		planningCellsMed[9][3] = cellJeudi1700Med;  planningCellsMed[9][4] = cellVendredi1700Med; planningCellsMed[9][5] = cellSamedi1700Med;
	}
	
	/**
	 * Vide toutes les cellules du planning
	 */
	private void viderToutesLesCellules() {
		for (int h = 0; h < 10; h++) {
			for (int j = 0; j < 6; j++) {
				if (planningCellsMed[h][j] != null) {
					planningCellsMed[h][j].getChildren().clear();
					planningCellsMed[h][j].setStyle("-fx-background-color: #EFF2F5; -fx-background-radius: 10; -fx-border-color: black; -fx-border-radius: 10; -fx-border-width: 0.1px;");
				}
			}
		}
	}
	
	/**
	 * Ajoute un RDV dans le planning
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
	    
	    VBox cell = planningCellsMed[heureIndex][jourIndex];
	    if (cell == null) return;
	    
	    // Créer le label pour le RDV
	    Label lblRDV = new Label(rdv.getAffichageCellule());
	    
	    // 🆕 STYLE IDENTIQUE À LA SECRÉTAIRE
	    lblRDV.setStyle(
	        "-fx-background-color: " + rdv.getStatut().getCouleur() + "; " +
	        "-fx-text-fill: white; " +
	        "-fx-padding: 5; " +                    // ✅ 5 au lieu de 10 15
	        "-fx-background-radius: 5; " +          // ✅ 5 au lieu de 12
	        "-fx-font-size: 11px; " +               // ✅ 11px au lieu de 13
	        "-fx-cursor: hand;"                     // ✅ Pas de font-weight bold
	    );
	    
	    lblRDV.setMaxWidth(Double.MAX_VALUE);
	    lblRDV.setWrapText(true);                   // 🆕 TRÈS IMPORTANT pour éviter l'overflow !
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
				Stage currentStage = (Stage) btnDeconnexion.getScene().getWindow();
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

	// ==================== RAFRAÎCHISSEMENT ====================
	
	/**
	 * RMÉTHODE CENTRALE - rafraîchit tout (planning, graphique, consultations)
	 */
	/**
	 * 🆕 Rafraîchit TOUTES les données du dashboard
	 */
	public void rafraichirTout() {
	    System.out.println("🔄 Rafraîchissement complet dashboard médecin...");
	    
	    chargerPatients();
	    chargerRendezVous(selectedDate);
	    chargerConsultationsDuJour();
	    chargerStatistiquesAccueil();
	    chargerGraphiqueSemaine();
	    
	    System.out.println("✅ Dashboard médecin rafraîchi");
	}

	/**
	 * Rafraîchit le planning (recharge depuis la BDD)
	 */
	public void rafraichirPlanning() {
	    chargerRendezVous(selectedDate);
	    chargerConsultationsDuJour();
	    chargerStatistiquesAccueil();
	    chargerGraphiqueSemaine();
	}
	
	/**
	 * 🆕 NOUVELLE MÉTHODE - Charger vraies statistiques
	 * À AJOUTER dans MedecinDashboardController
	 */
	private void chargerStatistiquesAccueil() {
	    if (medecinConnecte == null) return;
	    
	    System.out.println("📊 Chargement statistiques médecin...");
	    
	    // RDV aujourd'hui
	    List<RendezVous> rdvAujourdhui = rendezVousDAO.getRendezVousByDate(LocalDate.now());
	    long rdvPrevus = rdvAujourdhui.stream()
	        .filter(rdv -> rdv.getMedecin().getIdMedecin() == medecinConnecte.getIdMedecin())
	        .filter(rdv -> rdv.getStatut() == RendezVous.Statut.PREVU)
	        .count();
	    if (lblRdvAujourdhui != null) lblRdvAujourdhui.setText(String.valueOf(rdvPrevus));
	    
	    // Total consultations
	    int total = consultationDAO.compterConsultationsMedecin(medecinConnecte.getIdMedecin());
	    if (lblTotalConsultations != null) lblTotalConsultations.setText(String.valueOf(total));
	    
	    // Patients uniques
	    List<String> patients = consultationDAO.getNumSecuPatientsConsultes(medecinConnecte.getIdMedecin());
	    if (lblTotalPatients != null) lblTotalPatients.setText(String.valueOf(patients.size()));
	    
	    // Revenus
	    double revenus = consultationDAO.getTotalRevenusMedecin(medecinConnecte.getIdMedecin());
	    if (lblRevenusMois != null) lblRevenusMois.setText(String.format("%.0f", revenus));
	    
	    System.out.println("✅ Stats médecin chargées");
	}
	
	// ==================== ACTIONS PATIENTS ====================
	
	/**
	 * Ouvre le dossier médical d'un patient
	 */
	private void ouvrirDossierMedical(Patient patient) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/dossier_medical.fxml"));
			Parent root = loader.load();
			
			DossierMedicalController controller = loader.getController();
			// 🆕 PASSER LE MÉDECIN CONNECTÉ (pour filtrer ses consultations)
			controller.afficherDossier(patient, medecinConnecte);
			
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
	 * 🆕 Recherche patient avec LAMBDA (FiltreService)
	 */
	@FXML
	private void rechercherPatient() {
		String recherche = txtRecherchePatientMed.getText();
		
		if (recherche == null || recherche.trim().isEmpty()) {
			// Si vide, recharger tous les patients
			chargerPatients();
		} else {
			// 🆕 UTILISER LE SERVICE AVEC LAMBDA
			List<Patient> resultats = FiltreService.rechercherPatients(
				new ArrayList<>(listePatients), 
				recherche
			);
			
			tablePatientsMed.setItems(FXCollections.observableArrayList(resultats));
			System.out.println("🔍 " + resultats.size() + " résultat(s) trouvé(s)");
		}
	}
	
	// ==================== ACTIONS CONSULTATIONS ====================
	
	/**
	 * Ouvre la fenêtre pour commencer une consultation
	 */
	private void ouvrirConsultation(RendezVous rdv) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/commencer_consultation.fxml"));
			Parent root = loader.load();
			
			CommencerConsultationController controller = loader.getController();
			controller.setData(rdv, this);
			
			Stage stage = new Stage();
			stage.setTitle("Commencer Consultation");
			stage.setScene(new Scene(root));
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
			stage.show();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 🆕 Enregistre une consultation (appelée depuis CommencerConsultationController)
	 * SAUVEGARDE EN BDD
	 */
	public void ajouterConsultation(RendezVous rdv, Consultation consultation) {
	    // 1. Enregistrer la consultation dans la BDD
	    boolean success = consultationDAO.ajouterConsultation(
	        consultation, 
	        rdv.getPatient().getNumSocial(), 
	        medecinConnecte.getIdMedecin()
	    );
	    
	    if (success) {
	        System.out.println("✅ Consultation enregistrée en BDD");
	        
	        // 2. Mettre à jour le statut du RDV
	        rdv.setStatut(RendezVous.Statut.EFFECTUE);
	        rendezVousDAO.modifierRendezVous(rdv);
	        
	        // 3. Rafraîchir le planning et les consultations du jour
	        rafraichirTout(); // 🔄 RAFRAÎCHIR TOUT

	        System.out.println("✅ Historique patients rechargé");
	    } else {
	        System.err.println("❌ Échec enregistrement consultation");
	        Alert alert = new Alert(Alert.AlertType.ERROR);
	        alert.setContentText("Erreur lors de l'enregistrement de la consultation");
	        alert.show();
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
			controller.setDashboardControllerMed(this);
			// 🆕 Passer le médecin connecté pour pré-remplir
			controller.initialiserPourMedecin(medecinConnecte);
			
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
	 * Ajoute un RDV (appelée depuis AjouterRDVController)
	 * SAUVEGARDE EN BDD
	 */
	public void ajouterRDV(RendezVous rdv) {
	    // 1. Enregistrer en BDD
	    boolean success = rendezVousDAO.ajouterRendezVous(rdv);
	    
	    if (success) {
	        System.out.println("✅ RDV enregistré en BDD");
	        
	        // Recharger aussi les consultations du jour si c'est aujourd'hui
	        if (rdv.getDate().equals(LocalDate.now())) {
	            chargerConsultationsDuJour();
	        }
	        
	        System.out.println("✅ Planning rechargé");
	        
	        // 4. Message de succès
	        Alert alert = new Alert(Alert.AlertType.INFORMATION);
	        alert.setTitle("Succès");
	        alert.setContentText("Rendez-vous ajouté avec succès !");
	        alert.show();
	        
	        rafraichirTout(); // 🔄 RAFRAÎCHIR TOUT
	    } else {
	        System.err.println("❌ Échec ajout RDV");
	        Alert alert = new Alert(Alert.AlertType.ERROR);
	        alert.setContentText("Erreur lors de l'ajout du rendez-vous");
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
			rafraichirTout();
			System.out.println("✅ RDV supprimé");
	        rafraichirTout(); // 🔄 RAFRAÎCHIR TOUT
		} else {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Erreur");
			alert.setContentText("Impossible de supprimer le RDV de la BDD");
			alert.show();
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
    
	// ==================== MÉTHODES APPELÉES PAR LES BOUTONS FXML ====================
	
	/**
	 * Ouvre la fenêtre pour ajouter un RDV
	 * Appelée par le bouton "Nouveau Rendez-vous" dans le FXML
	 */
	@FXML
	private void ouvrirAjouterRDV() {
		ajouterNouveauRDV();
	}
	
	// ==================== MÉTHODES D'EXPORT ====================
	
	/**
	 * Exporte le rapport d'accueil du médecin en PDF
	 * Appelée par le bouton "Exporter Rapport" dans l'onglet Accueil
	 */
	@FXML
	private void exporterRapportPDF() {
		try {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Enregistrer le rapport médecin");
			fileChooser.setInitialFileName("rapport_medecin_" + medecinConnecte.getNom() + "_" + 
				LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf");
			fileChooser.getExtensionFilters().add(
				new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
			);
			
			File file = fileChooser.showSaveDialog(btnAccueil.getScene().getWindow());
			if (file != null) {
				genererRapportMedecinPDF(file);
				
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
	
	// ==================== MÉTHODES PRIVÉES DE GÉNÉRATION ====================
	
	/**
	 * Génère le PDF du rapport médecin avec statistiques personnelles
	 */
	private void genererRapportMedecinPDF(File file) throws Exception {
		if (medecinConnecte == null) {
			throw new Exception("Aucun médecin connecté");
		}
		
		PDDocument document = new PDDocument();
		PDPage page = new PDPage(PDRectangle.A4);
		document.addPage(page);
		
		PDPageContentStream contentStream = new PDPageContentStream(document, page);
		
		// === EN-TÊTE ===
		contentStream.beginText();
		contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 22);
		contentStream.newLineAtOffset(50, 750);
		contentStream.showText("VitaDesk - Rapport Medical");
		contentStream.endText();
		
		// === LIGNE DE SÉPARATION ===
		contentStream.setLineWidth(2);
		contentStream.moveTo(50, 740);
		contentStream.lineTo(550, 740);
		contentStream.stroke();
		
		// === INFORMATIONS MÉDECIN ===
		contentStream.beginText();
		contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
		contentStream.newLineAtOffset(50, 710);
		contentStream.showText("Dr. " + medecinConnecte.getPrenom() + " " + medecinConnecte.getNom());
		contentStream.endText();
		
		contentStream.beginText();
		contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
		contentStream.newLineAtOffset(50, 690);
		contentStream.showText("Specialite : " + medecinConnecte.getSpecialite());
		contentStream.endText();
		
		contentStream.beginText();
		contentStream.newLineAtOffset(50, 670);
		contentStream.showText("Date : " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
		contentStream.endText();
		
		int yPosition = 630;
		
		// === STATISTIQUES PERSONNELLES ===
		contentStream.beginText();
		contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
		contentStream.newLineAtOffset(50, yPosition);
		contentStream.showText("Statistiques Personnelles");
		contentStream.endText();
		yPosition -= 30;
		
		// Nombre de patients suivis
		contentStream.beginText();
		contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
		contentStream.newLineAtOffset(70, yPosition);
		contentStream.showText("Nombre de patients suivis : " + listePatients.size());
		contentStream.endText();
		yPosition -= 25;
		
		// Nombre total de consultations
		List<Consultation> toutesConsultations = consultationDAO.getConsultationsByMedecin(
			medecinConnecte.getIdMedecin()
		);
		contentStream.beginText();
		contentStream.newLineAtOffset(70, yPosition);
		contentStream.showText("Nombre total de consultations : " + toutesConsultations.size());
		contentStream.endText();
		yPosition -= 25;
		
		// RDV aujourd'hui
		List<RendezVous> rdvAujourdhui = listeRDV.stream()
			.filter(rdv -> rdv.getDate().equals(LocalDate.now()))
			.collect(java.util.stream.Collectors.toList());
		contentStream.beginText();
		contentStream.newLineAtOffset(70, yPosition);
		contentStream.showText("Rendez-vous aujourd'hui : " + rdvAujourdhui.size());
		contentStream.endText();
		yPosition -= 25;
		
		// Consultations prévues aujourd'hui
		long consultationsPrevues = rdvAujourdhui.stream()
			.filter(rdv -> rdv.getStatut() == RendezVous.Statut.PREVU)
			.count();
		contentStream.beginText();
		contentStream.newLineAtOffset(70, yPosition);
		contentStream.showText("Consultations prevues aujourd'hui : " + consultationsPrevues);
		contentStream.endText();
		yPosition -= 25;
		
		// Consultations effectuées aujourd'hui
		long consultationsEffectuees = rdvAujourdhui.stream()
			.filter(rdv -> rdv.getStatut() == RendezVous.Statut.EFFECTUE)
			.count();
		contentStream.beginText();
		contentStream.newLineAtOffset(70, yPosition);
		contentStream.showText("Consultations effectuees aujourd'hui : " + consultationsEffectuees);
		contentStream.endText();
		yPosition -= 40;
		
		// === STATISTIQUES DE LA SEMAINE ===
		contentStream.beginText();
		contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
		contentStream.newLineAtOffset(50, yPosition);
		contentStream.showText("Activite de la Semaine");
		contentStream.endText();
		yPosition -= 30;
		
		// Récupérer les stats de la semaine
		int[] consultationsParJour = consultationDAO.getConsultationsParJourSemaine(
			medecinConnecte.getIdMedecin()
		);
		
		String[] jours = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
		
		contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
		for (int i = 0; i < 7; i++) {
			contentStream.beginText();
			contentStream.newLineAtOffset(70, yPosition);
			contentStream.showText(jours[i] + " : " + consultationsParJour[i] + " consultation(s)");
			contentStream.endText();
			yPosition -= 20;
		}
		
		// Total semaine
		int totalSemaine = 0;
		for (int nb : consultationsParJour) {
			totalSemaine += nb;
		}
		yPosition -= 10;
		contentStream.beginText();
		contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
		contentStream.newLineAtOffset(70, yPosition);
		contentStream.showText("Total semaine : " + totalSemaine + " consultation(s)");
		contentStream.endText();
		
		// === PIED DE PAGE ===
		contentStream.beginText();
		contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 10);
		contentStream.newLineAtOffset(50, 50);
		contentStream.showText("Genere par VitaDesk le " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " - Page 1/1");
		contentStream.endText();
		
		contentStream.close();
		document.save(file);
		document.close();
		
		System.out.println("✅ Rapport médecin PDF généré : " + file.getAbsolutePath());
	}
    
}