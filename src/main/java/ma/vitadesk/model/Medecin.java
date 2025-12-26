package ma.vitadesk.model;

import javafx.beans.property.SimpleStringProperty;

/**
 * Classe modèle pour un Médecin
 * Version améliorée avec les ID pour faciliter les opérations BDD
 */
public class Medecin {
    // ID pour les liaisons BDD (pas affichés à l'utilisateur)
    private int idUtilisateur;  // Lien avec la table utilisateur
    private int idMedecin;       // ID unique du médecin
    
    // Propriétés affichées dans l'interface (JavaFX Properties)
    private final SimpleStringProperty nom = new SimpleStringProperty();
    private final SimpleStringProperty prenom = new SimpleStringProperty();
    private final SimpleStringProperty specialite = new SimpleStringProperty();
    private final SimpleStringProperty telephone = new SimpleStringProperty();
    private final SimpleStringProperty email = new SimpleStringProperty();
    
    /**
     * Constructeur vide (nécessaire pour certaines opérations)
     */
    public Medecin() {
        super();
    }
    
    /**
     * Constructeur utilisé par l'interface (sans ID)
     * Les ID seront ajoutés après l'insertion en BDD
     */
    public Medecin(String nom, String prenom, String specialite, String telephone, String email) {
        this.nom.set(nom);
        this.prenom.set(prenom);
        this.specialite.set(specialite);
        this.telephone.set(telephone);
        this.email.set(email);
    }
    
    /**
     * Constructeur complet avec ID (utilisé par les DAO)
     * Quand on charge depuis la BDD, on a tous les champs
     */
    public Medecin(int idMedecin, int idUtilisateur, String nom, String prenom, 
                   String specialite, String telephone, String email) {
        this.idMedecin = idMedecin;
        this.idUtilisateur = idUtilisateur;
        this.nom.set(nom);
        this.prenom.set(prenom);
        this.specialite.set(specialite);
        this.telephone.set(telephone);
        this.email.set(email);
    }

    // === PROPERTIES pour JavaFX (binding avec les TableView) ===
    public SimpleStringProperty nomProperty() { return nom; }
    public SimpleStringProperty prenomProperty() { return prenom; }
    public SimpleStringProperty specialiteProperty() { return specialite; }
    public SimpleStringProperty telephoneProperty() { return telephone; }
    public SimpleStringProperty emailProperty() { return email; }

    // === GETTERS ===
    public int getIdUtilisateur() { return idUtilisateur; }
    public int getIdMedecin() { return idMedecin; }
    public String getNom() { return nom.get(); }
    public String getPrenom() { return prenom.get(); }
    public String getSpecialite() { return specialite.get(); }
    public String getTelephone() { return telephone.get(); }
    public String getEmail() { return email.get(); }

    // === SETTERS ===
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }
    public void setIdMedecin(int idMedecin) { this.idMedecin = idMedecin; }
    public void setNom(String nom) { this.nom.set(nom); }
    public void setPrenom(String prenom) { this.prenom.set(prenom); }
    public void setSpecialite(String specialite) { this.specialite.set(specialite); }
    public void setTelephone(String telephone) { this.telephone.set(telephone); }
    public void setEmail(String email) { this.email.set(email); }
}