package bj.salledereunion.controller;

import bj.salledereunion.model.Utilisateur;
import bj.salledereunion.security.ConfigurationJaas;
import bj.salledereunion.security.GestionnaireIdentifiants;
import bj.salledereunion.security.UtilisateurPrincipal;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(LoginServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String email = request.getParameter("email");
        String motDePasse = request.getParameter("motDePasse");
        if (email == null || email.isBlank() || motDePasse == null || motDePasse.isEmpty()) {
            request.setAttribute("erreur", "Veuillez saisir votre adresse e-mail et votre mot de passe.");
            doGet(request, response);
            return;
        }

        GestionnaireIdentifiants gestionnaire =
                new GestionnaireIdentifiants(email.trim(), motDePasse.toCharArray());
        try {
            Subject sujet = new Subject();
            LoginContext contexte = new LoginContext("SalleDeReunion", sujet, gestionnaire,
                    new ConfigurationJaas());
            contexte.login();
            Utilisateur utilisateur = sujet.getPrincipals(UtilisateurPrincipal.class).stream()
                    .findFirst().orElseThrow().getUtilisateur();
            request.getSession(true);
            request.changeSessionId();
            request.getSession(true).setAttribute("utilisateur", utilisateur);
            request.getSession().setAttribute("jaasLoginContext", contexte);
            response.sendRedirect(request.getContextPath() + "/dashboard");
        } catch (LoginException erreur) {
            if (erreur.getCause() != null) {
                LOGGER.log(Level.SEVERE, "Échec technique de l'authentification JAAS.", erreur);
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        "Le service d'authentification est temporairement indisponible.");
                return;
            }
            request.setAttribute("erreur", "Adresse e-mail ou mot de passe incorrect.");
            doGet(request, response);
        } finally {
            gestionnaire.effacer();
        }
    }
}
