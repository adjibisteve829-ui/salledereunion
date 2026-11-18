package bj.salledereunion.controller;

import bj.salledereunion.dao.UtilisateurDao;
import bj.salledereunion.model.Utilisateur;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/parametres")
public class ParametresServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ParametresServlet.class.getName());
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
        Object attendu = request.getSession(false).getAttribute("parametresCsrfToken");
        String fourni = request.getParameter("csrf");
        if (!(attendu instanceof String valeur) || fourni == null
                || !MessageDigest.isEqual(valeur.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                        fourni.getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Le formulaire a expiré. Rechargez la page.");
            return;
        }
        try {
            Utilisateur utilisateur = (Utilisateur) request.getSession(false)
                    .getAttribute("utilisateur");
            String nom = request.getParameter("nom");
            String email = request.getParameter("email");
            String motDePasse = request.getParameter("motDePasse");
            nom = nom == null ? "" : nom.trim();
            email = email == null ? "" : email.trim();
            motDePasse = motDePasse == null ? "" : motDePasse;
            if (nom.isEmpty() || nom.length() > 160
                    || email.length() > 254
                    || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                throw new IllegalArgumentException(
                        "Vérifiez votre nom et votre adresse e-mail.");
            }
            if (!motDePasse.isEmpty() && (motDePasse.length() < 12
                    || motDePasse.length() > 200)) {
                throw new IllegalArgumentException(
                        "Le nouveau mot de passe doit contenir entre 12 et 200 caractères.");
            }
            new UtilisateurDao().modifierProfil(utilisateur.getId(), nom, email, motDePasse);
            request.getSession(false).setAttribute("utilisateur",
                    new Utilisateur(utilisateur.getId(), nom, email,
                            utilisateur.getNiveauHierarchique(), utilisateur.getRole()));
            response.sendRedirect(request.getContextPath() + "/parametres?success=1");
        } catch (IllegalArgumentException exception) {
            request.setAttribute("erreur", exception.getMessage());
            afficher(request, response);
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Impossible de modifier le profil utilisateur.", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Impossible d'enregistrer les paramètres du compte.");
        }
    }

    private void afficher(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getSession().getAttribute("parametresCsrfToken") == null) {
            byte[] aleatoire = new byte[32];
            RANDOM.nextBytes(aleatoire);
            request.getSession().setAttribute("parametresCsrfToken",
                    Base64.getUrlEncoder().withoutPadding().encodeToString(aleatoire));
        }
        request.getRequestDispatcher("/WEB-INF/views/parametres.jsp").forward(request, response);
    }
}
