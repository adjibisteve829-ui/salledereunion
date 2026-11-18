package bj.salledereunion.controller;

import bj.salledereunion.dao.ReservationDao;
import bj.salledereunion.model.Utilisateur;
import bj.salledereunion.service.ReservationService;
import bj.salledereunion.service.SalleService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/reservations")
public class ReservationServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ReservationServlet.class.getName());
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Utilisateur utilisateur = (Utilisateur) request.getSession(false)
                    .getAttribute("utilisateur");
            if (utilisateur.estAdministrateur()) {
                response.sendRedirect(request.getContextPath() + "/admin#reservations");
                return;
            }
            if (request.getSession().getAttribute("reservationCsrfToken") == null) {
                byte[] aleatoire = new byte[32];
                RANDOM.nextBytes(aleatoire);
                request.getSession().setAttribute("reservationCsrfToken",
                        Base64.getUrlEncoder().withoutPadding().encodeToString(aleatoire));
            }
            request.setAttribute("salles", new SalleService().listerSalles());
            request.setAttribute("reservations",
                    new ReservationDao().listerPourUtilisateur(utilisateur.getId(), 50));
            request.getRequestDispatcher("/WEB-INF/views/reservations.jsp").forward(request, response);
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Impossible de charger les réservations.", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Impossible de charger les réservations.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            Utilisateur utilisateur =
                    (Utilisateur) request.getSession(false).getAttribute("utilisateur");
            if (utilisateur.estAdministrateur()) {
                response.sendRedirect(request.getContextPath() + "/admin#reservations");
                return;
            }
            if (!jetonValide(request)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Le formulaire a expiré. Rechargez la page.");
                return;
            }
            String action = request.getParameter("action");
            if ("annuler".equals(action)) {
                new ReservationService().annulerPourUtilisateur(
                        lireId(request.getParameter("reservationId")), utilisateur.getId());
                response.sendRedirect(request.getContextPath() + "/reservations?annulee=1");
            } else {
                long salleId = lireId(request.getParameter("salleId"));
                LocalDateTime debut = lireDate(request.getParameter("dateDebut"));
                LocalDateTime fin = lireDate(request.getParameter("dateFin"));
                String motif = request.getParameter("motif");
                new ReservationService().reserver(utilisateur, salleId, debut, fin, motif);
                response.sendRedirect(request.getContextPath() + "/reservations?success=1");
            }
        } catch (IllegalArgumentException exception) {
            request.setAttribute("erreur", exception.getMessage());
            doGet(request, response);
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Échec de l'enregistrement d'une réservation.", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Impossible d'enregistrer la réservation.");
        }
    }

    private LocalDateTime lireDate(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            throw new IllegalArgumentException("Veuillez renseigner les dates de réservation.");
        }
        try {
            return LocalDateTime.parse(valeur);
        } catch (java.time.format.DateTimeParseException exception) {
            throw new IllegalArgumentException("Veuillez saisir une date de réservation valide.");
        }
    }

    private boolean jetonValide(HttpServletRequest request) {
        Object attendu = request.getSession(false) == null ? null
                : request.getSession(false).getAttribute("reservationCsrfToken");
        String fourni = request.getParameter("csrf");
        return attendu instanceof String valeur && fourni != null
                && MessageDigest.isEqual(
                        valeur.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                        fourni.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private long lireId(String valeur) {
        try {
            long id = Long.parseLong(valeur);
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException | NullPointerException exception) {
            throw new IllegalArgumentException("L'identifiant de la réservation est invalide.");
        }
    }
}
