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
@Table(name = "salle")
public class Salle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false, unique = true, length = 120)
    private String nom;
    @Column(nullable = false)
    private int capacite;
    @Column(nullable = false, columnDefinition = "text")
    private String equipements;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private StatutSalle statut;

    protected Salle() {
    }

    public Salle(long id, String nom, int capacite, String equipements, StatutSalle statut) {
        this.id = id;
        this.nom = nom;
        this.capacite = capacite;
        this.equipements = equipements;
        this.statut = statut;
    }

    public Salle(String nom, int capacite, String equipements) {
        this.nom = nom;
        this.capacite = capacite;
        this.equipements = equipements;
        this.statut = StatutSalle.DISPONIBLE;
    }

    public long getId() { return id; }
    public String getNom() { return nom; }
    public int getCapacite() { return capacite; }
    public String getEquipements() { return equipements; }
    public StatutSalle getStatut() { return statut; }
    public void setNom(String nom) { this.nom = nom; }
    public void setCapacite(int capacite) { this.capacite = capacite; }
    public void setEquipements(String equipements) { this.equipements = equipements; }
    public void setStatut(StatutSalle statut) { this.statut = statut; }
}
