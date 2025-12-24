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
}