package bj.salledereunion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demande_reservation_id", nullable = false)
    private Reservation demandeReservation;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinataire_id", nullable = false)
    private Utilisateur destinataire;
    @Column(nullable = false, columnDefinition = "text")
    private String message;
    @Column(name = "delai_minutes", nullable = false)
    private int delaiMinutes;
    @Column(name = "date_envoi", nullable = false)
    private LocalDateTime dateEnvoi;
    @Column(name = "date_echeance", nullable = false)
    private LocalDateTime dateEcheance;
    @Column(nullable = false)
    private boolean traitee;

    protected Notification() {
    }

    public Notification(Reservation reservation, Reservation demandeReservation,
            Utilisateur destinataire, String message, int delaiMinutes,
            LocalDateTime dateEnvoi, LocalDateTime dateEcheance) {
        this.reservation = reservation;
        this.demandeReservation = demandeReservation;
        this.destinataire = destinataire;
        this.message = message;
        this.delaiMinutes = delaiMinutes;
        this.dateEnvoi = dateEnvoi;
        this.dateEcheance = dateEcheance;
    }

    public long getId() { return id; }
    public long getReservationId() { return reservation.getId(); }
    public long getDestinataireId() { return destinataire.getId(); }
    public String getMessage() { return message; }
    public int getDelaiMinutes() { return delaiMinutes; }
    public LocalDateTime getDateEnvoi() { return dateEnvoi; }
    public LocalDateTime getDateEcheance() { return dateEcheance; }
    public boolean isTraitee() { return traitee; }
    public Reservation getReservation() { return reservation; }
    public Reservation getDemandeReservation() { return demandeReservation; }
    public void setTraitee(boolean traitee) { this.traitee = traitee; }
}
