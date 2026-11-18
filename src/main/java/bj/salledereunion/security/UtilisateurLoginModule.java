package bj.salledereunion.security;

import bj.salledereunion.dao.UtilisateurDao;
import bj.salledereunion.model.Utilisateur;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

public class UtilisateurLoginModule implements LoginModule {
    private Subject subject;
    private CallbackHandler callbackHandler;
    private Utilisateur utilisateur;
    private boolean authenticated;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler,
            Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public boolean login() throws LoginException {
        if (callbackHandler == null) {
            throw new LoginException("Aucun gestionnaire de justificatifs JAAS n'est configuré.");
        }
        NameCallback nom = new NameCallback("Email");
        PasswordCallback motDePasse = new PasswordCallback("Mot de passe", false);
        try {
            callbackHandler.handle(new Callback[]{nom, motDePasse});
            char[] secret = motDePasse.getPassword();
            if (secret == null || secret.length == 0) {
                throw new FailedLoginException("Identifiants invalides.");
            }
            try {
                utilisateur = new UtilisateurDao().authentifier(nom.getName(), secret);
            } catch (GeneralSecurityException | SQLException | RuntimeException exception) {
                LoginException erreur = new LoginException("L'authentification est indisponible.");
                erreur.initCause(exception);
                throw erreur;
            } finally {
                java.util.Arrays.fill(secret, '\0');
            }
            if (utilisateur == null) {
                throw new FailedLoginException("Identifiants invalides.");
            }
            authenticated = true;
            return true;
        } catch (IOException | UnsupportedCallbackException exception) {
            LoginException erreur = new LoginException("Lecture des identifiants impossible.");
            erreur.initCause(exception);
            throw erreur;
        } finally {
            motDePasse.clearPassword();
        }
    }

    @Override
    public boolean commit() throws LoginException {
        if (!authenticated || utilisateur == null) {
            return false;
        }
        if (subject.isReadOnly()) {
            throw new LoginException("Le sujet JAAS est en lecture seule.");
        }
        subject.getPrincipals().add(new UtilisateurPrincipal(utilisateur));
        subject.getPrincipals().add(new RolePrincipal(utilisateur.getRole().name()));
        return true;
    }

    @Override
    public boolean abort() {
        authenticated = false;
        utilisateur = null;
        return true;
    }

    @Override
    public boolean logout() {
        Set<UtilisateurPrincipal> utilisateurs =
                subject.getPrincipals(UtilisateurPrincipal.class);
        Set<RolePrincipal> roles = subject.getPrincipals(RolePrincipal.class);
        utilisateurs.clear();
        roles.clear();
        authenticated = false;
        utilisateur = null;
        return true;
    }
}
