package ma.vitadesk.model;

/**
 * Classe qui représente un utilisateur du système (Médecin ou Secrétaire)
 * Cette classe sert à stocker les informations de l'utilisateur connecté
 */
public class Utilisateur {
    private int id;
    private String nom;
    private String prenom;
    private String specialite; // Seulement pour les médecins
    private String telephone;
    private String email;
    private String login;
    private Role role;
    
    // Enum pour définir les rôles possibles dans le système
    public enum Role {
        MEDECIN,
        SECRETAIRE,
        ADMIN
    }
    
    // Constructeur complet pour créer un utilisateur avec toutes ses infos
    public Utilisateur(int id, String nom, String prenom, String specialite, 
                      String telephone, String email, String login, Role role) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.specialite = specialite;
        this.telephone = telephone;
        this.email = email;
        this.login = login;
        this.role = role;
    }
    
    // Constructeur vide (utile pour créer un objet vide puis le remplir)
    public Utilisateur() {
    }

    // === GETTERS ===
    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getSpecialite() { return specialite; }
    public String getTelephone() { return telephone; }
    public String getEmail() { return email; }
    public String getLogin() { return login; }
    public Role getRole() { return role; }
    
    // === SETTERS ===
    public void setId(int id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public void setEmail(String email) { this.email = email; }
    public void setLogin(String login) { this.login = login; }
    public void setRole(Role role) { this.role = role; }
    
    // Méthode pour obtenir le nom complet (utile pour l'affichage)
    public String getNomComplet() {
        return prenom + " " + nom;
    }
    
    // Méthode toString() pour afficher les infos (utile pour le debug)
    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", role=" + role +
                '}';
    }
}