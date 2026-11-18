package bj.salledereunion.security;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

public final class GestionnaireIdentifiants implements CallbackHandler {
    private final String email;
    private final char[] motDePasse;

    public GestionnaireIdentifiants(String email, char[] motDePasse) {
        this.email = email;
        this.motDePasse = motDePasse.clone();
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof NameCallback nom) {
                nom.setName(email);
            } else if (callback instanceof PasswordCallback motDePasseCallback) {
                motDePasseCallback.setPassword(motDePasse);
            } else {
                throw new UnsupportedCallbackException(callback);
            }
        }
    }

    public void effacer() {
        java.util.Arrays.fill(motDePasse, '\0');
    }
}
