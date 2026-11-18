package bj.salledereunion.service;

import bj.salledereunion.dao.SalleDao;
import bj.salledereunion.model.Salle;
import java.time.LocalDateTime;
import java.util.List;

public class SalleService {
    private final SalleDao salleDao = new SalleDao();

    public List<Salle> listerSalles() {
        return salleDao.lister();
    }

    public List<Salle> listerDisponibles(LocalDateTime debut, LocalDateTime fin) {
        return salleDao.listerDisponibles(debut, fin);
    }
}
