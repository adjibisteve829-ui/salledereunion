package bj.salledereunion.service;

import bj.salledereunion.dao.NotificationDao;
import bj.salledereunion.dao.ReservationDao;
import bj.salledereunion.dao.ReglesPreemptionDao;
import bj.salledereunion.model.Reservation;
import bj.salledereunion.model.ReglesPreemption;
import bj.salledereunion.model.Role;
import bj.salledereunion.model.Salle;
import bj.salledereunion.model.StatutReservation;
import bj.salledereunion.model.Utilisateur;
import bj.salledereunion.util.PersistenceUtil;
import jakarta.persistence.LockModeType;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class PreemptionService {
    private final ReservationDao reservationDao = new ReservationDao();
    private final NotificationDao notificationDao = new NotificationDao();

    public void demander(Utilisateur autorite, long salleId, LocalDateTime debut,
            LocalDateTime fin, String motif) {
        ReservationService.validerPeriode(debut, fin, motif);
        if (!autorite.estAutorite()) {
            throw new IllegalArgumentException("Seule une autorité peut demander une préemption.");
        }
        PersistenceUtil.executerTransaction(entityManager -> {
            Salle salle = entityManager.find(Salle.class, salleId, LockModeType.PESSIMISTIC_WRITE);
            ReservationService.verifierSalle(salle);
            List<Reservation> conflits =
                    reservationDao.reservationsChevauchantes(entityManager, salleId, debut, fin);
            if (conflits.stream().anyMatch(reservation ->
                    reservation.getStatut() == StatutReservation.EN_ATTENTE_PREEMPTION)) {
                throw new IllegalArgumentException(
                        "Une demande de préemption est déjà en cours pour ce créneau.");
            }
            int niveauMaximum = niveauAutoriteMaximum(entityManager);
            ReglesPreemption regles = ReglesPreemptionDao.lire(entityManager);
            for (Reservation reservation : conflits) {
                if (autorite.getNiveauHierarchique()
                        <= reservation.getUtilisateur().getNiveauHierarchique()) {
                    throw new IllegalArgumentException(
                            "Votre niveau hiérarchique ne permet pas de préempter cette réservation.");
                }
            }
            StatutReservation statut = conflits.isEmpty()
                    ? StatutReservation.CONFIRMEE : StatutReservation.EN_ATTENTE_PREEMPTION;
            Reservation demande = reservationDao.creer(entityManager, salleId, autorite.getId(),
                    debut, fin, motif, statut, true);
            if (!conflits.isEmpty()) {
                int delai = autorite.getNiveauHierarchique() >= niveauMaximum
                        ? regles.delaiNiveauMax() : regles.delaiAutres();
                LocalDateTime maintenant = LocalDateTime.now();
                String message = "Une autorité de niveau " + autorite.getNiveauHierarchique()
                        + " demande cette salle. Veuillez la libérer avant l'échéance.";
                for (Reservation reservation : conflits) {
                    notificationDao.creer(entityManager, reservation, demande,
                            reservation.getUtilisateurId(), message, delai, maintenant);
                }
            }
            reservationDao.rafraichirStatutsSalles(entityManager);
            return null;
        });
    }

    public void traiterEcheances() {
        PersistenceUtil.executerTransaction(entityManager -> {
            LocalDateTime maintenant = LocalDateTime.now();
            int seuilLiberation = ReglesPreemptionDao.lire(entityManager).seuilLiberation();
            List<NotificationDao.PreemptionEchue> echeances =
                    notificationDao.listerEcheances(entityManager, maintenant);
            for (NotificationDao.PreemptionEchue echeance : echeances) {
                Reservation occupant = echeance.reservation();
                Duration tempsRestant = Duration.between(maintenant, occupant.getDateFin());
                if (tempsRestant.compareTo(Duration.ofMinutes(seuilLiberation)) <= 0) {
                    reservationDao.appliquerPreemption(occupant, maintenant,
                            StatutReservation.LIBEREE);
                } else {
                    reservationDao.appliquerPreemption(occupant, occupant.reduireDuree(maintenant),
                            StatutReservation.REDUITE);
                }
                echeance.notification().setTraitee(true);
            }
            confirmerDemandesPretes(entityManager, maintenant);
            reservationDao.rafraichirStatutsSalles(entityManager);
            return null;
        });
    }

    private int niveauAutoriteMaximum(jakarta.persistence.EntityManager entityManager) {
        Integer niveau = entityManager.createQuery(
                "SELECT coalesce(max(u.niveauHierarchique), 0) FROM Utilisateur u "
                        + "WHERE u.role = :role", Integer.class)
                .setParameter("role", Role.AUTORITE)
                .getSingleResult();
        return niveau;
    }

    private void confirmerDemandesPretes(
            jakarta.persistence.EntityManager entityManager, LocalDateTime maintenant) {
        entityManager.createQuery("UPDATE Reservation demande "
                + "SET demande.statut = :confirmee "
                + "WHERE demande.statut = :enAttente AND demande.dateDebut <= :maintenant "
                + "AND demande.dateFin > :maintenant "
                + "AND NOT EXISTS (SELECT occupant.id FROM Reservation occupant "
                + "WHERE occupant.salle.id = demande.salle.id AND occupant.id <> demande.id "
                + "AND occupant.dateDebut < demande.dateFin "
                + "AND occupant.dateFin > demande.dateDebut AND occupant.statut IN :statuts)")
                .setParameter("confirmee", StatutReservation.CONFIRMEE)
                .setParameter("enAttente", StatutReservation.EN_ATTENTE_PREEMPTION)
                .setParameter("maintenant", maintenant)
                .setParameter("statuts", List.of(StatutReservation.CONFIRMEE,
                        StatutReservation.REDUITE, StatutReservation.EN_ATTENTE_PREEMPTION))
                .executeUpdate();
    }
}
