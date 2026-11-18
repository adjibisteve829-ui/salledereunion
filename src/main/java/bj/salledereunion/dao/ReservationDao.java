package bj.salledereunion.dao;

import bj.salledereunion.model.Reservation;
import bj.salledereunion.model.Salle;
import bj.salledereunion.model.StatutReservation;
import bj.salledereunion.model.StatutSalle;
import bj.salledereunion.model.Utilisateur;
import bj.salledereunion.util.PersistenceUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;

public class ReservationDao {
    public List<Reservation> listerAvenir(int limite) {
        return PersistenceUtil.executer(entityManager ->
                entityManager.createQuery("SELECT r FROM Reservation r "
                        + "WHERE r.dateFin >= :maintenant AND r.statut IN :statuts "
                        + "ORDER BY r.dateDebut", Reservation.class)
                        .setParameter("maintenant", LocalDateTime.now())
                        .setParameter("statuts", List.of(StatutReservation.CONFIRMEE,
                                StatutReservation.EN_ATTENTE_PREEMPTION,
                                StatutReservation.REDUITE))
                        .setMaxResults(limite)
                        .getResultList());
    }

    public List<Reservation> listerPourAdministration() {
        return PersistenceUtil.executer(entityManager -> entityManager.createQuery(
                "SELECT r FROM Reservation r JOIN FETCH r.salle JOIN FETCH r.utilisateur "
                        + "WHERE r.utilisateur.role <> :role ORDER BY r.dateDebut DESC",
                Reservation.class)
                .setParameter("role", bj.salledereunion.model.Role.ADMINISTRATEUR)
                .getResultList());
    }

    public List<Reservation> listerPourUtilisateur(long utilisateurId, int limite) {
        return PersistenceUtil.executer(entityManager -> entityManager.createQuery(
                "SELECT r FROM Reservation r JOIN FETCH r.salle "
                        + "WHERE r.utilisateur.id = :utilisateurId "
                        + "AND r.dateFin >= :maintenant AND r.statut IN :statuts "
                        + "ORDER BY r.dateDebut", Reservation.class)
                .setParameter("utilisateurId", utilisateurId)
                .setParameter("maintenant", LocalDateTime.now())
                .setParameter("statuts", List.of(StatutReservation.CONFIRMEE,
                        StatutReservation.REDUITE,
                        StatutReservation.EN_ATTENTE_PREEMPTION))
                .setMaxResults(limite)
                .getResultList());
    }

    public long compterPourUtilisateur(long utilisateurId) {
        return PersistenceUtil.executer(entityManager -> entityManager.createQuery(
                "SELECT count(r) FROM Reservation r WHERE r.utilisateur.id = :utilisateurId "
                        + "AND r.dateFin >= :maintenant AND r.statut IN :statuts", Long.class)
                .setParameter("utilisateurId", utilisateurId)
                .setParameter("maintenant", LocalDateTime.now())
                .setParameter("statuts", List.of(StatutReservation.CONFIRMEE,
                        StatutReservation.REDUITE,
                        StatutReservation.EN_ATTENTE_PREEMPTION))
                .getSingleResult());
    }

    public long compterPourAdministration() {
        return PersistenceUtil.executer(entityManager -> entityManager.createQuery(
                "SELECT count(r) FROM Reservation r WHERE r.utilisateur.role <> :role "
                        + "AND r.dateFin >= :maintenant AND r.statut IN :statuts", Long.class)
                .setParameter("role", bj.salledereunion.model.Role.ADMINISTRATEUR)
                .setParameter("maintenant", LocalDateTime.now())
                .setParameter("statuts", List.of(StatutReservation.CONFIRMEE,
                        StatutReservation.REDUITE,
                        StatutReservation.EN_ATTENTE_PREEMPTION))
                .getSingleResult());
    }

    public long compterActives() {
        return PersistenceUtil.executer(entityManager ->
                entityManager.createQuery("SELECT count(r) FROM Reservation r "
                        + "WHERE r.dateDebut <= :maintenant AND r.dateFin > :maintenant "
                        + "AND r.statut IN :statuts", Long.class)
                        .setParameter("maintenant", LocalDateTime.now())
                        .setParameter("statuts", List.of(StatutReservation.CONFIRMEE,
                                StatutReservation.REDUITE))
                        .getSingleResult());
    }

    public List<Reservation> reservationsChevauchantes(EntityManager entityManager,
            long salleId, LocalDateTime debut, LocalDateTime fin) {
        return entityManager.createQuery("SELECT r FROM Reservation r "
                + "WHERE r.salle.id = :salleId AND r.dateDebut < :fin AND r.dateFin > :debut "
                + "AND r.statut IN :statuts", Reservation.class)
                .setParameter("salleId", salleId)
                .setParameter("debut", debut)
                .setParameter("fin", fin)
                .setParameter("statuts", List.of(StatutReservation.CONFIRMEE,
                        StatutReservation.REDUITE, StatutReservation.EN_ATTENTE_PREEMPTION))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList();
    }

    public Reservation creer(EntityManager entityManager, long salleId, long utilisateurId,
            LocalDateTime debut, LocalDateTime fin, String motif, StatutReservation statut,
            boolean prioritaire) {
        Salle salle = entityManager.getReference(Salle.class, salleId);
        Utilisateur utilisateur = entityManager.getReference(Utilisateur.class, utilisateurId);
        Reservation reservation = new Reservation(salle, utilisateur, debut, fin, motif,
                statut, prioritaire);
        entityManager.persist(reservation);
        return reservation;
    }

    public void appliquerPreemption(Reservation reservation, LocalDateTime nouvelleFin,
            StatutReservation statut) {
        reservation.setDateFin(nouvelleFin);
        reservation.setStatut(statut);
    }

    public void modifierPourAdmin(long id, long salleId, long utilisateurId,
            LocalDateTime debut, LocalDateTime fin, String motif) {
        PersistenceUtil.executerTransaction(entityManager -> {
            Reservation reservation = entityManager.find(Reservation.class, id,
                    LockModeType.PESSIMISTIC_WRITE);
            if (reservation == null) {
                throw new IllegalArgumentException("Cette réservation n'existe pas.");
            }
            if (reservation.getStatut() == StatutReservation.ANNULEE
                    || reservation.getStatut() == StatutReservation.LIBEREE) {
                throw new IllegalArgumentException(
                        "Une réservation annulée ou libérée ne peut plus être modifiée.");
            }
            Long notificationsEnCours = entityManager.createQuery(
                    "SELECT count(n) FROM Notification n WHERE n.traitee = false "
                            + "AND (n.reservation.id = :id OR n.demandeReservation.id = :id)",
                    Long.class).setParameter("id", id).getSingleResult();
            if (notificationsEnCours > 0) {
                throw new IllegalArgumentException(
                        "Cette réservation participe à une préemption en cours et ne peut pas être modifiée.");
            }
            Salle salle = entityManager.find(Salle.class, salleId,
                    LockModeType.PESSIMISTIC_WRITE);
            if (salle == null) {
                throw new IllegalArgumentException("La salle sélectionnée n'existe pas.");
            }
            Utilisateur utilisateur = entityManager.find(Utilisateur.class, utilisateurId);
            if (utilisateur == null) {
                throw new IllegalArgumentException("L'utilisateur sélectionné n'existe pas.");
            }
            if (utilisateur.getRole() == bj.salledereunion.model.Role.ADMINISTRATEUR) {
                throw new IllegalArgumentException(
                        "Une réservation ne peut pas être affectée à un compte administrateur.");
            }
            List<Reservation> conflits = entityManager.createQuery(
                    "SELECT r FROM Reservation r WHERE r.id <> :id AND r.salle.id = :salleId "
                            + "AND r.dateDebut < :fin AND r.dateFin > :debut "
                            + "AND r.statut IN :statuts", Reservation.class)
                    .setParameter("id", id)
                    .setParameter("salleId", salleId)
                    .setParameter("debut", debut)
                    .setParameter("fin", fin)
                    .setParameter("statuts", List.of(StatutReservation.CONFIRMEE,
                            StatutReservation.REDUITE,
                            StatutReservation.EN_ATTENTE_PREEMPTION))
                    .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                    .getResultList();
            if (!conflits.isEmpty()) {
                throw new IllegalArgumentException(
                        "Cette salle est déjà réservée sur cette période.");
            }
            reservation.setSalle(salle);
            reservation.setUtilisateur(utilisateur);
            reservation.setDateDebut(debut);
            reservation.setDateFin(fin);
            reservation.setMotif(motif);
            rafraichirStatutsSalles(entityManager);
            return null;
        });
    }

    public void annulerPourAdmin(long id) {
        PersistenceUtil.executerTransaction(entityManager -> {
            Reservation reservation = entityManager.find(Reservation.class, id,
                    LockModeType.PESSIMISTIC_WRITE);
            if (reservation == null) {
                throw new IllegalArgumentException("Cette réservation n'existe pas.");
            }
            if (reservation.getStatut() == StatutReservation.ANNULEE
                    || reservation.getStatut() == StatutReservation.LIBEREE) {
                throw new IllegalArgumentException("Cette réservation est déjà clôturée.");
            }
            reservation.setStatut(StatutReservation.ANNULEE);
            entityManager.createQuery("UPDATE Notification n SET n.traitee = true "
                    + "WHERE n.traitee = false AND (n.reservation.id = :id "
                    + "OR n.demandeReservation.id = :id)")
                    .setParameter("id", id)
                    .executeUpdate();
            rafraichirStatutsSalles(entityManager);
            return null;
        });
    }

    public void annulerPourUtilisateur(long id, long utilisateurId) {
        PersistenceUtil.executerTransaction(entityManager -> {
            Reservation reservation = entityManager.find(Reservation.class, id,
                    LockModeType.PESSIMISTIC_WRITE);
            if (reservation == null || reservation.getUtilisateurId() != utilisateurId) {
                throw new IllegalArgumentException(
                        "Cette réservation ne vous appartient pas ou n'existe pas.");
            }
            if (!reservation.getDateDebut().isAfter(LocalDateTime.now())) {
                throw new IllegalArgumentException(
                        "Une réservation commencée ne peut plus être annulée.");
            }
            if (reservation.getStatut() != StatutReservation.CONFIRMEE
                    && reservation.getStatut() != StatutReservation.REDUITE) {
                throw new IllegalArgumentException(
                        "Cette réservation ne peut pas être annulée depuis votre espace.");
            }
            reservation.setStatut(StatutReservation.ANNULEE);
            marquerNotificationsTraitees(entityManager, id);
            rafraichirStatutsSalles(entityManager);
            return null;
        });
    }

    private static void marquerNotificationsTraitees(
            EntityManager entityManager, long reservationId) {
        entityManager.createQuery("UPDATE Notification n SET n.traitee = true "
                + "WHERE n.traitee = false AND (n.reservation.id = :id "
                + "OR n.demandeReservation.id = :id)")
                .setParameter("id", reservationId)
                .executeUpdate();
    }

    public void actualiserStatutsSalles() {
        PersistenceUtil.executerTransaction(entityManager -> {
            rafraichirStatutsSalles(entityManager);
            return null;
        });
    }

    public void rafraichirStatutsSalles(EntityManager entityManager) {
        LocalDateTime maintenant = LocalDateTime.now();
        List<Salle> salles = entityManager.createQuery("SELECT s FROM Salle s", Salle.class)
                .getResultList();
        for (Salle salle : salles) {
            Long nombreOccupees = entityManager.createQuery(
                    "SELECT count(r) FROM Reservation r WHERE r.salle.id = :salleId "
                            + "AND r.dateDebut <= :maintenant AND r.dateFin > :maintenant "
                            + "AND r.statut IN :statuts", Long.class)
                    .setParameter("salleId", salle.getId())
                    .setParameter("maintenant", maintenant)
                    .setParameter("statuts", List.of(StatutReservation.CONFIRMEE,
                            StatutReservation.REDUITE))
                    .getSingleResult();
            if (nombreOccupees > 0) {
                salle.setStatut(StatutSalle.OCCUPEE);
                continue;
            }
            Long nombreReservees = entityManager.createQuery(
                    "SELECT count(r) FROM Reservation r WHERE r.salle.id = :salleId "
                            + "AND r.dateDebut > :maintenant "
                            + "AND r.statut IN :statuts", Long.class)
                    .setParameter("salleId", salle.getId())
                    .setParameter("maintenant", maintenant)
                    .setParameter("statuts", List.of(StatutReservation.CONFIRMEE,
                            StatutReservation.REDUITE,
                            StatutReservation.EN_ATTENTE_PREEMPTION))
                    .getSingleResult();
            salle.setStatut(nombreReservees > 0
                    ? StatutSalle.RESERVEE : StatutSalle.DISPONIBLE);
        }
    }
}
