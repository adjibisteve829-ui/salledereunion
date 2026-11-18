package bj.salledereunion.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(LogoutServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Object contexte = request.getSession(false) == null ? null
                : request.getSession(false).getAttribute("jaasLoginContext");
        if (contexte instanceof LoginContext loginContext) {
            try {
                loginContext.logout();
            } catch (LoginException exception) {
                LOGGER.log(Level.WARNING, "La déconnexion JAAS n'a pas abouti.", exception);
            }
        }
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/login");
    }
}
