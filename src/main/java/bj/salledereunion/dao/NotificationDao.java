package bj.salledereunion.dao;

import bj.salledereunion.model.Notification;
import bj.salledereunion.model.Reservation;
import bj.salledereunion.model.Utilisateur;
import bj.salledereunion.util.PersistenceUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationDao {
    public List<Notification> listerPourUtilisateur(long destinataireId, int limite) {
        return PersistenceUtil.executer(entityManager ->
                entityManager.createQuery("SELECT n FROM Notification n "
                        + "WHERE n.destinataire.id = :destinataireId ORDER BY n.dateEnvoi DESC",
                        Notification.class)
                        .setParameter("destinataireId", destinataireId)
                        .setMaxResults(limite)
                        .getResultList());
    }

    public void creer(EntityManager entityManager, Reservation reservation,
            Reservation demandeReservation, long destinataireId, String message,
            int delaiMinutes, LocalDateTime dateEnvoi) {
        Utilisateur destinataire = entityManager.getReference(Utilisateur.class, destinataireId);
        entityManager.persist(new Notification(reservation, demandeReservation, destinataire,
                message, delaiMinutes, dateEnvoi, dateEnvoi.plusMinutes(delaiMinutes)));
    }

    public List<PreemptionEchue> listerEcheances(EntityManager entityManager,
            LocalDateTime maintenant) {
        List<Notification> notifications = entityManager.createQuery(
                "SELECT n FROM Notification n WHERE n.traitee = false "
                        + "AND n.dateEcheance <= :maintenant ORDER BY n.dateEcheance",
                Notification.class)
                .setParameter("maintenant", maintenant)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList();
        List<PreemptionEchue> echeances = new ArrayList<>();
        for (Notification notification : notifications) {
            Reservation reservation = entityManager.find(Reservation.class,
                    notification.getReservationId(), LockModeType.PESSIMISTIC_WRITE);
            echeances.add(new PreemptionEchue(notification, reservation));
        }
        return echeances;
    }

    public record PreemptionEchue(Notification notification, Reservation reservation) {
    }
}
