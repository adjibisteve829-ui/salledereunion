package bj.salledereunion.service;

import bj.salledereunion.dao.ReservationDao;
import bj.salledereunion.model.Reservation;
import bj.salledereunion.model.Salle;
import bj.salledereunion.model.StatutReservation;
import bj.salledereunion.model.Utilisateur;
import bj.salledereunion.util.PersistenceUtil;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;

public class ReservationService {
    private final ReservationDao reservationDao = new ReservationDao();
    private final PreemptionService preemptionService = new PreemptionService();

    public void reserver(Utilisateur utilisateur, long salleId, LocalDateTime debut,
            LocalDateTime fin, String motif) {
        validerPeriode(debut, fin, motif);
        if (utilisateur.estAutorite()) {
            preemptionService.demander(utilisateur, salleId, debut, fin, motif);
            return;
        }
        PersistenceUtil.executerTransaction(entityManager -> {
            verifierSalle(entityManager.find(Salle.class, salleId, LockModeType.PESSIMISTIC_WRITE));
            List<Reservation> conflits =
                    reservationDao.reservationsChevauchantes(entityManager, salleId, debut, fin);
            if (!conflits.isEmpty()) {
                throw new IllegalArgumentException("Cette salle est déjà réservée sur cette période.");
            }
            reservationDao.creer(entityManager, salleId, utilisateur.getId(), debut, fin,
                    motif, StatutReservation.CONFIRMEE, false);
            reservationDao.rafraichirStatutsSalles(entityManager);
            return null;
        });
    }

    public void modifierPourAdmin(long reservationId, long salleId, long utilisateurId,
            LocalDateTime debut, LocalDateTime fin, String motif) {
        validerPeriode(debut, fin, motif);
        reservationDao.modifierPourAdmin(reservationId, salleId, utilisateurId,
                debut, fin, motif);
    }

    public void annulerPourAdmin(long reservationId) {
        reservationDao.annulerPourAdmin(reservationId);
    }

    public void annulerPourUtilisateur(long reservationId, long utilisateurId) {
        reservationDao.annulerPourUtilisateur(reservationId, utilisateurId);
    }

    static void verifierSalle(Salle salle) {
        if (salle == null) {
            throw new IllegalArgumentException("La salle demandée n'existe pas.");
        }
    }

    static void validerPeriode(LocalDateTime debut, LocalDateTime fin, String motif) {
        if (debut == null || fin == null || !fin.isAfter(debut)
                || !debut.isAfter(LocalDateTime.now()) || motif == null || motif.isBlank()) {
            throw new IllegalArgumentException("Vérifiez le motif et les dates de réservation.");
        }
        if (motif.length() > 500) {
            throw new IllegalArgumentException("Le motif ne peut pas dépasser 500 caractères.");
        }
    }
}
