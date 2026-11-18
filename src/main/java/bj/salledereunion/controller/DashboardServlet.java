package bj.salledereunion.controller;

import bj.salledereunion.dao.ReservationDao;
import bj.salledereunion.dao.SalleDao;
import bj.salledereunion.model.Utilisateur;
import bj.salledereunion.service.NotificationService;
import bj.salledereunion.service.PreemptionService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(DashboardServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            ReservationDao reservationDao = new ReservationDao();
            reservationDao.actualiserStatutsSalles();
            new PreemptionService().traiterEcheances();
            request.setAttribute("nombreSalles", new SalleDao().compterDisponibles());
            request.setAttribute("reservationsActives", reservationDao.compterActives());
            Utilisateur utilisateur = (Utilisateur) request.getSession(false).getAttribute("utilisateur");
            request.setAttribute("mesReservations",
                    reservationDao.listerPourUtilisateur(utilisateur.getId(), 5));
            request.setAttribute("nombreMesReservations",
                    utilisateur.estAdministrateur()
                            ? reservationDao.compterPourAdministration()
                            : reservationDao.compterPourUtilisateur(utilisateur.getId()));
            request.setAttribute("notifications",
                    new NotificationService().pourUtilisateur(utilisateur.getId()));
            request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Impossible de charger le tableau de bord.", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Impossible de charger les données du tableau de bord.");
        }
    }
}
