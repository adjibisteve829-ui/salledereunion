package bj.salledereunion.service;

import bj.salledereunion.dao.NotificationDao;
import bj.salledereunion.model.Notification;
import java.util.List;

public class NotificationService {
    private final NotificationDao notificationDao = new NotificationDao();

    public List<Notification> pourUtilisateur(long utilisateurId) {
        return notificationDao.listerPourUtilisateur(utilisateurId, 10);
    }
}
