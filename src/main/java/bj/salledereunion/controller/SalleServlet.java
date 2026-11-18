package bj.salledereunion.controller;

import bj.salledereunion.service.SalleService;
import bj.salledereunion.dao.ReservationDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/salles")
public class SalleServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(SalleServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            new ReservationDao().actualiserStatutsSalles();
            SalleService service = new SalleService();
            request.setAttribute("salles", service.listerSalles());
            String debutValeur = request.getParameter("dateDebut");
            String finValeur = request.getParameter("dateFin");
            if ((debutValeur != null && !debutValeur.isBlank())
                    || (finValeur != null && !finValeur.isBlank())) {
                LocalDateTime debut = lireDate(debutValeur);
                LocalDateTime fin = lireDate(finValeur);
                if (!fin.isAfter(debut) || !debut.isAfter(LocalDateTime.now())) {
                    throw new IllegalArgumentException(
                            "Choisissez une période future dont la fin suit le début.");
                }
                request.setAttribute("sallesDisponibles",
                        service.listerDisponibles(debut, fin));
                request.setAttribute("debutRecherche", debutValeur);
                request.setAttribute("finRecherche", finValeur);
            }
            request.getRequestDispatcher("/WEB-INF/views/salles.jsp").forward(request, response);
        } catch (IllegalArgumentException exception) {
            request.setAttribute("erreur", exception.getMessage());
            try {
                request.setAttribute("salles", new SalleService().listerSalles());
                request.getRequestDispatcher("/WEB-INF/views/salles.jsp").forward(request, response);
            } catch (RuntimeException erreur) {
                LOGGER.log(Level.SEVERE, "Impossible de réafficher la liste des salles.", erreur);
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        "Impossible de charger les salles.");
            }
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Impossible de charger la liste des salles.", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Impossible de charger les salles.");
        }
    }

    private LocalDateTime lireDate(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            throw new IllegalArgumentException("Renseignez le début et la fin de la période.");
        }
        try {
            return LocalDateTime.parse(valeur);
        } catch (java.time.format.DateTimeParseException exception) {
            throw new IllegalArgumentException("La période sélectionnée est invalide.");
        }
    }
}
