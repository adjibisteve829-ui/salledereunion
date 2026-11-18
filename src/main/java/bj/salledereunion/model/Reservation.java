package bj.salledereunion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservation")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "salle_id", nullable = false)
    private Salle salle;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;
    @Column(name = "date_debut", nullable = false)
    private LocalDateTime dateDebut;
    @Column(name = "date_fin", nullable = false)
    private LocalDateTime dateFin;
    @Column(nullable = false, length = 500)
    private String motif;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private StatutReservation statut;
    @Column(nullable = false)
    private boolean prioritaire;

    protected Reservation() {
    }

    public Reservation(Salle salle, Utilisateur utilisateur, LocalDateTime dateDebut,
            LocalDateTime dateFin, String motif, StatutReservation statut, boolean prioritaire) {
        this.salle = salle;
        this.utilisateur = utilisateur;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.motif = motif;
        this.statut = statut;
        this.prioritaire = prioritaire;
    }

    public long getId() { return id; }
    public long getSalleId() { return salle.getId(); }
    public long getUtilisateurId() { return utilisateur.getId(); }
    public Salle getSalle() { return salle; }
    public Utilisateur getUtilisateur() { return utilisateur; }
    public LocalDateTime getDateDebut() { return dateDebut; }
    public LocalDateTime getDateFin() { return dateFin; }
    public String getMotif() { return motif; }
    public StatutReservation getStatut() { return statut; }
    public boolean isPrioritaire() { return prioritaire; }
    public void setSalle(Salle salle) { this.salle = salle; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }
    public void setMotif(String motif) { this.motif = motif; }
    public void setStatut(StatutReservation statut) { this.statut = statut; }

    public LocalDateTime reduireDuree(LocalDateTime maintenant) {
        LocalDateTime debutRestant = dateDebut.isAfter(maintenant) ? dateDebut : maintenant;
        long secondesRestantes = Duration.between(debutRestant, dateFin).getSeconds();
        if (secondesRestantes <= 0) {
            return debutRestant;
        }
        return debutRestant.plusSeconds(Math.max(1, secondesRestantes / 2));
    }
}
