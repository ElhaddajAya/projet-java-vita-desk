package ma.vitadesk.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ConsultationDuJour {
    private final StringProperty numero;
    private final StringProperty heure;
    private final StringProperty patient;
    private final StringProperty dateNaissance;
    private final StringProperty derniereVisite;
    private final RendezVous rendezVous; // Référence au RDV complet

    public ConsultationDuJour(String numero, RendezVous rdv) {
        this.numero = new SimpleStringProperty(numero);
        this.heure = new SimpleStringProperty(rdv.getHeure().toString());
        this.patient = new SimpleStringProperty(rdv.getPatient().getPrenom() + " " + rdv.getPatient().getNom());
        this.dateNaissance = new SimpleStringProperty(rdv.getPatient().getDateNaissance());
        this.derniereVisite = new SimpleStringProperty("22/12/2025"); 
        this.rendezVous = rdv;
    }

    // Properties pour JavaFX
    public StringProperty numeroProperty() { return numero; }
    public StringProperty heureProperty() { return heure; }
    public StringProperty patientProperty() { return patient; }
    public StringProperty dateNaissanceProperty() { return dateNaissance; }
    public StringProperty derniereVisiteProperty() { return derniereVisite; }

    // Getters
    public String getNumero() { return numero.get(); }
    public String getHeure() { return heure.get(); }
    public String getPatient() { return patient.get(); }
    public String getDateNaissance() { return dateNaissance.get(); }
    public String getDerniereVisite() { return derniereVisite.get(); }
    public RendezVous getRendezVous() { return rendezVous; }
}