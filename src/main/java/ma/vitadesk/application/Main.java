package ma.vitadesk.application;

import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ma.vitadesk.util.DatabaseConnection;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        DatabaseConnection.testConnection(); // doit afficher "OK" dans la console

        URL fxmlUrl = getClass().getClassLoader().getResource("view/fxml/login.fxml");
        
        if (fxmlUrl == null) {
            System.err.println("FICHIER login.fxml INTROUVABLE !");
            System.err.println("Cherché dans : " + getClass().getClassLoader().getResource("."));
            return;
        }

        // le composant qui charge le layout (tous les composants définis dans le FXML)
        Parent root = FXMLLoader.load(fxmlUrl);

        Scene scene = new Scene(root);
        primaryStage.setTitle("VitaDesk – Connexion");
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
