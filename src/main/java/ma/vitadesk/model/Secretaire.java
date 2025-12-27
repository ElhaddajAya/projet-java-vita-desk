package ma.vitadesk.model;

import javafx.beans.property.SimpleStringProperty;

/**
 * Classe modèle pour une Secrétaire
 */
public class Secretaire {
    // ID pour les liaisons BDD (pas affichés à l'utilisateur)
    private int idUtilisateur;  // Lien avec la table utilisateur
    private int idSecretaire;   // ID unique de la secrétaire
    
    // Propriétés affichées dans l'interface (JavaFX Properties)
    private final SimpleStringProperty nom = new SimpleStringProperty();
    private final SimpleStringProperty prenom = new SimpleStringProperty();
    private final SimpleStringProperty telephone = new SimpleStringProperty();
    private final SimpleStringProperty email = new SimpleStringProperty();
    
    /**
     * Constructeur vide (nécessaire pour certaines opérations)
     */
    public Secretaire() {
        super();
    }
    
    /**
     * Constructeur utilisé par l'interface (sans ID)
     * Les ID seront ajoutés après l'insertion en BDD
     */
    public Secretaire(String nom, String prenom, String telephone, String email) {
        this.nom.set(nom);
        this.prenom.set(prenom);
        this.telephone.set(telephone);
        this.email.set(email);
    }
    
    /**
     * Constructeur complet avec ID (utilisé par les DAO)
     * Quand on charge depuis la BDD, on a tous les champs
     */
    public Secretaire(int idSecretaire, int idUtilisateur, String nom, String prenom, 
                      String telephone, String email) {
        this.idSecretaire = idSecretaire;
        this.idUtilisateur = idUtilisateur;
        this.nom.set(nom);
        this.prenom.set(prenom);
        this.telephone.set(telephone);
        this.email.set(email);
    }

    // === PROPERTIES pour JavaFX (binding avec les TableView) ===
    public SimpleStringProperty nomProperty() { return nom; }
    public SimpleStringProperty prenomProperty() { return prenom; }
    public SimpleStringProperty telephoneProperty() { return telephone; }
    public SimpleStringProperty emailProperty() { return email; }

    // === GETTERS ===
    public int getIdUtilisateur() { return idUtilisateur; }
    public int getIdSecretaire() { return idSecretaire; }
    public String getNom() { return nom.get(); }
    public String getPrenom() { return prenom.get(); }
    public String getTelephone() { return telephone.get(); }
    public String getEmail() { return email.get(); }

    // === SETTERS ===
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }
    public void setIdSecretaire(int idSecretaire) { this.idSecretaire = idSecretaire; }
    public void setNom(String nom) { this.nom.set(nom); }
    public void setPrenom(String prenom) { this.prenom.set(prenom); }
    public void setTelephone(String telephone) { this.telephone.set(telephone); }
    public void setEmail(String email) { this.email.set(email); }
}