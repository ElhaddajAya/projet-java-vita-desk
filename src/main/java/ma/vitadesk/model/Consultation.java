package ma.vitadesk.model;

/**
 * Classe modèle pour une Consultation
 * Version améliorée avec idConsultation pour les opérations BDD
 */
public class Consultation {
    // ID pour la BDD (pas affiché à l'utilisateur)
    private int idConsultation;
    
    // Attributs de la consultation
    private String date;
    private String medecin;
    private String diagnostic;
    private String traitement;
    private String observations;
    private double prixConsultation;

    /**
     * Constructeur utilisé par l'interface (sans ID)
     * L'ID sera ajouté après insertion en BDD
     */
    public Consultation(String date, String medecin, String diagnostic, String traitement, 
                       String observations, double prixConsultation) {
        this.date = date;
        this.medecin = medecin;
        this.diagnostic = diagnostic;
        this.traitement = traitement;
        this.observations = observations;
        this.prixConsultation = prixConsultation;
    }
    
    /**
     * Constructeur complet avec ID (utilisé par les DAO)
     * Quand on charge depuis la BDD
     */
    public Consultation(int idConsultation, String date, String medecin, String diagnostic, 
                       String traitement, String observations, double prixConsultation) {
        this.idConsultation = idConsultation;
        this.date = date;
        this.medecin = medecin;
        this.diagnostic = diagnostic;
        this.traitement = traitement;
        this.observations = observations;
        this.prixConsultation = prixConsultation;
    }

    // === GETTERS ===
    public int getIdConsultation() { return idConsultation; }
    public String getDate() { return date; }
    public String getMedecin() { return medecin; }
    public String getDiagnostic() { return diagnostic; }
    public String getTraitement() { return traitement; }
    public String getObservations() { return observations; }
    public double getPrixConsultation() { return prixConsultation; }
    
    // === SETTERS ===
    public void setIdConsultation(int idConsultation) { this.idConsultation = idConsultation; }
    
    public void setDate(String date) {
        this.date = date;
    }

    public void setMedecin(String medecin) {
        this.medecin = medecin;
    }

    public void setDiagnostic(String diagnostic) {
        this.diagnostic = diagnostic;
    }

    public void setTraitement(String traitement) {
        this.traitement = traitement;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public void setPrixConsultation(double prixConsultation) {
        this.prixConsultation = prixConsultation;
    }
}