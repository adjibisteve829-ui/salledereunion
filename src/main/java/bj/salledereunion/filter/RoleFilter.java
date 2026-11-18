package bj.salledereunion.filter;

import bj.salledereunion.dao.UtilisateurDao;
import bj.salledereunion.model.Utilisateur;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebFilter(urlPatterns = "/admin")
public class RoleFilter implements Filter {
    private static final Logger LOGGER = Logger.getLogger(RoleFilter.class.getName());

    @Override
    public void doFilter(jakarta.servlet.ServletRequest request,
            jakarta.servlet.ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        Object utilisateur = httpRequest.getSession(false) == null ? null
                : httpRequest.getSession(false).getAttribute("utilisateur");
        if (utilisateur == null) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
            return;
        }
        if (!(utilisateur instanceof Utilisateur compte)) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        try {
            Utilisateur compteActuel = new UtilisateurDao().trouver(compte.getId());
            if (compteActuel == null || !compteActuel.estAdministrateur()) {
                httpRequest.getSession(false).invalidate();
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            httpRequest.getSession(false).setAttribute("utilisateur", compteActuel);
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE,
                    "Impossible de vérifier le rôle administrateur courant.", exception);
            httpResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Impossible de vérifier les droits d'administration.");
            return;
        }
        chain.doFilter(request, response);
    }
}
