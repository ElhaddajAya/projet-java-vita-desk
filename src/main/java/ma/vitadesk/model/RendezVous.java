package ma.vitadesk.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Classe modèle pour un Rendez-vous
 * Version améliorée avec idRDV pour les opérations BDD (UPDATE, DELETE)
 */
public class RendezVous {
    
    // ID pour la BDD (nécessaire pour modifier/supprimer)
    private int idRDV;
    
    private LocalDate date;
    private LocalTime heure;
    private Patient patient;
    private Medecin medecin;
    private String motif;
    private Statut statut;
    
    // Enum pour les statuts possibles
    public enum Statut {
        PREVU("Prévu", "#007bff"),        // bleu
        EFFECTUE("Effectué", "#28a745"),  // vert
        ANNULE("Annulé", "red"),          // rouge
        REPORTE("Reporté", "#ffc107"),    // jaune
        ABSENT("Absent", "#6c757d");      // gris

        private final String label;
        private final String couleur;

        Statut(String label, String couleur) {
            this.label = label;
            this.couleur = couleur;
        }

        public String getLabel() { return label; }
        public String getCouleur() { return couleur; }
    }

    /**
     * Constructeur utilisé par l'interface (sans ID)
     * L'ID sera ajouté après insertion en BDD
     */
    public RendezVous(LocalDate date, LocalTime heure, Patient patient, Medecin medecin, 
                      String motif, Statut statut) {
        this.date = date;
        this.heure = heure;
        this.patient = patient;
        this.medecin = medecin;
        this.motif = motif;
        this.statut = statut != null ? statut : Statut.PREVU; // par défaut Prévu
    }
    
    /**
     * Constructeur complet avec ID (utilisé par les DAO)
     * Quand on charge depuis la BDD
     */
    public RendezVous(int idRDV, LocalDate date, LocalTime heure, Patient patient, 
                      Medecin medecin, String motif, Statut statut) {
        this.idRDV = idRDV;
        this.date = date;
        this.heure = heure;
        this.patient = patient;
        this.medecin = medecin;
        this.motif = motif;
        this.statut = statut != null ? statut : Statut.PREVU;
    }

    // === GETTERS ===
    public int getIdRDV() { return idRDV; }
    public LocalDate getDate() { return date; }
    public LocalTime getHeure() { return heure; }
    public Patient getPatient() { return patient; }
    public Medecin getDocteur() { return medecin; }
    public Medecin getMedecin() { return medecin; } // Alias pour compatibilité
    public String getMotif() { return motif; }
    public Statut getStatut() { return statut; }
    
    // === SETTERS ===
    public void setIdRDV(int idRDV) { this.idRDV = idRDV; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setHeure(LocalTime heure) { this.heure = heure; }
    public void setMotif(String motif) { this.motif = motif; }
    public void setStatut(Statut statut) { this.statut = statut; }

    /**
     * Affichage dans la cellule du planning
     * Format : Prénom Nom + Dr. Nom + (Statut)
     */
    public String getAffichageCellule() {
        return patient.getPrenom() + " " + patient.getNom() + 
               "\nDr. " + medecin.getNom() + 
               "\n(" + statut.getLabel() + ")";
    }
}