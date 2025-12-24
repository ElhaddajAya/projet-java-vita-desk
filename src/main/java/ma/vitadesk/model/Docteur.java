package ma.vitadesk.model;

import javafx.beans.property.SimpleStringProperty;

public class Docteur {
	private final SimpleStringProperty nom = new SimpleStringProperty();
    private final SimpleStringProperty prenom = new SimpleStringProperty();
    private final SimpleStringProperty specialite = new SimpleStringProperty();
    private final SimpleStringProperty telephone = new SimpleStringProperty();
    private final SimpleStringProperty email = new SimpleStringProperty();
    
    public Docteur() {
    		super();
    }
    
    public Docteur(String nom, String prenom, String specialite, String telephone, String email) {
        this.nom.set(nom);
        this.prenom.set(prenom);
        this.specialite.set(specialite);
        this.telephone.set(telephone);
        this.email.set(email);
    }

    // Properties
    public SimpleStringProperty nomProperty() { return nom; }
    public SimpleStringProperty prenomProperty() { return prenom; }
    public SimpleStringProperty specialiteProperty() { return specialite; }
    public SimpleStringProperty telephoneProperty() { return telephone; }
    public SimpleStringProperty emailProperty() { return email; }

    // Getters
    public String getNom() { return nom.get(); }
    public String getPrenom() { return prenom.get(); }
    public String getSpecialite() { return specialite.get(); }
    public String getTelephone() { return telephone.get(); }
    public String getEmail() { return email.get(); }

    // Setters pour édition
    public void setNom(String nom) { this.nom.set(nom); }
    public void setPrenom(String prenom) { this.prenom.set(prenom); }
    public void setSpecialite(String specialite) { this.specialite.set(specialite); }
    public void setTelephone(String telephone) { this.telephone.set(telephone); }
    public void setEmail(String email) { this.email.set(email); }
}