package ma.vitadesk.service;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import ma.vitadesk.model.Patient;
import ma.vitadesk.model.Medecin;
import ma.vitadesk.model.RendezVous;

/**
 * Classe de service avec expressions LAMBDA
 * Montre l'utilisation des interfaces fonctionnelles et lambda
 * (Requis pour la soutenance - concepts du cours)
 */
public class FiltreService {
    
    /**
     * Filtre les patients selon un critère personnalisé
     * UTILISE LAMBDA : Predicate<Patient> est une interface fonctionnelle
     * 
     * Exemple d'utilisation :
     *   - Filtrer les hommes : filtrerPatients(liste, p -> p.getSexe().equals("M"))
     *   - Filtrer par nom : filtrerPatients(liste, p -> p.getNom().contains("Ben"))
     */
    public static List<Patient> filtrerPatients(List<Patient> patients, Predicate<Patient> critere) {
        return patients.stream()
                       .filter(critere)  // Lambda appliquée ici
                       .collect(Collectors.toList());
    }
    
    /**
     * Filtre les médecins selon un critère
     * Exemple : filtrerMedecins(liste, m -> m.getSpecialite().equals("Cardiologue"))
     */
    public static List<Medecin> filtrerMedecins(List<Medecin> medecins, Predicate<Medecin> critere) {
        return medecins.stream()
                       .filter(critere)
                       .collect(Collectors.toList());
    }
    
    /**
     * Filtre les RDV selon un critère
     * Exemple : filtrerRDV(liste, rdv -> rdv.getStatut() == Statut.PREVU)
     */
    public static List<RendezVous> filtrerRDV(List<RendezVous> rdvList, Predicate<RendezVous> critere) {
        return rdvList.stream()
                      .filter(critere)
                      .collect(Collectors.toList());
    }
    
    /**
     * Recherche textuelle dans les patients (nom, prénom, CIN, tel)
     * UTILISE LAMBDA pour combiner plusieurs conditions
     */
    public static List<Patient> rechercherPatients(List<Patient> patients, String recherche) {
        String rechercheMin = recherche.toLowerCase().trim();
        
        // Lambda combinée : vérifie nom OU prénom OU CIN OU téléphone
        return patients.stream()
                       .filter(p -> 
                           p.getNom().toLowerCase().contains(rechercheMin) ||
                           p.getPrenom().toLowerCase().contains(rechercheMin) ||
                           p.getCin().toLowerCase().contains(rechercheMin) ||
                           p.getTelephone().contains(rechercheMin) ||
                           p.getNumSocial().contains(rechercheMin)
                       )
                       .collect(Collectors.toList());
    }
    
    /**
     * Recherche textuelle dans les médecins
     */
    public static List<Medecin> rechercherMedecins(List<Medecin> medecins, String recherche) {
        String rechercheMin = recherche.toLowerCase().trim();
        
        return medecins.stream()
                       .filter(m -> 
                           m.getNom().toLowerCase().contains(rechercheMin) ||
                           m.getPrenom().toLowerCase().contains(rechercheMin) ||
                           m.getSpecialite().toLowerCase().contains(rechercheMin) ||
                           m.getEmail().toLowerCase().contains(rechercheMin)
                       )
                       .collect(Collectors.toList());
    }
    
    /**
     * Compte les RDV qui correspondent à un critère
     * UTILISE LAMBDA pour le comptage conditionnel
     * Exemple : compterRDV(liste, rdv -> rdv.getStatut() == Statut.EFFECTUE)
     */
    public static long compterRDV(List<RendezVous> rdvList, Predicate<RendezVous> critere) {
        return rdvList.stream()
                      .filter(critere)
                      .count();
    }
    
    /**
     * Vérifie si au moins un élément correspond au critère
     * UTILISE LAMBDA : anyMatch
     * Exemple : existeRDV(liste, rdv -> rdv.getPatient().equals(patient))
     */
    public static boolean existeRDV(List<RendezVous> rdvList, Predicate<RendezVous> critere) {
        return rdvList.stream()
                      .anyMatch(critere);
    }
}