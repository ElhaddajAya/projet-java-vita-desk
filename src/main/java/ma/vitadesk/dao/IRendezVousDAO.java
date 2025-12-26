package ma.vitadesk.dao;

import java.time.LocalDate;
import java.util.List;
import ma.vitadesk.model.RendezVous;

/**
 * Interface DAO pour les rendez-vous
 * Gère toutes les opérations sur les RDV dans la BDD
 */
public interface IRendezVousDAO {
    
    /**
     * Récupère tous les rendez-vous
     * @return liste de tous les RDV
     */
    List<RendezVous> getAllRendezVous();
    
    /**
     * Récupère les RDV d'un médecin spécifique
     * @param idMedecin l'ID du médecin
     * @return liste des RDV du médecin
     */
    List<RendezVous> getRendezVousByMedecin(int idMedecin);
    
    /**
     * Récupère les RDV d'une date précise
     * @param date la date recherchée
     * @return liste des RDV de cette date
     */
    List<RendezVous> getRendezVousByDate(LocalDate date);
    
    /**
     * Ajoute un nouveau RDV
     * @param rdv le rendez-vous à ajouter
     * @return true si ajout réussi
     */
    boolean ajouterRendezVous(RendezVous rdv);
    
    /**
     * Modifie un RDV existant (ex: changer le statut)
     * @param rdv le RDV modifié
     * @return true si modif réussie
     */
    boolean modifierRendezVous(RendezVous rdv);
    
    /**
     * Supprime un RDV
     * @param idRDV l'ID du RDV à supprimer
     * @return true si suppression réussie
     */
    boolean supprimerRendezVous(int idRDV);
}