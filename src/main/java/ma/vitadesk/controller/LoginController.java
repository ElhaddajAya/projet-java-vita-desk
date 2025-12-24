package ma.vitadesk.controller;

import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ma.vitadesk.model.Docteur;
import ma.vitadesk.util.DatabaseConnection;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import at.favre.lib.crypto.bcrypt.BCrypt;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;

public class LoginController implements Initializable {

	@FXML private TextField txtLogin;
	@FXML private TextField txtPassword;
	@FXML private ComboBox<String> cbRole;
	@FXML private Label lblMessage;
    @FXML private Button btnConnecter;
    @FXML private Label lblError;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// On ajoute les options (items)
		cbRole.getItems().addAll("SECRETAIRE", "MEDECIN");
	}
	
	@FXML
	private void handleLogin() {
	    // Réinitialiser les erreurs
	    lblError.setVisible(false);
	    clearErrorStyle(txtLogin);
	    clearErrorStyle(txtPassword);
	    clearErrorStyle(cbRole);

	    // Extraire les textes depuis les champs
	    String login = txtLogin.getText().trim();
	    String motDePasse = txtPassword.getText();
	    String role = cbRole.getValue();

	    // Validation des champs vides
	    boolean hasError = false;
	    if (login.isEmpty()) {
	        setErrorStyle(txtLogin);
	        hasError = true;
	    }
	    if (motDePasse.isEmpty()) {
	        setErrorStyle(txtPassword);
	        hasError = true;
	    }
	    if (role == null) {
	        setErrorStyle(cbRole);
	        hasError = true;
	    }
	    if (hasError) {
	        lblError.setText("Veuillez remplir tous les champs");
	        lblError.setVisible(true);
	        return;
	    }

	    try (Connection conn = DatabaseConnection.getConnection()) {
	        String sql = "SELECT motDePasse, role FROM utilisateur WHERE login = ?";
	        PreparedStatement pst = conn.prepareStatement(sql);
	        pst.setString(1, login);
	        ResultSet rs = pst.executeQuery();

	        if (rs.next()) {
	            String hashedPassword = rs.getString("motDePasse");
	            String userRole = rs.getString("role");

	            // Vérification du mot de passe + rôle
	            if (BCrypt.verifyer().verify(motDePasse.toCharArray(), hashedPassword).verified && userRole.equals(role)) {
	                // Connexion réussie
	                Stage loginStage = (Stage) txtLogin.getScene().getWindow();
	                loginStage.close();

	                // Ouvre le dashboard correspondant
	                if (role.equals("SECRETAIRE")) {
	                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/secretaire_dashboard.fxml"));
	                    Parent root = loader.load();
	                    Stage stage = new Stage();
	                    stage.setTitle("VitaDesk - Dashboard Secrétaire");
	                    stage.setScene(new Scene(root));
	                    stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
	                    stage.setResizable(false);
	                    stage.centerOnScreen();
	                    stage.show();
	                } else if (role.equals("MEDECIN")) {
	                    // Créer un médecin fictif et le passer au controller
	                    Docteur medecinConnecte = new Docteur();
	                    medecinConnecte.setNom("ESSADI");
	                    medecinConnecte.setPrenom("Alae");
	                    medecinConnecte.setSpecialite("Généraliste");
	                    medecinConnecte.setEmail("medecin@email.com");
	                    medecinConnecte.setTelephone("0123456789");
	                    
	                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/medecin_dashboard.fxml"));
	                    Parent root = loader.load();
	                    
	                    // Récupérer le controller et passer le médecin
	                    MedecinDashboardController controller = loader.getController();
	                    controller.setMedecin(medecinConnecte);
	                    
	                    Stage stage = new Stage();
	                    stage.setTitle("VitaDesk - Dashboard Médecin");
	                    stage.setScene(new Scene(root));
	                    stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
	                    stage.setResizable(false);
	                    stage.centerOnScreen();
	                    stage.show();
	                }
	            } else {
	                // Mot de passe ou rôle incorrect
	                lblError.setText("Mot de passe incorrect ou rôle non correspondant");
	                lblError.setVisible(true);
	                setErrorStyle(txtPassword);
	                setErrorStyle(cbRole);
	            }
	        } else {
	            // Utilisateur non trouvé
	            lblError.setText("Utilisateur non trouvé");
	            lblError.setVisible(true);
	            setErrorStyle(txtLogin);
	        }

	    } catch (SQLException | IOException e) {
	        e.printStackTrace();
	        lblError.setText("Erreur de connexion à la base de données");
	        lblError.setVisible(true);
	        setErrorStyle(txtLogin);
	        setErrorStyle(txtPassword);
	        setErrorStyle(cbRole);
	    }
	}

	// === Méthodes pour gérer les bordures d'erreur ===

	private void setErrorStyle(Control control) {
	    control.setStyle("-fx-background-color: white; -fx-border-color: red; -fx-border-width: 0.3px; -fx-border-radius: 3px;");
	}

	private void clearErrorStyle(Control control) {
	    control.setStyle("-fx-background-color: white; -fx-border-width: 0.2px; -fx-border-color: black; -fx-border-radius: 3;");
	}
	
}