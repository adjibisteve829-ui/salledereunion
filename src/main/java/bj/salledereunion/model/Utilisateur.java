package bj.salledereunion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "utilisateur")
public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false, length = 160)
    private String nom;
    @Column(nullable = false, unique = true, length = 254)
    private String email;
    @Column(name = "mot_de_passe", nullable = false, length = 255)
    private String motDePasse;
    @Column(name = "niveau_hierarchique", nullable = false)
    private int niveauHierarchique;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private Role role;

    protected Utilisateur() {
    }

    public Utilisateur(long id, String nom, String email, int niveauHierarchique, Role role) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.niveauHierarchique = niveauHierarchique;
        this.role = role;
    }

    public Utilisateur(String nom, String email, String motDePasse,
            int niveauHierarchique, Role role) {
        this.nom = nom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.niveauHierarchique = niveauHierarchique;
        this.role = role;
    }

    public long getId() { return id; }
    public String getNom() { return nom; }
    public String getEmail() { return email; }
    public int getNiveauHierarchique() { return niveauHierarchique; }
    public Role getRole() { return role; }
    public String getMotDePasse() { return motDePasse; }
    public void effacerMotDePasse() { motDePasse = null; }
    public void setNom(String nom) { this.nom = nom; }
    public void setEmail(String email) { this.email = email; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
    public void setNiveauHierarchique(int niveauHierarchique) {
        this.niveauHierarchique = niveauHierarchique;
    }
    public void setRole(Role role) { this.role = role; }
    public boolean estAutorite() { return role == Role.AUTORITE; }
    public boolean estAdministrateur() { return role == Role.ADMINISTRATEUR; }
}
