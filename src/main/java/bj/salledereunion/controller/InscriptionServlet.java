package bj.salledereunion.controller;

import bj.salledereunion.dao.UtilisateurDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/inscription")
public class InscriptionServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(InscriptionServlet.class.getName());
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        afficher(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!jetonValide(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Le formulaire a expiré. Rechargez la page.");
            return;
        }

        String nom = request.getParameter("nom");
        String email = request.getParameter("email");
        char[] motDePasse = request.getParameter("motDePasse") == null
                ? new char[0] : request.getParameter("motDePasse").toCharArray();
        char[] confirmation = request.getParameter("confirmation") == null
                ? new char[0] : request.getParameter("confirmation").toCharArray();
        try {
            nom = nom == null ? "" : nom.trim();
            email = email == null ? "" : email.trim();
            if (nom.isEmpty() || nom.length() > 160) {
                throw new IllegalArgumentException("Veuillez saisir un nom (160 caractères maximum).");
            }
            if (email.length() > 254 || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                throw new IllegalArgumentException("Veuillez saisir une adresse e-mail valide.");
            }
            if (motDePasse.length < 12 || motDePasse.length > 200) {
                throw new IllegalArgumentException(
                        "Le mot de passe doit contenir entre 12 et 200 caractères.");
            }
            if (!Arrays.equals(motDePasse, confirmation)) {
                throw new IllegalArgumentException("Les mots de passe ne correspondent pas.");
            }
            new UtilisateurDao().inscrire(nom, email, motDePasse);
            response.sendRedirect(request.getContextPath() + "/login?inscription=1");
        } catch (IllegalArgumentException exception) {
            request.setAttribute("erreur", exception.getMessage());
            request.setAttribute("nomSaisi", nom);
            request.setAttribute("emailSaisi", email);
            afficher(request, response);
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Échec de la création d'un compte client.", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Impossible de créer le compte pour le moment.");
        } finally {
            Arrays.fill(motDePasse, '\0');
            Arrays.fill(confirmation, '\0');
        }
    }

    private boolean jetonValide(HttpServletRequest request) {
        Object attendu = request.getSession(false) == null ? null
                : request.getSession(false).getAttribute("inscriptionCsrfToken");
        String fourni = request.getParameter("csrf");
        return attendu instanceof String valeur && fourni != null
                && java.security.MessageDigest.isEqual(
                        valeur.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                        fourni.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private void afficher(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getSession().getAttribute("inscriptionCsrfToken") == null) {
            byte[] aleatoire = new byte[32];
            RANDOM.nextBytes(aleatoire);
            request.getSession().setAttribute("inscriptionCsrfToken",
                    Base64.getUrlEncoder().withoutPadding().encodeToString(aleatoire));
        }
        request.getRequestDispatcher("/WEB-INF/views/inscription.jsp").forward(request, response);
    }
}
