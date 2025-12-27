package ma.vitadesk.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Classe modèle pour un Patient
 * Version améliorée avec idPatient pour les opérations BDD
 */
public class Patient {
    // ID pour la BDD (pas affiché à l'utilisateur)
    private int idPatient;
    
    // Propriétés JavaFX pour l'affichage dans les TableView
    private final SimpleStringProperty numSocial = new SimpleStringProperty();
    private final SimpleStringProperty nom = new SimpleStringProperty();
    private final SimpleStringProperty prenom = new SimpleStringProperty();
    private final SimpleStringProperty dateNaissance = new SimpleStringProperty();
    private final SimpleStringProperty telephone = new SimpleStringProperty();
    private final SimpleStringProperty cin = new SimpleStringProperty();
    private final SimpleStringProperty sexe = new SimpleStringProperty();
    private final SimpleStringProperty adresse = new SimpleStringProperty();
    private final SimpleStringProperty groupeSanguin = new SimpleStringProperty("-");
    private final SimpleStringProperty allergies = new SimpleStringProperty("-");
    private final SimpleStringProperty antecedents = new SimpleStringProperty("-");
    
    // Liste des consultations du patient (chargée depuis la BDD)
    private final ObservableList<Consultation> consultations = FXCollections.observableArrayList();

    /**
     * Constructeur utilisé par l'interface (sans ID)
     * L'ID sera ajouté après insertion en BDD
     */
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
    
    /**
     * Constructeur complet avec ID (utilisé par les DAO)
     * Quand on charge depuis la BDD
     */
    public Patient(int idPatient, String numSocial, String nom, String prenom, 
                   String dateNaissance, String telephone, String cin, String sexe, String adresse) {
        this.idPatient = idPatient;
        this.numSocial.set(numSocial);
        this.nom.set(nom);
        this.prenom.set(prenom);
        this.dateNaissance.set(dateNaissance);
        this.telephone.set(telephone);
        this.cin.set(cin);
        this.sexe.set(sexe);
        this.adresse.set(adresse);
    }

    // === PROPERTIES pour JavaFX ===
    public SimpleStringProperty numSocialProperty() { return numSocial; }
    public SimpleStringProperty nomProperty() { return nom; }
    public SimpleStringProperty prenomProperty() { return prenom; }
    public SimpleStringProperty dateNaissanceProperty() { return dateNaissance; }
    public SimpleStringProperty telephoneProperty() { return telephone; }
    public SimpleStringProperty cinProperty() { return cin; }
    public SimpleStringProperty sexeProperty() { return sexe; }
    public SimpleStringProperty adresseProperty() { return adresse; }
    public SimpleStringProperty groupeSanguinProperty() { return groupeSanguin; }
    public SimpleStringProperty allergiesProperty() { return allergies; }
    public SimpleStringProperty antecedentsProperty() { return antecedents; }
    
    /**
     * Retourne la liste des consultations du patient
     * ObservableList = mise à jour auto dans l'interface
     */
    public ObservableList<Consultation> getConsultations() {
        return consultations;
    }

    // === GETTERS ===
    public int getIdPatient() { return idPatient; }
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
    public String getGroupeSanguin() { 
        String groupe = groupeSanguin.get();
        return (groupe == null || groupe.trim().isEmpty()) ? "-" : groupe;
    }
    public String getAllergies() { 
        String allerg = allergies.get();
        return (allerg == null || allerg.trim().isEmpty()) ? "-" : allerg;
    }
    public String getAntecedents() { 
        String antec = antecedents.get();
        return (antec == null || antec.trim().isEmpty()) ? "-" : antec;
    }
    
    // === SETTERS ===
    public void setIdPatient(int idPatient) { this.idPatient = idPatient; }
    public void setNom(String nom) { this.nom.set(nom); }
    public void setPrenom(String prenom) { this.prenom.set(prenom); }
    public void setDateNaissance(String dateNaissance) { this.dateNaissance.set(dateNaissance); }
    public void setTelephone(String telephone) { this.telephone.set(telephone); }
    public void setCin(String cin) { this.cin.set(cin); }
    public void setSexe(String sexe) { this.sexe.set(sexe); }
    public void setAdresse(String adresse) { this.adresse.set(adresse); }
    public void setGroupeSanguin(String groupe) { 
        this.groupeSanguin.set(groupe == null ? "-" : groupe); 
    }
    public void setAllergies(String allergies) { 
        this.allergies.set(allergies == null ? "-" : allergies); 
    }
    public void setAntecedents(String antecedents) { 
        this.antecedents.set(antecedents == null ? "-" : antecedents); 
    }
    
    /**
     * Ajoute une consultation à la liste du patient
     * Utilisé quand on charge les consultations depuis la BDD
     */
    public void ajouterConsultation(Consultation consultation) {
        consultations.add(consultation);
    }
}