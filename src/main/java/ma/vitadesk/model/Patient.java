package ma.vitadesk.model;

import javafx.beans.property.SimpleStringProperty;

public class Patient {
    
    private final SimpleStringProperty numSocial = new SimpleStringProperty();
    private final SimpleStringProperty nom = new SimpleStringProperty();
    private final SimpleStringProperty prenom = new SimpleStringProperty();
    private final SimpleStringProperty dateNaissance = new SimpleStringProperty();
    private final SimpleStringProperty telephone = new SimpleStringProperty();
    private final SimpleStringProperty cin = new SimpleStringProperty();
    private final SimpleStringProperty sexe = new SimpleStringProperty();
    private final SimpleStringProperty adresse = new SimpleStringProperty();

    public Patient(String numSocial, String nom, String prenom, String dateNaissance, 
                   String telephone, String cin, String sexe, String adresse) {
        this.numSocial.set(numSocial);
        this.nom.set(nom);
        this.prenom.set(prenom);
        this.dateNaissance.set(dateNaissance);
        this.telephone.set(telephone);
        this.cin.set(cin);
        this.sexe.set(sexe);
        this.adresse.set(adresse);
    }

    // Properties (obligatoires pour PropertyValueFactory)
    public SimpleStringProperty numSocialProperty() { return numSocial; }
    public SimpleStringProperty nomProperty() { return nom; }
    public SimpleStringProperty prenomProperty() { return prenom; }
    public SimpleStringProperty dateNaissanceProperty() { return dateNaissance; }
    public SimpleStringProperty telephoneProperty() { return telephone; }
    public SimpleStringProperty cinProperty() { return cin; }
    public SimpleStringProperty sexeProperty() { return sexe; }
    public SimpleStringProperty adresseProperty() { return adresse; }

    // Getters classiques
    public String getNumSocial() { return numSocial.get(); }
    public String getNom() { return nom.get(); }
    public String getPrenom() { return prenom.get(); }
    public String getDateNaissance() { return dateNaissance.get(); }
    public String getTelephone() { return telephone.get(); }
    public String getCin() { return cin.get(); }
    public String getSexe() { return sexe.get(); }
    public String getAdresse() {
        String addr = adresse.get();
        return (addr == null || addr.trim().isEmpty()) ? "-" : addr;
    }
    
    // Setters pour édition
    public void setNom(String nom) { this.nom.set(nom); }
    public void setPrenom(String prenom) { this.prenom.set(prenom); }
    public void setDateNaissance(String dateNaissance) { this.dateNaissance.set(dateNaissance); }
    public void setTelephone(String telephone) { this.telephone.set(telephone); }
    public void setCin(String cin) { this.cin.set(cin); }
    public void setSexe(String sexe) { this.sexe.set(sexe); }
    public void setAdresse(String adresse) { this.adresse.set(adresse); }
}