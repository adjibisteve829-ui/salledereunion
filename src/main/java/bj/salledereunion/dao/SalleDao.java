package bj.salledereunion.dao;

import bj.salledereunion.model.Salle;
import bj.salledereunion.model.StatutSalle;
import bj.salledereunion.util.PersistenceUtil;
import java.time.LocalDateTime;
import java.util.List;

public class SalleDao {
    public List<Salle> lister() {
        return PersistenceUtil.executer(entityManager -> entityManager.createQuery(
                "SELECT s FROM Salle s ORDER BY s.nom", Salle.class).getResultList());
    }

    public List<Salle> listerDisponibles(LocalDateTime debut, LocalDateTime fin) {
        return PersistenceUtil.executer(entityManager -> entityManager.createQuery(
                "SELECT s FROM Salle s WHERE NOT EXISTS (SELECT r.id FROM Reservation r "
                        + "WHERE r.salle.id = s.id AND r.dateDebut < :fin AND r.dateFin > :debut "
                        + "AND r.statut IN :statuts) ORDER BY s.nom", Salle.class)
                .setParameter("debut", debut)
                .setParameter("fin", fin)
                .setParameter("statuts", List.of(
                        bj.salledereunion.model.StatutReservation.CONFIRMEE,
                        bj.salledereunion.model.StatutReservation.REDUITE,
                        bj.salledereunion.model.StatutReservation.EN_ATTENTE_PREEMPTION))
                .getResultList());
    }

    public long compterDisponibles() {
        return PersistenceUtil.executer(entityManager -> entityManager.createQuery(
                "SELECT count(s) FROM Salle s WHERE s.statut = :statut", Long.class)
                .setParameter("statut", StatutSalle.DISPONIBLE)
                .getSingleResult());
    }

    public Salle trouver(long id) {
        return PersistenceUtil.executer(entityManager -> entityManager.find(Salle.class, id));
    }

    public void creer(String nom, int capacite, String equipements) {
        PersistenceUtil.executerTransaction(entityManager -> {
            entityManager.persist(new Salle(nom, capacite, equipements));
            return null;
        });
    }

    public void modifier(long id, String nom, int capacite, String equipements) {
        PersistenceUtil.executerTransaction(entityManager -> {
            Salle salle = entityManager.find(Salle.class, id);
            if (salle == null) {
                throw new IllegalArgumentException("Cette salle n'existe pas.");
            }
            salle.setNom(nom);
            salle.setCapacite(capacite);
            salle.setEquipements(equipements);
            return null;
        });
    }

    public void supprimer(long id) {
        PersistenceUtil.executerTransaction(entityManager -> {
            Salle salle = entityManager.find(Salle.class, id);
            if (salle == null) {
                throw new IllegalArgumentException("Cette salle n'existe pas.");
            }
            Long reservations = entityManager.createQuery(
                    "SELECT count(r) FROM Reservation r WHERE r.salle.id = :id",
                    Long.class).setParameter("id", id).getSingleResult();
            if (reservations > 0) {
                throw new IllegalArgumentException(
                        "Cette salle est liée à des réservations et ne peut pas être supprimée.");
            }
            entityManager.remove(salle);
            return null;
        });
    }
}
