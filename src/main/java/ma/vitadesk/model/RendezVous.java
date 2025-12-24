package ma.vitadesk.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class RendezVous {

	private LocalDate date;
    private LocalTime heure;
    private Patient patient;
    private Docteur docteur;
    private String motif;
    private Statut statut;
    
    // Enum pour les statuts possibles
    public enum Statut {
        PREVU("Prévu", "#28a745"),        // vert
        EFFECTUE("Effectué", "#007bff"),  // bleu
        ANNULE("Annulé", "red"),      // rouge
        REPORTE("Reporté", "#ffc107"),    // jaune
        ABSENT("Absent", "#6c757d");       // gris

        private final String label;
        private final String couleur;

        Statut(String label, String couleur) {
            this.label = label;
            this.couleur = couleur;
        }

        public String getLabel() { return label; }
        public String getCouleur() { return couleur; }
    }

    public RendezVous(LocalDate date, LocalTime heure, Patient patient, Docteur docteur, String motif, Statut statut) {
        this.date = date;
        this.heure = heure;
        this.patient = patient;
        this.docteur = docteur;
        this.motif = motif;
        this.statut = statut != null ? statut : Statut.PREVU; // par défaut Prévu
    }

    // Getters
    public LocalDate getDate() { return date; }
    public LocalTime getHeure() { return heure; }
    public Patient getPatient() { return patient; }
    public Docteur getDocteur() { return docteur; }
    public String getMotif() { return motif; }
    public Statut getStatut() { return statut; }
    
    // Setters
    public void setStatut(Statut statut) { this.statut = statut; }

    // Affichage dans la cellule
    public String getAffichageCellule() {
        return patient.getPrenom() + " " + patient.getNom() + 
               "\nDr. " + docteur.getNom() + 
               "\n(" + statut.getLabel() + ")";
    }
}
