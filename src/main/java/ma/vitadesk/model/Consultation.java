package ma.vitadesk.model;

public class Consultation {
    private String date;
    private String medecin;
    private String diagnostic;
    private String traitement;
    private String observations;
    private double prixConsultation;

    public Consultation(String date, String medecin, String diagnostic, String traitement, String observations, double prixConsultation) {
        this.date = date;
        this.medecin = medecin;
        this.diagnostic = diagnostic;
        this.traitement = traitement;
        this.observations = observations;
        this.prixConsultation = prixConsultation;
    }

    public String getDate() { return date; }
    public String getMedecin() { return medecin; }
    public String getDiagnostic() { return diagnostic; }
    public String getTraitement() { return traitement; }
    public String getObservations() { return observations; }
    public double getPrixConsultation() { return prixConsultation; }
    
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